@@
       {
         path: 'submissions',
         name: 'StudentSubmissions',
         component: () => import('@/views/student/homework/HomeworkList.vue'),
         meta: { title: '作业列表' }
       },
+      {
+        path: 'homeworks/:homeworkId/submissions/:submissionId/result',
+        name: 'StudentSubmissionResult',
+        component: () => import('@/views/student/homework/SubmissionResult.vue'),
+        meta: { title: '批改结果' }
+      },
       {
         path: 'course-selection',
         name: 'CourseSelection',
         component: () => import('@/views/student/course-selection/index.vue'),
         meta: { title: '选课中心' }
       },
