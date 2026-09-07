package com.agiantii.backend.service.impl;

import com.agiantii.backend.mapper.HomeworkSubmissionMapper;
import com.agiantii.backend.pojo.homework.HomeworkSubmission;
import com.agiantii.backend.service.ISubmissionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class SubmissionServiceImpl extends ServiceImpl<HomeworkSubmissionMapper, HomeworkSubmission> implements ISubmissionService {

}
