package com.Royhang

import com.Royhang.RecommendationServer.dbService
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.mllib.linalg.distributed.{CoordinateMatrix, MatrixEntry}

class SparkUserCFRecommender(spark: SparkSession) {

  def hybridRecommend(ratingsDF: DataFrame,
                      targetUser: String,
                      searchQuery: Option[String],
                      k: Int = 20,
                      n: Int = 10): Array[Int] = {

    // 优先返回搜索结果
    searchQuery.filter(_.nonEmpty).foreach { q =>
      val searchResults = dbService.searchMovies(spark, q).map(_.movieId)
      if (searchResults.nonEmpty) return searchResults.take(n).toArray
    }

    // 无搜索结果时执行原推荐逻辑
    recommend(ratingsDF, targetUser, k, n)
  }

  def recommend(ratingsDF: DataFrame, targetUser: String, k: Int = 20, n: Int = 10): Array[Int] = {
    // 确保数据去重
    val uniqueRatings = ratingsDF.dropDuplicates("user", "movie")

    // 构建映射表
    val (userMap, movieMap) = buildMappings(uniqueRatings)
    val movieReverseMap = movieMap.map(_.swap)

    // 验证目标用户存在
    val targetUserIndex = userMap.getOrElse(targetUser, -1L)
    if (targetUserIndex == -1) {
      println(s"[WARN] 用户 $targetUser 无评分记录，返回热门电影")
      val popularMovies = dbService.getPopularMovies(spark, n)
      return popularMovies.map(_.movieId).toArray
    }

    println(s"[DEBUG] 开始处理用户 $targetUser 的推荐请求")

    // 添加进度条支持
    val totalSteps = 5
    val progressAccumulator = spark.sparkContext.longAccumulator("Progress")

    // 显示进度条
    showProgress(0, totalSteps)
    val broadcastUserMap = spark.sparkContext.broadcast(userMap)
    val broadcastMovieMap = spark.sparkContext.broadcast(movieMap)

    // 转换为矩阵条目
    val matrixEntries = uniqueRatings.rdd.map { row =>
      val user = row.getAs[String]("user")
      val movie = row.getAs[Int]("movie")
      MatrixEntry(
        broadcastUserMap.value(user),  // 从广播变量获取索引
        broadcastMovieMap.value(movie),
        row.getAs[Double]("rating")
      )
    }

    broadcastUserMap.unpersist()
    broadcastMovieMap.unpersist()
    val coordMatrix = new CoordinateMatrix(matrixEntries)

    // 缓存矩阵以便多次使用
    coordMatrix.entries.cache()

    // 获取目标用户评分的电影
    val targetUserMovies = matrixEntries
      .filter(_.i == targetUserIndex)
      .map(_.j)
      .collect()
      .toSet

    progressAccumulator.add(1)
    showProgress(progressAccumulator.value, totalSteps)

    progressAccumulator.add(1)
    showProgress(progressAccumulator.value, totalSteps)

    val userVectors = coordMatrix.transpose.toRowMatrix()
    val userSimilarities = userVectors.columnSimilarities(0.1)

    // 获取最相似的K个用户
    val similarUsers = userSimilarities.entries
      .filter(_.i == targetUserIndex)
      .sortBy(-_.value)
      .take(k)

    println(s"[DEBUG] 找到 ${similarUsers.length} 个相似用户")

    progressAccumulator.add(1)
    showProgress(progressAccumulator.value, totalSteps)

    // 广播目标用户已评分电影
    val targetUserMoviesBc = spark.sparkContext.broadcast(targetUserMovies)

    // 收集相似用户评分的电影并过滤
    val candidateMovies = similarUsers.flatMap { entry =>
      val similarUserIndex = entry.j
      matrixEntries
        .filter(_.i == similarUserIndex)
        .filter(e => !targetUserMoviesBc.value.contains(e.j))
        .map(e => (e.j, entry.value * e.value)) // 加权评分
        .collect()
    }
    println(s"[DEBUG] 候选电影数量: ${candidateMovies.length}")

    // 汇总电影评分
    val movieScores = collection.mutable.Map[Long, Double]()
    candidateMovies.foreach { case (movieId, score) =>
      movieScores(movieId) = movieScores.getOrElse(movieId, 0.0) + score
    }
    println(s"[DEBUG] 有效候选电影数: ${movieScores.size}")
    // 获取评分最高的N部电影
    val topNMovies = movieScores.toArray
      .sortBy(-_._2)
      .take(n)
      .map(_._1)

    progressAccumulator.add(1)
    showProgress(progressAccumulator.value, totalSteps)

    // 转换回原始电影ID
    val result = topNMovies.flatMap(movieIdx => movieReverseMap.get(movieIdx))

    // 释放广播变量
    targetUserMoviesBc.unpersist()

    // 释放矩阵缓存
    coordMatrix.entries.unpersist()

    if (result.isEmpty) {
      println("[WARN] 推荐结果为空，返回热门电影")
      dbService.getPopularMovies(spark, n).map(_.movieId).toArray
    } else result
  }

  private def buildMappings(ratingsDF: DataFrame): (Map[String, Long], Map[Int, Long]) = {
    val userMapRDD = ratingsDF.select("user").distinct().rdd
      .map(_.getString(0))
      .zipWithUniqueId()  // 生成唯一ID（分布式操作）
      .map { case (user, id) => (user, id) }

    // 生成电影索引（分布式操作）
    val movieMapRDD = ratingsDF.select("movie").distinct().rdd
      .map(_.getInt(0))
      .zipWithUniqueId()  // 生成唯一ID（分布式操作）
      .map { case (movie, id) => (movie, id) }

    // 将结果收集到Driver端（注意数据量需可控）
    val userMap = userMapRDD.collectAsMap()
    val movieMap = movieMapRDD.collectAsMap()

    (userMap.toMap, movieMap.toMap)
  }

  // 进度条显示函数
  private def showProgress(current: Long, total: Long): Unit = {
    val percent = (current.toDouble / total * 100).toInt
    val bar = "=" * (percent / 2) + " " * (50 - percent / 2)
    print(s"\r[$bar] $percent% ($current/$total)")
    if (current == total) println()
  }
}