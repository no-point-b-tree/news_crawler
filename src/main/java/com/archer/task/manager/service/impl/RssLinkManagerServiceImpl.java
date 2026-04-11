package com.archer.task.manager.service.impl;

import com.archer.task.entity.RssLink;
import com.archer.task.manager.service.RssLinkManagerService;
import com.archer.task.mapper.RssLinkMapper;
import com.archer.task.service.SingleRssTaskService;
import com.archer.task.service.impl.LatestSpiderTaskServiceImpl;
import com.archer.task.mapper.NewsMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class RssLinkManagerServiceImpl extends ServiceImpl<RssLinkMapper, RssLink> implements RssLinkManagerService {

    @Resource
    private RssLinkMapper rssLinkMapper;

    @Resource
    private NewsMapper newsMapper;

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    @Override
    public Page<RssLink> pageRssLinks(String keyword, Integer page, Integer size) {
        Page<RssLink> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<RssLink> queryWrapper = new LambdaQueryWrapper<>();
        
        queryWrapper.eq(RssLink::getIsDelete, "N");
        
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                .like(RssLink::getUrl, keyword)
                .or()
                .like(RssLink::getDomain, keyword)
            );
        }
        
        queryWrapper.orderByDesc(RssLink::getCreateTime);
        
        return rssLinkMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createRssLink(RssLink rssLink) {
        if (rssLink.getUrl() == null || rssLink.getUrl().trim().isEmpty()) {
            return false;
        }
        rssLink.setIsDelete("N");
        rssLink.setCreateTime(new Date());
        rssLink.setUpdateTime(new Date());
        return rssLinkMapper.insert(rssLink) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRssLink(RssLink rssLink) {
        if (rssLink.getId() == null) {
            return false;
        }
        rssLink.setUpdateTime(new Date());
        return rssLinkMapper.updateById(rssLink) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRssLink(Long id) {
        RssLink rssLink = rssLinkMapper.selectById(id);
        if (rssLink == null) {
            return false;
        }
        rssLink.setIsDelete("Y");
        rssLink.setUpdateTime(new Date());
        return rssLinkMapper.updateById(rssLink) > 0;
    }

    @Override
    public boolean startRssTask(String url) {
        try {
            Date current = new Date();
            Date startDate = DateUtils.addDays(current, -1);
            CountDownLatch countDownLatch = new CountDownLatch(1);
            
            SingleRssTaskService task = new SingleRssTaskService(
                url, 
                startDate, 
                current, 
                new LatestSpiderTaskServiceImpl(), 
                newsMapper, 
                countDownLatch
            );
            
            executorService.execute(task);
            countDownLatch.await();
            return true;
        } catch (Exception e) {
            log.error("启动RSS任务失败: {}", url, e);
            return false;
        }
    }
}