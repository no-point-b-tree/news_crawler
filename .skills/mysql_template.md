# MySQL数据库设计规范模板

## 1. 命名规范

### 表命名
- 使用小写字母，下划线分隔
- 使用复数形式（如：`users`, `orders`, `product_categories`）
- 避免使用MySQL保留字
- 长度不超过64字符

### 字段命名
- 使用小写字母，下划线分隔
- 避免使用保留字
- 布尔字段以 `is_`、`has_`、`can_` 开头
- 外键字段：`parent_table_id` 格式

### 索引命名
- 主键：`pk_table_name`
- 唯一索引：`uk_table_name_column_name`
- 普通索引：`idx_table_name_column_name`
- 全文索引：`ft_table_name_column_name`

## 2. 数据类型规范

### 常用类型
```sql
-- 整数类型
id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID'
status TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '状态：0-禁用，1-启用'

-- 字符串类型
username VARCHAR(50) NOT NULL COMMENT '用户名'
email VARCHAR(100) NOT NULL COMMENT '邮箱'
description TEXT COMMENT '描述'

-- 时间类型
create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
publish_date DATE COMMENT '发布日期'

-- 小数类型
price DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '价格'
rating DECIMAL(3, 2) NOT NULL DEFAULT 0.00 COMMENT '评分'

-- 布尔类型
is_deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是'
is_active TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '是否激活'
```

## 3. 表结构模板

### 基础表模板
```sql
CREATE TABLE `table_name` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(100) NOT NULL COMMENT '名称',
  `code` VARCHAR(50) NOT NULL COMMENT '编码',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `sort_order` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
  `create_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人ID',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_table_name_code` (`code`),
  KEY `idx_table_name_status` (`status`),
  KEY `idx_table_name_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='表名注释';
```

### 关联表模板
```sql
CREATE TABLE `relation_table` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` BIGINT UNSIGNED NOT NULL COMMENT '父表ID',
  `child_id` BIGINT UNSIGNED NOT NULL COMMENT '子表ID',
  `relation_type` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '关系类型',
  `extra_data` JSON DEFAULT NULL COMMENT '额外数据',
  `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_relation_parent_child` (`parent_id`, `child_id`, `relation_type`),
  KEY `idx_relation_parent_id` (`parent_id`),
  KEY `idx_relation_child_id` (`child_id`),
  KEY `idx_relation_create_time` (`create_time`),
  CONSTRAINT `fk_relation_parent` FOREIGN KEY (`parent_id`) REFERENCES `parent_table` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_relation_child` FOREIGN KEY (`child_id`) REFERENCES `child_table` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关联表注释';
```

### 日志表模板
```sql
CREATE TABLE `operation_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '用户ID',
  `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型',
  `operation_target` VARCHAR(100) NOT NULL COMMENT '操作目标',
  `operation_content` TEXT COMMENT '操作内容',
  `ip_address` VARCHAR(45) DEFAULT NULL COMMENT 'IP地址',
  `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用户代理',
  `request_params` JSON DEFAULT NULL COMMENT '请求参数',
  `response_result` JSON DEFAULT NULL COMMENT '响应结果',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态：0-失败，1-成功',
  `error_message` TEXT COMMENT '错误信息',
  `execute_time` INT UNSIGNED DEFAULT NULL COMMENT '执行时间(毫秒)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_log_user_id` (`user_id`),
  KEY `idx_log_operation_type` (`operation_type`),
  KEY `idx_log_create_time` (`create_time`),
  KEY `idx_log_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';
```

## 4. 索引规范

### 索引创建原则
```sql
-- 主键索引（必须）
ALTER TABLE `table_name` ADD PRIMARY KEY (`id`);

-- 唯一索引（业务唯一约束）
ALTER TABLE `table_name` ADD UNIQUE KEY `uk_table_name_field` (`field1`, `field2`);

-- 普通索引（查询条件字段）
ALTER TABLE `table_name` ADD INDEX `idx_table_name_field` (`field`);

-- 联合索引（最左前缀原则）
ALTER TABLE `table_name` ADD INDEX `idx_table_name_fields` (`field1`, `field2`, `field3`);

-- 全文索引（文本搜索）
ALTER TABLE `table_name` ADD FULLTEXT INDEX `ft_table_name_content` (`content`);
```

