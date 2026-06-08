import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: { title: '抽背首页' }
  },
  {
    path: '/questions',
    name: 'QuestionBank',
    component: () => import('../views/QuestionBank.vue'),
    meta: { title: '题库管理' }
  },
  {
    path: '/special-practice',
    name: 'SpecialPractice',
    component: () => import('../views/SpecialPractice.vue'),
    meta: { title: '专项抽题' }
  },
  {
    path: '/favorites',
    name: 'Favorites',
    component: () => import('../views/Favorites.vue'),
    meta: { title: '收藏题目' }
  },
  {
    path: '/unmastered',
    name: 'Unmastered',
    component: () => import('../views/Unmastered.vue'),
    meta: { title: '错题集' }
  },
  {
    path: '/statistics',
    name: 'Statistics',
    component: () => import('../views/Statistics.vue'),
    meta: { title: '学习诊断' }
  },
  {
    path: '/backups',
    name: 'BackupManagement',
    component: () => import('../views/BackupManagement.vue'),
    meta: { title: '备份管理' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
