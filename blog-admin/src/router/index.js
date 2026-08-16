import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/redirect/:path(.*)',
    name: 'Redirect',
    component: () => import('@/views/Redirect.vue'),
    meta: { title: 'Redirect', hidden: true }
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '数据看板', icon: 'DataAnalysis' }
      },
      {
        path: 'article',
        name: 'ArticleList',
        component: () => import('@/views/ArticleList.vue'),
        meta: { title: '文章管理', icon: 'Document' }
      },
      {
        path: 'article/create',
        name: 'ArticleCreate',
        component: () => import('@/views/ArticleEdit.vue'),
        meta: { title: '写文章', hidden: true }
      },
      {
        path: 'article/edit/:id',
        name: 'ArticleEdit',
        component: () => import('@/views/ArticleEdit.vue'),
        meta: { title: '编辑文章', hidden: true }
      },
      {
        path: 'category',
        name: 'CategoryList',
        component: () => import('@/views/CategoryList.vue'),
        meta: { title: '分类管理', icon: 'Folder' }
      },
      {
        path: 'tag',
        name: 'TagList',
        component: () => import('@/views/TagList.vue'),
        meta: { title: '标签管理', icon: 'PriceTag' }
      },
      {
        path: 'comment',
        name: 'CommentList',
        component: () => import('@/views/CommentList.vue'),
        meta: { title: '评论管理', icon: 'ChatDotRound' }
      },
      {
        path: 'message',
        name: 'MessageList',
        component: () => import('@/views/MessageList.vue'),
        meta: { title: '留言管理', icon: 'Message' }
      },
      {
        path: 'resume',
        name: 'ResumeEdit',
        component: () => import('@/views/ResumeEdit.vue'),
        meta: { title: '简历管理', icon: 'User' }
      },
      {
        path: 'link',
        name: 'LinkList',
        component: () => import('@/views/LinkList.vue'),
        meta: { title: '友情链接', icon: 'Link' }
      },
      {
        path: 'operation-log',
        name: 'OperationLogList',
        component: () => import('@/views/OperationLogList.vue'),
        meta: { title: '操作日志', icon: 'Clock' }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/Settings.vue'),
        meta: { title: '系统设置', icon: 'Setting' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory('/admin/'),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.path !== '/login' && !userStore.token) {
    next('/login')
  } else {
    next()
  }
})

export default router