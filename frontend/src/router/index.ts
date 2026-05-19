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
    path: '/team-management',
    name: 'TeamManagement',
    component: () => import('../pages/TeamManagement.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/tournaments/:torneoId/matches',
    name: 'MatchSchedule',
    component: () => import('../pages/MatchSchedulePage.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/admin/matches',
    name: 'AdminMatchManager',
    component: () => import('../pages/AdminMatchManagerPage.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin/users',
    name: 'AdminUserManager',
    component: () => import('../pages/AdminUserManagerPage.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/capitan/jugadores',
    name: 'CaptainPlayerManager',
    component: () => import('../pages/CaptainPlayerManagerPage.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('../pages/HomePage.vue'),
    meta: { requiresAuth: false }
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
