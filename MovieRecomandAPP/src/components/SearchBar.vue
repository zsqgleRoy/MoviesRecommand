<!-- components/SearchBar.vue -->
<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElInput, ElButton, ElIcon } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const router = useRouter()
const searchQuery = ref('')

const performSearch = () => {
  const query = searchQuery.value.trim()
  if (query) {
    router.push({
      path: '/search',
      query: { q: query }
    })
    searchQuery.value = ''
  }
}
</script>

<template>
  <div class="search-container">
    <el-input
      v-model="searchQuery"
      placeholder="请输入搜索关键词"
      size="large"
      clearable
      @keyup.enter="performSearch"
    >
      <template #append>
        <el-button
          type="primary"
          :icon="Search"
          size="large"
          @click="performSearch"
        />
      </template>
    </el-input>
  </div>
</template>

<style scoped>
.search-container {
  max-width: 800px;
  margin: 20px auto;
  padding: 20px;

  :deep(.el-input-group__append) {
    background-color: var(--el-color-primary);
    padding: 0 20px;
    
    .el-button {
      color: white;
    }
  }
}
</style>