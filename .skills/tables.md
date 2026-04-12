# 数据表合集

1. 表设计的通用规则
   1. id字段固定为bigint
   2. is_delete固定为是否删除标识，Y表示已删除,N表示未删除
   3. create_time是创建时间
   4. update_time是更新时间

```sql
-- archer.css_selector_link_relation definition


CREATE TABLE `css_selector_link_relation` (
  `id` bigint NOT NULL COMMENT 'id',
  `url` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'url',
  `css_selector_group` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'css选择器',
  `is_delete` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '是否删除,N否,Y是',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `date_css_selector` varchar(500) DEFAULT NULL COMMENT '日期选择器',
  `hyper_link_css_selector` varchar(500) DEFAULT NULL COMMENT '超链接选择器',
  `enabled` varchar(100) DEFAULT NULL COMMENT 'ON启用,OFF关闭',
  `date_pattern` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '日期匹配规则',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='css选择器和url的关联关系表';

-- archer.match_rule definition

CREATE TABLE `match_rule` (
  `id` bigint NOT NULL COMMENT 'id',
  `rule` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '规则',
  `rule_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '规则类型',
  `is_delete` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '是否删除N否,Y是',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通用匹配规则表';


CREATE TABLE `news_link` (
  `id` bigint NOT NULL COMMENT 'id',
  `domain` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '域名',
  `url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'url',
  `is_delete` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '是否删除',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '开始时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '结束时间',
  `lang` varchar(500) DEFAULT NULL COMMENT '语言',
  `name` varchar(500) DEFAULT NULL COMMENT '名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='新闻链接';

-- archer.task_log definition

CREATE TABLE `task_log` (
  `id` bigint NOT NULL COMMENT 'ID',
  `task_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '任务类型',
  `is_delete` char(1) DEFAULT NULL COMMENT '是否删除,N否,Y是',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `start_date` timestamp NULL DEFAULT NULL COMMENT '新闻开始时间',
  `end_date` timestamp NULL DEFAULT NULL COMMENT '新闻结束时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='任务日志';

CREATE TABLE `news` (
  `id` bigint NOT NULL COMMENT 'id',
  `url` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '新闻url',
  `title` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '标题',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '原始内容',
  `tika_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT 'tika处理后的内容',
  `summary` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '摘要',
  `is_delete` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '是否删除',
  `source` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '来源',
  `publish_time` timestamp NULL DEFAULT NULL COMMENT '新闻发布时间',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `status` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '状态',
  `source_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '追踪来源的url',
  `task_id` bigint DEFAULT NULL,
  `zh_title` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '中文标题',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='爬取的新闻内容表';

-- archer.task_url_log definition

CREATE TABLE `task_url_log` (
  `id` bigint NOT NULL COMMENT 'ID',
  `task_id` bigint NOT NULL COMMENT '任务ID',
  `url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '处理的URL',
  `news_count` int DEFAULT '0' COMMENT '找到的新闻数量',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '处理状态: SUCCESS-成功, NO_NEWS-无新闻, ERROR-错误',
  `error_message` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '错误信息',
  `is_delete` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'N' COMMENT '是否删除,N否,Y是',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_url` (`url`(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='任务URL处理日志表';
```

