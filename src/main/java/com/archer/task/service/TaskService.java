package com.archer.task.service;

import java.util.Date;
import java.util.List;

public interface TaskService {

    void startTask();
    
    void startTask(Date startDate, Date endDate);
    
    void startTask(Date startDate, Date endDate, String taskType);
}
