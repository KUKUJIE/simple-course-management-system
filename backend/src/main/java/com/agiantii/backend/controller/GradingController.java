package com.agiantii.backend.controller;

import com.agiantii.backend.common.R;
import com.agiantii.backend.common.TokenStore;
import com.agiantii.backend.dto.GradeRequest;
import com.agiantii.backend.mapper.HomeworkAnswerMapper;
import com.agiantii.backend.mapper.HomeworkMapper;
import com.agiantii.backend.mapper.HomeworkSubmissionMapper;
import com.agiantii.backend.pojo.homework.Homework;
import com.agiantii.backend.pojo.homework.HomeworkAnswer;
import com.agiantii.backend.pojo.homework.HomeworkSubmission;
import com.agiantii.backend.utils.CsvUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class GradingController {

    @Autowired
    private HomeworkSubmissionMapper submissionMapper;

    @Autowired
    private HomeworkAnswerMapper answerMapper;

    @Autowired
    private HomeworkMapper homeworkMapper;

    @Resource
    private TokenStore tokenStore;

    @PostMapping("/submissions/{submissionId}/grade")
    @Transactional
    public R<HomeworkSubmission> gradeSubmission(@PathVariable("submissionId") Long submissionId, @RequestBody GradeRequest req, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        HomeworkSubmission submission = submissionMapper.selectById(submissionId);
        if (submission == null) return R.error("Submission not found",400);

        // require authentication and teacher role
        Map<String, Object> tokenInfo = tokenStore.resolve(authHeader);
        if (tokenInfo == null) {
            return R.error("Authentication required",401);
        }
        String role = (String) tokenInfo.get("role");
        Integer entityId = (Integer) tokenInfo.get("entityId");
        if (!"teacher".equals(role)) {
            return R.error("Only teachers can grade submissions",403);
        }

        Homework hw = homeworkMapper.selectById(submission.getHomeworkId());
        if (hw == null) return R.error("Homework not found",400);
        if (!entityId.equals(hw.getTeacherId().intValue())) {
            return R.error("Teacher not authorized to grade this submission",403);
        }

        double total = 0.0;
        if (req.getAnswers() != null) {
            for (GradeRequest.AnswerGrade ag : req.getAnswers()) {
                HomeworkAnswer answer = new HomeworkAnswer();
                answer.setSubmissionId(submissionId);
                answer.setQuestionId(ag.getQuestionId());
                answer.setScoreGiven(ag.getScoreGiven());
                answer.setComment(ag.getComment());
                answerMapper.insert(answer);
                if (ag.getScoreGiven() != null) total += ag.getScoreGiven();
            }
        }

        submission.setFinalScore(total);
        submission.setGradedBy(Long.valueOf(entityId));
        submission.setGradedAt(LocalDateTime.now());
        submission.setStatus("graded");
        submissionMapper.updateById(submission);

        return R.success(submission, "Graded");
    }

    @GetMapping("/homeworks/{homeworkId}/export")
    public ResponseEntity<?> exportHomeworkCsv(@PathVariable("homeworkId") Long homeworkId, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // only teacher of this homework or admin can export
        Map<String, Object> tokenInfo = tokenStore.resolve(authHeader);
        if (tokenInfo != null) {
            String role = (String) tokenInfo.get("role");
            Integer entityId = (Integer) tokenInfo.get("entityId");
            Homework hw = homeworkMapper.selectById(homeworkId);
            if (hw == null) return ResponseEntity.badRequest().body(R.error("Homework not found",400));
            if ("teacher".equals(role) && !entityId.equals(hw.getTeacherId().intValue())) {
                return ResponseEntity.status(403).body(R.error("Teacher not authorized to export this homework",403));
            }
            // admins allowed
        }

        List<HomeworkSubmission> submissions = submissionMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<HomeworkSubmission>().eq("homework_id", homeworkId)
        );

        List<String[]> rows = new ArrayList<>();
        for (HomeworkSubmission s : submissions) {
            rows.add(new String[]{
                    String.valueOf(s.getSubmissionId()),
                    String.valueOf(s.getStudentId()),
                    s.getSubmitTime() == null ? "" : s.getSubmitTime().toString(),
                    s.getIsLate() == null ? "0" : String.valueOf(s.getIsLate()),
                    s.getFinalScore() == null ? "" : s.getFinalScore().toString(),
                    s.getAttachmentPath() == null ? "" : s.getAttachmentPath(),
                    s.getStatus() == null ? "" : s.getStatus()
            });
        }

        String[] header = new String[]{"submissionId", "studentId", "submitTime", "isLate", "finalScore", "attachmentPath", "status"};
        byte[] csvBytes;
        try {
            csvBytes = CsvUtil.toCsvBytes(rows, header);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(R.error("Failed to generate CSV: " + e.getMessage(),500));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", "homework_" + homeworkId + "_export.csv");

        return ResponseEntity.ok().headers(headers).body(csvBytes);
    }
}
