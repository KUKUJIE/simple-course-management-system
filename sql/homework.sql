-- Homework module DDL for simple-course-management-system
-- Tables: t_homework, t_homework_question, t_homework_submission, t_homework_answer, t_knowledge_point

CREATE TABLE IF NOT EXISTS `t_knowledge_point` (
  `knowledge_point_id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(200) NOT NULL,
  `description` TEXT,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`knowledge_point_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `t_homework` (
  `homework_id` BIGINT NOT NULL AUTO_INCREMENT,
  `section_id` BIGINT NOT NULL,
  `teacher_id` BIGINT NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `description` TEXT,
  `publish_time` DATETIME,
  `deadline` DATETIME,
  `allow_late` TINYINT(1) DEFAULT 0,
  `total_score` DECIMAL(6,2) DEFAULT 100,
  `allow_submission_types` VARCHAR(50) DEFAULT 'both',
  `status` VARCHAR(20) DEFAULT 'published',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`homework_id`),
  INDEX (`section_id`),
  INDEX (`teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `t_homework_question` (
  `question_id` BIGINT NOT NULL AUTO_INCREMENT,
  `homework_id` BIGINT NOT NULL,
  `seq` INT DEFAULT 0,
  `qtype` VARCHAR(50),
  `content` TEXT,
  `score` DECIMAL(6,2) DEFAULT 0,
  `knowledge_point_id` BIGINT,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`question_id`),
  INDEX (`homework_id`),
  INDEX (`knowledge_point_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `t_homework_submission` (
  `submission_id` BIGINT NOT NULL AUTO_INCREMENT,
  `homework_id` BIGINT NOT NULL,
  `student_id` BIGINT NOT NULL,
  `submit_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `is_late` TINYINT(1) DEFAULT 0,
  `status` VARCHAR(20) DEFAULT 'submitted',
  `submit_text` TEXT,
  `attachment_path` VARCHAR(1000),
  `final_score` DECIMAL(6,2),
  `graded_by` BIGINT,
  `graded_at` DATETIME,
  PRIMARY KEY (`submission_id`),
  INDEX (`homework_id`),
  INDEX (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `t_homework_answer` (
  `answer_id` BIGINT NOT NULL AUTO_INCREMENT,
  `submission_id` BIGINT NOT NULL,
  `question_id` BIGINT NOT NULL,
  `answer_text` TEXT,
  `attachment_path` VARCHAR(1000),
  `score_given` DECIMAL(6,2),
  `comment` TEXT,
  PRIMARY KEY (`answer_id`),
  INDEX (`submission_id`),
  INDEX (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Sample data: one knowledge point and one homework
INSERT INTO `t_knowledge_point` (`name`, `description`) VALUES
('线性代数-矩阵运算', '矩阵乘法与逆矩阵相关知识点');

INSERT INTO `t_homework` (`section_id`, `teacher_id`, `title`, `description`, `publish_time`, `deadline`, `allow_late`, `total_score`, `allow_submission_types`) VALUES
(1, 1, '第1次作业：矩阵运算', '请完成下列题目，支持在线答题或上传文件。', NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 1, 100, 'both');

