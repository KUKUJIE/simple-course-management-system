package com.agiantii.backend.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理，将 Spring 底层异常转换为业务友好的 400 响应
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public R<String> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.warn("数据完整性约束冲突: {}", e.getMessage());

        String message = e.getMessage();
        if (message == null) {
            return R.error("数据冲突，请检查输入", 400);
        }

        String detail = extractDetail(e);
        log.info("根因信息: {}", detail);

        // 唯一约束冲突映射
        String friendly = mapConstraintMessage(detail);
        return R.error(friendly, 400);
    }

    /**
     * 递归获取最深层 cause 的 message
     */
    private String extractDetail(Throwable e) {
        Throwable cause = e;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause.getMessage() != null ? cause.getMessage() : "";
    }

    /**
     * 根据约束名返回中文提示
     */
    private String mapConstraintMessage(String detail) {
        String lower = detail.toLowerCase();

        if (lower.contains("uk_student_no") || lower.contains("student_no")) {
            return "该学号已存在，请更换";
        }
        if (lower.contains("uk_student_user") || (lower.contains("student") && lower.contains("user_id"))) {
            return "该用户已绑定其他学生账号";
        }
        if (lower.contains("uk_teacher_no") || lower.contains("teacher_no")) {
            return "该工号已存在，请更换";
        }
        if (lower.contains("uk_teacher_user") || (lower.contains("teacher") && lower.contains("user_id"))) {
            return "该用户已绑定其他教师账号";
        }
        if (lower.contains("uk_course_code") || lower.contains("course_code")) {
            return "该课程编码已存在，请更换";
        }
        if (lower.contains("uk_section_code") || lower.contains("section_code")) {
            return "该教学班编码已存在，请更换";
        }
        if (lower.contains("uk_major_code") || lower.contains("major_code")) {
            return "该专业编码已存在，请更换";
        }
        if (lower.contains("uk_department_code") || lower.contains("department_code")) {
            return "该院系编码已存在，请更换";
        }

        if (lower.contains("duplicate") || lower.contains("unique") || lower.contains("constraint")) {
            return "数据重复或冲突，请检查输入";
        }

        return "数据冲突，请检查输入";
    }
}
