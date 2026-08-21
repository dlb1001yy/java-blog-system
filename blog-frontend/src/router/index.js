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
  // 简历展示功能暂时屏蔽（恢复时取消注释即可；我的简历编辑 /profile/resume、分享页 /resume/share/:token 保留）
  // {
  //   path: '/resume',
  //   name: 'Resume',
  //   meta: { requiresAuth: true },
  //   component: () => import('@/views/Resume.vue')
  // },
  {
    path: '/resume/share/:token',
    name: 'ResumeShare',
    meta: { title: '简历' },
    component: () => import('@/views/Resume.vue')
  },
  // {
  //   path: '/resume/:userId',
  //   name: 'UserResume',
  //   meta: { requiresAuth: true },
  //   component: () => import('@/views/Resume.vue')
  // },
  {
    path: '/profile/resume',
    name: 'ProfileResume',
    meta: { requiresAuth: true },
    component: () => import('@/views/ProfileResume.vue')
  },
  {
    path: '/about',
    name: 'About',
    component: () => import('@/views/About.vue')
  },
  {
    path: '/about-site',
    name: 'AboutSite',
    meta: { title: '关于站点' },
    component: () => import('@/views/AboutSite.vue')
  },
  {
    path: '/interview',
    name: 'Interview',
    meta: { title: '面试刷题', requiresAuth: true },
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
    meta: { requiresAuth: true },
    component: () => import('@/views/Exam.vue')
  },
  {
    path: '/scores',
    name: 'Scores',
    meta: { title: '成绩查询', requiresAuth: true },
    component: () => import('@/views/Scores.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue')
  },
  {
    path: '/exam/:paperId',
    name: 'ExamTaking',
    component: () => import('@/views/ExamTaking.vue'),
    meta: { hideLayout: true, requiresAuth: true }
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

// 登录限制：刷题/考试/成绩/简历相关页面未登录跳转登录页（登录后回跳）
router.beforeEach((to) => {
  if (to.meta.requiresAuth && !localStorage.getItem('token')) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
})

export default router