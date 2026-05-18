import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../pages/LoginPage.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../pages/DashboardPage.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/tournaments',
    name: 'TournamentManager',
    component: () => import('../pages/TournamentManager.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/',
    redirect: () => {
      const auth = useAuthStore()
      return auth.isAuthenticated ? '/dashboard' : '/login'
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  const auth = useAuthStore()
  const requiresAuth = to.meta.requiresAuth as boolean | undefined

  if (to.path === '/login' && auth.isAuthenticated) {
    next('/dashboard')
  } else if (requiresAuth && !auth.isAuthenticated) {
    next('/login')
  } else {
    next()
  }
})

export default router
