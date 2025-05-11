import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeMovies.vue'
import GETRecommand from '@/views/MovieRecommendation.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/LoginRegister',
      name: 'loginRegister',
      component: () => import('@/components/common/LoginRegister.vue')
    },
    {
      path: '/new',
      name: 'new',
      component: () => import('@/components/common/LoginRegister.vue')
    },
    {
      path: '/dafen',
      name: 'dafen',
      component: () => import('@/views/DaFen.vue')
    },
    {
      path: '/hot',
      name: 'hot',
      component: () => import('@/components/common/LoginRegister.vue')
    },
    {
      path: '/rated',
      name: 'rated',
      component: () => import('@/views/rated.vue')
    },
    {
      path: '/movie/:id',
      name: 'movie_detail',
      component: () => import('@/views/MovieDetail.vue')
    },
    {
      path: '/like',
      name: 'like',
      component: () => import('@/components/common/LoginRegister.vue')
    },
    {
      path: '/movies',
      name: 'movies',
      component: () => import('@/components/common/LoginRegister.vue')
    },
    {
      path: '/movies/:id',
      name: 'movie',
      component: () => import('@/components/common/LoginRegister.vue')
    },
    {
      path: '/contact',
      name: 'contact',
      component: () => import('@/components/common/LoginRegister.vue')
    },
    {
      path: '/getMovieRecommand',
      name: 'getMovieRecommand',
      component: GETRecommand,
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('../views/AboutView.vue'),
    },
    {
      path: '/search',
      name: 'SearchResults',
      component: () => import('@/views/SearchResults.vue'),
      props: (route) => ({ query: route.query.q })
    }
  ],
})

export default router
