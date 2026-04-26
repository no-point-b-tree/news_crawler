package com.archer.task.service;

import com.archer.task.config.ScheduleTaskProperties;
import com.archer.task.manager.dto.NewsExportRequest;
import com.archer.task.manager.service.NewsExportService;
import com.archer.task.service.mail.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleTaskService {

    private final TaskService taskService;

    private final NewsExportService newsExportService;

    private final MailService mailService;

    private final ScheduleTaskProperties scheduleTaskProperties;

    @Scheduled(cron = "${schedule.task.cron:0 0 22 * * ?}")
    public void executeDailyTask() {
        log.info("定时任务开始执行");

        Date now = new Date();
        Date yesterday = org.apache.commons.lang3.time.DateUtils.addDays(now, -1);

        taskService.startTask(yesterday, now);

        NewsExportRequest exportRequest = new NewsExportRequest();
        exportRequest.setStartTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(yesterday));
        exportRequest.setEndTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now));

        String jsonContent = newsExportService.exportToJson(exportRequest);

        String fileName = "news_export_" + new SimpleDateFormat("yyyyMMdd").format(now) + ".json";

        mailService.sendJsonAttachment(jsonContent, fileName);

        log.info("定时任务执行完成");
    }
}
