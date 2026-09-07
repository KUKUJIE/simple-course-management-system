@@
         {
           path: 'homeworks/:homeworkId/submissions/:submissionId',
           name: 'TeacherHomeworkSubmissionGrade',
           component: () => import('@/views/teacher/homework/SubmissionGrade.vue'),
           meta: { title: '批阅提交' }
         },
+        {
+          path: 'homeworks/create',
+          name: 'TeacherHomeworkCreate',
+          component: () => import('@/views/teacher/homework/CreateHomework.vue'),
+          meta: { title: '创建作业' }
+        },
         {
           path: 'homeworks/:id/stats',
           name: 'TeacherHomeworkStats',
           component: () => import('@/views/teacher/homework/HomeworkStats.vue'),
           meta: { title: '作业统计' }
         }
       ]
