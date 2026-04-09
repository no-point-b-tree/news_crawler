package com.archer.task.service;

import com.archer.task.entity.TaskLog;
import com.baomidou.mybatisplus.extension.service.IService;

public interface TaskLogService extends IService<TaskLog> {

    void addTask(TaskLog taskLog);

}
