package com.archer.task.manager.service;

import com.archer.task.entity.TaskLog;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

public interface TaskLogManagerService extends IService<TaskLog> {

    Page<TaskLog> pageTaskLogs(String taskType, String startDate, String endDate, Integer page, Integer size);

    boolean deleteTaskLog(Long id);
}