<script setup lang="ts">
import { ref, watchEffect } from 'vue'
import { useRoute } from 'vue-router'
import { ElInput, ElButton, ElIcon, ElSkeleton, ElEmpty } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import MovieCard from '@/components/MovieCard.vue'
import SearchBar from '@/components/SearchBar.vue'
import type { Recommendation } from '@/types/movie'
const recommendations = ref<Recommendation[]>([])

const route = useRoute()
const isLoading = ref(false)
const error = ref<string | null>(null)

const fetchResults = async (query: string) => {
  try {
    isLoading.value = true
    error.value = null
    
    // 实际API调用
    const response = await fetch(
      `http://localhost:8080/api/search?q=${encodeURIComponent(query)}`
    )
    if (!response.ok) throw new Error('搜索失败')
    
    const recommendation = await response.json()
    recommendations.value = recommendation.data;

  } catch (err) {
    error.value = err instanceof Error ? err.message : '未知错误'
  } finally {
    isLoading.value = false
  }
}

watchEffect(() => {
  const query = route.query.q
  if (typeof query === 'string' && query.trim()) {
    fetchResults(query.trim())
  }
})
</script>

<template>
  <div class="search-page">
    <div class="search-box">
      <SearchBar />
    </div>

    <div class="results-container">
      <h1 v-if="typeof route.query.q === 'string'" class="search-title">
        得到 <em>{{ recommendations.length }}</em> 个搜索结果
      </h1>

      <el-skeleton :rows="6" animated v-if="isLoading" />

      <el-empty v-else-if="error" :description="error" image="/error.png" />

      <el-empty
        v-else-if="recommendations.length === 0"
        description="未找到相关结果"
      />

      <div v-else class="movie-list">
        <MovieCard
          v-for="recommendation in recommendations"
          :key="recommendation.movieId"
          :movie="{
            id: recommendation.movieId,
            title: recommendation.title,
            poster: recommendation.cover,
            genres: recommendation.genres,
            year: recommendation.year,
            rating: recommendation.score,
            userRating:1
          }"
        />
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.search-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.search-box {
  margin-bottom: 40px;
}

.search-title {
  font-size: 24px;
  margin-bottom: 30px;
  
  em {
    color: var(--el-color-primary);
    font-style: normal;
  }
}

.results-list {
  background: var(--el-bg-color);
  border-radius: 8px;
  box-shadow: var(--el-box-shadow-light);
}

.result-item {
  padding: 20px;
  border-bottom: 1px solid var(--el-border-color);
  transition: background 0.3s;

  &:hover {
    background: var(--el-fill-color-light);
  }

  &:last-child {
    border-bottom: none;
  }

  .title {
    color: var(--el-color-primary);
    margin-bottom: 8px;
    font-size: 18px;
  }

  .content {
    color: var(--el-text-color-secondary);
    line-height: 1.6;
  }
}

.movie-grid {
      margin: -10px;
  
      .el-col {
        padding: 10px;
      }
    }
  
    .rated-movie-card {
      background: #fff;
      border-radius: 8px;
      overflow: hidden;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
      transition: transform 0.3s ease;
  
      &:hover {
        transform: translateY(-5px);
      }
  
      .movie-cover {
        position: relative;
        padding-top: 150%;
        background: #f5f7fa;
  
        img {
          position: absolute;
          top: 0;
          left: 0;
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }
  
      .movie-info {
        padding: 15px;
  
        .movie-title {
          font-size: 1rem;
          margin: 0 0 10px;
          color: #303133;
          line-height: 1.4;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }
  
        .meta-info {
          display: flex;
          flex-direction: column;
          gap: 8px;
          margin-bottom: 12px;
  
          .year {
            font-size: 0.9rem;
            color: #909399;
          }
  
          .rating-stars {
            :deep(.el-rate__icon) {
              font-size: 16px;
            }
          }
  
          .rating-value {
            font-size: 0.9rem;
            color: #FF9900;
            font-weight: 500;
          }
        }
  
        .genre-tags {
          display: flex;
          flex-wrap: wrap;
          gap: 6px;
  
          .genre-tag {
            margin-right: 6px;
            background: rgba(64, 158, 255, 0.1);
            color: #409EFF;
            border: none;
          }
        }
      }
    }
  
    .movie-list {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
        gap: 20px;
    }

    .empty-state {
      margin: 40px 0;
    }
</style>