### 索引优化建议
1. 为WHERE、JOIN、ORDER BY、GROUP BY字段创建索引
2. 避免在索引列上使用函数或计算
3. 联合索引字段顺序：等值查询字段在前，范围查询字段在后
4. 单表索引数量不超过5个
5. 索引字段长度不超过767字节

## 5. 约束规范

### 外键约束
```sql
-- 添加外键
ALTER TABLE `child_table` 
ADD CONSTRAINT `fk_child_parent` 
FOREIGN KEY (`parent_id`) 
REFERENCES `parent_table` (`id`) 
ON DELETE RESTRICT 
ON UPDATE CASCADE;

-- 删除外键
ALTER TABLE `child_table` DROP FOREIGN KEY `fk_child_parent`;
```

### 检查约束（MySQL 8.0+）
```sql
ALTER TABLE `table_name` 
ADD CONSTRAINT `chk_table_name_status` 
CHECK (`status` IN (0, 1, 2));

ALTER TABLE `table_name` 
ADD CONSTRAINT `chk_table_name_age` 
CHECK (`age` >= 0 AND `age` <= 150);
```

## 6. 分区表模板

### 按时间范围分区
```sql
CREATE TABLE `partition_table` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `data` VARCHAR(500) NOT NULL COMMENT '数据',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分区表'
PARTITION BY RANGE (YEAR(create_time)) (
  PARTITION p2023 VALUES LESS THAN (2024),
  PARTITION p2024 VALUES LESS THAN (2025),
  PARTITION p2025 VALUES LESS THAN (2026),
  PARTITION p_max VALUES LESS THAN MAXVALUE
);
```

## 7. 视图模板

### 常用视图
```sql
-- 基础视图
CREATE VIEW `view_table_summary` AS
SELECT 
  t.id,
  t.name,
  t.status,
  COUNT(r.id) AS relation_count,
  MAX(r.create_time) AS last_relation_time
FROM `table_name` t
LEFT JOIN `relation_table` r ON t.id = r.parent_id
WHERE t.is_deleted = 0
GROUP BY t.id;

-- 安全视图（隐藏敏感字段）
CREATE VIEW `view_user_safe` AS
SELECT 
  id,
  username,
  nickname,
  email,
  avatar,
  status,
  create_time
FROM `users`
WHERE is_deleted = 0;
```

## 8. 存储过程/函数模板

### 存储过程模板
```sql
DELIMITER $$

CREATE PROCEDURE `sp_process_data`(
  IN p_input_param VARCHAR(100),
  OUT p_output_result INT
)
BEGIN
  DECLARE v_error_code INT DEFAULT 0;
  DECLARE v_error_msg VARCHAR(500) DEFAULT '';
  DECLARE EXIT HANDLER FOR SQLEXCEPTION
  BEGIN
    GET DIAGNOSTICS CONDITION 1
      v_error_code = MYSQL_ERRNO,
      v_error_msg = MESSAGE_TEXT;
    SET p_output_result = -1;
    ROLLBACK;
  END;

  START TRANSACTION;
  
  -- 业务逻辑
  INSERT INTO `log_table` (operation, create_time) 
  VALUES (p_input_param, NOW());
  
  SET p_output_result = LAST_INSERT_ID();
  
  COMMIT;
END$$

DELIMITER ;
```

### 函数模板
```sql
DELIMITER $$

CREATE FUNCTION `fn_calculate_discount`(
  p_price DECIMAL(10, 2),
  p_discount_rate DECIMAL(5, 2)
) RETURNS DECIMAL(10, 2)
DETERMINISTIC
BEGIN
  DECLARE v_result DECIMAL(10, 2);
  
  IF p_discount_rate > 1 THEN
    SET p_discount_rate = 1;
  ELSEIF p_discount_rate < 0 THEN
    SET p_discount_rate = 0;
  END IF;
  
  SET v_result = p_price * (1 - p_discount_rate);
  
  RETURN ROUND(v_result, 2);
END$$

DELIMITER ;
```

## 9. 触发器模板

