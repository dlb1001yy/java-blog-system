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
      // 简历管理暂时屏蔽（恢复时取消注释，并同步恢复 Sidebar.vue 中的菜单项）
      // {
      //   path: 'resume',
      //   name: 'ResumeEdit',
      //   component: () => import('@/views/ResumeEdit.vue'),
      //   meta: { title: '简历管理', icon: 'User' }
      // },
      {
        path: 'resumeManage',
        name: 'ResumeManage',
        component: () => import('@/views/ResumeManage.vue'),
        meta: { title: '用户简历管理', icon: 'Avatar' }
      },
      {
        path: 'link',
        name: 'LinkList',
        component: () => import('@/views/LinkList.vue'),
        meta: { title: '友情链接', icon: 'Link' }
      },
      {
        path: 'interview-questions',
        name: 'InterviewQuestionList',
        component: () => import('@/views/InterviewQuestionList.vue'),
        meta: { title: '面试题管理', icon: 'Notebook' }
      },
      {
        path: 'music',
        name: 'MusicManage',
        component: () => import('@/views/MusicManage.vue'),
        meta: { title: '音乐管理', icon: 'Headset' }
      },
      {
        path: 'exam-questions',
        name: 'ExamQuestionList',
        component: () => import('@/views/ExamQuestionList.vue'),
        meta: { title: '题库管理', icon: 'Collection' }
      },
      {
        path: 'exam-papers',
        name: 'ExamPaperList',
        component: () => import('@/views/ExamPaperList.vue'),
        meta: { title: '试卷管理', icon: 'Tickets' }
      },
      {
        path: 'marking',
        name: 'MarkingCenter',
        component: () => import('@/views/MarkingCenter.vue'),
        meta: { title: '阅卷中心', icon: 'EditPen' }
      },
      {
        path: 'users',
        name: 'UserList',
        component: () => import('@/views/UserList.vue'),
        meta: { title: '用户管理', icon: 'UserFilled' }
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