###
存在web访问日志表。
```sql
CREATE TABLE `web_access_log` (
  `id` bigint NOT NULL COMMENT 'id',
  `url` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'url',
  `content` longtext COMMENT '内容',
  `web_index_type` varchar(200) COMMENT 'web索引类型',
  `is_delete` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '是否删除,N否,Y是',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='web网站访问记录表';
```
在SingleNewsTaskService里面。每次访问配置的网页的时候，请加入访问日志。注意解析后的Hyperlink的内容不需要加日志了。
web_index_type在访问网页正常的时候，指定为index,异常的时候，指定为index_exception。内容默认是获取的网页的内容。异常则是堆栈异常的错误信息。
请添加相关实体。mapper。以及想办法加入构造参数，把mapper传入进来。其他模块调用改方法的，需要先在spring注入，然后把注入的mapper传进来。