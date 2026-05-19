-- ============================================================
-- 学生选课管理系统 数据库 V2 (sms)
-- Schema + 初始化数据 + 索引/视图/触发器/存储过程
--
-- 【版本说明】本文件为当前轮次评审通过的新版数据库草稿，是后续
--            前后端改造与协作的统一参考基线。
--            仓库中其他旧 SQL 文件（sms.sql / sms-struct.sql 等）
--            仅保留备查，不再作为新版开发口径。
-- ============================================================

-- ------------------------------------------------------------
-- 1. 数据库创建
-- ------------------------------------------------------------
DROP DATABASE IF EXISTS sms;
CREATE DATABASE sms
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
USE sms;

-- ------------------------------------------------------------
-- 2. 基础实体表（按依赖顺序：先建被引用的表）
-- ------------------------------------------------------------

-- 2.1 院系表
CREATE TABLE t_department (
    department_id   INT AUTO_INCREMENT PRIMARY KEY,
    department_code VARCHAR(20)  NOT NULL,
    department_name VARCHAR(100) NOT NULL,
    office_phone    VARCHAR(30),
    status          TINYINT NOT NULL DEFAULT 1 COMMENT '1=正常 0=停用',
    CONSTRAINT uk_department_code UNIQUE (department_code),
    CONSTRAINT uk_department_name UNIQUE (department_name)
) ENGINE=InnoDB COMMENT='院系';

-- 2.2 教室表
CREATE TABLE t_classroom (
    classroom_id INT AUTO_INCREMENT PRIMARY KEY,
    building     VARCHAR(50) NOT NULL,
    room_no      VARCHAR(20) NOT NULL,
    capacity     INT NOT NULL DEFAULT 0,
    status       TINYINT NOT NULL DEFAULT 1 COMMENT '1=可用 0=不可用',
    remark       VARCHAR(200),
    CONSTRAINT uk_building_room UNIQUE (building, room_no),
    CONSTRAINT ck_capacity CHECK (capacity >= 0)
) ENGINE=InnoDB COMMENT='教室';

-- 2.3 用户账号表
CREATE TABLE t_user (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(50) NOT NULL,
    password   VARCHAR(255) NOT NULL COMMENT '密码',
    role       ENUM('student','teacher','admin') NOT NULL,
    status     TINYINT NOT NULL DEFAULT 1 COMMENT '1=正常 0=停用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_username UNIQUE (username)
) ENGINE=InnoDB COMMENT='用户账号';

-- 2.4 专业表
CREATE TABLE t_major (
    major_id      INT AUTO_INCREMENT PRIMARY KEY,
    department_id INT NOT NULL,
    major_code    VARCHAR(20) NOT NULL,
    major_name    VARCHAR(100) NOT NULL,
    status        TINYINT NOT NULL DEFAULT 1 COMMENT '1=正常 0=停用',
    CONSTRAINT uk_major_code UNIQUE (major_code),
    CONSTRAINT uk_major_name UNIQUE (major_name),
    CONSTRAINT fk_major_dept FOREIGN KEY (department_id)
        REFERENCES t_department(department_id)
) ENGINE=InnoDB COMMENT='专业';

-- 2.5 学生表
CREATE TABLE t_student (
    student_id      INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT NOT NULL,
    major_id        INT NOT NULL,
    student_no      VARCHAR(30) NOT NULL,
    student_name    VARCHAR(50) NOT NULL,
    gender          ENUM('M','F') NOT NULL,
    phone           VARCHAR(30),
    email           VARCHAR(100),
    enrollment_year INT NOT NULL,
    status          TINYINT NOT NULL DEFAULT 1 COMMENT '1=正常 0=休学/退学',
    CONSTRAINT uk_student_no UNIQUE (student_no),
    CONSTRAINT uk_student_user UNIQUE (user_id),
    CONSTRAINT fk_student_user FOREIGN KEY (user_id)
        REFERENCES t_user(id),
    CONSTRAINT fk_student_major FOREIGN KEY (major_id)
        REFERENCES t_major(major_id)
) ENGINE=InnoDB COMMENT='学生';

