package com.archer.task.manager.service;

import com.archer.task.entity.TaskLog;
import com.archer.task.manager.vo.TaskDetailVO;
import com.archer.task.manager.vo.WebsiteNewsVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

public interface TaskLogManagerService extends IService<TaskLog> {

    Page<TaskLog> pageTaskLogs(String taskType, String startDate, String endDate, Integer page, Integer size);

    boolean deleteTaskLog(Long id);

    TaskDetailVO getTaskDetails(Long taskId);

    Page<WebsiteNewsVO> getWebsiteNews(String sourceUrl, Long taskId, Integer page, Integer size);

    boolean startAllTasks(String startDate, String endDate, String taskType, Integer concurrentCount);
}