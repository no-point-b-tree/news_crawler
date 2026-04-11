package com.archer.task.manager.controller;

import com.archer.task.entity.CssSelectorLinkRelation;
import com.archer.task.entity.NewsLink;
import com.archer.task.manager.dto.NewsLinkPageRequest;
import com.archer.task.manager.service.NewsLinkManagerService;
import com.archer.task.manager.vo.NewsLinkWithCssCountVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class NewsLinkManagerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NewsLinkManagerService newsLinkManagerService;

    @Test
    void testPageNewsLinksWithCssCount() throws Exception {
        // 准备测试数据
        NewsLinkWithCssCountVO vo1 = new NewsLinkWithCssCountVO();
        vo1.setId(1L);
        vo1.setUrl("https://www.example1.com");
        vo1.setName("Example Site 1");
        vo1.setLang("en");
        vo1.setCreateTime(new Date());
        vo1.setCssSelectorCount(3);
        vo1.setEnabledCssSelectorCount(2);
        vo1.setDisabledCssSelectorCount(1);

        NewsLinkWithCssCountVO vo2 = new NewsLinkWithCssCountVO();
        vo2.setId(2L);
        vo2.setUrl("https://www.example2.com");
        vo2.setName("Example Site 2");
        vo2.setLang("zh");
        vo2.setCreateTime(new Date());
        vo2.setCssSelectorCount(5);
        vo2.setEnabledCssSelectorCount(4);
        vo2.setDisabledCssSelectorCount(1);

        Page<NewsLinkWithCssCountVO> mockPage = new Page<>(1, 10, 2);
        mockPage.setRecords(Arrays.asList(vo1, vo2));

        // 模拟服务层调用
        when(newsLinkManagerService.pageNewsLinksWithCssCount(anyString(), anyList(), anyInt(), anyInt()))
                .thenReturn(mockPage);

        // 构建请求
        NewsLinkPageRequest request = new NewsLinkPageRequest();
        request.setKeyword("example");
        request.setLangs(Arrays.asList("en", "zh"));
        request.setPage(1);
        request.setSize(10);

        // 执行测试
        mockMvc.perform(post("/api/manager/news-links/page-with-css-count")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records[0].id").value(1))
                .andExpect(jsonPath("$.records[0].url").value("https://www.example1.com"))
                .andExpect(jsonPath("$.records[0].cssSelectorCount").value(3))
                .andExpect(jsonPath("$.records[0].enabledCssSelectorCount").value(2))
                .andExpect(jsonPath("$.records[0].disabledCssSelectorCount").value(1))
                .andExpect(jsonPath("$.records[1].id").value(2))
                .andExpect(jsonPath("$.records[1].cssSelectorCount").value(5));
    }

    @Test
    void testPageNewsLinksWithCssCountNoKeyword() throws Exception {
        // 准备测试数据
        NewsLinkWithCssCountVO vo = new NewsLinkWithCssCountVO();
        vo.setId(1L);
        vo.setUrl("https://www.example.com");
        vo.setName("Example Site");
        vo.setLang("en");
        vo.setCreateTime(new Date());
        vo.setCssSelectorCount(0);
        vo.setEnabledCssSelectorCount(0);
        vo.setDisabledCssSelectorCount(0);

        Page<NewsLinkWithCssCountVO> mockPage = new Page<>(1, 10, 1);
        mockPage.setRecords(Arrays.asList(vo));

        // 模拟服务层调用（无关键词）
        when(newsLinkManagerService.pageNewsLinksWithCssCount(isNull(), anyList(), anyInt(), anyInt()))
                .thenReturn(mockPage);

        // 构建请求（无关键词）
        NewsLinkPageRequest request = new NewsLinkPageRequest();
        request.setPage(1);
        request.setSize(10);

        // 执行测试
        mockMvc.perform(post("/api/manager/news-links/page-with-css-count")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records[0].cssSelectorCount").value(0));
    }
}