-- 2.6 教师表
CREATE TABLE t_teacher (
    teacher_id    INT AUTO_INCREMENT PRIMARY KEY,
    user_id       INT NOT NULL,
    department_id INT NOT NULL,
    teacher_no    VARCHAR(30) NOT NULL,
    teacher_name  VARCHAR(50) NOT NULL,
    title         VARCHAR(50),
    phone         VARCHAR(30),
    email         VARCHAR(100),
    status        TINYINT NOT NULL DEFAULT 1 COMMENT '1=正常 0=停用',
    CONSTRAINT uk_teacher_no UNIQUE (teacher_no),
    CONSTRAINT uk_teacher_user UNIQUE (user_id),
    CONSTRAINT fk_teacher_user FOREIGN KEY (user_id)
        REFERENCES t_user(id),
    CONSTRAINT fk_teacher_dept FOREIGN KEY (department_id)
        REFERENCES t_department(department_id)
) ENGINE=InnoDB COMMENT='教师';

-- 2.7 管理员表
CREATE TABLE t_admin (
    admin_id   INT AUTO_INCREMENT PRIMARY KEY,
    user_id    INT NOT NULL,
    admin_no   VARCHAR(30) NOT NULL,
    admin_name VARCHAR(50) NOT NULL,
    phone      VARCHAR(30),
    email      VARCHAR(100),
    status     TINYINT NOT NULL DEFAULT 1 COMMENT '1=正常 0=停用',
    CONSTRAINT uk_admin_no UNIQUE (admin_no),
    CONSTRAINT uk_admin_user UNIQUE (user_id),
    CONSTRAINT fk_admin_user FOREIGN KEY (user_id)
        REFERENCES t_user(id)
) ENGINE=InnoDB COMMENT='管理员';

-- 2.8 课程表
CREATE TABLE t_course (
    course_id     INT AUTO_INCREMENT PRIMARY KEY,
    department_id INT NOT NULL,
    course_code   VARCHAR(30) NOT NULL,
    course_name   VARCHAR(100) NOT NULL,
    credit        DECIMAL(3,1) NOT NULL,
    total_hours   INT NOT NULL,
    course_type   VARCHAR(30) COMMENT '必修/选修/公选',
    description   TEXT,
    status        TINYINT NOT NULL DEFAULT 1 COMMENT '1=正常 0=停开',
    CONSTRAINT uk_course_code UNIQUE (course_code),
    CONSTRAINT fk_course_dept FOREIGN KEY (department_id)
        REFERENCES t_department(department_id),
    CONSTRAINT ck_credit CHECK (credit > 0),
    CONSTRAINT ck_total_hours CHECK (total_hours > 0)
) ENGINE=InnoDB COMMENT='课程';

-- 2.9 教学班表
CREATE TABLE t_course_section (
    section_id      INT AUTO_INCREMENT PRIMARY KEY,
    course_id       INT NOT NULL,
    teacher_id      INT NOT NULL,
    classroom_id    INT,
    section_code    VARCHAR(30) NOT NULL,
    semester        VARCHAR(30) NOT NULL COMMENT '学期，如 2025-2026-1',
    schedule_text   VARCHAR(200) COMMENT '上课时间描述',
    capacity_limit  INT NOT NULL DEFAULT 0,
    selected_count  INT NOT NULL DEFAULT 0,
    status          TINYINT NOT NULL DEFAULT 1 COMMENT '1=开课 0=停开',
    CONSTRAINT uk_section_code_semester UNIQUE (section_code, semester),
    CONSTRAINT fk_section_course FOREIGN KEY (course_id)
        REFERENCES t_course(course_id),
    CONSTRAINT fk_section_teacher FOREIGN KEY (teacher_id)
        REFERENCES t_teacher(teacher_id),
    CONSTRAINT fk_section_classroom FOREIGN KEY (classroom_id)
        REFERENCES t_classroom(classroom_id),
    CONSTRAINT ck_capacity_limit CHECK (capacity_limit >= 0),
    CONSTRAINT ck_selected_count CHECK (selected_count >= 0)
) ENGINE=InnoDB COMMENT='教学班';

