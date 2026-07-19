package com.archer.task.ai;

import com.archer.task.constant.NewsStatusEnum;
import com.archer.task.entity.News;
import com.archer.task.mapper.NewsMapper;
import com.archer.task.util.TemplateUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

/**
 * AI组件来处理
 */
@Component
@Slf4j
public class AiTaskComponent {

    @Resource
    private TemplateUtil templateUtil;

    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();

    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(4, 6, 60, TimeUnit.SECONDS, queue, new BasicThreadFactory.Builder().namingPattern("ai-task-%d").build());

    // 运行中的任务数量
    private final ConcurrentHashMap<Long, CountDownLatch> taskCountDown = new ConcurrentHashMap<>();

    @Resource
    private NewsMapper newsMapper;

    @Resource
    private DeepSeekHelper deepSeekHelper;


    public void startAiTask(List<News> newsList, Long taskId) {
        threadPoolExecutor.execute(() -> {
            submitAITask(newsList, taskId);
        });
    }

    public void setTaskCountDown(Long taskId, CountDownLatch countDownLatch) {
        // 等待任务是0
        taskCountDown.put(taskId, countDownLatch);
    }


    private void submitAITask(List<News> newsList, Long taskId) {
        for (News news : newsList) {
            UpdateWrapper<News> update = new UpdateWrapper<>();
            update.eq("id", news.getId());
            try {
                // 直接对新闻进行后处理动作
                String c = templateUtil.renderTemplate("summary", Collections.singletonMap("content", news.getTikaContent()));
                String result = deepSeekHelper.processWithAi(c);
                JsonNode node = new ObjectMapper().readTree(result);
                if (node.get("isRelation").asBoolean()) {
                    // 相关
                    update.set("status", NewsStatusEnum.IS_RELEVANT);
                } else {
                    update.set("status", NewsStatusEnum.IS_NOT_RELEVANT);
                }
                update.set("summary", node.get("summary").asText());
                update.set("chinese_content", node.get("chineseContent").asText());
                String publishDate = node.get("publishDate").asText();
                if (StringUtils.isNotBlank(publishDate) && news.getPublishTime() == null) {
                    update.set("publish_time", DateUtils.parseDate(publishDate, "yyyy-MM-dd"));
                }
                if (news.getPublishTime() == null) {
                    update.set("publish_time", new Date());
                }
                newsMapper.update(update);
            } catch (Exception e) {
                log.error("AI处理失败, 新闻ID:{}", news.getId());
                update.set("status", NewsStatusEnum.AI_DEAL_FAIL);
                newsMapper.update(update);
            }
            try {
                String c = templateUtil.renderTemplate("title", Collections.singletonMap("title", news.getTitle()));
                String result = deepSeekHelper.processWithAi(c);
                String zhTitle = new ObjectMapper().readTree(result).get("title").asText();
                UpdateWrapper<News> titleUpdate = new UpdateWrapper<>();
                titleUpdate.eq("id", news.getId());
                titleUpdate.set("zh_title", zhTitle);
                newsMapper.update(titleUpdate);
            } catch (Exception e) {
                log.error("标题处理失败:", e);
            }
        }
        CountDownLatch cl = taskCountDown.get(taskId);
        if (cl != null) {
            cl.countDown();
            log.info("剩余AI任务数量:{}", cl.getCount());
            if (cl.getCount() == 0) {
                taskCountDown.remove(taskId);
            }
        }

    }
}
