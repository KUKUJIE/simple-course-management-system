package com.agiantii.backend.controller;

import com.agiantii.backend.common.R;
import com.agiantii.backend.mapper.CourseMapper;
import com.agiantii.backend.mapper.UserMapper;
import com.agiantii.backend.pojo.Course;
import com.agiantii.backend.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CourseMapper courseMapper;

    // Users
    @GetMapping("/users")
    public R<List<User>> listUsers() {
        List<User> users = userMapper.selectList(null);
        return R.success(users, "ok");
    }

    @PostMapping("/users")
    public R<User> createUser(@RequestBody Map<String, Object> body) {
        String username = (String) body.get("username");
        String password = (String) body.get("password");
        String role = (String) body.get("role");
        if (username == null || password == null || role == null) return R.error("username/password/role required",400);
        User u = new User();
        u.setUsername(username);
        u.setPassword(password);
        u.setRole(role);
        u.setStatus(1);
        userMapper.insert(u);
        return R.success(u, "created");
    }

    @PutMapping("/users/{id}")
    public R<User> updateUser(@PathVariable("id") Integer id, @RequestBody Map<String, Object> body) {
        User u = userMapper.selectById(id);
        if (u == null) return R.error("user not found",404);
        if (body.containsKey("password")) u.setPassword((String) body.get("password"));
        if (body.containsKey("role")) u.setRole((String) body.get("role"));
        if (body.containsKey("status")) u.setStatus((Integer) body.get("status"));
        userMapper.updateById(u);
        return R.success(u, "updated");
    }

    @DeleteMapping("/users/{id}")
    public R<String> deleteUser(@PathVariable("id") Integer id) {
        int d = userMapper.deleteById(id);
        if (d > 0) return R.success(null, "deleted");
        return R.error("not found",404);
    }

    // Courses
    @GetMapping("/courses")
    public R<List<Course>> listCourses() {
        List<Course> list = courseMapper.selectList(null);
        return R.success(list, "ok");
    }

    @PostMapping("/courses")
    public R<Course> createCourse(@RequestBody Course c) {
        c.setCreatedAt(LocalDateTime.now());
        courseMapper.insert(c);
        return R.success(c, "created");
    }

    @PutMapping("/courses/{id}")
    public R<Course> updateCourse(@PathVariable("id") Long id, @RequestBody Course body) {
        Course c = courseMapper.selectById(id);
        if (c == null) return R.error("course not found",404);
        c.setName(body.getName());
        c.setCourseCode(body.getCourseCode());
        c.setDescription(body.getDescription());
        c.setTeacherId(body.getTeacherId());
        courseMapper.updateById(c);
        return R.success(c, "updated");
    }

    @DeleteMapping("/courses/{id}")
    public R<String> deleteCourse(@PathVariable("id") Long id) {
        int d = courseMapper.deleteById(id);
        if (d > 0) return R.success(null, "deleted");
        return R.error("not found",404);
    }
}
