package com.agiantii.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class GradeRequest {
    private Long graderId; // teacher id
    private List<AnswerGrade> answers;

    @Data
    public static class AnswerGrade {
        private Long questionId;
        private Long answerId; // optional if existing
        private Double scoreGiven;
        private String comment;
    }
}
