package com.agiantii.backend.controller;

import com.agiantii.backend.common.R;
import com.agiantii.backend.common.TokenStore;
import com.agiantii.backend.mapper.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin")
@Api(tags = "P0-管理员端管理接口")
public class AdminApiController {

    @Resource
    private TokenStore tokenStore;

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private CourseSectionMapper courseSectionMapper;

    @Resource
    private DepartmentMapper departmentMapper;

    @Resource
    private MajorMapper majorMapper;

    @Resource
    private ClassroomMapper classroomMapper;

    @Resource
    private TeacherMapper teacherMapper;

    @Resource
    private StudentMapper studentMapper;

    @Resource
    private UserMapper userMapper;

    // ==================== Token 校验 ====================

    private Map<String, Object> requireAdmin(String authHeader) {
        Map<String, Object> info = tokenStore.resolve(authHeader);
        if (info == null) return null;
        if (!"admin".equals(info.get("role"))) return null;
        return info;
    }

    // ==================== 课程管理 ====================

    @GetMapping("/courses")
    @ApiOperation("管理员-查询课程列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<List<Map<String, Object>>> listCourses(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("GET /admin/courses");
        List<Map<String, Object>> list = courseMapper.selectAllForAdmin();
        return R.success(list, "成功");
    }

    @GetMapping("/courses/{id}")
    @ApiOperation("管理员-查询课程详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "课程ID", required = true, paramType = "path"),
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<Map<String, Object>> getCourse(
            @PathVariable Integer id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("GET /admin/courses/{}", id);
        Map<String, Object> course = courseMapper.selectByIdForAdmin(id);
        if (course == null) return R.error("课程不存在", 404);
        return R.success(course, "成功");
    }

    @PostMapping("/courses")
    @ApiOperation("管理员-新增课程")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<Map<String, Object>> addCourse(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("POST /admin/courses: {}", body);

        String courseCode = (String) body.get("courseCode");
        String name = (String) body.get("name");
        if (courseCode == null || courseCode.trim().isEmpty() || name == null || name.trim().isEmpty()) {
            return R.error("课程编码和名称不能为空", 400);
        }
        body.putIfAbsent("status", 1);
        body.putIfAbsent("credit", 3.0);
        body.putIfAbsent("totalHours", 48);
        body.putIfAbsent("courseType", "必修");
        body.putIfAbsent("departmentId", 1);

        // 校验 departmentId 存在
        Integer deptId = toInt(body.get("departmentId"));
        if (departmentMapper.selectById(deptId) == null) {
            return R.error("所属院系不存在", 400);
        }

        courseMapper.insertCourse(body);

        Integer newId = toInt(body.get("id"));
        Map<String, Object> course = courseMapper.selectByIdForAdmin(newId);
        return R.success(course, "课程新增成功");
    }

    @PutMapping("/courses/{id}")
    @ApiOperation("管理员-修改课程")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "课程ID", required = true, paramType = "path"),
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<Map<String, Object>> updateCourse(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("PUT /admin/courses/{}: {}", id, body);

        Map<String, Object> existing = courseMapper.selectByIdForAdmin(id);
        if (existing == null) return R.error("课程不存在", 404);

        // 校验 departmentId（如果前端传入）
        if (body.get("departmentId") != null) {
            Integer deptId = toInt(body.get("departmentId"));
            if (departmentMapper.selectById(deptId) == null) {
                return R.error("所属院系不存在", 400);
            }
        }

        // 只更新前端传入的字段，未传字段从已有记录回填
        for (Map.Entry<String, Object> entry : existing.entrySet()) {
            body.putIfAbsent(entry.getKey(), entry.getValue());
        }

        body.put("id", id);
        courseMapper.updateCourse(body);

        Map<String, Object> updated = courseMapper.selectByIdForAdmin(id);
        return R.success(updated, "课程修改成功");
    }

