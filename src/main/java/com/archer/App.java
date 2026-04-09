package com.archer;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
@MapperScan("com.archer.**.mapper")
public class App {


    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

//	@PostConstruct
//	public void init() {
//		String url ="https://www.boe.es/";
//		List<Article> newsPageList = latestSpiderTaskService.getNewsPageResult(latestSpiderTaskService.findNewsPageUrlList(url));
//		List<Article> rssResult = latestSpiderTaskService.getRssResult(latestSpiderTaskService.findRssUrlList(url));
//		log.info("rssResult: {}",rssResult);
//		log.info("newsPageList: {}",newsPageList);
//		
//	}

}
