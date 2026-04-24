package com.archer.task.manager.service;

import com.archer.task.entity.NewsLink;
import com.archer.task.entity.TaskLog;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface NewsLinkManagerService extends IService<NewsLink> {

    Page<NewsLink> pageNewsLinks(String keyword, List<String> langs, Integer page, Integer size);
    
    Page<com.archer.task.manager.vo.NewsLinkWithCssCountVO> pageNewsLinksWithCssCount(String keyword, List<String> langs, Integer page, Integer size);

    List<String> getAllLanguages();
    
    List<com.archer.task.manager.controller.LocaleController.LocaleInfo> getLocaleLanguages();

    boolean createNewsLink(NewsLink newsLink);

    boolean updateNewsLink(NewsLink newsLink);

    boolean deleteNewsLink(Long id);

    boolean startSingleTask(String url, String startDate, String endDate);

    int findHyperLinkCount(String url, String startDate, String endDate);

    Page<TaskLog> pageTaskLogsByUrl(String url, Integer page, Integer size);

    Page<com.archer.task.entity.CssSelectorLinkRelation> pageCssSelectorsByUrl(String url, Integer page, Integer size);

    boolean addCssSelector(String url, String cssSelectorGroup, String dateCssSelector, String hyperLinkCssSelector, String datePattern, String enabled);

    boolean updateCssSelector(Long id, String cssSelectorGroup, String dateCssSelector, String hyperLinkCssSelector, String datePattern, String enabled);

    boolean deleteCssSelector(Long id);
}