package com.agiantii.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class HomeworkCreateRequest {
    private Long sectionId;
    private Long teacherId; // optional, will be filled from token if omitted
    private String title;
    private String description;
    private String deadline; // ISO datetime string
    private Integer allowLate; // 0/1
    private Double totalScore;
    private String allowSubmissionTypes; // both / file / text
    private List<Question> questions;

    @Data
    public static class Question {
        private Integer seq;
        private String qtype;
        private String content;
        private Double score;
        private Long knowledgePointId;
        private String knowledgePointName; // optional, if provided create KP
    }
}
