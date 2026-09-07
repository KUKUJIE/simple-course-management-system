@@
       {
         path: '/admin',
         name: 'Admin',
         component: () => import('@/views/admin/index.vue'),
         children: [
+          { path: 'users', name: 'AdminUsers', component: () => import('@/views/admin/UserManagement.vue'), meta: { title: '用户管理' } },
+          { path: 'courses', name: 'AdminCourses', component: () => import('@/views/admin/CourseManagement.vue'), meta: { title: '课程管理' } },
           { path: 'settings', name: 'AdminSettings', component: () => import('@/views/admin/settings/index.vue'), meta: { title: '系统设置' } }
         ]
       },
