import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue')
  },
  {
    path: '/articles',
    name: 'ArticleList',
    component: () => import('@/views/ArticleList.vue')
  },
  {
    path: '/article/:id',
    name: 'ArticleDetail',
    component: () => import('@/views/ArticleDetail.vue')
  },
  {
    path: '/category',
    name: 'Category',
    component: () => import('@/views/Category.vue')
  },
  {
    path: '/category/:id',
    name: 'CategoryArticles',
    component: () => import('@/views/ArticleList.vue')
  },
  {
    path: '/tags',
    name: 'Tags',
    component: () => import('@/views/Tags.vue')
  },
  {
    path: '/archives',
    name: 'Archives',
    component: () => import('@/views/Archives.vue')
  },
  {
    path: '/music',
    name: 'Music',
    component: () => import('@/views/Music.vue')
  },
  {
    path: '/resume',
    name: 'Resume',
    component: () => import('@/views/Resume.vue')
  },
  {
    path: '/about',
    name: 'About',
    component: () => import('@/views/About.vue')
  },
  {
    path: '/interview',
    name: 'Interview',
    meta: { title: '面试刷题' },
    component: () => import('@/views/Interview.vue')
  },
  {
    path: '/messages',
    name: 'Messages',
    component: () => import('@/views/MessageBoard.vue')
  },
  {
    path: '/exam',
    name: 'Exam',
    component: () => import('@/views/Exam.vue')
  },
  {
    path: '/scores',
    name: 'Scores',
    meta: { title: '成绩查询' },
    component: () => import('@/views/Scores.vue')
  },
  {
    path: '/exam/:paperId',
    name: 'ExamTaking',
    component: () => import('@/views/ExamTaking.vue'),
    meta: { hideLayout: true }
  }
]

const router = createRouter({
  history: createWebHistory('/blog/'),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) return savedPosition
    return { top: 0 }
  }
})

export default router