package com.archer.task.service;

import java.util.Date;
import java.util.List;

public interface TaskService {

    void startTask();

    List<String> getRuleList(List<Date> dateList);

}
