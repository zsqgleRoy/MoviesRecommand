package com.Royhang

import akka.http.scaladsl.settings.ServerSettings

import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethod

import scala.collection.immutable
import akka.http.scaladsl.server.Directives._
import ch.megard.akka.http.cors.scaladsl.model.{HttpHeaderRange, HttpOriginMatcher}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.headers.{`Access-Control-Allow-Headers`, `Access-Control-Allow-Methods`, `Access-Control-Allow-Origin`}
import akka.http.scaladsl.model.{HttpMethods, StatusCodes}
import spray.json.DefaultJsonProtocol._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import org.apache.spark.sql.SparkSession
import spray.json.RootJsonFormat

import scala.concurrent.Future
import scala.io.StdIn

// 数据模型定义
case class UserRatingResponse(
                               movieId: Int,
                               rating: Double,
                               title: String,
                               cover: String,
                               genres: String,
                               year: Int
                             )
case class UserRating(
                       movieId: Int,
                       rating: Double,
                       title: String,
                       cover: String,
                       genres: String,
                       year: Int
                     )
case class MovieDetail(
                        movieId: Int,
                        name: String,
                        alias: Option[String],
                        cover: Option[String],
                        directors: Option[String],
                        actors: Option[String],
                        genres: Option[String],
                        languages: Option[String],
                        mins: Option[Int],
                        officialSite: Option[String],
                        regions: Option[String],
                        releaseDate: Option[String],
                        doubanScore: Option[Double],
                        doubanVotes: Option[Int],
                        storyline: Option[String],
                        imdbId: Option[String],
                        directorIds: Option[String],
                        actorIds: Option[String]
                      )
case class PersonDetail(
                         id: Int,
                         name: Option[String],
                         sex: Option[String],
                         nameEn: Option[String],
                         nameZh: Option[String],
                         birth: Option[String],
                         birthplace: Option[String],
                         constellatory: Option[String],
                         profession: Option[String],
                         biography: Option[String]
                       )
case class Recommendation(movieId: Int, title: String, cover: String, genres: String, year: Int, score: Float)
case class ApiResponse(code: Int, data: List[Recommendation], message: String)
case class RateResponse(code: Int, data: List[UserRatingResponse], message: String)
case class DetailResponse(code: Int, data: Option[MovieDetail], message: String)
case class AuthRequest(username: String, password: String)
case class AuthResponse(code: Int, message: String)
case class PersonResponse(code: Int, data: Option[PersonDetail], message: String)

object RecommendationCache {
  private var cache: Map[String, List[Recommendation]] = Map.empty

  def getOrUpdate(userId: String)(generate: => List[Recommendation]): List[Recommendation] = synchronized {
    cache.get(userId) match {
      case Some(existing) => existing
      case None =>
        val result = generate
        cache += (userId -> result)
        result
    }
  }
  def remove(userId: String): Unit = synchronized {
    cache -= userId
  }

  // 可选：添加清空缓存的扩展方法
  def clear(): Unit = synchronized {
    cache = Map.empty
  }
}

object RecommendationServer extends App {
  implicit val system: ActorSystem = ActorSystem("recommendation-system")

  import system.dispatcher
  import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings._ // 导入CORS隐式转换

  // 注册JSON格式
  implicit val personDetailFormat: RootJsonFormat[PersonDetail] = jsonFormat10(PersonDetail)
  implicit val personResponseFormat: RootJsonFormat[PersonResponse] = jsonFormat3(PersonResponse)
  implicit val recommendationFormat: RootJsonFormat[Recommendation] = jsonFormat6(Recommendation)
  implicit val responseFormat: RootJsonFormat[ApiResponse] = jsonFormat3(ApiResponse)
  implicit val authRequestFormat: RootJsonFormat[AuthRequest] = jsonFormat2(AuthRequest)
  implicit val authResponseFormat: RootJsonFormat[AuthResponse] = jsonFormat2(AuthResponse)
  implicit val movieDetailFormat: RootJsonFormat[MovieDetail] = jsonFormat18(MovieDetail)
  implicit val detailResponseFormat: RootJsonFormat[DetailResponse] = jsonFormat3(DetailResponse)

