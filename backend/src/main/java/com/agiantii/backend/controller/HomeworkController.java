package com.agiantii.backend.controller;

import com.agiantii.backend.common.R;
import com.agiantii.backend.common.TokenStore;
import com.agiantii.backend.dto.HomeworkCreateRequest;
import com.agiantii.backend.mapper.HomeworkMapper;
import com.agiantii.backend.mapper.HomeworkQuestionMapper;
import com.agiantii.backend.mapper.KnowledgePointMapper;
import com.agiantii.backend.pojo.homework.Homework;
import com.agiantii.backend.pojo.homework.HomeworkQuestion;
import com.agiantii.backend.pojo.homework.KnowledgePoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/homeworks")
public class HomeworkController {

    @Autowired
    private HomeworkMapper homeworkMapper;

    @Autowired
    private HomeworkQuestionMapper questionMapper;

    @Autowired
    private KnowledgePointMapper knowledgePointMapper;

    @Resource
    private TokenStore tokenStore;

    @PostMapping
    public R<Map<String, Object>> createHomework(@RequestBody HomeworkCreateRequest req, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // if teacherId not provided, try to get from token
        Long teacherId = req.getTeacherId();
        Map<String, Object> tokenInfo = tokenStore.resolve(authHeader);
        if (teacherId == null && tokenInfo != null) {
            String role = (String) tokenInfo.get("role");
            if ("teacher".equals(role)) {
                Integer entityId = (Integer) tokenInfo.get("entityId");
                if (entityId != null) teacherId = entityId.longValue();
            }
        }
        if (teacherId == null) {
            return R.error("Teacher identity required", 401);
        }

        Homework hw = new Homework();
        hw.setSectionId(req.getSectionId());
        hw.setTeacherId(teacherId);
        hw.setTitle(req.getTitle());
        hw.setDescription(req.getDescription());
        hw.setPublishTime(LocalDateTime.now());
        try {
            if (req.getDeadline() != null) {
                hw.setDeadline(LocalDateTime.parse(req.getDeadline(), DateTimeFormatter.ISO_DATE_TIME));
            }
        } catch (Exception e) {
            // ignore parse error, leave null
        }
        hw.setAllowLate(req.getAllowLate());
        hw.setTotalScore(req.getTotalScore());
        hw.setAllowSubmissionTypes(req.getAllowSubmissionTypes());

        int inserted = homeworkMapper.insert(hw);
        if (inserted <= 0) {
            return R.error("Failed to create homework", 500);
        }

        // insert questions
        if (req.getQuestions() != null) {
            for (HomeworkCreateRequest.Question q : req.getQuestions()) {
                Long kpId = q.getKnowledgePointId();
                if ((kpId == null || kpId == 0) && q.getKnowledgePointName() != null && !q.getKnowledgePointName().trim().isEmpty()) {
                    // try to find existing
                    KnowledgePoint kp = knowledgePointMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<KnowledgePoint>().eq("name", q.getKnowledgePointName().trim()));
                    if (kp == null) {
                        kp = new KnowledgePoint();
                        kp.setName(q.getKnowledgePointName().trim());
                        knowledgePointMapper.insert(kp);
                    }
                    kpId = kp.getKnowledgePointId();
                }

                HomeworkQuestion hq = new HomeworkQuestion();
                hq.setHomeworkId(hw.getHomeworkId());
                hq.setSeq(q.getSeq() == null ? 0 : q.getSeq());
                hq.setQtype(q.getQtype());
                hq.setContent(q.getContent());
                hq.setScore(q.getScore() == null ? 0.0 : q.getScore());
                hq.setKnowledgePointId(kpId);
                questionMapper.insert(hq);
            }
        }

        return R.success(Map.of("homeworkId", hw.getHomeworkId()), "Created");
    }
}
