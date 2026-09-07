package com.agiantii.backend.controller;

import com.agiantii.backend.common.TokenStore;
import com.agiantii.backend.mapper.HomeworkMapper;
import com.agiantii.backend.mapper.HomeworkSubmissionMapper;
import com.agiantii.backend.pojo.homework.Homework;
import com.agiantii.backend.pojo.homework.HomeworkSubmission;
import com.agiantii.backend.utils.FileStorageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> submitHomework(
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
                return ResponseEntity.status(403).body("Only students can submit homeworks with token authentication");
            }
            Integer entityId = (Integer) tokenInfo.get("entityId");
            if (entityId != null) studentId = entityId.longValue();
        }

        // fallback to request param if token not present
        if (studentId == null) {
            if (studentIdParam == null) {
                return ResponseEntity.status(400).body("studentId is required when no Authorization token provided");
            }
            studentId = studentIdParam;
        }

        Homework hw = homeworkMapper.selectById(homeworkId);
        if (hw == null) return ResponseEntity.badRequest().body("Homework not found");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = hw.getDeadline();
        boolean isLate = false;
        if (deadline != null && now.isAfter(deadline)) {
            isLate = true;
        }
        if (isLate && (hw.getAllowLate() == null || hw.getAllowLate() == 0)) {
            return ResponseEntity.status(400).body("Deadline passed and late submissions are not allowed");
        }

        String attachmentPath = null;
        if (file != null && !file.isEmpty()) {
            try {
                // validate
                FileStorageUtil.validateFile(file);
                attachmentPath = FileStorageUtil.saveFile(file);
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(400).body("File save failed: " + e.getMessage());
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
            return ResponseEntity.ok(submission);
        }
        return ResponseEntity.status(500).body("Failed to save submission");
    }

    @GetMapping("/{id}/submissions")
    public ResponseEntity<?> listSubmissions(@PathVariable("id") Long homeworkId, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // if token present, only allow teachers of this homework or admins
        Map<String, Object> tokenInfo = tokenStore.resolve(authHeader);
        if (tokenInfo != null) {
            String role = (String) tokenInfo.get("role");
            Integer entityId = (Integer) tokenInfo.get("entityId");
            if ("teacher".equals(role)) {
                Homework hw = homeworkMapper.selectById(homeworkId);
                if (hw == null) return ResponseEntity.badRequest().body("Homework not found");
                if (!entityId.equals(hw.getTeacherId().intValue())) {
                    return ResponseEntity.status(403).body("Teacher not authorized to view submissions for this homework");
                }
            }
            // admins allowed
        }

        List<HomeworkSubmission> list = submissionMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<HomeworkSubmission>()
                        .eq("homework_id", homeworkId)
        );
        return ResponseEntity.ok(list);
    }

    @GetMapping("/submissions/{submissionId}")
    public ResponseEntity<?> getSubmission(@PathVariable("submissionId") Long submissionId, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        HomeworkSubmission s = submissionMapper.selectById(submissionId);
        if (s == null) return ResponseEntity.notFound().build();

        Map<String, Object> tokenInfo = tokenStore.resolve(authHeader);
        if (tokenInfo != null) {
            String role = (String) tokenInfo.get("role");
            Integer entityId = (Integer) tokenInfo.get("entityId");
            if ("student".equals(role)) {
                if (!entityId.equals(s.getStudentId().intValue())) {
                    return ResponseEntity.status(403).body("Student not authorized to view this submission");
                }
            } else if ("teacher".equals(role)) {
                Homework hw = homeworkMapper.selectById(s.getHomeworkId());
                if (hw == null) return ResponseEntity.badRequest().body("Homework not found");
                if (!entityId.equals(hw.getTeacherId().intValue())) {
                    return ResponseEntity.status(403).body("Teacher not authorized to view this submission");
                }
            }
        }

        return ResponseEntity.ok(s);
    }
}