  case class RatingRequest(
                            userId: String,
                            movieId: Int,
                            rating: Double,
                            timestamp: String
                          )

  // 创建SparkSession
  val spark = SparkSession.builder()
    .appName("UserCFRecommender")
    .master("local[*]")
    .config("spark.driver.memory", "16g")          // 提升Driver内存
    .config("spark.executor.memory", "32g")        // 提升Executor内存
    .config("spark.memory.fraction", "0.8")       // 内存分配比例
    .config("spark.memory.storageFraction", "0.3")
    .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    .config("spark.sql.shuffle.partitions", "800")
    .config("spark.memory.offHeap.enabled", "true")
    .config("spark.memory.offHeap.size", "4g")
    .config("spark.task.maxFailures", "1")
    .config("spark.executor.extraJavaOptions", "-XX:+UseG1GC -XX:InitiatingHeapOccupancyPercent=35") // 优化GC
    .getOrCreate()

  // 注册剩余JSON格式
  implicit val userRatingFormat: RootJsonFormat[UserRatingResponse] = jsonFormat6(UserRatingResponse)
  implicit val ratingRequestFormat: RootJsonFormat[RatingRequest] = jsonFormat4(RatingRequest)

  // 假设DatabaseService已正确实现
  val dbService = new DatabaseService(
    "jdbc:mysql://localhost:3306/movie_db",
    "root",
    "123"
  )

  // 定义路由
  val route =
    path("api" / "recommend" / Segment) { userId =>
      get {
        parameters("q".?) { searchQuery =>
          complete {
            Future {
              try {
                val recommendations = RecommendationCache.getOrUpdate(userId) {
                  val ratingsDF = dbService.readRatingsAsDF(spark)
                  val recommender = new SparkUserCFRecommender(spark)
                  val movieIds = recommender.hybridRecommend(ratingsDF, userId, searchQuery)
                  dbService.getMovieDetailsSpark(spark, movieIds)
                }
                ApiResponse(200, recommendations, "Success")
              } catch {
                case e: Exception =>
                  ApiResponse(500, Nil, s"Error: ${e.getMessage}")
              }
            }
          }
        }
      }
    } ~
      path("api" / "users" / Segment / "ratings") { userId =>
        get {
          complete {
            Future {
              val ratings = dbService.getUserRatings(userId)
              ApiResponse(
                200,
                ratings.map(r => Recommendation(
                  r.movieId,
                  r.title,
                  r.cover,
                  r.genres,
                  r.year,
                  r.rating.toFloat
                )),
                "Success"
              )
            }
          }
        }
      }~
      path("api" / "search") {
        get {
          parameters("q".?, "page".as[Int].?(1), "pageSize".as[Int].?(20)) {
            (queryOpt, page, pageSize) =>
              complete {
                Future {
                  queryOpt match {
                    case Some(query) if query.nonEmpty =>
                      try {
                        val offset = (page - 1) * pageSize
                        val results = dbService.searchMovies(spark, query, pageSize + 1)
                        val hasMore = results.length > pageSize
                        val pagedResults = results.take(pageSize)

                        ApiResponse(
                          200,
                          pagedResults,
                          s"${pagedResults.length} 条结果" + (if(hasMore) " (还有更多)" else "")
                        )
                      } catch {
                        case e: Exception =>
                          ApiResponse(500, Nil, s"搜索失败: ${e.getMessage}")
                      }
                    case _ =>
                      ApiResponse(400, Nil, "请输入搜索关键词")
                  }
                }
              }
          }
        }
      } ~
      path("api" / "ratings") {
        post {
          entity(as[RatingRequest]) { request =>
            complete {
              Future {
                RecommendationCache.remove(request.userId)
                if (dbService.insertRating(
                  request.userId,
                  request.movieId,
                  request.rating,
                  request.timestamp
                )) {
                  ApiResponse(200, Nil, "评分成功")
                } else {
                  ApiResponse(500, Nil, "评分失败")
                }
              }
            }
          }
        }
      } ~
      path("api" / "register") {
        post {
          entity(as[AuthRequest]) { request =>
            complete {
              Future {
                if (dbService.registerUser(request.username, request.password)) {
                  AuthResponse(200, "Registration successful")
                } else {
                  AuthResponse(400, "Registration failed")
                }
              }
            }
          }
        }
      } ~
      path("api" / "login") {
        post {
          entity(as[AuthRequest]) { request =>
            complete {
              Future {
                if (dbService.loginUser(request.username, request.password)) {
                  AuthResponse(200, dbService.getToken())
                } else {
                  AuthResponse(401, "Invalid credentials")
                }
              }
            }
          }
        }
      } ~
      path("api" / "movies" / "recommendations") {
        get {
          complete {
            Future {
              val movies = dbService.getAllMovies()
              val recommendations = movies.map(m =>
                Recommendation(
                  m.movieId,
                  m.title,
                  m.cover,
                  m.genres,
                  m.year,
                  m.score
                )
              )
              ApiResponse(200, recommendations, "Success")
            }
          }
        }
      } ~
      path("api" / "movies" / IntNumber) { movieId =>
        get {
          complete {
            Future {
              dbService.getMovieDetail(movieId) match {
                case Some(detail) =>
                  DetailResponse(200, Some(detail), "Success")
                case None =>
                  DetailResponse(404, None, "Movie not found")
              }
            }
          }
        }
      } ~
      path("api" / "person" / IntNumber) { personId =>
        get {
          complete {
            Future {
              dbService.getPersonDetail(personId) match {
                case Some(detail) =>
                  PersonResponse(200, Some(detail), "Success")
                case None =>
                  PersonResponse(404, None, "Person not found")
              }
            }
          }
        }
      }