### 审计触发器
```sql
DELIMITER $$

CREATE TRIGGER `trg_table_name_before_update`
BEFORE UPDATE ON `table_name`
FOR EACH ROW
BEGIN
  SET NEW.update_time = NOW();
  
  IF OLD.status != NEW.status THEN
    INSERT INTO `status_change_log` (
      table_name, 
      record_id, 
      old_status, 
      new_status, 
      change_time
    ) VALUES (
      'table_name',
      OLD.id,
      OLD.status,
      NEW.status,
      NOW()
    );
  END IF;
END$$

DELIMITER ;
```

## 10. 最佳实践

### 建表语句完整示例
```sql
-- 用户表示例
CREATE TABLE `users` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
  `salt` VARCHAR(50) NOT NULL COMMENT '密码盐值',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
  `gender` TINYINT UNSIGNED DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
  `birthday` DATE DEFAULT NULL COMMENT '生日',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(45) DEFAULT NULL COMMENT '最后登录IP',
  `login_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '登录次数',
  `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users_username` (`username`),
  UNIQUE KEY `uk_users_email` (`email`),
  UNIQUE KEY `uk_users_phone` (`phone`),
  KEY `idx_users_status` (`status`),
  KEY `idx_users_create_time` (`create_time`),
  KEY `idx_users_last_login_time` (`last_login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
```

### 配置建议
```sql
-- 字符集配置
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET COLLATION_CONNECTION = utf8mb4_unicode_ci;

-- 时区配置
SET time_zone = '+08:00';

-- SQL模式配置（推荐）
SET sql_mode = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';
```

## 11. 维护脚本

### 备份脚本模板
```sql
-- 结构备份
mysqldump -u root -p --no-data database_name > structure.sql

-- 数据备份
mysqldump -u root -p --no-create-info database_name > data.sql

-- 完整备份
mysqldump -u root -p database_name > full_backup.sql

-- 压缩备份
mysqldump -u root -p database_name | gzip > backup_$(date +%Y%m%d).sql.gz
```

### 优化脚本
```sql
-- 分析表
ANALYZE TABLE `table_name`;

-- 优化表
OPTIMIZE TABLE `table_name`;

-- 修复表
REPAIR TABLE `table_name`;

-- 检查表
CHECK TABLE `table_name`;
```

### 监控脚本
```sql
-- 查看表大小
SELECT 
  table_name AS `表名`,
  ROUND(((data_length + index_length) / 1024 / 1024), 2) AS `大小(MB)`
FROM information_schema.tables 
WHERE table_schema = 'database_name'
ORDER BY (data_length + index_length) DESC;

-- 查看索引使用情况
SELECT 
  table_name,
  index_name,
  rows_read,
  rows_inserted,
  rows_updated,
  rows_deleted
FROM sys.schema_index_statistics
WHERE table_schema = 'database_name';
```

## 12. 安全规范

### 权限管理
```sql
-- 创建只读用户
CREATE USER 'readonly_user'@'%' IDENTIFIED BY 'StrongPassword123!';
GRANT SELECT ON database_name.* TO 'readonly_user'@'%';

-- 创建读写用户
CREATE USER 'rw_user'@'localhost' IDENTIFIED BY 'StrongPassword456!';
GRANT SELECT, INSERT, UPDATE, DELETE ON database_name.* TO 'rw_user'@'localhost';

-- 创建管理用户
CREATE USER 'admin_user'@'localhost' IDENTIFIED BY 'StrongPassword789!';
GRANT ALL PRIVILEGES ON database_name.* TO 'admin_user'@'localhost';

-- 刷新权限
FLUSH PRIVILEGES;
```

### 数据加密
```sql
-- 使用AES加密
INSERT INTO `sensitive_data` (encrypted_field)
VALUES (AES_ENCRYPT('plain_text', 'encryption_key'));

-- 解密查询
SELECT AES_DECRYPT(encrypted_field, 'encryption_key') AS plain_text
FROM `sensitive_data`;

-- 使用SHA2哈希
UPDATE `users` 
SET password_hash = SHA2(CONCAT('salt', 'password'), 256)
WHERE id = 1;
```

---

**版本记录：**
- v1.0.0 (2024-01-01): 初始版本
- 适用MySQL版本：5.7+，推荐8.0+
- 字符集：utf8mb4
- 排序规则：utf8mb4_unicode_ci
- 存储引擎：InnoDB