    @PutMapping("/courses/{id}/disable")
    @ApiOperation("管理员-停用课程")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "课程ID", required = true, paramType = "path"),
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<String> disableCourse(
            @PathVariable Integer id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("PUT /admin/courses/{}/disable", id);

        Map<String, Object> course = courseMapper.selectByIdForAdmin(id);
        if (course == null) return R.error("课程不存在", 404);

        int refSections = courseMapper.countReferencedSections(id);
        if (refSections > 0) {
            return R.error("该课程下存在 " + refSections + " 个运行中的教学班，无法停用", 400);
        }

        courseMapper.disableCourse(id);
        return R.success("课程已停用");
    }

    // ==================== 教学班管理 ====================

    @GetMapping("/sections")
    @ApiOperation("管理员-查询教学班列表（支持按课程/教师/状态筛选）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "courseId", value = "课程ID（可选）", paramType = "query"),
            @ApiImplicitParam(name = "teacherId", value = "教师ID（可选）", paramType = "query"),
            @ApiImplicitParam(name = "status", value = "状态 1=运行中 0=已关闭（可选）", paramType = "query"),
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<List<Map<String, Object>>> listSections(
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) Integer teacherId,
            @RequestParam(required = false) Integer status,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("GET /admin/sections: courseId={}, teacherId={}, status={}", courseId, teacherId, status);
        List<Map<String, Object>> list = courseSectionMapper.selectAllForAdmin(courseId, teacherId, status);
        return R.success(list, "成功");
    }

    @GetMapping("/sections/{id}")
    @ApiOperation("管理员-查询教学班详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "教学班ID", required = true, paramType = "path"),
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<Map<String, Object>> getSection(
            @PathVariable Integer id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("GET /admin/sections/{}", id);
        Map<String, Object> section = courseSectionMapper.selectByIdForAdmin(id);
        if (section == null) return R.error("教学班不存在", 404);
        return R.success(section, "成功");
    }

    @PostMapping("/sections")
    @ApiOperation("管理员-新增教学班")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<Map<String, Object>> addSection(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("POST /admin/sections: {}", body);

        // 基础校验
        String msg = validateSection(body, null);
        if (msg != null) return R.error(msg, 400);

        // 自动生成 sectionCode（前端未传时，格式: {courseCode}-{NN}）
        String sectionCode = (String) body.get("sectionCode");
        if (sectionCode == null || sectionCode.trim().isEmpty()) {
            Integer courseId = toInt(body.get("courseId"));
            Map<String, Object> course = courseMapper.selectByIdForAdmin(courseId);
            String courseCode = (String) course.get("courseCode");
            Integer maxSeq = courseSectionMapper.selectMaxSectionSeqByCourseId(courseId, courseCode);
            sectionCode = courseCode + "-" + String.format("%02d", maxSeq + 1);
            body.put("sectionCode", sectionCode);
        }

        body.putIfAbsent("status", 1);
        body.putIfAbsent("selectedCount", 0);
        courseSectionMapper.insertSection(body);

        Integer newId = toInt(body.get("sectionId"));
        Map<String, Object> section = courseSectionMapper.selectByIdForAdmin(newId);
        return R.success(section, "教学班新增成功");
    }

    @PutMapping("/sections/{id}")
    @ApiOperation("管理员-修改教学班")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "教学班ID", required = true, paramType = "path"),
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<Map<String, Object>> updateSection(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("PUT /admin/sections/{}: {}", id, body);

        Map<String, Object> existing = courseSectionMapper.selectByIdForAdmin(id);
        if (existing == null) return R.error("教学班不存在", 404);

        String msg = validateSection(body, existing);
        if (msg != null) return R.error(msg, 400);

        // 只更新前端传入的字段，未传字段从已有记录回填
        for (Map.Entry<String, Object> entry : existing.entrySet()) {
            body.putIfAbsent(entry.getKey(), entry.getValue());
        }

        body.put("sectionId", id);
        courseSectionMapper.updateSection(body);

        Map<String, Object> updated = courseSectionMapper.selectByIdForAdmin(id);
        return R.success(updated, "教学班修改成功");
    }

    @PutMapping("/sections/{id}/close")
    @ApiOperation("管理员-关闭教学班")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "教学班ID", required = true, paramType = "path"),
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<String> closeSection(
            @PathVariable Integer id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("PUT /admin/sections/{}/close", id);

        Map<String, Object> section = courseSectionMapper.selectByIdForAdmin(id);
        if (section == null) return R.error("教学班不存在", 404);

        courseSectionMapper.closeSection(id);
        return R.success("教学班已关闭");
    }

    @DeleteMapping("/sections/{id}")
    @ApiOperation("管理员-删除教学班（仅当无选课记录时）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "教学班ID", required = true, paramType = "path"),
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<String> deleteSection(
            @PathVariable Integer id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("DELETE /admin/sections/{}", id);

        Map<String, Object> section = courseSectionMapper.selectByIdForAdmin(id);
        if (section == null) return R.error("教学班不存在", 404);

        int enrolled = courseSectionMapper.countEnrollments(id);
        if (enrolled > 0) {
            return R.error("该教学班存在 " + enrolled + " 条活跃选课记录，无法删除", 400);
        }

        courseSectionMapper.deleteSectionById(id);
        return R.success("教学班已删除");
    }

    private String validateSection(Map<String, Object> body, Map<String, Object> existing) {
        // courseId、teacherId、classroomId 必须存在
        Object courseIdObj = body.getOrDefault("courseId", existing != null ? existing.get("courseId") : null);
        Object teacherIdObj = body.getOrDefault("teacherId", existing != null ? existing.get("teacherId") : null);
        Object classroomIdObj = body.getOrDefault("classroomId", existing != null ? existing.get("classroomId") : null);

        if (courseIdObj == null) return "课程ID不能为空";
        if (teacherIdObj == null) return "教师ID不能为空";
        if (classroomIdObj == null) return "教室ID不能为空";

        Integer courseId = toInt(courseIdObj);
        Integer teacherId = toInt(teacherIdObj);
        Integer classroomId = toInt(classroomIdObj);

        Map<String, Object> course = courseMapper.selectByIdForAdmin(courseId);
        if (course == null) return "课程不存在";
        if (!Integer.valueOf(1).equals(course.get("status"))) return "课程已停用，无法创建教学班";

        Map<String, Object> teacher = teacherMapper.selectByIdForAdmin(teacherId);
        if (teacher == null) return "教师不存在";
        if (!Integer.valueOf(1).equals(teacher.get("status"))) return "教师已停用，无法创建教学班";

        com.agiantii.backend.pojo.Classroom classroom = classroomMapper.selectById(classroomId);
        if (classroom == null) return "教室不存在";
        if (!Integer.valueOf(1).equals(classroom.getStatus())) return "教室已停用，无法创建教学班";

        // capacity > 0
        Object capObj = body.getOrDefault("capacityLimit", existing != null ? existing.get("capacityLimit") : null);
        if (capObj != null) {
            int cap = toInt(capObj);
            if (cap <= 0) return "容量必须大于0";
            // capacity >= selectedCount
            int selCount = existing != null ? toInt(existing.get("selectedCount")) : 0;
            if (cap < selCount) return "容量不能小于已选人数(" + selCount + ")";
        }

        return null;
    }

    private int toInt(Object obj) {
        if (obj instanceof Integer) return (Integer) obj;
        if (obj instanceof Number) return ((Number) obj).intValue();
        if (obj instanceof String) return Integer.parseInt((String) obj);
        return 0;
    }

    // ==================== 院系管理 ====================

    @GetMapping("/departments")
    @ApiOperation("管理员-查询院系列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<List<com.agiantii.backend.pojo.Department>> listDepartments(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("GET /admin/departments");
        return R.success(departmentMapper.selectAll(), "成功");
    }

    @GetMapping("/departments/{id}")
    @ApiOperation("管理员-查询院系详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "院系ID", required = true, paramType = "path"),
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<com.agiantii.backend.pojo.Department> getDepartment(
            @PathVariable Integer id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("GET /admin/departments/{}", id);
        com.agiantii.backend.pojo.Department dept = departmentMapper.selectById(id);
        if (dept == null) return R.error("院系不存在", 404);
        return R.success(dept, "成功");
    }

    @PostMapping("/departments")
    @ApiOperation("管理员-新增院系")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<com.agiantii.backend.pojo.Department> addDepartment(
            @RequestBody com.agiantii.backend.pojo.Department department,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("POST /admin/departments: {}", department);
        if (department.getDepartmentName() == null || department.getDepartmentName().trim().isEmpty()) {
            return R.error("院系名称不能为空", 400);
        }
        if (department.getStatus() == null) department.setStatus(1);
        departmentMapper.insert(department);
        return R.success(department, "院系新增成功");
    }

    @PutMapping("/departments/{id}")
    @ApiOperation("管理员-修改院系")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "院系ID", required = true, paramType = "path"),
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<com.agiantii.backend.pojo.Department> updateDepartment(
            @PathVariable Integer id,
            @RequestBody com.agiantii.backend.pojo.Department department,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("PUT /admin/departments/{}: {}", id, department);
        if (departmentMapper.selectById(id) == null) return R.error("院系不存在", 404);

        // 只更新前端传入的字段，未传字段从已有记录回填
        com.agiantii.backend.pojo.Department existing = departmentMapper.selectById(id);
        if (department.getDepartmentCode() == null) department.setDepartmentCode(existing.getDepartmentCode());
        if (department.getDepartmentName() == null) department.setDepartmentName(existing.getDepartmentName());
        if (department.getOfficePhone() == null) department.setOfficePhone(existing.getOfficePhone());

        department.setDepartmentId(id);
        departmentMapper.update(department);
        return R.success(departmentMapper.selectById(id), "院系修改成功");
    }

    @PutMapping("/departments/{id}/disable")
    @ApiOperation("管理员-停用院系")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "院系ID", required = true, paramType = "path"),
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<String> disableDepartment(
            @PathVariable Integer id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("PUT /admin/departments/{}/disable", id);
        if (departmentMapper.selectById(id) == null) return R.error("院系不存在", 404);

        int refMajors = departmentMapper.countReferencedMajors(id);
        int refTeachers = departmentMapper.countReferencedTeachers(id);
        if (refMajors > 0 || refTeachers > 0) {
            return R.error("该院系下存在 " + refMajors + " 个专业、" + refTeachers + " 名教师，无法停用", 400);
        }

        departmentMapper.disable(id);
        return R.success("院系已停用");
    }

    // ==================== 专业管理 ====================

    @GetMapping("/majors")
    @ApiOperation("管理员-查询专业列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<List<Map<String, Object>>> listMajors(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("GET /admin/majors");
        return R.success(majorMapper.selectAllWithDept(), "成功");
    }

    @GetMapping("/majors/{id}")
    @ApiOperation("管理员-查询专业详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "专业ID", required = true, paramType = "path"),
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<Map<String, Object>> getMajor(
            @PathVariable Integer id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("GET /admin/majors/{}", id);
        Map<String, Object> major = majorMapper.selectByIdWithDept(id);
        if (major == null) return R.error("专业不存在", 404);
        return R.success(major, "成功");
    }

    @PostMapping("/majors")
    @ApiOperation("管理员-新增专业")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<com.agiantii.backend.pojo.Major> addMajor(
            @RequestBody com.agiantii.backend.pojo.Major major,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("POST /admin/majors: {}", major);
        if (major.getMajorName() == null || major.getMajorName().trim().isEmpty()) {
            return R.error("专业名称不能为空", 400);
        }
        if (major.getDepartmentId() == null) {
            return R.error("所属院系不能为空", 400);
        }
        if (departmentMapper.selectById(major.getDepartmentId()) == null) {
            return R.error("所属院系不存在", 400);
        }
        if (major.getStatus() == null) major.setStatus(1);
        majorMapper.insert(major);
        return R.success(major, "专业新增成功");
    }

    @PutMapping("/majors/{id}")
    @ApiOperation("管理员-修改专业")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "专业ID", required = true, paramType = "path"),
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<Map<String, Object>> updateMajor(
            @PathVariable Integer id,
            @RequestBody com.agiantii.backend.pojo.Major major,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("PUT /admin/majors/{}: {}", id, major);
        if (majorMapper.selectById(id) == null) return R.error("专业不存在", 404);
        if (major.getDepartmentId() != null && departmentMapper.selectById(major.getDepartmentId()) == null) {
            return R.error("所属院系不存在", 400);
        }

        // 只更新前端传入的字段，未传字段从已有记录回填
        com.agiantii.backend.pojo.Major existingMajor = majorMapper.selectById(id);
        if (major.getDepartmentId() == null) major.setDepartmentId(existingMajor.getDepartmentId());
        if (major.getMajorCode() == null) major.setMajorCode(existingMajor.getMajorCode());
        if (major.getMajorName() == null) major.setMajorName(existingMajor.getMajorName());

        major.setMajorId(id);
        majorMapper.update(major);
        return R.success(majorMapper.selectByIdWithDept(id), "专业修改成功");
    }

    @PutMapping("/majors/{id}/disable")
    @ApiOperation("管理员-停用专业")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "专业ID", required = true, paramType = "path"),
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<String> disableMajor(
            @PathVariable Integer id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("PUT /admin/majors/{}/disable", id);
        if (majorMapper.selectById(id) == null) return R.error("专业不存在", 404);

        int refStudents = majorMapper.countReferencedStudents(id);
        if (refStudents > 0) {
            return R.error("该专业下存在 " + refStudents + " 名学生，无法停用", 400);
        }

        majorMapper.disable(id);
        return R.success("专业已停用");
    }

    // ==================== 教室管理 ====================

    @GetMapping("/classrooms")
    @ApiOperation("管理员-查询教室列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<List<com.agiantii.backend.pojo.Classroom>> listClassrooms(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("GET /admin/classrooms");
        return R.success(classroomMapper.selectAll(), "成功");
    }

    @GetMapping("/classrooms/{id}")
    @ApiOperation("管理员-查询教室详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "教室ID", required = true, paramType = "path"),
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<com.agiantii.backend.pojo.Classroom> getClassroom(
            @PathVariable Integer id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("GET /admin/classrooms/{}", id);
        com.agiantii.backend.pojo.Classroom room = classroomMapper.selectById(id);
        if (room == null) return R.error("教室不存在", 404);
        return R.success(room, "成功");
    }

    @PostMapping("/classrooms")
    @ApiOperation("管理员-新增教室")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<com.agiantii.backend.pojo.Classroom> addClassroom(
            @RequestBody com.agiantii.backend.pojo.Classroom classroom,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("POST /admin/classrooms: {}", classroom);
        if (classroom.getBuilding() == null || classroom.getBuilding().trim().isEmpty()) {
            return R.error("教学楼不能为空", 400);
        }
        if (classroom.getRoomNo() == null || classroom.getRoomNo().trim().isEmpty()) {
            return R.error("教室编号不能为空", 400);
        }
        if (classroom.getStatus() == null) classroom.setStatus(1);
        if (classroom.getCapacity() == null) classroom.setCapacity(50);
        classroomMapper.insert(classroom);
        return R.success(classroom, "教室新增成功");
    }

    @PutMapping("/classrooms/{id}")
    @ApiOperation("管理员-修改教室")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "教室ID", required = true, paramType = "path"),
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<com.agiantii.backend.pojo.Classroom> updateClassroom(
            @PathVariable Integer id,
            @RequestBody com.agiantii.backend.pojo.Classroom classroom,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("PUT /admin/classrooms/{}: {}", id, classroom);
        if (classroomMapper.selectById(id) == null) return R.error("教室不存在", 404);

        // 只更新前端传入的字段，未传字段从已有记录回填
        com.agiantii.backend.pojo.Classroom existingRoom = classroomMapper.selectById(id);
        if (classroom.getBuilding() == null) classroom.setBuilding(existingRoom.getBuilding());
        if (classroom.getRoomNo() == null) classroom.setRoomNo(existingRoom.getRoomNo());
        if (classroom.getCapacity() == null) classroom.setCapacity(existingRoom.getCapacity());
        if (classroom.getRemark() == null) classroom.setRemark(existingRoom.getRemark());

        classroom.setClassroomId(id);
        classroomMapper.update(classroom);
        return R.success(classroomMapper.selectById(id), "教室修改成功");
    }

    @PutMapping("/classrooms/{id}/disable")
    @ApiOperation("管理员-停用教室")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "教室ID", required = true, paramType = "path"),
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<String> disableClassroom(
            @PathVariable Integer id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("PUT /admin/classrooms/{}/disable", id);
        if (classroomMapper.selectById(id) == null) return R.error("教室不存在", 404);

        int refSections = classroomMapper.countReferencedSections(id);
        if (refSections > 0) {
            return R.error("该教室下存在 " + refSections + " 个运行中的教学班，无法停用", 400);
        }

        classroomMapper.disable(id);
        return R.success("教室已停用");
    }

    // ==================== 下拉数据接口 ====================

    @GetMapping("/courses/options")
    @ApiOperation("管理员-课程下拉列表（用于新增教学班表单）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<List<Map<String, Object>>> courseOptions(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("GET /admin/courses/options");
        return R.success(courseMapper.selectActiveForOptions(), "成功");
    }

    @GetMapping("/teachers/options")
    @ApiOperation("管理员-教师下拉列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<List<com.agiantii.backend.pojo.Teacher>> teacherOptions(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("GET /admin/teachers/options");
        return R.success(teacherMapper.selectAll(), "成功");
    }

    @GetMapping("/classrooms/options")
    @ApiOperation("管理员-教室下拉列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<List<com.agiantii.backend.pojo.Classroom>> classroomOptions(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("GET /admin/classrooms/options");
        return R.success(classroomMapper.selectActiveAll(), "成功");
    }

    @GetMapping("/majors/options")
    @ApiOperation("管理员-专业下拉列表（用于新增学生表单）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<List<com.agiantii.backend.pojo.Major>> majorOptions(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("GET /admin/majors/options");
        return R.success(majorMapper.selectActiveAll(), "成功");
    }

    @GetMapping("/departments/options")
    @ApiOperation("管理员-院系下拉列表（用于新增教师表单）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<List<com.agiantii.backend.pojo.Department>> departmentOptions(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("GET /admin/departments/options");
        return R.success(departmentMapper.selectActiveAll(), "成功");
    }

    // ==================== 学生管理 ====================

    @GetMapping("/students")
    @ApiOperation("管理员-查询学生列表（支持按专业/状态筛选）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "majorId", value = "专业ID（可选）", paramType = "query"),
            @ApiImplicitParam(name = "status", value = "状态 1=正常 0=休学/退学（可选）", paramType = "query"),
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<List<Map<String, Object>>> listStudents(
            @RequestParam(required = false) Integer majorId,
            @RequestParam(required = false) Integer status,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("GET /admin/students: majorId={}, status={}", majorId, status);
        List<Map<String, Object>> list = studentMapper.selectAllForAdmin(majorId, status);
        return R.success(list, "成功");
    }

    @GetMapping("/students/{id}")
    @ApiOperation("管理员-查询学生详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "学生ID", required = true, paramType = "path"),
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<Map<String, Object>> getStudent(
            @PathVariable Integer id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("GET /admin/students/{}", id);
        Map<String, Object> student = studentMapper.selectByIdForAdmin(id);
        if (student == null) return R.error("学生不存在", 404);
        return R.success(student, "成功");
    }

    @Transactional
    @GetMapping("/students/next-no")
    @ApiOperation("管理员-预览下一个学号")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "majorId", value = "专业ID", required = true, paramType = "query"),
            @ApiImplicitParam(name = "year", value = "入学年份", paramType = "query"),
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<Map<String, Object>> nextStudentNo(
            @RequestParam Integer majorId,
            @RequestParam(defaultValue = "2025") Integer year,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        Map<String, Object> major = majorMapper.selectByIdWithDept(majorId);
        if (major == null) return R.error("专业不存在", 400);
        String deptCode = (String) major.get("departmentCode");
        if (deptCode == null) {
            Integer deptId = toInt(major.get("departmentId"));
            com.agiantii.backend.pojo.Department dept = departmentMapper.selectById(deptId);
            if (dept == null) return R.error("该专业未关联院系", 400);
            deptCode = dept.getDepartmentCode();
        }
        String prefix = "S" + deptCode + year;
        Integer maxSeq = studentMapper.selectMaxSeqByPrefix(prefix);
        int seq = (maxSeq == null ? 0 : maxSeq) + 1;
        String studentNo = prefix + String.format("%04d", seq);

        Map<String, Object> result = new HashMap<>();
        result.put("studentNo", studentNo);
        result.put("username", studentNo);
        result.put("password", "123456");
        return R.success(result, "成功");
    }

    @Transactional
    @PostMapping("/students")
    @ApiOperation("管理员-新增学生（自动创建登录账号）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<Map<String, Object>> addStudent(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("POST /admin/students: {}", body);

        // 基础校验
        String studentName = (String) body.get("studentName");
        if (studentName == null || studentName.trim().isEmpty()) return R.error("姓名不能为空", 400);
        if (body.get("majorId") == null) return R.error("专业不能为空", 400);
        Integer majorId = toInt(body.get("majorId"));
        Map<String, Object> major = majorMapper.selectByIdWithDept(majorId);
        if (major == null) return R.error("专业不存在", 400);

        // 获取 department_code，用于生成学号
        String deptCode = (String) major.get("departmentCode");
        if (deptCode == null) {
            // fallback: 直接查院系
            Integer deptId = toInt(major.get("departmentId"));
            com.agiantii.backend.pojo.Department dept = departmentMapper.selectById(deptId);
            if (dept == null) return R.error("该专业未关联院系，无法生成学号", 400);
            deptCode = dept.getDepartmentCode();
        }

        body.putIfAbsent("enrollmentYear", 2025);
        int year = toInt(body.get("enrollmentYear"));

        // 生成学号（如果前端未传）
        String studentNo = (String) body.get("studentNo");
        if (studentNo == null || studentNo.trim().isEmpty()) {
            String prefix = "S" + deptCode + year;
            Integer maxSeq = studentMapper.selectMaxSeqByPrefix(prefix);
            int seq = (maxSeq == null ? 0 : maxSeq) + 1;
            studentNo = prefix + String.format("%04d", seq);
            body.put("studentNo", studentNo);
        }

        // 创建 t_user 登录账号
        Map<String, Object> user = new HashMap<>();
        user.put("username", body.getOrDefault("username", studentNo));
        user.put("password", body.getOrDefault("password", "123456"));
        user.put("role", "student");
        user.put("status", 1);
        userMapper.insertUserAdmin(user);
        Integer userId = toInt(user.get("userId"));
        if (userId == 0) return R.error("创建登录账号失败", 500);

        // 插入 t_student
        body.put("userId", userId);
        body.putIfAbsent("status", 1);
        body.putIfAbsent("gender", "M");
        studentMapper.insertStudentAdmin(body);

        Integer newId = toInt(body.get("studentId"));
        Map<String, Object> student = studentMapper.selectByIdForAdmin(newId);
        student.put("username", user.get("username"));
        return R.success(student, "学生新增成功");
    }

    @PutMapping("/students/{id}")
    @ApiOperation("管理员-修改学生")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "学生ID", required = true, paramType = "path"),
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<Map<String, Object>> updateStudent(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("PUT /admin/students/{}: {}", id, body);

        Map<String, Object> existing = studentMapper.selectByIdForAdmin(id);
        if (existing == null) return R.error("学生不存在", 404);

        if (body.get("majorId") != null && majorMapper.selectById(toInt(body.get("majorId"))) == null) {
            return R.error("专业不存在", 400);
        }

        // 只更新前端传入的字段
        for (Map.Entry<String, Object> entry : existing.entrySet()) {
            body.putIfAbsent(entry.getKey(), entry.getValue());
        }

        body.put("studentId", id);
        studentMapper.updateStudentAdmin(body);

        Map<String, Object> updated = studentMapper.selectByIdForAdmin(id);
        return R.success(updated, "学生修改成功");
    }

    @Transactional
    @PutMapping("/students/{id}/disable")
    @ApiOperation("管理员-停用学生")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "学生ID", required = true, paramType = "path"),
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<String> disableStudent(
            @PathVariable Integer id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("PUT /admin/students/{}/disable", id);

        if (studentMapper.selectByIdForAdmin(id) == null) return R.error("学生不存在", 404);

        int refEnrollments = studentMapper.countReferencedEnrollments(id);
        if (refEnrollments > 0) {
            return R.error("该学生存在 " + refEnrollments + " 条活跃选课记录，无法停用", 400);
        }

        studentMapper.disableStudent(id);
        Integer userId = studentMapper.selectUserIdByStudentId(id);
        if (userId != null) userMapper.disableUser(userId);
        return R.success("学生已停用");
    }

    @Transactional
    @DeleteMapping("/students/{id}")
    @ApiOperation("管理员-删除学生（仅当无选课记录时）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "学生ID", required = true, paramType = "path"),
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<String> deleteStudent(
            @PathVariable Integer id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("DELETE /admin/students/{}", id);

        if (studentMapper.selectByIdForAdmin(id) == null) return R.error("学生不存在", 404);

        int refEnrollments = studentMapper.countReferencedEnrollments(id);
        if (refEnrollments > 0) {
            return R.error("该学生存在 " + refEnrollments + " 条活跃选课记录，无法删除，请先停用", 400);
        }

        Integer userId = studentMapper.selectUserIdByStudentId(id);
        studentMapper.deleteStudentById(id);
        if (userId != null) userMapper.deleteUserNyId(userId);
        return R.success("学生已删除");
    }

    // ==================== 教师管理 ====================

    @GetMapping("/teachers")
    @ApiOperation("管理员-查询教师列表（支持按院系/状态筛选）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "departmentId", value = "院系ID（可选）", paramType = "query"),
            @ApiImplicitParam(name = "status", value = "状态 1=正常 0=停用（可选）", paramType = "query"),
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<List<Map<String, Object>>> listTeachers(
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) Integer status,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("GET /admin/teachers: departmentId={}, status={}", departmentId, status);
        List<Map<String, Object>> list = teacherMapper.selectAllForAdmin(departmentId, status);
        return R.success(list, "成功");
    }

    @GetMapping("/teachers/{id}")
    @ApiOperation("管理员-查询教师详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "教师ID", required = true, paramType = "path"),
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<Map<String, Object>> getTeacher(
            @PathVariable Integer id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("GET /admin/teachers/{}", id);
        Map<String, Object> teacher = teacherMapper.selectByIdForAdmin(id);
        if (teacher == null) return R.error("教师不存在", 404);
        return R.success(teacher, "成功");
    }

    @Transactional
    @GetMapping("/teachers/next-no")
    @ApiOperation("管理员-预览下一个工号")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "departmentId", value = "院系ID", required = true, paramType = "query"),
            @ApiImplicitParam(name = "year", value = "年份", paramType = "query"),
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<Map<String, Object>> nextTeacherNo(
            @RequestParam Integer departmentId,
            @RequestParam(defaultValue = "2025") Integer year,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        com.agiantii.backend.pojo.Department dept = departmentMapper.selectById(departmentId);
        if (dept == null) return R.error("院系不存在", 400);
        String prefix = "T" + dept.getDepartmentCode() + year;
        Integer maxSeq = teacherMapper.selectMaxSeqByPrefix(prefix);
        int seq = (maxSeq == null ? 0 : maxSeq) + 1;
        String teacherNo = prefix + String.format("%04d", seq);

        Map<String, Object> result = new HashMap<>();
        result.put("teacherNo", teacherNo);
        result.put("username", teacherNo);
        result.put("password", "123456");
        return R.success(result, "成功");
    }

    @Transactional
    @PostMapping("/teachers")
    @ApiOperation("管理员-新增教师（自动创建登录账号）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<Map<String, Object>> addTeacher(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("POST /admin/teachers: {}", body);

        // 基础校验
        String teacherName = (String) body.get("teacherName");
        if (teacherName == null || teacherName.trim().isEmpty()) return R.error("姓名不能为空", 400);
        if (body.get("departmentId") == null) return R.error("院系不能为空", 400);
        Integer departmentId = toInt(body.get("departmentId"));
        com.agiantii.backend.pojo.Department dept = departmentMapper.selectById(departmentId);
        if (dept == null) return R.error("院系不存在", 400);
        String deptCode = dept.getDepartmentCode();

        body.putIfAbsent("year", 2025);
        int year = toInt(body.get("year"));

        // 生成工号（如果前端未传）
        String teacherNo = (String) body.get("teacherNo");
        if (teacherNo == null || teacherNo.trim().isEmpty()) {
            String prefix = "T" + deptCode + year;
            Integer maxSeq = teacherMapper.selectMaxSeqByPrefix(prefix);
            int seq = (maxSeq == null ? 0 : maxSeq) + 1;
            teacherNo = prefix + String.format("%04d", seq);
            body.put("teacherNo", teacherNo);
        }

        // 创建 t_user 登录账号
        Map<String, Object> user = new HashMap<>();
        user.put("username", body.getOrDefault("username", teacherNo));
        user.put("password", body.getOrDefault("password", "123456"));
        user.put("role", "teacher");
        user.put("status", 1);
        userMapper.insertUserAdmin(user);
        Integer userId = toInt(user.get("userId"));
        if (userId == 0) return R.error("创建登录账号失败", 500);

        // 插入 t_teacher
        body.put("userId", userId);
        body.putIfAbsent("status", 1);
        teacherMapper.insertTeacherAdmin(body);

        Integer newId = toInt(body.get("teacherId"));
        Map<String, Object> teacher = teacherMapper.selectByIdForAdmin(newId);
        teacher.put("username", user.get("username"));
        return R.success(teacher, "教师新增成功");
    }

    @PutMapping("/teachers/{id}")
    @ApiOperation("管理员-修改教师")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "教师ID", required = true, paramType = "path"),
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<Map<String, Object>> updateTeacher(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("PUT /admin/teachers/{}: {}", id, body);

        Map<String, Object> existing = teacherMapper.selectByIdForAdmin(id);
        if (existing == null) return R.error("教师不存在", 404);

        if (body.get("departmentId") != null && departmentMapper.selectById(toInt(body.get("departmentId"))) == null) {
            return R.error("院系不存在", 400);
        }

        // 只更新前端传入的字段
        for (Map.Entry<String, Object> entry : existing.entrySet()) {
            body.putIfAbsent(entry.getKey(), entry.getValue());
        }

        body.put("teacherId", id);
        teacherMapper.updateTeacherAdmin(body);

        Map<String, Object> updated = teacherMapper.selectByIdForAdmin(id);
        return R.success(updated, "教师修改成功");
    }

    @Transactional
    @PutMapping("/teachers/{id}/disable")
    @ApiOperation("管理员-停用教师")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "教师ID", required = true, paramType = "path"),
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<String> disableTeacher(
            @PathVariable Integer id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("PUT /admin/teachers/{}/disable", id);

        if (teacherMapper.selectByIdForAdmin(id) == null) return R.error("教师不存在", 404);

        int refSections = teacherMapper.countReferencedSections(id);
        if (refSections > 0) {
            return R.error("该教师存在 " + refSections + " 个运行中的教学班，无法停用", 400);
        }

        teacherMapper.disableTeacher(id);
        Integer userId = teacherMapper.selectUserIdByTeacherId(id);
        if (userId != null) userMapper.disableUser(userId);
        return R.success("教师已停用");
    }

    @Transactional
    @DeleteMapping("/teachers/{id}")
    @ApiOperation("管理员-删除教师（仅当无教学班时）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "教师ID", required = true, paramType = "path"),
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<String> deleteTeacher(
            @PathVariable Integer id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return R.error("未登录或非管理员", 401);
        log.info("DELETE /admin/teachers/{}", id);

        if (teacherMapper.selectByIdForAdmin(id) == null) return R.error("教师不存在", 404);

        int refSections = teacherMapper.countReferencedSections(id);
        if (refSections > 0) {
            return R.error("该教师存在 " + refSections + " 个运行中的教学班，无法删除，请先停用", 400);
        }

        Integer userId = teacherMapper.selectUserIdByTeacherId(id);
        teacherMapper.delete(id);
        if (userId != null) userMapper.deleteUserNyId(userId);
        return R.success("教师已删除");
    }
}