  // CORS配置
  val corsSettings = CorsSettings.defaultSettings
    .withAllowedOrigins(HttpOriginMatcher.*)
    .withAllowedMethods(
      immutable.Seq[HttpMethod](
        HttpMethods.GET,
        HttpMethods.POST,
        HttpMethods.OPTIONS
      )
    )
    .withAllowedHeaders(HttpHeaderRange(
      "Content-Type",
      "Authorization",
      "X-Requested-With",
      "Accept",
      "Origin" // 新增Origin头支持
    ))
    .withAllowCredentials(true)
    .withMaxAge(Some(36000L))

  // 错误处理函数
  def handleErrors(inner: Route): Route = {
    handleExceptions(ExceptionHandler {
      case e: Exception =>
        // 显式添加 CORS 头
        respondWithHeaders(
          `Access-Control-Allow-Methods`(HttpMethods.GET, HttpMethods.POST, HttpMethods.OPTIONS),
          `Access-Control-Allow-Headers`("Content-Type", "Authorization", "X-Requested-With", "Accept", "Origin")
        ) {
          complete(StatusCodes.InternalServerError -> ApiResponse(500, Nil, e.getMessage))
        }
    }) {
      handleRejections(corsRejectionHandler) {
        inner
      }
    }
  }
  // 最终路由
  val finalRoute = cors(corsSettings) {
    handleErrors(route)
  }

  // 配置服务器超时
  val originalSettings = ServerSettings(system)
  val updatedTimeouts = originalSettings.timeouts.withRequestTimeout(3600.seconds)
  val serverSettings = originalSettings.withTimeouts(updatedTimeouts)

  // 启动服务器
  val bindingFuture = Http().bindAndHandle(
    handler = finalRoute,
    interface = "localhost",
    port = 8080,
    settings = serverSettings
  )

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")

  // 优雅关闭
  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete { _ =>
      spark.stop() // 关闭SparkSession
      system.terminate()
      println("Server stopped")
    }
}