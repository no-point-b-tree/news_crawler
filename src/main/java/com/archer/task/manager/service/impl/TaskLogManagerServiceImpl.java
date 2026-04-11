package com.archer.task.manager.service.impl;

import com.archer.task.entity.TaskLog;
import com.archer.task.manager.service.TaskLogManagerService;
import com.archer.task.mapper.TaskLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskLogManagerServiceImpl extends ServiceImpl<TaskLogMapper, TaskLog> implements TaskLogManagerService {

    private final TaskLogMapper taskLogMapper;

    @Override
    public Page<TaskLog> pageTaskLogs(String taskType, String startDate, String endDate, Integer page, Integer size) {
        Page<TaskLog> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<TaskLog> queryWrapper = new LambdaQueryWrapper<>();
        
        queryWrapper.eq(TaskLog::getIsDelete, "N");
        
        if (StringUtils.hasText(taskType)) {
            queryWrapper.like(TaskLog::getTaskType, taskType);
        }
        
        // 注意：这里简化处理，实际应该根据startDate和endDate进行时间范围查询
        // 由于前端传递的是字符串，这里需要转换为Date类型
        // 这里先按创建时间排序
        
        queryWrapper.orderByDesc(TaskLog::getCreateTime);
        
        return taskLogMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTaskLog(Long id) {
        TaskLog taskLog = taskLogMapper.selectById(id);
        if (taskLog == null) {
            return false;
        }
        taskLog.setIsDelete("Y");
        taskLog.setUpdateTime(new Date());
        return taskLogMapper.updateById(taskLog) > 0;
    }
}