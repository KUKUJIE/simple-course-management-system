package com.agiantii.backend.controller;

import com.agiantii.backend.common.Constant;
import com.agiantii.backend.common.R;
import com.agiantii.backend.mapper.TeacherMapper;
import com.agiantii.backend.mapper.UserMapper;
import com.agiantii.backend.pojo.Teacher;
import com.agiantii.backend.pojo.User;
import com.agiantii.backend.utils.SimpleUtil;
import com.agiantii.backend.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
// 已废弃 旧版教师控制器 已从Spring容器移除 教师功能迁移到新架构
// @RestController
// @RequestMapping("teacher")
public class TeacherController {
    @Resource
    private TeacherMapper teacherMapper;
    @Resource
    private UserMapper userMapper;

    @PostMapping("/register")
    public R<String> register(@RequestBody Teacher teacher, @RequestParam(value = "password",defaultValue  ="123") String password){
        if(userMapper.selectUserById(teacher.getTeacherId())!= null){
            return R.error("该教师已注册");
        }
        log.info("register teacher: {} password:{}" , teacher.toString(),password);
        User user  = new User();
        user.setRole(Constant.ROLE_TEACHER);
        user.setPassword(password);
        user.setId(teacher.getTeacherId());
        user.setUsername("teacher_" + teacher.getTeacherId());

        try{
            teacherMapper.insert(teacher);
            userMapper.insertUser(user);
            return R.success(Constant.REGISTER_SUCCESS);
        }
        catch (Exception e){
            return R.error(Constant.REGISTER_FAIL);
        }
    }
    @PostMapping("update")
    public R<String> update(@RequestBody Teacher teacher){
        log.info("update teacher: {} " , teacher.toString());
        try{
            teacherMapper.update(teacher);
            return R.success("更新成功");
        }
        catch (Exception e){
            return R.error("更新失败");
            }
    }

    @GetMapping("deleteByTeacherId")
    public R<String> deleteByTeacherId(@RequestParam("teacherId") Integer teacherId){
        log.info("delete teacher: {} " , teacherId.toString());
        try{
            teacherMapper.delete(teacherId);
            return R.success("删除成功");
        }
        catch (Exception e){
            return R.error("删除失败");
        }
    }
    @GetMapping("selectByTeacherId")
    public R<Teacher> selectById(@RequestParam(value = "teacherId") Integer teacherId){
        try{
            Teacher teacher = teacherMapper.selectByTeacherId(teacherId);
            return R.success(teacher,"查询教师成功");
        }
        catch (Exception e){
            return R.error("查询失败");
        }
    }
    @GetMapping("selectByTeacherName")
    public R<List<Teacher>> selectByName(@RequestParam(value = "teacherName",defaultValue = "",required = false) String teacherName){
        if(StringUtil.isEmpty(teacherName)){
            List<Teacher> teachers = teacherMapper.selectAll();
            return R.success(teachers,SimpleUtil.succcessMessage(teachers));
        }
        try{
            List<Teacher> teachers = teacherMapper.selectByTeacherName(teacherName);
            return R.success(teachers, SimpleUtil.succcessMessage(teachers));
        }
        catch (Exception e){
            log.error(e.getMessage());
            return R.error("查询失败");
        }
    }
    @GetMapping("selectAll")
    public R<List<Teacher>> selectAll(){
        try{
            List<Teacher> teachers = teacherMapper.selectAll();
            return R.success(teachers, SimpleUtil.succcessMessage(teachers));
        }
        catch (Exception e){
            log.error(e.getMessage());
            return R.error("查询失败");
        }
    }
}
