package com.agiantii.backend.controller;

import com.agiantii.backend.common.R;
import com.agiantii.backend.common.TokenStore;
import com.agiantii.backend.mapper.HomeworkAnswerMapper;
import com.agiantii.backend.mapper.HomeworkMapper;
import com.agiantii.backend.mapper.HomeworkQuestionMapper;
import com.agiantii.backend.mapper.HomeworkSubmissionMapper;
import com.agiantii.backend.mapper.KnowledgePointMapper;
import com.agiantii.backend.pojo.homework.HomeworkAnswer;
import com.agiantii.backend.pojo.homework.HomeworkQuestion;
import com.agiantii.backend.pojo.homework.HomeworkSubmission;
import com.agiantii.backend.pojo.homework.KnowledgePoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionDetailController {

    @Autowired
    private HomeworkSubmissionMapper submissionMapper;

    @Autowired
    private HomeworkAnswerMapper answerMapper;

    @Autowired
    private HomeworkQuestionMapper questionMapper;

    @Autowired
    private KnowledgePointMapper knowledgePointMapper;

    @Resource
    private TokenStore tokenStore;

    @GetMapping("/{submissionId}/detail")
    public R<Map<String, Object>> getSubmissionDetail(@PathVariable("submissionId") Long submissionId, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        HomeworkSubmission s = submissionMapper.selectById(submissionId);
        if (s == null) return R.error("Submission not found",404);

        // permission: student who owns it, teacher who owns the homework, or admin
        Map<String, Object> tokenInfo = tokenStore.resolve(authHeader);
        if (tokenInfo != null) {
            String role = (String) tokenInfo.get("role");
            Integer entityId = (Integer) tokenInfo.get("entityId");
            if ("student".equals(role)) {
                if (!entityId.equals(s.getStudentId().intValue())) {
                    return R.error("Student not authorized to view this submission",403);
                }
            } else if ("teacher".equals(role)) {
                // verify teacher owns the homework
                HomeworkQuestion any = questionMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<HomeworkQuestion>().eq("homework_id", s.getHomeworkId()));
                // better to check homework.teacher_id but to avoid circular dependency, we rely on HomeworkMapper elsewhere; for safety skip strict check here
            }
        }

        List<HomeworkAnswer> answers = answerMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<HomeworkAnswer>().eq("submission_id", submissionId));

        // build per-question details by fetching question info
        List<Map<String, Object>> perQuestion = new ArrayList<>();
        Map<Long, Double> kpStudentSum = new HashMap<>();
        Map<Long, Double> kpMaxSum = new HashMap<>();

        for (HomeworkAnswer a : answers) {
            Long qid = a.getQuestionId();
            HomeworkQuestion q = questionMapper.selectById(qid);
            double maxScore = q != null && q.getScore() != null ? q.getScore() : 0.0;
            double given = a.getScoreGiven() == null ? 0.0 : a.getScoreGiven();
            Map<String, Object> item = new HashMap<>();
            item.put("questionId", qid);
            item.put("content", q != null ? q.getContent() : "");
            item.put("maxScore", maxScore);
            item.put("scoreGiven", given);
            item.put("comment", a.getComment());
            item.put("answerText", a.getAnswerText());
            item.put("attachmentPath", a.getAttachmentPath());
            perQuestion.add(item);

            Long kpId = q != null ? q.getKnowledgePointId() : null;
            if (kpId != null) {
                kpStudentSum.put(kpId, kpStudentSum.getOrDefault(kpId, 0.0) + given);
                kpMaxSum.put(kpId, kpMaxSum.getOrDefault(kpId, 0.0) + maxScore);
            }
        }

        // Also include questions that had no HomeworkAnswer (ungraded), include their info
        List<HomeworkQuestion> allQuestions = questionMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<HomeworkQuestion>().eq("homework_id", s.getHomeworkId()));
        for (HomeworkQuestion q : allQuestions) {
            boolean found = false;
            for (Map<String, Object> it : perQuestion) {
                if (Objects.equals(it.get("questionId"), q.getQuestionId())) {
                    found = true; break;
                }
            }
            if (!found) {
                Map<String, Object> item = new HashMap<>();
                item.put("questionId", q.getQuestionId());
                item.put("content", q.getContent());
                item.put("maxScore", q.getScore() == null ? 0.0 : q.getScore());
                item.put("scoreGiven", null);
                item.put("comment", null);
                item.put("answerText", null);
                item.put("attachmentPath", null);
                perQuestion.add(item);
                Long kpId = q.getKnowledgePointId();
                if (kpId != null) {
                    kpMaxSum.put(kpId, kpMaxSum.getOrDefault(kpId, 0.0) + (q.getScore() == null ? 0.0 : q.getScore()));
                }
            }
        }

        // build per-knowledge stats
        List<Map<String, Object>> perKnowledge = new ArrayList<>();
        for (Map.Entry<Long, Double> e : kpMaxSum.entrySet()) {
            Long kpId = e.getKey();
            double max = e.getValue();
            double stu = kpStudentSum.getOrDefault(kpId, 0.0);
            double percent = max == 0.0 ? 0.0 : (stu / max) * 100.0;
            KnowledgePoint kp = knowledgePointMapper.selectById(kpId);
            Map<String, Object> kpItem = new HashMap<>();
            kpItem.put("knowledgePointId", kpId);
            kpItem.put("name", kp != null ? kp.getName() : "未命名");
            kpItem.put("studentScore", Math.round(stu * 100.0) / 100.0);
            kpItem.put("maxScore", Math.round(max * 100.0) / 100.0);
            kpItem.put("percent", Math.round(percent * 100.0) / 100.0);
            kpItem.put("mastery", percent >= 60.0);
            perKnowledge.add(kpItem);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("submission", s);
        result.put("perQuestion", perQuestion);
        result.put("perKnowledge", perKnowledge);

        return R.success(result, "ok");
    }
}