-- 2.10 选课记录表
-- 业务规则：退课为逻辑删除（status=0），退课后允许重选同一教学班（恢复旧记录）
-- 唯一约束 (student_id, section_id) 保证同一学生不能重复选同一教学班
-- 退课记录保留以便审计，重选时 UPDATE 恢复而非 DELETE+INSERT（保留 enrollment_id）
CREATE TABLE t_enrollment (
    enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id    INT NOT NULL,
    section_id    INT NOT NULL,
    select_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status        TINYINT NOT NULL DEFAULT 1 COMMENT '1=正常 0=退课',
    source        VARCHAR(50) COMMENT '选课来源',
    remark        VARCHAR(200),
    CONSTRAINT fk_enrollment_student FOREIGN KEY (student_id)
        REFERENCES t_student(student_id),
    CONSTRAINT fk_enrollment_section FOREIGN KEY (section_id)
        REFERENCES t_course_section(section_id),
    CONSTRAINT uk_student_section UNIQUE (student_id, section_id)
) ENGINE=InnoDB COMMENT='选课记录';

-- 2.11 成绩表（弱实体，依附于选课记录）
CREATE TABLE t_score (
    score_id      INT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id INT NOT NULL,
    usual_score   DECIMAL(5,2),
    exam_score    DECIMAL(5,2),
    final_score   DECIMAL(5,2),
    gpa_point     DECIMAL(3,2),
    is_passed     TINYINT COMMENT '1=通过 0=未通过',
    graded_at     DATETIME,
    graded_by     VARCHAR(50),
    CONSTRAINT uk_enrollment UNIQUE (enrollment_id),
    CONSTRAINT fk_score_enrollment FOREIGN KEY (enrollment_id)
        REFERENCES t_enrollment(enrollment_id)
        ON DELETE CASCADE,
    CONSTRAINT ck_usual_score CHECK (usual_score IS NULL OR (usual_score >= 0 AND usual_score <= 100)),
    CONSTRAINT ck_exam_score CHECK (exam_score IS NULL OR (exam_score >= 0 AND exam_score <= 100)),
    CONSTRAINT ck_final_score CHECK (final_score IS NULL OR (final_score >= 0 AND final_score <= 100))
) ENGINE=InnoDB COMMENT='成绩（弱实体）';

-- ------------------------------------------------------------
-- 3. 索引（加速联调查询，确保插入<1s、搜索/删除<2s）
-- ------------------------------------------------------------

-- 3.1 t_user：登录认证 + 角色/状态筛选
CREATE INDEX idx_user_role ON t_user(role);
CREATE INDEX idx_user_status ON t_user(status);
CREATE INDEX idx_user_username_password ON t_user(username, password);

-- 3.2 t_student：专业/姓名/状态/入学年份/联系方式
CREATE INDEX idx_student_major ON t_student(major_id);
CREATE INDEX idx_student_name ON t_student(student_name);
CREATE INDEX idx_student_status ON t_student(status);
CREATE INDEX idx_student_enrollment_year ON t_student(enrollment_year);
CREATE INDEX idx_student_phone ON t_student(phone);
CREATE INDEX idx_student_email ON t_student(email);

-- 3.3 t_teacher：院系/状态/姓名
CREATE INDEX idx_teacher_dept ON t_teacher(department_id);
CREATE INDEX idx_teacher_status ON t_teacher(status);
CREATE INDEX idx_teacher_name ON t_teacher(teacher_name);

-- 3.4 t_course：院系/状态/类型/名称 + 复合索引
CREATE INDEX idx_course_dept ON t_course(department_id);
CREATE INDEX idx_course_status ON t_course(status);
CREATE INDEX idx_course_type ON t_course(course_type);
CREATE INDEX idx_course_name ON t_course(course_name);
CREATE INDEX idx_course_dept_status ON t_course(department_id, status);

-- 3.5 t_course_section：课程/教师/学期/教室/状态 + 复合索引
CREATE INDEX idx_section_course ON t_course_section(course_id);
CREATE INDEX idx_section_teacher ON t_course_section(teacher_id);
CREATE INDEX idx_section_semester ON t_course_section(semester);
CREATE INDEX idx_section_status ON t_course_section(status);
CREATE INDEX idx_section_semester_status ON t_course_section(semester, status);
CREATE INDEX idx_section_classroom ON t_course_section(classroom_id);

-- 3.6 t_enrollment（60,000+行，最热点表）：学生/教学班/状态 + 复合索引
CREATE INDEX idx_enrollment_student ON t_enrollment(student_id);
CREATE INDEX idx_enrollment_section ON t_enrollment(section_id);
CREATE INDEX idx_enrollment_status ON t_enrollment(status);
CREATE INDEX idx_enrollment_student_status ON t_enrollment(student_id, status);
CREATE INDEX idx_enrollment_section_status ON t_enrollment(section_id, status);
CREATE INDEX idx_enrollment_select_time ON t_enrollment(select_time);

