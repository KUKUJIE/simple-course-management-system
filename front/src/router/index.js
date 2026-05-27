import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/views/login/index.vue'
import StudentLayout from '@/layouts/StudentLayout.vue'
import TeacherLayout from '@/layouts/TeacherLayout.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: Login
    },
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/student',
      component: StudentLayout,
      redirect: '/student/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'StudentDashboard',
          component: () => import('@/views/student/dashboard/index.vue'),
          meta: { title: '首页' }
        },
        {
          path: 'course-selection',
          name: 'CourseSelection',
          component: () => import('@/views/student/course-selection/index.vue'),
          meta: { title: '选课中心' }
        },
        {
          path: 'my-courses',
          name: 'MyCourses',
          component: () => import('@/views/student/my-courses/index.vue'),
          meta: { title: '我的课程' }
        },
        {
          path: 'grades',
          name: 'Grades',
          component: () => import('@/views/student/grades/index.vue'),
          meta: { title: '成绩查询' }
        },
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('@/views/student/profile/index.vue'),
          meta: { title: '个人信息' }
        }
      ]
    },
    {
      path: '/teacher',
      component: TeacherLayout,
      redirect: '/teacher/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'TeacherDashboard',
          component: () => import('@/views/teacher/dashboard/index.vue'),
          meta: {
            title: '教师工作台',
            requiresAuth: true,
            role: 'teacher'
          }
        },
        {
          path: 'courses',
          name: 'TeacherCourses',
          component: () => import('@/views/teacher/courses/index.vue'),
          meta: {
            title: '课程管理',
            requiresAuth: true,
            role: 'teacher'
          }
        },
        {
          path: 'grades',
          name: 'TeacherGrades',
          component: () => import('@/views/teacher/grades/index.vue'),
          meta: {
            title: '成绩管理',
            requiresAuth: true,
            role: 'teacher'
          }
        },
        {
          path: 'profile',
          name: 'TeacherProfile',
          component: () => import('@/views/teacher/profile/index.vue'),
          meta: {
            title: '个人信息',
            requiresAuth: true,
            role: 'teacher'
          }
        }
      ]
    },
    {
      path: '/admin',
      component: AdminLayout,
      redirect: '/admin/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'AdminDashboard',
          component: () => import('@/views/admin/dashboard/index.vue'),
          meta: {
            title: '管理员工作台',
            requiresAuth: true,
            role: 'admin'
          }
        },
        {
          path: 'students',
          name: 'StudentManagement',
          component: () => import('@/views/admin/students/index.vue'),
          meta: {
            title: '学生管理',
            requiresAuth: true,
            role: 'admin'
          }
        },
        {
          path: 'teachers',
          name: 'TeacherManagement',
          component: () => import('@/views/admin/teachers/index.vue'),
          meta: {
            title: '教师管理',
            requiresAuth: true,
            role: 'admin'
          }
        },
        {
          path: 'courses',
          name: 'CourseManagement',
          component: () => import('@/views/admin/courses/index.vue'),
          meta: {
            title: '课程管理',
            requiresAuth: true,
            role: 'admin'
          }
        },
        {
          path: 'sections',
          name: 'SectionManagement',
          component: () => import('@/views/admin/sections/index.vue'),
          meta: {
            title: '教学班管理',
            requiresAuth: true,
            role: 'admin'
          }
        },
        {
          path: 'departments',
          name: 'DepartmentManagement',
          component: () => import('@/views/admin/departments/index.vue'),
          meta: {
            title: '院系管理',
            requiresAuth: true,
            role: 'admin'
          }
        },
        {
          path: 'majors',
          name: 'MajorManagement',
          component: () => import('@/views/admin/majors/index.vue'),
          meta: {
            title: '专业管理',
            requiresAuth: true,
            role: 'admin'
          }
        },
        {
          path: 'classrooms',
          name: 'ClassroomManagement',
          component: () => import('@/views/admin/classrooms/index.vue'),
          meta: {
            title: '教室管理',
            requiresAuth: true,
            role: 'admin'
          }
        }
      ]
    },
    {
      path: '/403',
      name: 'Forbidden',
      component: () => import('@/views/error/403.vue'),
      meta: { title: '无权访问' }
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/error/404.vue'),
      meta: { title: '页面不存在' }
    }
  ]
})

router.beforeEach((to, from, next) => {
  const uid = localStorage.getItem('uid')
  const role = localStorage.getItem('role')

  if (to.meta.requiresAuth) {
    if (!uid || !role) {
      next('/login')
    } else if (to.meta.role && to.meta.role !== role) {
      next('/403')
    } else {
      next()
    }
  } else {
    if (uid && role && to.path === '/login') {
      const routes = {
        student: '/student/dashboard',
        teacher: '/teacher/dashboard',
        admin: '/admin/dashboard'
      }
      next(routes[role])
    } else {
      next()
    }
  }
})

export default router
