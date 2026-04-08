# Spider Skill - 爬虫处理程序基础架构

## 概述
提供基于playwright的Java爬虫程序开发框架。核心功能包括根据地址探测网页的RSS链接，

## 依赖配置
```xml
  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
      <groupId>com.baomidou</groupId>
      <artifactId>mybatis-plus-boot-starter</artifactId>
      <version>${mybatis-plus.version}</version>
    </dependency>

    <dependency>
      <groupId>com.mysql</groupId>
      <artifactId>mysql-connector-j</artifactId>
      <scope>runtime</scope>
    </dependency>

    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <optional>true</optional>
    </dependency>

    <dependency>
      <groupId>com.microsoft.playwright</groupId>
      <artifactId>playwright</artifactId>
      <version>1.58.0</version>
    </dependency>

    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>
```

## 项目结构
```
src/main/java/com/archer/task/
├── entity/
│   └── Article.java          # 文章内容实体，封装数据用
├── service/
│   └──LatestSpiderTaskService.java          # 基于playwright实现获取网站相关信息的服务
|   ├── impl/
|       └──LatestSpiderTaskServiceImpl.java # LatestSpiderTaskService的具体实现
```

## 核心代码模板

### 1. 基于playwright实现获取网站相关信息的服务
```java
package com.archer.task.service;

import java.util.List;

import com.archer.task.entity.Article;
import com.microsoft.playwright.Browser;

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
	List<Article> getRssResult(List<String> rssUrlList);
	
}

```

### 2. 文章内容
```java
package com.archer.task.entity;

import java.util.Date;

import lombok.Data;

/**
 * 文章内容封装
 */
@Data
public class Article {

	// 文章
	private String title;
	
	// 内容
	private String content;
	
	// 发布日期
	private Date publishDate;
	
}
```



## 使用示例
```java
package com.archer;

import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.archer.task.entity.Article;
import com.archer.task.service.LatestSpiderTaskService;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

@SpringBootApplication
public class App {
	
	@Resource
	private LatestSpiderTaskService latestSpiderTaskService;

	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}
	
	@PostConstruct
	public void init() {
		String url ="https://www.baidu.com";
		List<Article> newsPageList = latestSpiderTaskService.getNewsPageResult(latestSpiderTaskService.findNewsPageUrlList(url));
		List<Article> rssResult = latestSpiderTaskService.getRssResult(latestSpiderTaskService.findRssUrlList(url));
		
	}
}

```

## 注意事项
1. 获取RSS链接，以及获取网站新闻页，博客页都需要使用playwright去实现，如果单独使用dom和java逻辑无法判断rss的，请考虑下是否能用谷歌浏览器的rss插件结合代码实现
