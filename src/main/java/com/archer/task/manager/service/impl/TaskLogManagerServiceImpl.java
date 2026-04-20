package com.archer.task.manager.service.impl;

import com.archer.task.entity.News;
import com.archer.task.entity.NewsLink;
import com.archer.task.entity.TaskLog;
import com.archer.task.entity.TaskUrlLog;
import com.archer.task.manager.service.TaskLogManagerService;
import com.archer.task.manager.vo.TaskDetailVO;
import com.archer.task.manager.vo.WebsiteNewsVO;
import com.archer.task.mapper.NewsLinkMapper;
import com.archer.task.mapper.NewsMapper;
import com.archer.task.mapper.TaskLogMapper;
import com.archer.task.mapper.TaskUrlLogMapper;
import com.archer.task.service.TaskService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskLogManagerServiceImpl extends ServiceImpl<TaskLogMapper, TaskLog> implements TaskLogManagerService {

    private final TaskLogMapper taskLogMapper;
    private final NewsMapper newsMapper;
    private final NewsLinkMapper newsLinkMapper;
    private final TaskUrlLogMapper taskUrlLogMapper;
    private final TaskService taskService;

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

    @Override
    public TaskDetailVO getTaskDetails(Long taskId) {
        TaskDetailVO taskDetailVO = new TaskDetailVO();
        taskDetailVO.setTaskId(taskId);

        // 获取任务信息
        TaskLog taskLog = taskLogMapper.selectById(taskId);
        if (taskLog != null) {
            taskDetailVO.setTaskType(taskLog.getTaskType());
        }

        // 查询该任务下的所有URL处理记录
        LambdaQueryWrapper<TaskUrlLog> taskUrlLogQueryWrapper = new LambdaQueryWrapper<>();
        taskUrlLogQueryWrapper.eq(TaskUrlLog::getTaskId, taskId)
                .eq(TaskUrlLog::getIsDelete, "N");

        List<TaskUrlLog> taskUrlLogs = taskUrlLogMapper.selectList(taskUrlLogQueryWrapper);

        // 按URL统计新闻数量（直接从task_url_log表中获取）
        Map<String, Integer> newsCountBySourceUrl = taskUrlLogs.stream()
                .collect(Collectors.toMap(
                        TaskUrlLog::getUrl,
                        TaskUrlLog::getNewsCount,
                        (count1, count2) -> count1 + count2  // 如果有重复URL，合并数量
                ));

        // 构建网站统计信息
        List<TaskDetailVO.WebsiteSummary> websiteSummaries = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : newsCountBySourceUrl.entrySet()) {
            String sourceUrl = entry.getKey();
            Long newsCount = entry.getValue().longValue();

            // 查找对应的网站信息
            LambdaQueryWrapper<NewsLink> newsLinkQueryWrapper = new LambdaQueryWrapper<>();
            newsLinkQueryWrapper.eq(NewsLink::getUrl, sourceUrl)
                    .eq(NewsLink::getIsDelete, "N")
                    .last("limit 1");

            NewsLink newsLink = newsLinkMapper.selectOne(newsLinkQueryWrapper);

            TaskDetailVO.WebsiteSummary websiteSummary = new TaskDetailVO.WebsiteSummary();
            websiteSummary.setUrl(sourceUrl);
            websiteSummary.setNewsCount(newsCount.intValue());

            if (newsLink != null) {
                websiteSummary.setName(newsLink.getName());
            } else {
                // 如果没有找到对应的网站信息，使用URL作为名称
                websiteSummary.setName(sourceUrl);
            }

            websiteSummaries.add(websiteSummary);
        }

        taskDetailVO.setWebsites(websiteSummaries);
        return taskDetailVO;
    }

    @Override
    public Page<WebsiteNewsVO> getWebsiteNews(String sourceUrl, Long taskId, Integer page, Integer size) {
        Page<News> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<News> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(News::getIsDelete, "N")
                .eq(News::getSourceUrl, sourceUrl);

        if (taskId != null) {
            queryWrapper.eq(News::getTaskId, taskId);
        }

        queryWrapper.orderByDesc(News::getPublishTime);

        Page<News> newsPage = newsMapper.selectPage(pageParam, queryWrapper);

        // 转换为VO
        Page<WebsiteNewsVO> resultPage = new Page<>(newsPage.getCurrent(), newsPage.getSize(), newsPage.getTotal());

        List<WebsiteNewsVO> voList = newsPage.getRecords().stream().map(news -> {
            WebsiteNewsVO vo = new WebsiteNewsVO();
            vo.setId(news.getId());
            vo.setTitle(news.getTitle());
            vo.setZhTitle(news.getZhTitle()); // 添加中文标题
            vo.setSummary(news.getSummary()); // 添加摘要

            // 优先使用tikaContent（紧凑型文本），如果为空则使用原始content
            if (news.getTikaContent() != null && !news.getTikaContent().trim().isEmpty()) {
                vo.setContent(news.getTikaContent());
            } else {
                // 如果tikaContent为空，使用原始content
                vo.setContent(news.getContent());
            }

            vo.setPublishTime(news.getPublishTime());
            vo.setCreateTime(news.getCreateTime());
            vo.setSourceUrl(news.getSourceUrl());
            vo.setSource(news.getSource()); // 添加来源字段
            vo.setStatus(news.getStatus());
            vo.setUrl(news.getUrl());
            return vo;
        }).collect(Collectors.toList());

        resultPage.setRecords(voList);
        return resultPage;
    }

    @Override
    public boolean startAllTasks(String startDate, String endDate, String taskType, Integer concurrentCount) {
        try {
            log.info("开始执行所有任务，参数: startDate={}, endDate={}, taskType={}, concurrentCount={}",
                    startDate, endDate, taskType, concurrentCount);

            // 解析日期字符串为Date对象
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);

            // 调用TaskService执行任务，传递所有参数
            taskService.startTask(start, end, taskType, concurrentCount);

            log.info("所有任务已开始执行");
            return true;

        } catch (Exception e) {
            log.error("执行所有任务失败", e);
            return false;
        }
    }
}