-- SQL for admin demo: create t_course table if not exists
CREATE TABLE IF NOT EXISTS `t_course` (
  `course_id` BIGINT NOT NULL AUTO_INCREMENT,
  `course_code` VARCHAR(64),
  `name` VARCHAR(255) NOT NULL,
  `description` TEXT,
  `teacher_id` BIGINT,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
