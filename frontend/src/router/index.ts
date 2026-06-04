import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    // Public
    {
      path: '/',
      component: () => import('@/views/LandingView.vue')
    },
    {
      path: '/login',
      component: () => import('@/views/LoginView.vue')
    },
    {
      path: '/register',
      component: () => import('@/views/RegisterView.vue')
    },
    // Authenticated
    {
      path: '/app',
      component: () => import('@/views/DashboardView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/knowledge-base/:id',
      component: () => import('@/views/KnowledgeBaseView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/document/:id',
      component: () => import('@/views/DocumentView.vue'),
      meta: { requiresAuth: true }
    }
  ]
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if ((to.path === '/login' || to.path === '/register') && token) {
    next('/app')
  } else {
    next()
  }
})

export default router
