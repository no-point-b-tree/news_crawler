package com.archer.task.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 超链接
 */
@Data
@EqualsAndHashCode
public class Hyperlink {

	private String href;
	
	private String content;

	private String pattern;
	
}
