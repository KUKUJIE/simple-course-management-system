package com.agiantii.backend.service.impl;

import com.agiantii.backend.mapper.HomeworkMapper;
import com.agiantii.backend.pojo.homework.Homework;
import com.agiantii.backend.service.IHomeworkService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class HomeworkServiceImpl extends ServiceImpl<HomeworkMapper, Homework> implements IHomeworkService {
    // business logic will be added here
}
