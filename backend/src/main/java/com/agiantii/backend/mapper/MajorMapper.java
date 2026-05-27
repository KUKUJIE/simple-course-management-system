package com.agiantii.backend.mapper;

import com.agiantii.backend.pojo.Major;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface MajorMapper {

    @Select("SELECT m.major_id, m.department_id, m.major_code, m.major_name, m.status, " +
            "d.department_name, d.department_code AS departmentCode " +
            "FROM t_major m LEFT JOIN t_department d ON m.department_id = d.department_id " +
            "ORDER BY m.major_id")
    List<Map<String, Object>> selectAllWithDept();

    @Select("SELECT m.major_id, m.department_id, m.major_code, m.major_name, m.status, " +
            "d.department_name, d.department_code AS departmentCode " +
            "FROM t_major m LEFT JOIN t_department d ON m.department_id = d.department_id " +
            "WHERE m.major_id = #{majorId}")
    Map<String, Object> selectByIdWithDept(@Param("majorId") Integer majorId);

    @Select("SELECT major_id, department_id, major_code, major_name, status FROM t_major WHERE major_id = #{majorId}")
    Major selectById(@Param("majorId") Integer majorId);

    @Insert("INSERT INTO t_major(department_id, major_code, major_name, status) VALUES(#{departmentId}, #{majorCode}, #{majorName}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "majorId")
    void insert(Major major);

    @Update("UPDATE t_major SET department_id = #{departmentId}, major_code = #{majorCode}, major_name = #{majorName}, status = #{status} WHERE major_id = #{majorId}")
    void update(Major major);

    @Update("UPDATE t_major SET status = 0 WHERE major_id = #{majorId}")
    void disable(@Param("majorId") Integer majorId);

    @Select("SELECT COUNT(*) FROM t_student WHERE major_id = #{majorId} AND status = 1")
    int countReferencedStudents(@Param("majorId") Integer majorId);

    @Select("SELECT major_id, major_code, major_name, status FROM t_major WHERE status = 1 ORDER BY major_id")
    List<Major> selectActiveAll();
}
