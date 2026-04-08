package com.archer.task.service;

import java.util.Date;
import java.util.List;

import com.archer.task.entity.Article;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Playwright;

/**
 * 基于playwright实现获取网站相关信息的服务
 */
public interface LatestSpiderTaskService {
	
	/**
	 * 根据网站url查询网站所有的RSS订阅地址
	 * @param url url地址
	 * @return RSS订阅地址集合
	 */
	List<String> findRssUrlList(String url);
	
	/**
	 * 获取浏览器对象，单例模式，对于一个service实现类内部使用单例
	 * @return 浏览器对象
	 */
	Browser getBrowser();
	
	/**
	 * 获取新闻页的所有链接 主要逻辑导航栏有类似blog news report的按钮，获取点击后新的url地址存到集合，
	 * 注意部分按钮需要导航栏悬浮才能显示
	 * @param url url地址
	 * @return 所有的新闻页链接
	 */
	List<String> findNewsPageUrlList(String url);

	/**
	 * 根据RSS链接集合 获取RSS最新订阅的新闻数据 然后将数据封装到Article里面，包含文章，标题，发布日期
	 * @param rssUrlList RSS链接集合
	 * @return 文章内容集合
	 */
	List<Article> getRssResult(List<String> rssUrlList, Date startDate, Date endDate);
	
	/**
	 * 根据新闻页链接集合，获取所有页面超链接内容，要求超链接页面可见，然后将数据封装到Article里面，包含文章，标题，发布日期
	 * @param pageUrlList 新闻页集合
	 * @return 文章内容集合
	 */
	List<Article> getNewsPageResult(List<String> pageUrlList);

	/**
	 * 解析RSS XML内容，获取所有文章条目
	 * @param rssUrl RSS链接
	 * @return 文章集合
	 */
	List<Article> parseRss(String rssUrl);

	/**
	 * 根据文章URL获取完整的文章内容
	 * @param articleUrl 文章URL
	 * @return 文章内容（包含content字段）
	 */
	Article getArticleContent(String articleUrl);

}
