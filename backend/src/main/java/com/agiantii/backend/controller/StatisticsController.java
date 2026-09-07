package com.agiantii.backend.controller;

import com.agiantii.backend.common.R;
import com.agiantii.backend.mapper.HomeworkAnswerMapper;
import com.agiantii.backend.mapper.HomeworkMapper;
import com.agiantii.backend.mapper.HomeworkSubmissionMapper;
import com.agiantii.backend.pojo.homework.HomeworkAnswer;
import com.agiantii.backend.pojo.homework.HomeworkSubmission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private HomeworkSubmissionMapper submissionMapper;

    @Autowired
    private HomeworkAnswerMapper answerMapper;

    @Autowired
    private HomeworkMapper homeworkMapper;

    @GetMapping("/homework/{homeworkId}/overview")
    public R<Map<String, Object>> overview(@PathVariable("homeworkId") Long homeworkId) {
        List<HomeworkSubmission> submissions = submissionMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<HomeworkSubmission>()
                        .eq("homework_id", homeworkId)
        );

        if (submissions == null || submissions.isEmpty()) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("count", 0);
            empty.put("average", 0);
            empty.put("passRate", 0);
            empty.put("distribution", new int[]{0,0,0,0,0});
            empty.put("perQuestionAvg", Collections.emptyMap());
            return R.success(empty, "ok");
        }

        int count = submissions.size();
        double sum = 0;
        int gradedCount = 0;
        int passCount = 0;
        int[] buckets = new int[5]; // 0:0-59,1:60-69,2:70-79,3:80-89,4:90-100

        for (HomeworkSubmission s : submissions) {
            if (s.getFinalScore() != null) {
                double sc = s.getFinalScore();
                sum += sc;
                gradedCount++;
                if (sc >= 60) passCount++;
                int idx;
                if (sc < 60) idx = 0;
                else if (sc < 70) idx = 1;
                else if (sc < 80) idx = 2;
                else if (sc < 90) idx = 3;
                else idx = 4;
                buckets[idx]++;
            }
        }

        double avg = gradedCount == 0 ? 0 : sum / gradedCount;
        double passRate = gradedCount == 0 ? 0 : (passCount * 1.0 / gradedCount);

        // per-question average
        List<HomeworkAnswer> answers = answerMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<HomeworkAnswer>()
                        .inSql("submission_id", "SELECT submission_id FROM t_homework_submission WHERE homework_id = " + homeworkId)
        );

        Map<Long, List<HomeworkAnswer>> byQuestion = answers.stream().collect(Collectors.groupingBy(HomeworkAnswer::getQuestionId));
        Map<String, Double> perQuestionAvg = new LinkedHashMap<>();
        for (Map.Entry<Long, List<HomeworkAnswer>> e : byQuestion.entrySet()) {
            Long qid = e.getKey();
            List<HomeworkAnswer> list = e.getValue();
            double ssum = 0;
            int cnt = 0;
            for (HomeworkAnswer a : list) {
                if (a.getScoreGiven() != null) {
                    ssum += a.getScoreGiven();
                    cnt++;
                }
            }
            double qavg = cnt == 0 ? 0 : ssum / cnt;
            perQuestionAvg.put(String.valueOf(qid), qavg);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        result.put("gradedCount", gradedCount);
        result.put("average", Math.round(avg * 100.0) / 100.0);
        result.put("passRate", Math.round(passRate * 10000.0) / 100.0); // percent with 2 decimals
        result.put("distribution", buckets);
        result.put("perQuestionAvg", perQuestionAvg);

        return R.success(result, "ok");
    }
}
