package com.agiantii.backend.mapper;

import com.agiantii.backend.pojo.User;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {
    @Insert("insert into t_user (id,password,role) values (#{id},#{password},#{role})")
    void insertUser(User user);

    // ======== 管理员端：创建用户账号（自动生成ID） ========
    @Insert("INSERT INTO t_user(username, password, role, status, created_at) VALUES(#{username}, #{password}, #{role}, #{status}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "userId")
    void insertUserAdmin(Map<String, Object> user);

    @Update("UPDATE t_user SET status = 0 WHERE id = #{userId}")
    void disableUser(@Param("userId") Integer userId);

    @Update("UPDATE t_user SET status = #{status} WHERE id = #{userId}")
    void updateUserStatus(@Param("userId") Integer userId, @Param("status") Integer status);

    @Delete("delete from t_user where id=#{id}")
    void deleteUserNyId(int id);
    @Update("update t_user set password=#{password} where id=#{id}")
    void updateUser(User user);

    @Select("select * from t_user wheere name like contact('%',#{name},'%')")
    List<User> selectByUserName(@Param("name") String userName);

    @Select("select * from t_user where role = #{role}")
    List<User> selectUserByRole(String role);
    @Select("select * from t_user where id = #{id}")
    User selectUserById(int id);
    @Select("select * from t_user where id = #{id} and password = #{password}")
    User selectUserByIdAndPassword(@Param("id") Integer id, @Param("password") String password);

    @Select("select * from t_user where username = #{username} and password = #{password}")
    User selectByUsernameAndPassword(@Param("username") String username, @Param("password") String password);
}
