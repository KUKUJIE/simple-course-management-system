package com.agiantii.backend.mapper;


import com.agiantii.backend.pojo.Teacher;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface TeacherMapper {

    @Insert("insert into t_teacher(teacher_id, teacher_name) values (#{teacherId}, #{teacherName})")
    public void insert(Teacher teacher);


    @Delete("delete from t_teacher where teacher_id = #{teacherId}")
    public void delete(Integer teacherId);

    @Update("update t_teacher set teacher_name = #{teacherName} where teacher_id = #{teacherId}")
    public void update(Teacher teacher);

    @Select("select teacher_id, teacher_name from t_teacher where teacher_id = #{teacherId}")
    public Teacher selectByTeacherId(Integer teacherId);

    @Select("select teacher_id, teacher_name from t_teacher where teacher_name like concat ('%',#{name},'%')")
    public List<Teacher> selectByTeacherName(@Param("name") String name);

    @Select("select teacher_id, teacher_name from t_teacher")
    public List<Teacher> selectAll();

    @Select("select teacher_name from t_teacher where user_id = #{userId}")
    String selectNameByUserId(@Param("userId") Integer userId);

    @Select("select teacher_id from t_teacher where user_id = #{userId}")
    Integer selectIdByUserId(@Param("userId") Integer userId);

    @Select("select teacher_name from t_teacher where teacher_id = #{teacherId}")
    String selectNameByTeacherId(@Param("teacherId") Integer teacherId);

    // ======== 管理员端教师管理方法 ========

    @Select("<script>" +
            "SELECT t.teacher_id AS teacherId, t.teacher_no AS teacherNo, t.teacher_name AS teacherName, " +
            "t.department_id AS departmentId, d.department_name AS departmentName, " +
            "t.title, t.phone, t.email, t.status " +
            "FROM t_teacher t " +
            "LEFT JOIN t_department d ON t.department_id = d.department_id " +
            "WHERE 1=1 " +
            "<if test='departmentId != null'> AND t.department_id = #{departmentId}</if>" +
            "<if test='status != null'> AND t.status = #{status}</if>" +
            "ORDER BY t.teacher_id" +
            "</script>")
    List<Map<String, Object>> selectAllForAdmin(
            @Param("departmentId") Integer departmentId,
            @Param("status") Integer status);

    @Select("SELECT t.teacher_id AS teacherId, t.teacher_no AS teacherNo, t.teacher_name AS teacherName, " +
            "t.user_id AS userId, t.department_id AS departmentId, d.department_name AS departmentName, " +
            "t.title, t.phone, t.email, t.status " +
            "FROM t_teacher t " +
            "LEFT JOIN t_department d ON t.department_id = d.department_id " +
            "WHERE t.teacher_id = #{teacherId}")
    Map<String, Object> selectByIdForAdmin(@Param("teacherId") Integer teacherId);

    @Insert("INSERT INTO t_teacher(user_id, department_id, teacher_no, teacher_name, title, phone, email, status) " +
            "VALUES(#{userId}, #{departmentId}, #{teacherNo}, #{teacherName}, #{title}, #{phone}, #{email}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "teacherId")
    void insertTeacherAdmin(Map<String, Object> teacher);

    @Update("UPDATE t_teacher SET department_id = #{departmentId}, teacher_no = #{teacherNo}, " +
            "teacher_name = #{teacherName}, title = #{title}, phone = #{phone}, " +
            "email = #{email}, status = #{status} " +
            "WHERE teacher_id = #{teacherId}")
    void updateTeacherAdmin(Map<String, Object> teacher);

    @Update("UPDATE t_teacher SET status = 0 WHERE teacher_id = #{teacherId}")
    void disableTeacher(@Param("teacherId") Integer teacherId);

    @Select("SELECT COUNT(*) FROM t_course_section WHERE teacher_id = #{teacherId} AND status = 1")
    int countReferencedSections(@Param("teacherId") Integer teacherId);

    /** 删除保护：检查全部教学班记录（不限状态） */
    @Select("SELECT COUNT(*) FROM t_course_section WHERE teacher_id = #{teacherId}")
    int countAllSections(@Param("teacherId") Integer teacherId);

    /** 查询指定前缀的最大序号，用于生成工号 */
    @Select("SELECT MAX(CAST(SUBSTRING(teacher_no, LENGTH(#{prefix})+1) AS UNSIGNED)) FROM t_teacher WHERE teacher_no LIKE CONCAT(#{prefix}, '%')")
    Integer selectMaxSeqByPrefix(@Param("prefix") String prefix);

    /** 根据 teacher_id 查询 user_id（用于停用同步） */
    @Select("SELECT user_id FROM t_teacher WHERE teacher_id = #{teacherId}")
    Integer selectUserIdByTeacherId(@Param("teacherId") Integer teacherId);
}
