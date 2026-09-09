import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/Login.vue'),
      meta: { title: '登录' }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/Register.vue'),
      meta: { title: '注册' }
    },
    {
      path: '/',
      component: () => import('@/layouts/MainLayout.vue'),
      children: [
        { path: '', name: 'home', component: () => import('@/views/Home.vue'), meta: { title: '首页' } },
        { path: 'post/:id', name: 'postDetail', component: () => import('@/views/PostDetail.vue') },
        { path: 'write', name: 'postEdit', component: () => import('@/views/PostEdit.vue'), meta: { requiresAuth: true, title: '发帖' } },
        { path: 'edit/:id', name: 'postEditId', component: () => import('@/views/PostEdit.vue'), meta: { requiresAuth: true, title: '编辑' } },
        { path: 'user/:id', name: 'userProfile', component: () => import('@/views/UserProfile.vue') },
        { path: 'notifications', name: 'notifications', component: () => import('@/views/Notification.vue'), meta: { requiresAuth: true, title: '通知' } }
      ]
    },
    { path: '/:pathMatch(.*)*', name: 'notFound', component: () => import('@/views/NotFound.vue') }
  ]
})

// 全局前置守卫:未登录拦截
router.beforeEach((to) => {
  if (to.meta.requiresAuth && !localStorage.getItem('token')) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} · TechHub` : 'TechHub 技术社区'
})

export default router
