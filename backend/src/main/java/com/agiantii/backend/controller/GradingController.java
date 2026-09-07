package com.agiantii.backend.controller;

import com.agiantii.backend.dto.GradeRequest;
import com.agiantii.backend.mapper.HomeworkAnswerMapper;
import com.agiantii.backend.mapper.HomeworkSubmissionMapper;
import com.agiantii.backend.pojo.homework.HomeworkAnswer;
import com.agiantii.backend.pojo.homework.HomeworkSubmission;
import com.agiantii.backend.utils.CsvUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class GradingController {

    @Autowired
    private HomeworkSubmissionMapper submissionMapper;

    @Autowired
    private HomeworkAnswerMapper answerMapper;

    @PostMapping("/submissions/{submissionId}/grade")
    @Transactional
    public ResponseEntity<?> gradeSubmission(@PathVariable("submissionId") Long submissionId, @RequestBody GradeRequest req) {
        HomeworkSubmission submission = submissionMapper.selectById(submissionId);
        if (submission == null) return ResponseEntity.badRequest().body("Submission not found");

        double total = 0.0;
        if (req.getAnswers() != null) {
            for (GradeRequest.AnswerGrade ag : req.getAnswers()) {
                HomeworkAnswer answer = new HomeworkAnswer();
                answer.setSubmissionId(submissionId);
                answer.setQuestionId(ag.getQuestionId());
                answer.setScoreGiven(ag.getScoreGiven());
                answer.setComment(ag.getComment());
                // insert as new answer record
                answerMapper.insert(answer);
                if (ag.getScoreGiven() != null) total += ag.getScoreGiven();
            }
        }

        submission.setFinalScore(total);
        submission.setGradedBy(req.getGraderId());
        submission.setGradedAt(LocalDateTime.now());
        submission.setStatus("graded");
        submissionMapper.updateById(submission);

        return ResponseEntity.ok(submission);
    }

    @GetMapping("/homeworks/{homeworkId}/export")
    public ResponseEntity<?> exportHomeworkCsv(@PathVariable("homeworkId") Long homeworkId) {
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
            return ResponseEntity.status(500).body("Failed to generate CSV: " + e.getMessage());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", "homework_" + homeworkId + "_export.csv");

        return ResponseEntity.ok().headers(headers).body(csvBytes);
    }
}
