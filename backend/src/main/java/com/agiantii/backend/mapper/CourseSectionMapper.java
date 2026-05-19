package com.agiantii.backend.mapper;

import com.agiantii.backend.pojo.vo.CourseSectionVo;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface CourseSectionMapper {

    @Select("SELECT cs.section_id AS sectionId, cs.section_code AS sectionCode, cs.semester, " +
            "cs.course_id AS courseId, c.course_code AS courseCode, c.course_name AS courseName, " +
            "c.credit, c.total_hours AS totalHours, c.course_type AS courseType, " +
            "t.teacher_name AS teacherName, t.title, " +
            "cr.building, cr.room_no AS roomNo, " +
            "cs.schedule_text AS scheduleText, cs.capacity_limit AS capacityLimit, " +
            "cs.selected_count AS selectedCount, " +
            "(cs.capacity_limit - cs.selected_count) AS remaining, cs.status " +
            "FROM t_course_section cs " +
            "JOIN t_course c ON cs.course_id = c.course_id " +
            "JOIN t_teacher t ON cs.teacher_id = t.teacher_id " +
            "LEFT JOIN t_classroom cr ON cs.classroom_id = cr.classroom_id " +
            "WHERE cs.status = #{status} " +
            "ORDER BY cs.section_id")
    List<CourseSectionVo> selectAllWithDetails(@Param("status") Integer status);

    @Select("SELECT cs.section_id AS sectionId, cs.section_code AS sectionCode, cs.semester, " +
            "cs.course_id AS courseId, c.course_code AS courseCode, c.course_name AS courseName, " +
            "c.credit, c.total_hours AS totalHours, c.course_type AS courseType, c.description, " +
            "t.teacher_name AS teacherName, t.title, " +
            "cr.building, cr.room_no AS roomNo, " +
            "cs.schedule_text AS scheduleText, cs.capacity_limit AS capacityLimit, " +
            "cs.selected_count AS selectedCount, " +
            "(cs.capacity_limit - cs.selected_count) AS remaining, cs.status " +
            "FROM t_course_section cs " +
            "JOIN t_course c ON cs.course_id = c.course_id " +
            "JOIN t_teacher t ON cs.teacher_id = t.teacher_id " +
            "LEFT JOIN t_classroom cr ON cs.classroom_id = cr.classroom_id " +
            "WHERE cs.section_id = #{sectionId}")
    CourseSectionVo selectSectionDetail(@Param("sectionId") Integer sectionId);

    @Select("SELECT cs.section_id AS sectionId, cs.section_code AS sectionCode, cs.semester, " +
            "c.course_name AS courseName, c.credit, " +
            "cs.schedule_text AS scheduleText, " +
            "cr.building, cr.room_no AS roomNo, " +
            "cs.capacity_limit AS capacityLimit, cs.selected_count AS selectedCount, " +
            "(SELECT COUNT(*) FROM t_enrollment e WHERE e.section_id = cs.section_id AND e.status = 1) AS enrolledCount " +
            "FROM t_course_section cs " +
            "JOIN t_course c ON cs.course_id = c.course_id " +
            "LEFT JOIN t_classroom cr ON cs.classroom_id = cr.classroom_id " +
            "WHERE cs.teacher_id = #{teacherId} AND cs.status = 1 " +
            "ORDER BY cs.semester DESC, cs.section_id")
    List<CourseSectionVo> selectByTeacherId(@Param("teacherId") Integer teacherId);

    @Select("SELECT teacher_id FROM t_course_section WHERE section_id = #{sectionId}")
    Integer selectTeacherIdBySectionId(@Param("sectionId") Integer sectionId);

    // ======== 管理员端教学班管理方法 ========

    @Select("<script>" +
            "SELECT cs.section_id AS sectionId, cs.section_code AS sectionCode, cs.semester, " +
            "cs.course_id AS courseId, c.course_name AS courseName, " +
            "cs.teacher_id AS teacherId, t.teacher_name AS teacherName, " +
            "cs.classroom_id AS classroomId, CONCAT(cr.building, ' ', cr.room_no) AS classroomName, " +
            "cs.capacity_limit AS capacityLimit, cs.selected_count AS selectedCount, " +
            "cs.schedule_text AS scheduleText, cs.status " +
            "FROM t_course_section cs " +
            "JOIN t_course c ON cs.course_id = c.course_id " +
            "JOIN t_teacher t ON cs.teacher_id = t.teacher_id " +
            "LEFT JOIN t_classroom cr ON cs.classroom_id = cr.classroom_id " +
            "WHERE 1=1 " +
            "<if test='courseId != null'> AND cs.course_id = #{courseId}</if>" +
            "<if test='teacherId != null'> AND cs.teacher_id = #{teacherId}</if>" +
            "<if test='status != null'> AND cs.status = #{status}</if>" +
            "ORDER BY cs.semester DESC, cs.section_id" +
            "</script>")
    List<Map<String, Object>> selectAllForAdmin(
            @Param("courseId") Integer courseId,
            @Param("teacherId") Integer teacherId,
            @Param("status") Integer status);

    @Select("SELECT cs.section_id AS sectionId, cs.section_code AS sectionCode, cs.semester, " +
            "cs.course_id AS courseId, c.course_name AS courseName, " +
            "cs.teacher_id AS teacherId, t.teacher_name AS teacherName, " +
            "cs.classroom_id AS classroomId, CONCAT(cr.building, ' ', cr.room_no) AS classroomName, " +
            "cs.capacity_limit AS capacityLimit, cs.selected_count AS selectedCount, " +
            "cs.schedule_text AS scheduleText, cs.status " +
            "FROM t_course_section cs " +
            "JOIN t_course c ON cs.course_id = c.course_id " +
            "JOIN t_teacher t ON cs.teacher_id = t.teacher_id " +
            "LEFT JOIN t_classroom cr ON cs.classroom_id = cr.classroom_id " +
            "WHERE cs.section_id = #{sectionId}")
    Map<String, Object> selectByIdForAdmin(@Param("sectionId") Integer sectionId);

    @Insert("INSERT INTO t_course_section(section_code, course_id, teacher_id, classroom_id, " +
            "semester, capacity_limit, schedule_text, status) " +
            "VALUES(#{sectionCode}, #{courseId}, #{teacherId}, #{classroomId}, " +
            "#{semester}, #{capacityLimit}, #{scheduleText}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "sectionId")
    void insertSection(Map<String, Object> section);

    @Update("UPDATE t_course_section SET section_code = #{sectionCode}, course_id = #{courseId}, " +
            "teacher_id = #{teacherId}, classroom_id = #{classroomId}, semester = #{semester}, " +
            "capacity_limit = #{capacityLimit}, schedule_text = #{scheduleText}, status = #{status} " +
            "WHERE section_id = #{sectionId}")
    void updateSection(Map<String, Object> section);

    @Update("UPDATE t_course_section SET status = 0 WHERE section_id = #{sectionId}")
    void closeSection(@Param("sectionId") Integer sectionId);

    @Select("SELECT COUNT(*) FROM t_enrollment WHERE section_id = #{sectionId} AND status = 1")
    int countEnrollments(@Param("sectionId") Integer sectionId);

    @Select("SELECT selected_count FROM t_course_section WHERE section_id = #{sectionId}")
    Integer selectSelectedCount(@Param("sectionId") Integer sectionId);
}
