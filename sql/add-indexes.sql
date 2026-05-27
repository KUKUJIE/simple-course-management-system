-- ============================================================
-- 性能优化索引（追加）
-- 在 schema-v2.sql + generated_data_new.sql 导入后执行
-- 目标：插入 < 1s，删除/搜索 < 2s
-- ============================================================

USE sms;

-- ============================================================
-- t_enrollment（60,000+ 行，最大表，视图核心联调表）
-- ============================================================

-- 视图 v_student_course / v_section_stats / v_student_gpa 全部使用 e.status = 1
-- 存储过程 sp_enroll 频繁查询 status = 0 / status = 1
CREATE INDEX idx_enrollment_status ON t_enrollment(status);

-- 复合索引：按学生查其有效选课（最频繁的查询场景）
CREATE INDEX idx_enrollment_student_status ON t_enrollment(student_id, status);

-- 复合索引：按教学班统计有效选课人数（sp_enroll 容量检查 + v_section_stats）
CREATE INDEX idx_enrollment_section_status ON t_enrollment(section_id, status);

-- 按选课时间排序（个人课表、选课日志）
CREATE INDEX idx_enrollment_select_time ON t_enrollment(select_time);

-- ============================================================
-- t_score（54,000+ 行，GPA 计算 / 成绩查询）
-- ============================================================

-- v_student_gpa 按 is_passed 聚合
CREATE INDEX idx_score_is_passed ON t_score(is_passed);

-- 按成绩查询/排序
CREATE INDEX idx_score_final_score ON t_score(final_score);

-- 按录入时间排序
CREATE INDEX idx_score_graded_at ON t_score(graded_at);

-- ============================================================
-- t_student（12,000+ 行，按状态/入学年份筛选）
-- ============================================================

CREATE INDEX idx_student_status ON t_student(status);
CREATE INDEX idx_student_enrollment_year ON t_student(enrollment_year);

-- 手机号/邮箱搜索
CREATE INDEX idx_student_phone ON t_student(phone);
CREATE INDEX idx_student_email ON t_student(email);

-- ============================================================
-- t_teacher（500 行）
-- ============================================================

CREATE INDEX idx_teacher_status ON t_teacher(status);
CREATE INDEX idx_teacher_name ON t_teacher(teacher_name);

-- ============================================================
-- t_course（400 行）
-- ============================================================

CREATE INDEX idx_course_status ON t_course(status);
CREATE INDEX idx_course_type ON t_course(course_type);
CREATE INDEX idx_course_name ON t_course(course_name);

-- 院系 + 状态组合（按院系浏览可用课程）
CREATE INDEX idx_course_dept_status ON t_course(department_id, status);

-- ============================================================
-- t_course_section（4,000 行）
-- ============================================================

CREATE INDEX idx_section_status ON t_course_section(status);

-- 按学期 + 状态查询开课列表（最常用的选课浏览场景）
CREATE INDEX idx_section_semester_status ON t_course_section(semester, status);

-- 教室占用查询
CREATE INDEX idx_section_classroom ON t_course_section(classroom_id);

-- ============================================================
-- t_user（12,500+ 行，登录认证）
-- ============================================================

-- 登录验证：username + password（username 已有 UNIQUE 索引，此索引覆盖密码校验避免回表）
CREATE INDEX idx_user_username_password ON t_user(username, password);

-- ============================================================
-- t_major（100+ 行）
-- ============================================================

CREATE INDEX idx_major_status ON t_major(status);

-- ============================================================
-- t_department（20 行，小表仅补充状态索引）
-- ============================================================

CREATE INDEX idx_department_status ON t_department(status);

-- ============================================================
-- t_classroom（500 行）
-- ============================================================

CREATE INDEX idx_classroom_status ON t_classroom(status);
