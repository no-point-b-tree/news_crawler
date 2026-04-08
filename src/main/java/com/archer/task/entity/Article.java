package com.archer.task.entity;

import java.util.Date;

import lombok.Data;

/**
 * 文章内容封装
 */
@Data
public class Article {

	private String url;

	// 文章
	private String title;

	private String description;

	// 内容
	private String content;
	
	// 发布日期
	private Date publishDate;
	
}
