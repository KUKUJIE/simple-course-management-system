package com.agiantii.backend.mapper;

import com.agiantii.backend.pojo.Department;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DepartmentMapper {

    @Select("SELECT department_id, department_code, department_name, office_phone, status FROM t_department ORDER BY department_id")
    List<Department> selectAll();

    @Select("SELECT department_id, department_code, department_name, office_phone, status FROM t_department WHERE department_id = #{departmentId}")
    Department selectById(@Param("departmentId") Integer departmentId);

    @Insert("INSERT INTO t_department(department_code, department_name, office_phone, status) VALUES(#{departmentCode}, #{departmentName}, #{officePhone}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "departmentId")
    void insert(Department department);

    @Update("UPDATE t_department SET department_code = #{departmentCode}, department_name = #{departmentName}, office_phone = #{officePhone} WHERE department_id = #{departmentId}")
    void update(Department department);

    @Update("UPDATE t_department SET status = 0 WHERE department_id = #{departmentId}")
    void disable(@Param("departmentId") Integer departmentId);

    @Select("SELECT COUNT(*) FROM t_major WHERE department_id = #{departmentId} AND status = 1")
    int countReferencedMajors(@Param("departmentId") Integer departmentId);

    @Select("SELECT COUNT(*) FROM t_teacher WHERE department_id = #{departmentId} AND status = 1")
    int countReferencedTeachers(@Param("departmentId") Integer departmentId);

    @Select("SELECT department_id, department_code, department_name, office_phone, status FROM t_department WHERE status = 1 ORDER BY department_id")
    List<Department> selectActiveAll();
}
