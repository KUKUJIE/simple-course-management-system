package com.agiantii.backend.controller;

import com.agiantii.backend.pojo.homework.Homework;
import com.agiantii.backend.service.IHomeworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/homeworks")
public class HomeworkController {

    @Autowired
    private IHomeworkService homeworkService;

    @PostMapping
    public ResponseEntity<?> createHomework(@RequestBody Homework homework) {
        boolean saved = homeworkService.save(homework);
        if (saved) {
            return ResponseEntity.ok(homework);
        }
        return ResponseEntity.status(500).body("Failed to create homework");
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getHomework(@PathVariable("id") Long id) {
        Homework hw = homeworkService.getById(id);
        if (hw == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(hw);
    }

    @GetMapping
    public ResponseEntity<?> listHomeworks(@RequestParam(value = "sectionId", required = false) Long sectionId) {
        List<Homework> list;
        if (sectionId != null) {
            list = homeworkService.lambdaQuery().eq(Homework::getSectionId, sectionId).list();
        } else {
            list = homeworkService.list();
        }
        return ResponseEntity.ok(list);
    }
}