-- 3.7 t_score（54,000+行）：是否通过/成绩/时间
CREATE INDEX idx_score_is_passed ON t_score(is_passed);
CREATE INDEX idx_score_final_score ON t_score(final_score);
CREATE INDEX idx_score_graded_at ON t_score(graded_at);

-- 3.8 t_major / t_department / t_classroom
CREATE INDEX idx_major_dept ON t_major(department_id);
CREATE INDEX idx_major_status ON t_major(status);
CREATE INDEX idx_department_status ON t_department(status);
CREATE INDEX idx_classroom_status ON t_classroom(status);

-- ------------------------------------------------------------
-- 4. 视图
-- ------------------------------------------------------------

-- 4.1 学生选课完整视图
CREATE VIEW v_student_course AS
SELECT
    s.student_id,
    s.student_no,
    s.student_name,
    e.enrollment_id,
    e.select_time,
    cs.section_id,
    cs.section_code,
    cs.semester,
    c.course_code,
    c.course_name,
    c.credit,
    t.teacher_name,
    sc.usual_score,
    sc.exam_score,
    sc.final_score,
    sc.gpa_point,
    sc.is_passed
FROM t_student s
JOIN t_enrollment e ON s.student_id = e.student_id AND e.status = 1
JOIN t_course_section cs ON e.section_id = cs.section_id
JOIN t_course c ON cs.course_id = c.course_id
JOIN t_teacher t ON cs.teacher_id = t.teacher_id
LEFT JOIN t_score sc ON e.enrollment_id = sc.enrollment_id;

-- 4.2 教学班选课统计视图
CREATE VIEW v_section_stats AS
SELECT
    cs.section_id,
    cs.section_code,
    cs.semester,
    c.course_name,
    t.teacher_name,
    cs.capacity_limit,
    COUNT(e.enrollment_id) AS enrolled_count,
    cs.capacity_limit - COUNT(e.enrollment_id) AS remaining
FROM t_course_section cs
JOIN t_course c ON cs.course_id = c.course_id
JOIN t_teacher t ON cs.teacher_id = t.teacher_id
LEFT JOIN t_enrollment e ON cs.section_id = e.section_id AND e.status = 1
GROUP BY cs.section_id, cs.section_code, cs.semester, c.course_name, t.teacher_name, cs.capacity_limit;

-- 4.3 学生成绩汇总视图
CREATE VIEW v_student_gpa AS
SELECT
    s.student_id,
    s.student_no,
    s.student_name,
    COALESCE(ROUND(AVG(sc.gpa_point), 2), 0) AS avg_gpa,
    COUNT(CASE WHEN sc.is_passed = 1 THEN 1 END) AS passed_count,
    COUNT(sc.score_id) AS total_graded,
    SUM(c.credit) AS total_credits
FROM t_student s
LEFT JOIN t_enrollment e ON s.student_id = e.student_id AND e.status = 1
LEFT JOIN t_score sc ON e.enrollment_id = sc.enrollment_id
LEFT JOIN t_course_section cs ON e.section_id = cs.section_id
LEFT JOIN t_course c ON cs.course_id = c.course_id
GROUP BY s.student_id, s.student_no, s.student_name;

-- ------------------------------------------------------------
-- 5. 存储过程
-- ------------------------------------------------------------

DELIMITER //

