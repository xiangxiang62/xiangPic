import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '../pages/HomePage.vue'  // 根据实际路径修改
import UserLoginPage from '../pages/user/UserLoginPage.vue'  // 根据实际路径修改
import UserRegisterPage from '../pages/user/UserRegisterPage.vue'  // 根据实际路径修改
import UserManagePage from '../pages/admin/UserManagePage.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: '主页',
      component: HomePage,
    },
    {
      path: '/user/login',
      name: '用户登录',
      component: UserLoginPage,
    },
    {
      path: '/user/register',
      name: '用户注册',
      component: UserRegisterPage,
    },
    {
      path: '/admin/userManage',
      name: '用户管理',
      component: UserManagePage,
    },
  ],
})

export default router