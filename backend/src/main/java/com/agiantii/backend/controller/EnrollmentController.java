package com.agiantii.backend.controller;

import com.agiantii.backend.common.R;
import com.agiantii.backend.common.TokenStore;
import com.agiantii.backend.mapper.CourseSectionMapper;
import com.agiantii.backend.mapper.EnrollmentMapper;
import com.agiantii.backend.pojo.Enrollment;
import com.agiantii.backend.pojo.vo.CourseSectionVo;
import com.agiantii.backend.pojo.vo.EnrollmentVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@Api(tags = "P0-选课管理")
public class EnrollmentController {

    @Resource
    private EnrollmentMapper enrollmentMapper;

    @Resource
    private CourseSectionMapper courseSectionMapper;

    @Resource
    private TokenStore tokenStore;

    @PostMapping("/enrollments")
    @ApiOperation("学生选课")
    public R<Map<String, Object>> enroll(@RequestBody Map<String, Object> body) {
        Integer studentId = (Integer) body.get("studentId");
        Integer sectionId = (Integer) body.get("sectionId");
        String source = (String) body.getOrDefault("source", "自主选课");

        if (studentId == null || sectionId == null) {
            return R.error("studentId和sectionId不能为空", 400);
        }

        log.info("POST /enrollments: studentId={}, sectionId={}, source={}", studentId, sectionId, source);

        // 检查教学班是否存在
        CourseSectionVo section = courseSectionMapper.selectSectionDetail(sectionId);
        if (section == null) {
            return R.error("教学班不存在", 404);
        }

        // 检查教学班是否已关闭
        if (section.getStatus() != null && section.getStatus() == 0) {
            return R.error("该教学班已关闭，无法选课", 400);
        }

        // 检查是否已选（正常状态）
        Enrollment existing = enrollmentMapper.selectByStudentAndSection(studentId, sectionId);
        if (existing != null && existing.getStatus() == 1) {
            Map<String, Object> data = new HashMap<>();
            data.put("result", 2);
            data.put("enrollmentId", existing.getEnrollmentId());
            return R.error(data, "已选过该课程");
        }

        // 检查容量
        if (section.getCapacityLimit() > 0 && section.getSelectedCount() >= section.getCapacityLimit()) {
            Map<String, Object> data = new HashMap<>();
            data.put("result", 0);
            return R.error(data, "容量已满");
        }

        try {
            Map<String, Object> data = new HashMap<>();
            Integer enrollmentId;

            if (existing != null && existing.getStatus() == 0) {
                // 退课后重选：恢复旧记录
                int updated = enrollmentMapper.restoreEnrollment(studentId, sectionId, source);
                enrollmentId = existing.getEnrollmentId();
                data.put("result", updated > 0 ? 1 : -1);
            } else {
                // 首次选课：插入新记录
                Enrollment enrollment = new Enrollment();
                enrollment.setStudentId(studentId);
                enrollment.setSectionId(sectionId);
                enrollment.setSource(source);
                enrollmentMapper.insert(enrollment);
                enrollmentId = enrollment.getEnrollmentId();
                data.put("result", 1);
            }

            data.put("enrollmentId", enrollmentId);
            return R.success(data, "选课成功");
        } catch (Exception e) {
            log.error("enroll error: {}", e.getMessage(), e);
            Map<String, Object> data = new HashMap<>();
            data.put("result", -1);
            return R.error(data, "系统错误");
        }
    }

    @DeleteMapping("/enrollments/{id}")
    @ApiOperation("学生退课")
    @ApiImplicitParam(name = "id", value = "选课记录ID (enrollment_id)", required = true, paramType = "path")
    public R<Map<String, Object>> drop(@PathVariable Integer id) {
        log.info("DELETE /enrollments/{}", id);

        Enrollment enrollment = enrollmentMapper.selectById(id);
        if (enrollment == null || enrollment.getStatus() == 0) {
            Map<String, Object> data = new HashMap<>();
            data.put("result", 0);
            return R.error(data, "记录不存在或已退课");
        }

        try {
            int updated = enrollmentMapper.updateStatusToDropped(id);
            Map<String, Object> data = new HashMap<>();
            data.put("result", updated > 0 ? 1 : 0);

            if (updated > 0) {
                return R.success(data, "退课成功");
            } else {
                return R.error(data, "退课失败");
            }
        } catch (Exception e) {
            log.error("drop error: {}", e.getMessage(), e);
            Map<String, Object> data = new HashMap<>();
            data.put("result", -1);
            return R.error(data, "系统错误");
        }
    }

    @GetMapping("/students/{studentId}/enrollments")
    @ApiOperation("查询学生已选教学班列表（我的课程）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "studentId", value = "学生ID", required = true, paramType = "path"),
            @ApiImplicitParam(name = "semester", value = "学期筛选", paramType = "query"),
            @ApiImplicitParam(name = "status", value = "选课状态 1=在读 0=已退课", defaultValue = "1", paramType = "query")
    })
    public R<List<EnrollmentVo>> myEnrollments(
            @PathVariable Integer studentId,
            @RequestParam(required = false) String semester,
            @RequestParam(defaultValue = "1") Integer status) {
        log.info("GET /students/{}/enrollments: semester={}, status={}", studentId, semester, status);
        List<EnrollmentVo> list = enrollmentMapper.selectByStudentId(studentId, semester, status);
        return R.success(list, "成功");
    }

    @GetMapping("/teachers/{teacherId}/sections")
    @ApiOperation("教师查看自己授课的教学班列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "teacherId", value = "教师ID", required = true, paramType = "path"),
            @ApiImplicitParam(name = "Authorization", value = "Bearer {token}", required = true, dataType = "string", paramType = "header")
    })
    public R<List<CourseSectionVo>> teacherSections(
            @PathVariable Integer teacherId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // Token 身份校验：当前登录教师 entityId 必须与路径 teacherId 一致
        Map<String, Object> tokenInfo = tokenStore.validateOwner(authHeader, teacherId);
        if (tokenInfo == null) {
            return R.error("未登录或无权查看该教师的教学班", 401);
        }

        log.info("GET /teachers/{}/sections: authenticated teacherId={}", teacherId);
        List<CourseSectionVo> list = courseSectionMapper.selectByTeacherId(teacherId);
        return R.success(list, "成功");
    }
}
