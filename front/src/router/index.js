@@
         {
           path: 'homeworks/:homeworkId/submissions/:submissionId',
           name: 'TeacherHomeworkSubmissionGrade',
           component: () => import('@/views/teacher/homework/SubmissionGrade.vue'),
           meta: { title: '批阅提交' }
         },
+        {
+          path: 'homeworks/:id/stats',
+          name: 'TeacherHomeworkStats',
+          component: () => import('@/views/teacher/homework/HomeworkStats.vue'),
+          meta: { title: '作业统计' }
+        }
       ]
@@