-- 5.1 学生选课（退课后重选=恢复旧记录，首次选课=插入新记录）
-- 退课为逻辑退课（status=0），退课后允许重选同一教学班
-- selected_count 由触发器 trg_enrollment_insert / trg_enrollment_update 统一维护
CREATE PROCEDURE sp_enroll(
    IN p_student_id INT,
    IN p_section_id INT,
    IN p_source VARCHAR(50),
    OUT p_result INT
)
BEGIN
    DECLARE v_capacity INT;
    DECLARE v_selected INT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_result = -1;
    END;

    START TRANSACTION;

    -- 检查是否已有正常状态的选课记录
    IF EXISTS (SELECT 1 FROM t_enrollment
               WHERE student_id = p_student_id AND section_id = p_section_id AND status = 1) THEN
        SET p_result = 2; -- 已选课，不可重复选
        ROLLBACK;
    ELSE
        SELECT capacity_limit, selected_count INTO v_capacity, v_selected
        FROM t_course_section WHERE section_id = p_section_id
        FOR UPDATE;

        IF v_capacity > 0 AND v_selected >= v_capacity THEN
            SET p_result = 0; -- 容量已满
            ROLLBACK;
        ELSE
            -- 退课后重选：恢复旧记录（保留 enrollment_id，成绩等弱实体不受影响）
            IF EXISTS (SELECT 1 FROM t_enrollment
                       WHERE student_id = p_student_id AND section_id = p_section_id AND status = 0) THEN
                UPDATE t_enrollment
                SET status = 1, select_time = NOW(), source = p_source, remark = NULL
                WHERE student_id = p_student_id AND section_id = p_section_id AND status = 0;
            ELSE
                INSERT INTO t_enrollment (student_id, section_id, source, status)
                VALUES (p_student_id, p_section_id, p_source, 1);
            END IF;

            SET p_result = 1; -- 选课成功
            COMMIT;
        END IF;
    END IF;
END //

