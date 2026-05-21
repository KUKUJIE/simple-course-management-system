package com.agiantii.backend.controller;

import com.agiantii.backend.common.Constant;
import com.agiantii.backend.common.R;
import com.agiantii.backend.mapper.StudentMapper;
import com.agiantii.backend.mapper.UserMapper;
import com.agiantii.backend.pojo.Student;
import com.agiantii.backend.pojo.User;
import com.agiantii.backend.utils.SimpleUtil;
import com.agiantii.backend.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
// 已废弃 旧版学生控制器 已从Spring容器移除 学生功能迁移到新架构
// @RestController
// @RequestMapping("/student")
public class StudentController {
    @Resource
    private UserMapper userMapper;
    @Resource
    private StudentMapper studentMapper;
    @GetMapping("selectAll")
    public R<List<Student>> selectAll() {
        try{
            List<Student> students = studentMapper.selectAllStudent();
            return R.success(students, SimpleUtil.succcessMessage(students));
        }
        catch(Exception e){
            return R.error("查询失败");
        }
    }
    @GetMapping("selectByStudentName")
    R<List<Student>> selectByStudentName(String studentName){
        if(StringUtil.isEmpty(studentName)){
            List<Student> lis = studentMapper.selectAllStudent();
            return R.success(lis,SimpleUtil.succcessMessage(lis));
        }
        try{
            log.info("查询 姓名 类似 为 {} 中",studentName);
            List<Student> students = studentMapper.selectByStudentName(studentName);
            log.info(students.toString());
            return R.success(students, SimpleUtil.succcessMessage(students));
        }
        catch(Exception e){
            return R.error("查询失败");
        }
    }
    @GetMapping("selectByStudentId")
    R<Student> selectByStudentId(int studentId){
        try{
            log.info("查询 学生ID 为 {} 的学生",studentId);
            Student student = studentMapper.selectStudentById(studentId);
            log.info(student.toString());
            return R.success(student, "查询成功");
            }
        catch(Exception e){
            e.printStackTrace();
            return R.error("查询失败");
        }
    }
    @GetMapping("deleteByStudentId")
    R<String> deleteByStudentId(int studentId) {
        User user = userMapper.selectUserById(studentId);
        if (user == null) {
            return R.error("该学生不存在");
        }
        studentMapper.deleteStudentById(studentId);
        userMapper.deleteUserNyId(studentId);
        return R.success("删除成功");
    }
    @PostMapping("update")
    R<String> updateStudent(@RequestBody Student student){
        try{
            studentMapper.updateStudent(student);
            return R.success("更新成功");
        }
        catch(Exception e){
            return R.error(e.getMessage());
        }
    }
    @PostMapping("register")
    R<String> addStudent(@RequestBody Student student,@RequestParam(value = "password",defaultValue = "123") String password){
        log.info("添加学生中 {} Password {}", student, password);
        if(userMapper.selectUserById(student.getStudentId())!= null){
            return R.error("该学生已注册");
        }
        try{
            User user = new User();
            user.setPassword(password);
            user.setId(student.getStudentId());
            user.setUsername("student_" + student.getStudentId());
            user.setRole(Constant.ROLE_STUDENT);
            userMapper.insertUser(user);
            studentMapper.insertStudent(student);
            return R.success(Constant.REGISTER_SUCCESS);
        }
        catch (Exception e){
            return R.error(Constant.REGISTER_FAIL);
        }
    }
}
