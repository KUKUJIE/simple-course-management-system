package com.agiantii.backend.controller;

import com.agiantii.backend.common.R;
import com.agiantii.backend.common.TokenStore;
import com.agiantii.backend.mapper.HomeworkMapper;
import com.agiantii.backend.mapper.HomeworkSubmissionMapper;
import com.agiantii.backend.pojo.homework.Homework;
import com.agiantii.backend.pojo.homework.HomeworkSubmission;
import com.agiantii.backend.utils.FileStorageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/homeworks")
public class SubmissionController {

    @Autowired
    private HomeworkMapper homeworkMapper;

    @Autowired
    private HomeworkSubmissionMapper submissionMapper;

    @Resource
    private TokenStore tokenStore;

    @PostMapping("/{id}/submissions")
    public R<HomeworkSubmission> submitHomework(
            @PathVariable("id") Long homeworkId,
            @RequestParam(value = "studentId", required = false) Long studentIdParam,
            @RequestParam(value = "submitText", required = false) String submitText,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        // Resolve token if provided
        Map<String, Object> tokenInfo = tokenStore.resolve(authHeader);

        Long studentId = null;
        if (tokenInfo != null) {
            // token contains entityId which maps to student/teacher/admin entity
            Object role = tokenInfo.get("role");
            if (!"student".equals(role)) {
                return R.error("Only students can submit homeworks with token authentication",403);
            }
            Integer entityId = (Integer) tokenInfo.get("entityId");
            if (entityId != null) studentId = entityId.longValue();
        }

        // fallback to request param if token not present
        if (studentId == null) {
            if (studentIdParam == null) {
                return R.error("studentId is required when no Authorization token provided",400);
            }
            studentId = studentIdParam;
        }

        Homework hw = homeworkMapper.selectById(homeworkId);
        if (hw == null) return R.error("Homework not found",400);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = hw.getDeadline();
        boolean isLate = false;
        if (deadline != null && now.isAfter(deadline)) {
            isLate = true;
        }
        if (isLate && (hw.getAllowLate() == null || hw.getAllowLate() == 0)) {
            return R.error("Deadline passed and late submissions are not allowed",400);
        }

        String attachmentPath = null;
        if (file != null && !file.isEmpty()) {
            try {
                // validate
                FileStorageUtil.validateFile(file);
                attachmentPath = FileStorageUtil.saveFile(file);
            } catch (IOException e) {
                e.printStackTrace();
                return R.error("File save failed: " + e.getMessage(),400);
            }
        }

        HomeworkSubmission submission = new HomeworkSubmission();
        submission.setHomeworkId(homeworkId);
        submission.setStudentId(studentId);
        submission.setSubmitTime(now);
        submission.setIsLate(isLate ? 1 : 0);
        submission.setStatus("submitted");
        submission.setSubmitText(submitText);
        submission.setAttachmentPath(attachmentPath);

        int inserted = submissionMapper.insert(submission);
        if (inserted > 0) {
            return R.success(submission, "Submitted");
        }
        return R.error("Failed to save submission",500);
    }

    @GetMapping("/{id}/submissions")
    public R<List<HomeworkSubmission>> listSubmissions(@PathVariable("id") Long homeworkId, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // if token present, only allow teachers of this homework or admins
        Map<String, Object> tokenInfo = tokenStore.resolve(authHeader);
        if (tokenInfo != null) {
            String role = (String) tokenInfo.get("role");
            Integer entityId = (Integer) tokenInfo.get("entityId");
            if ("teacher".equals(role)) {
                Homework hw = homeworkMapper.selectById(homeworkId);
                if (hw == null) return R.error("Homework not found",400);
                if (!entityId.equals(hw.getTeacherId().intValue())) {
                    return R.error("Teacher not authorized to view submissions for this homework",403);
                }
            }
            // admins allowed
        }

        List<HomeworkSubmission> list = submissionMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<HomeworkSubmission>()
                        .eq("homework_id", homeworkId)
        );
        return R.success(list, "ok");
    }

    @GetMapping("/submissions/{submissionId}")
    public R<HomeworkSubmission> getSubmission(@PathVariable("submissionId") Long submissionId, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        HomeworkSubmission s = submissionMapper.selectById(submissionId);
        if (s == null) return R.error("Submission not found",404);

        Map<String, Object> tokenInfo = tokenStore.resolve(authHeader);
        if (tokenInfo != null) {
            String role = (String) tokenInfo.get("role");
            Integer entityId = (Integer) tokenInfo.get("entityId");
            if ("student".equals(role)) {
                if (!entityId.equals(s.getStudentId().intValue())) {
                    return R.error("Student not authorized to view this submission",403);
                }
            } else if ("teacher".equals(role)) {
                Homework hw = homeworkMapper.selectById(s.getHomeworkId());
                if (hw == null) return R.error("Homework not found",400);
                if (!entityId.equals(hw.getTeacherId().intValue())) {
                    return R.error("Teacher not authorized to view this submission",403);
                }
            }
        }

        return R.success(s, "ok");
    }
}