-- 5.2 学生退课（逻辑退课，status=0，selected_count 由触发器自动维护）
CREATE PROCEDURE sp_drop_course(
    IN p_enrollment_id INT,
    OUT p_result INT
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_result = -1;
    END;

    START TRANSACTION;

    UPDATE t_enrollment SET status = 0
    WHERE enrollment_id = p_enrollment_id AND status = 1;

    IF ROW_COUNT() > 0 THEN
        SET p_result = 1;
    ELSE
        SET p_result = 0;
    END IF;

    COMMIT;
END //

-- 5.3 录入成绩
CREATE PROCEDURE sp_grade(
    IN p_enrollment_id INT,
    IN p_usual_score DECIMAL(5,2),
    IN p_exam_score DECIMAL(5,2),
    IN p_graded_by VARCHAR(50),
    OUT p_result INT
)
BEGIN
    DECLARE v_final DECIMAL(5,2);
    DECLARE v_passed TINYINT;
    DECLARE v_gpa DECIMAL(3,2);

    SET v_final = ROUND(p_usual_score * 0.3 + p_exam_score * 0.7, 2);
    SET v_passed = IF(v_final >= 60, 1, 0);

    -- GPA换算（百分制→4.0标准）：≥90→4.0, ≥85→3.7, ≥82→3.3, ≥78→3.0,
    --                         ≥75→2.7, ≥72→2.3, ≥68→2.0, ≥64→1.5, ≥60→1.0, <60→0
    SET v_gpa = CASE
        WHEN v_final >= 90 THEN 4.0
        WHEN v_final >= 85 THEN 3.7
        WHEN v_final >= 82 THEN 3.3
        WHEN v_final >= 78 THEN 3.0
        WHEN v_final >= 75 THEN 2.7
        WHEN v_final >= 72 THEN 2.3
        WHEN v_final >= 68 THEN 2.0
        WHEN v_final >= 64 THEN 1.5
        WHEN v_final >= 60 THEN 1.0
        ELSE 0.0
    END;

    INSERT INTO t_score (enrollment_id, usual_score, exam_score,
                         final_score, gpa_point, is_passed, graded_at, graded_by)
    VALUES (p_enrollment_id, p_usual_score, p_exam_score,
            v_final, v_gpa, v_passed, NOW(), p_graded_by)
    ON DUPLICATE KEY UPDATE
        usual_score = p_usual_score,
        exam_score  = p_exam_score,
        final_score = v_final,
        gpa_point   = v_gpa,
        is_passed   = v_passed,
        graded_at   = NOW(),
        graded_by   = p_graded_by;

    SET p_result = 1;
END //

DELIMITER ;

-- ------------------------------------------------------------
-- 6. 触发器（selected_count 的唯一维护入口，存储过程不直接修改该字段）
-- ------------------------------------------------------------

DELIMITER //

-- 6.1 新增选课时自动同步 selected_count
CREATE TRIGGER trg_enrollment_insert
AFTER INSERT ON t_enrollment
FOR EACH ROW
BEGIN
    IF NEW.status = 1 THEN
        UPDATE t_course_section
        SET selected_count = selected_count + 1
        WHERE section_id = NEW.section_id;
    END IF;
END //

-- 6.2 选课状态变更时自动同步 selected_count（退课/重选）
CREATE TRIGGER trg_enrollment_update
AFTER UPDATE ON t_enrollment
FOR EACH ROW
BEGIN
    IF OLD.status = 1 AND NEW.status = 0 THEN
        UPDATE t_course_section
        SET selected_count = selected_count - 1
        WHERE section_id = NEW.section_id;
    ELSEIF OLD.status = 0 AND NEW.status = 1 THEN
        UPDATE t_course_section
        SET selected_count = selected_count + 1
        WHERE section_id = NEW.section_id;
    END IF;
END //

-- 6.2b 直接删除选课记录时同步 selected_count（仅活跃记录需减）
CREATE TRIGGER trg_enrollment_delete
AFTER DELETE ON t_enrollment
FOR EACH ROW
BEGIN
    IF OLD.status = 1 THEN
        UPDATE t_course_section
        SET selected_count = selected_count - 1
        WHERE section_id = OLD.section_id;
    END IF;
END //

-- 6.3 成绩变化时自动更新是否通过
CREATE TRIGGER trg_score_before_insert
BEFORE INSERT ON t_score
FOR EACH ROW
BEGIN
    IF NEW.final_score IS NOT NULL THEN
        SET NEW.is_passed = IF(NEW.final_score >= 60, 1, 0);
    END IF;
END //

CREATE TRIGGER trg_score_before_update
BEFORE UPDATE ON t_score
FOR EACH ROW
BEGIN
    IF NEW.final_score IS NOT NULL THEN
        SET NEW.is_passed = IF(NEW.final_score >= 60, 1, 0);
    END IF;
END //

DELIMITER ;

-- ------------------------------------------------------------
-- 7. 初始化数据
-- ------------------------------------------------------------

-- 7.1 院系
INSERT INTO t_department (department_code, department_name, office_phone) VALUES
('CS',    '计算机科学与技术学院', '010-11111111'),
('MATH',  '数学与统计学院',       '010-22222222'),
('ENG',   '外国语学院',           '010-33333333'),
('PHYS',  '物理学院',             '010-44444444'),
('ECON',  '经济管理学院',         '010-55555555');

-- 7.2 专业
INSERT INTO t_major (department_id, major_code, major_name) VALUES
(1, 'CS-01', '计算机科学与技术'),
(1, 'CS-02', '软件工程'),
(2, 'MATH-01', '数学与应用数学'),
(3, 'ENG-01', '英语'),
(4, 'PHYS-01', '物理学'),
(5, 'ECON-01', '工商管理');

-- 7.3 教室
INSERT INTO t_classroom (building, room_no, capacity, remark) VALUES
('教学楼A', '101', 60,  '多媒体教室'),
('教学楼A', '102', 120, '阶梯教室'),
('教学楼A', '201', 60,  '多媒体教室'),
('教学楼B', '101', 45,  '小教室'),
('教学楼B', '201', 45,  '小教室'),
('实验楼',   '301', 40,  '机房'),
('实验楼',   '302', 40,  '机房');

-- 7.4 用户账号（明文均为 123456）
INSERT INTO t_user (username, password, role, created_at) VALUES
('admin01',   '123456', 'admin',   NOW()),
('admin02',   '123456', 'admin',   NOW()),
('tch001',    '123456', 'teacher', NOW()),
('tch002',    '123456', 'teacher', NOW()),
('tch003',    '123456', 'teacher', NOW()),
('tch004',    '123456', 'teacher', NOW()),
('stu001',    '123456', 'student', NOW()),
('stu002',    '123456', 'student', NOW()),
('stu003',    '123456', 'student', NOW()),
('stu004',    '123456', 'student', NOW()),
('stu005',    '123456', 'student', NOW()),
('stu006',    '123456', 'student', NOW()),
('stu007',    '123456', 'student', NOW()),
('stu008',    '123456', 'student', NOW());

-- 7.5 管理员
INSERT INTO t_admin (user_id, admin_no, admin_name, phone, email) VALUES
(1, 'ADM2025001', '张三', '13800000001', 'admin01@university.edu.cn'),
(2, 'ADM2025002', '李四', '13800000002', 'admin02@university.edu.cn');

-- 7.6 教师
INSERT INTO t_teacher (user_id, department_id, teacher_no, teacher_name, title, phone, email) VALUES
(3, 1, 'T2025001', '王教授', '教授',     '13900000001', 'wang@university.edu.cn'),
(4, 1, 'T2025002', '刘副教授', '副教授',  '13900000002', 'liu@university.edu.cn'),
(5, 2, 'T2025003', '陈讲师', '讲师',     '13900000003', 'chen@university.edu.cn'),
(6, 3, 'T2025004', '赵教授', '教授',     '13900000004', 'zhao@university.edu.cn');

-- 7.7 学生
INSERT INTO t_student (user_id, major_id, student_no, student_name, gender, phone, email, enrollment_year) VALUES
(7,  1, 'S2025001', '赵小明', 'M', '13700000001', 'stu001@university.edu.cn', 2025),
(8,  1, 'S2025002', '钱小红', 'F', '13700000002', 'stu002@university.edu.cn', 2025),
(9,  2, 'S2025003', '孙小刚', 'M', '13700000003', 'stu003@university.edu.cn', 2025),
(10, 3, 'S2025004', '李小丽', 'F', '13700000004', 'stu004@university.edu.cn', 2025),
(11, 4, 'S2025005', '周小华', 'M', '13700000005', 'stu005@university.edu.cn', 2025),
(12, 5, 'S2025006', '吴小强', 'M', '13700000006', 'stu006@university.edu.cn', 2025),
(13, 6, 'S2025007', '郑小芳', 'F', '13700000007', 'stu007@university.edu.cn', 2025),
(14, 1, 'S2025008', '冯小文', 'M', '13700000008', 'stu008@university.edu.cn', 2025);

-- 7.8 课程
INSERT INTO t_course (department_id, course_code, course_name, credit, total_hours, course_type, description) VALUES
(1, 'CS-101', '程序设计基础',       4.0, 64, '必修', '讲授C语言程序设计'),
(1, 'CS-201', '数据结构与算法',     4.0, 64, '必修', '讲授线性表、树、图等核心数据结构'),
(1, 'CS-301', '数据库系统原理',     3.0, 48, '必修', '讲授关系数据库、SQL与事务处理'),
(2, 'MATH-101', '高等数学(上)',     5.0, 80, '必修', '函数、极限、微积分'),
(2, 'MATH-201', '线性代数',         3.0, 48, '必修', '矩阵、向量空间与特征值'),
(3, 'ENG-101', '大学英语(一)',      3.0, 48, '必修', '听、说、读、写基础训练'),
(1, 'CS-401', '软件工程导论',       2.0, 32, '选修', '软件开发流程与团队协作'),
(3, 'ENG-201', '商务英语',          2.0, 32, '选修', '商务场景下的英语应用');

-- 7.9 教学班
INSERT INTO t_course_section (course_id, teacher_id, classroom_id, section_code, semester, schedule_text, capacity_limit) VALUES
(1, 1, 1, 'CS-101-01', '2025-2026-1', '周一 1-2节, 周三 3-4节', 60),
(1, 2, 2, 'CS-101-02', '2025-2026-1', '周二 1-2节, 周四 3-4节', 120),
(2, 1, 3, 'CS-201-01', '2025-2026-1', '周一 3-4节, 周三 1-2节', 60),
(2, 2, 4, 'CS-201-02', '2025-2026-1', '周二 3-4节, 周四 1-2节', 45),
(3, 2, 6, 'CS-301-01', '2025-2026-1', '周五 1-4节', 40),
(4, 3, 2, 'MATH-101-01', '2025-2026-1', '周一 1-2节, 周三 1-2节, 周五 1-2节', 120),
(5, 3, 1, 'MATH-201-01', '2025-2026-1', '周二 5-6节, 周四 5-6节', 60),
(6, 4, 5, 'ENG-101-01', '2025-2026-1', '周一 5-6节, 周三 5-6节', 45),
(7, 1, 3, 'CS-401-01', '2025-2026-1', '周二 7-8节', 60),
(8, 4, 1, 'ENG-201-01', '2025-2026-1', '周一 7-8节', 60);

-- 7.10 选课记录
INSERT INTO t_enrollment (student_id, section_id, select_time, status, source) VALUES
(1, 1, '2025-09-01 09:00:00', 1, '自主选课'),
(1, 3, '2025-09-01 09:01:00', 1, '自主选课'),
(1, 6, '2025-09-01 09:02:00', 1, '自主选课'),
(1, 7, '2025-09-01 09:03:00', 1, '自主选课'),
(2, 2, '2025-09-01 10:00:00', 1, '自主选课'),
(2, 4, '2025-09-01 10:01:00', 1, '自主选课'),
(2, 5, '2025-09-01 10:02:00', 1, '自主选课'),
(2, 8, '2025-09-01 10:03:00', 1, '自主选课'),
(3, 1, '2025-09-02 08:00:00', 1, '自主选课'),
(3, 5, '2025-09-02 08:01:00', 1, '自主选课'),
(3, 6, '2025-09-02 08:02:00', 1, '自主选课'),
(4, 6, '2025-09-02 09:00:00', 1, '自主选课'),
(4, 8, '2025-09-02 09:01:00', 1, '自主选课'),
(5, 6, '2025-09-02 10:00:00', 1, '自主选课'),
(5, 7, '2025-09-02 10:01:00', 1, '自主选课'),
(6, 8, '2025-09-03 08:00:00', 1, '自主选课'),
(7, 8, '2025-09-03 08:01:00', 1, '自主选课'),
(8, 1, '2025-09-03 09:00:00', 1, '自主选课'),
(8, 3, '2025-09-03 09:01:00', 1, '自主选课'),
(8, 6, '2025-09-03 09:02:00', 1, '自主选课'),
(1, 9, '2025-09-04 08:00:00', 0, '自主选课'), -- 退课记录
(3, 9, '2025-09-04 08:01:00', 1, '自主选课');

-- 7.11 成绩记录（GPA按8档标准换算）
INSERT INTO t_score (enrollment_id, usual_score, exam_score, final_score, gpa_point, is_passed, graded_at, graded_by) VALUES
(1,  85, 90, 88.5, 3.7, 1, '2026-01-15 10:00:00', '王教授'),
(2,  92, 88, 89.2, 3.7, 1, '2026-01-15 10:00:00', '王教授'),
(3,  78, 82, 80.8, 3.0, 1, '2026-01-15 10:00:00', '陈讲师'),
(5,  90, 94, 92.8, 4.0, 1, '2026-01-15 10:00:00', '刘副教授'),
(6,  60, 55, 56.5, 0.0, 0, '2026-01-15 10:00:00', '陈讲师'),
(7,  70, 75, 73.5, 2.3, 1, '2026-01-15 10:00:00', '陈讲师'),
(8,  95, 90, 91.5, 4.0, 1, '2026-01-15 10:00:00', '赵教授'),
(9,  65, 68, 67.1, 1.5, 1, '2026-01-15 10:00:00', '王教授'),
(10, 88, 85, 85.9, 3.7, 1, '2026-01-15 10:00:00', '刘副教授'),
(11, 58, 50, 52.4, 0.0, 0, '2026-01-15 10:00:00', '陈讲师'),
(12, 82, 88, 86.2, 3.7, 1, '2026-01-15 10:00:00', '陈讲师'),
(13, 91, 87, 88.2, 3.7, 1, '2026-01-15 10:00:00', '赵教授'),
(15, 78, 80, 79.4, 3.0, 1, '2026-01-15 10:00:00', '王教授'),
(19, 85, 82, 82.9, 3.3, 1, '2026-01-15 10:00:00', '王教授');

-- 7.12 同步选课人数
UPDATE t_course_section cs
SET selected_count = (
    SELECT COUNT(*) FROM t_enrollment e
    WHERE e.section_id = cs.section_id AND e.status = 1
);

-- ------------------------------------------------------------
-- 8. 性能测试准备：批量数据生成脚本（存储过程）
-- ------------------------------------------------------------

DELIMITER //

CREATE PROCEDURE sp_generate_test_data(
    IN p_student_count INT
)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE v_user_id INT;

    WHILE i < p_student_count DO
        INSERT INTO t_user (username, password, role) VALUES
        (CONCAT('teststu_', i), '123456', 'student');
        SET v_user_id = LAST_INSERT_ID();

        INSERT INTO t_student (user_id, major_id, student_no, student_name, gender, enrollment_year)
        VALUES (v_user_id, 1, CONCAT('TS', LPAD(i, 6, '0')), CONCAT('测试学生', i), 'M', 2025);

        SET i = i + 1;
    END WHILE;
END //

DELIMITER ;
