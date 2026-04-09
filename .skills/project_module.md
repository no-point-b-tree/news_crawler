# 项目模块划分

假设有某个业务模块包  com.demo.manager，生成代码的时候要求遵循如下原则

- com.demo.manager下的controller包用于管理web相关的入口映射
- com.demo.manager下的service包用于管理业务的接口
- com.demo.manager下的service.impl包用于管理业务的接口实现
- com.demo.manager下的mapper包用于管理业务的数据库增删改查
- com.demo.manager下的mapper.xml包用于管理业务的数据库增删改查的xml文件，主要用于mybatisPlus
- 所有涉及删除的操作都使用标记字段逻辑删除