package com.agiantii.backend.mapper;

import com.agiantii.backend.pojo.Course;
import com.agiantii.backend.pojo.vo.CourseVo;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;
@Mapper
// 注意：本 Mapper 基于 V1 表结构编写，V2 迁移后 t_course 列名已变化，
// 已修复 JOIN 中 t_teacher 的列名引用，其余列名问题待相关模块整体迁移时处理。
public interface CourseMapper {
    @Insert("insert into t_course(teacher_id,id,name,term,credit,student_limit) values (#{teacherId},#{id},#{name},#{term},#{credit},#{studentLimit})")
    void insert(Course course);

    @Delete("delete from t_course where id = #{id}")
    void deleteByCourseId(Integer id);

    @Delete("delete from t_course where id = #{id} and teacher_id = #{teacherId}")
    void deleteByCourseIdAndTeacherId(@Param("id") Integer id, @Param("teacherId") Integer teacherId);

    @Update("update t_course set name = #{name}, term = #{term}, credit = #{credit},student_limit = #{studentLimit} where id = #{id} and teacher_id = #{teacherId}")
    void update(Course course);

    @Select("select course.*, teacher.teacher_name as teacherName from t_course as course left join t_teacher as teacher on course.teacher_id = teacher.teacher_id where course.name like concat('%',#{courseName},'%')")
    List<CourseVo> selectByCourseName(String courseName);

    @Select("select course.*, teacher.teacher_name as teacherName from t_course as course left join t_teacher as teacher on course.teacher_id = teacher.teacher_id where teacher.teacher_name like concat('%',#{teacherName},'%')")
    List<CourseVo> selectByTeacherName(String teacherName);

    @Select("select student_limit from t_course where id = #{courseId} and teacher_id = #{teacherId}")
    Integer countByCourseIdAndTeacherId(@Param("courseId") Integer courseId, @Param("teacherId") Integer teacherId);

    @Select("select course.*, teacher.teacher_name as teacherName from t_course as course left join t_teacher as teacher on course.teacher_id = teacher.teacher_id where teacher.teacher_id = #{teacherId}")
    List<CourseVo> selectByTeacherId(Integer teacherId);

    @Select("select course.*, teacher.teacher_name as teacherName from t_course as course left join t_teacher as teacher on course.teacher_id = teacher.teacher_id where course.id = #{courseId}")
    CourseVo selectByCourseId(Integer courseId);

    @Select("select course.*, teacher.teacher_name as teacherName from t_course as course left join t_teacher as teacher on course.teacher_id = teacher.teacher_id")
    List<CourseVo> selectAll();

    // ======== 管理员端课程管理方法（基于 V2 表结构）========

    @Select("SELECT c.course_id AS id, c.course_code AS courseCode, c.course_name AS name, " +
            "c.credit, c.total_hours AS totalHours, c.course_type AS courseType, " +
            "c.department_id AS departmentId, d.department_name AS departmentName, " +
            "c.description, c.status " +
            "FROM t_course c " +
            "LEFT JOIN t_department d ON c.department_id = d.department_id " +
            "ORDER BY c.course_id")
    List<Map<String, Object>> selectAllForAdmin();

    @Select("SELECT c.course_id AS id, c.course_code AS courseCode, c.course_name AS name, " +
            "c.credit, c.total_hours AS totalHours, c.course_type AS courseType, " +
            "c.department_id AS departmentId, d.department_name AS departmentName, " +
            "c.description, c.status " +
            "FROM t_course c " +
            "LEFT JOIN t_department d ON c.department_id = d.department_id " +
            "WHERE c.course_id = #{courseId}")
    Map<String, Object> selectByIdForAdmin(@Param("courseId") Integer courseId);

    @Insert("INSERT INTO t_course(course_code, course_name, credit, total_hours, course_type, " +
            "department_id, description, status) " +
            "VALUES(#{courseCode}, #{name}, #{credit}, #{totalHours}, #{courseType}, " +
            "#{departmentId}, #{description}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertCourse(Map<String, Object> course);

    @Update("UPDATE t_course SET course_code = #{courseCode}, course_name = #{name}, " +
            "credit = #{credit}, total_hours = #{totalHours}, course_type = #{courseType}, " +
            "department_id = #{departmentId}, description = #{description}, status = #{status} " +
            "WHERE course_id = #{id}")
    void updateCourse(Map<String, Object> course);

    @Update("UPDATE t_course SET status = 0 WHERE course_id = #{courseId}")
    void disableCourse(@Param("courseId") Integer courseId);

    @Select("SELECT COUNT(*) FROM t_course_section WHERE course_id = #{courseId} AND status = 1")
    int countReferencedSections(@Param("courseId") Integer courseId);

    @Select("SELECT COUNT(*) FROM t_course_section WHERE course_id = #{courseId}")
    int countAllSectionsByCourseId(@Param("courseId") Integer courseId);

    @Delete("DELETE FROM t_course WHERE course_id = #{courseId}")
    void deleteCourseById(@Param("courseId") Integer courseId);

    @Select("SELECT course_id AS id, course_code AS courseCode, course_name AS name " +
            "FROM t_course WHERE status = 1 ORDER BY course_id")
    List<Map<String, Object>> selectActiveForOptions();
}
