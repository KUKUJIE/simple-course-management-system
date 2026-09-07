package com.agiantii.backend.controller;

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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/homeworks")
public class SubmissionController {

    @Autowired
    private HomeworkMapper homeworkMapper;

    @Autowired
    private HomeworkSubmissionMapper submissionMapper;

    @PostMapping("/{id}/submissions")
    public ResponseEntity<?> submitHomework(
            @PathVariable("id") Long homeworkId,
            @RequestParam("studentId") Long studentId,
            @RequestParam(value = "submitText", required = false) String submitText,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
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
                attachmentPath = FileStorageUtil.saveFile(file);
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(500).body("File save failed: " + e.getMessage());
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
    public ResponseEntity<?> listSubmissions(@PathVariable("id") Long homeworkId) {
        List<HomeworkSubmission> list = submissionMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<HomeworkSubmission>()
                        .eq("homework_id", homeworkId)
        );
        return ResponseEntity.ok(list);
    }

    @GetMapping("/submissions/{submissionId}")
    public ResponseEntity<?> getSubmission(@PathVariable("submissionId") Long submissionId) {
        HomeworkSubmission s = submissionMapper.selectById(submissionId);
        if (s == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(s);
    }
}
