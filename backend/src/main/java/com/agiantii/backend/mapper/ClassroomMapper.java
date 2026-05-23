package com.agiantii.backend.mapper;

import com.agiantii.backend.pojo.Classroom;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ClassroomMapper {

    @Select("SELECT classroom_id, building, room_no, capacity, status, remark FROM t_classroom ORDER BY classroom_id")
    List<Classroom> selectAll();

    @Select("SELECT classroom_id, building, room_no, capacity, status, remark FROM t_classroom WHERE classroom_id = #{classroomId}")
    Classroom selectById(@Param("classroomId") Integer classroomId);

    @Insert("INSERT INTO t_classroom(building, room_no, capacity, status, remark) VALUES(#{building}, #{roomNo}, #{capacity}, #{status}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "classroomId")
    void insert(Classroom classroom);

    @Update("UPDATE t_classroom SET building = #{building}, room_no = #{roomNo}, capacity = #{capacity}, remark = #{remark}, status = #{status} WHERE classroom_id = #{classroomId}")
    void update(Classroom classroom);

    @Update("UPDATE t_classroom SET status = 0 WHERE classroom_id = #{classroomId}")
    void disable(@Param("classroomId") Integer classroomId);

    @Select("SELECT COUNT(*) FROM t_course_section WHERE classroom_id = #{classroomId} AND status = 1")
    int countReferencedSections(@Param("classroomId") Integer classroomId);

    @Select("SELECT COUNT(*) FROM t_course_section WHERE classroom_id = #{classroomId}")
    int countAllSectionsByClassroomId(@Param("classroomId") Integer classroomId);

    @Delete("DELETE FROM t_classroom WHERE classroom_id = #{classroomId}")
    void deleteById(@Param("classroomId") Integer classroomId);

    @Select("SELECT classroom_id, building, room_no, capacity, status FROM t_classroom WHERE status = 1 ORDER BY classroom_id")
    List<Classroom> selectActiveAll();
}
