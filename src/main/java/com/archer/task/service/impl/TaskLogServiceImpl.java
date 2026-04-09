package com.archer.task.service.impl;

import com.archer.task.entity.TaskLog;
import com.archer.task.mapper.TaskLogMapper;
import com.archer.task.service.TaskLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class TaskLogServiceImpl extends ServiceImpl<TaskLogMapper, TaskLog> implements TaskLogService {

    @Override
    @Transactional
    public void addTask(TaskLog taskLog) {
        taskLog.setCreateTime(new Date());
        taskLog.setUpdateTime(new Date());
        taskLog.setIsDelete("N");
        save(taskLog);
    }

}
