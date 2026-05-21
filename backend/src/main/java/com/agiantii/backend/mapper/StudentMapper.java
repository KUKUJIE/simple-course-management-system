package com.agiantii.backend.mapper;

import com.agiantii.backend.pojo.Student;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface StudentMapper {
    @Insert("insert into t_student(student_id, student_name) values (#{studentId}, #{studentName})")
    void insertStudent(Student student);

    @Delete("delete from t_student where student_id = #{studentId}")
    void deleteStudentById(Integer studentId);

    @Update("update t_student set student_name = #{studentName} where student_id = #{studentId}")
    void updateStudent(Student student);

    @Select("select student_id, student_name from t_student where student_name like CONCAT('%',#{name},'%')")
    List<Student> selectByStudentName(@Param("name") String studentName);

    @Select("select student_id, student_name from t_student")
    List<Student> selectAllStudent();

    @Select("select student_id, student_name from t_student where student_id = #{studentId}")
    Student selectStudentById(Integer studentId);

    @Select("select student_name from t_student where user_id = #{userId}")
    String selectNameByUserId(@Param("userId") Integer userId);

    @Select("select student_id from t_student where user_id = #{userId}")
    Integer selectIdByUserId(@Param("userId") Integer userId);

    // ======== 管理员端学生管理方法 ========

    @Select("<script>" +
            "SELECT s.student_id AS studentId, s.student_no AS studentNo, s.student_name AS studentName, " +
            "s.major_id AS majorId, m.major_name AS majorName, " +
            "s.gender, s.phone, s.email, s.enrollment_year AS enrollmentYear, s.status " +
            "FROM t_student s " +
            "LEFT JOIN t_major m ON s.major_id = m.major_id " +
            "WHERE 1=1 " +
            "<if test='majorId != null'> AND s.major_id = #{majorId}</if>" +
            "<if test='status != null'> AND s.status = #{status}</if>" +
            "ORDER BY s.student_id" +
            "</script>")
    List<Map<String, Object>> selectAllForAdmin(
            @Param("majorId") Integer majorId,
            @Param("status") Integer status);

    @Select("SELECT s.student_id AS studentId, s.student_no AS studentNo, s.student_name AS studentName, " +
            "s.user_id AS userId, s.major_id AS majorId, m.major_name AS majorName, " +
            "s.gender, s.phone, s.email, s.enrollment_year AS enrollmentYear, s.status " +
            "FROM t_student s " +
            "LEFT JOIN t_major m ON s.major_id = m.major_id " +
            "WHERE s.student_id = #{studentId}")
    Map<String, Object> selectByIdForAdmin(@Param("studentId") Integer studentId);

    @Insert("INSERT INTO t_student(user_id, major_id, student_no, student_name, gender, phone, email, enrollment_year, status) " +
            "VALUES(#{userId}, #{majorId}, #{studentNo}, #{studentName}, #{gender}, #{phone}, #{email}, #{enrollmentYear}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "studentId")
    void insertStudentAdmin(Map<String, Object> student);

    @Update("UPDATE t_student SET major_id = #{majorId}, student_no = #{studentNo}, " +
            "student_name = #{studentName}, gender = #{gender}, phone = #{phone}, " +
            "email = #{email}, enrollment_year = #{enrollmentYear}, status = #{status} " +
            "WHERE student_id = #{studentId}")
    void updateStudentAdmin(Map<String, Object> student);

    @Update("UPDATE t_student SET status = 0 WHERE student_id = #{studentId}")
    void disableStudent(@Param("studentId") Integer studentId);

    @Select("SELECT COUNT(*) FROM t_enrollment WHERE student_id = #{studentId} AND status = 1")
    int countReferencedEnrollments(@Param("studentId") Integer studentId);

    /** 删除保护：检查全部选课记录（不限状态） */
    @Select("SELECT COUNT(*) FROM t_enrollment WHERE student_id = #{studentId}")
    int countAllEnrollments(@Param("studentId") Integer studentId);

    /** 查询指定前缀的最大序号，用于生成学号 */
    @Select("SELECT MAX(CAST(SUBSTRING(student_no, LENGTH(#{prefix})+1) AS UNSIGNED)) FROM t_student WHERE student_no LIKE CONCAT(#{prefix}, '%')")
    Integer selectMaxSeqByPrefix(@Param("prefix") String prefix);

    /** 根据 student_id 查询 user_id（用于停用同步） */
    @Select("SELECT user_id FROM t_student WHERE student_id = #{studentId}")
    Integer selectUserIdByStudentId(@Param("studentId") Integer studentId);
}
