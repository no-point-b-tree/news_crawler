# 新闻管理页面CSS选择器数量功能

## 概述

在新闻管理页面添加了CSS选择器数量列，可以在列表页面直接查看每个网站配置的CSS选择器数量，无需跳转到详情页面。

## 新增接口

### 1. 获取包含CSS选择器数量的新闻链接列表

**接口地址**: `POST /api/manager/news-links/page-with-css-count`

**请求参数**:
```json
{
  "page": 1,
  "size": 10,
  "keyword": "example",  // 可选，搜索关键词
  "langs": ["en", "zh"]  // 可选，语言过滤
}
```

**响应示例**:
```json
{
  "records": [
    {
      "id": 1,
      "domain": "example.com",
      "url": "https://www.example.com",
      "isDelete": "N",
      "name": "示例网站",
      "lang": "zh",
      "createTime": "2024-01-01T10:00:00",
      "updateTime": "2024-01-01T10:00:00",
      "cssSelectorCount": 3,
      "enabledCssSelectorCount": 2,
      "disabledCssSelectorCount": 1
    }
  ],
  "total": 1,
  "size": 10,
  "current": 1,
  "pages": 1
}
```

**新增字段说明**:
- `cssSelectorCount`: CSS选择器总数
- `enabledCssSelectorCount`: 启用的CSS选择器数量
- `disabledCssSelectorCount`: 禁用的CSS选择器数量

### 2. 原有接口（保持不变）

**接口地址**: `POST /api/manager/news-links/page`

## 后端代码变更

### 1. 新增VO类

`NewsLinkWithCssCountVO.java` - 继承自`NewsLink`，添加CSS选择器统计字段

### 2. 服务层接口

`NewsLinkManagerService.java` - 新增方法:
```java
Page<NewsLinkWithCssCountVO> pageNewsLinksWithCssCount(String keyword, List<String> langs, Integer page, Integer size);
```

### 3. 服务层实现

`NewsLinkManagerServiceImpl.java` - 实现新方法，查询每个新闻链接的CSS选择器数量

### 4. 控制器层

`NewsLinkManagerController.java` - 新增接口:
```java
@PostMapping("/page-with-css-count")
public ResponseEntity<Page<NewsLinkWithCssCountVO>> pageNewsLinksWithCssCount(@RequestBody NewsLinkPageRequest request)
```

## 前端使用建议

### 1. 表格列配置

建议在新闻管理表格中添加以下列：

| 列名 | 字段 | 说明 |
|------|------|------|
| ID | id | 新闻链接ID |
| 网站名称 | name | 网站名称 |
| URL | url | 网站地址 |
| 语言 | lang | 网站语言 |
| CSS选择器总数 | cssSelectorCount | 配置的CSS选择器总数 |
| 启用CSS选择器 | enabledCssSelectorCount | 启用的CSS选择器数量 |
| 禁用CSS选择器 | disabledCssSelectorCount | 禁用的CSS选择器数量 |
| 创建时间 | createTime | 创建时间 |
| 操作 | - | 编辑、删除等操作 |

### 2. 状态显示建议

- **cssSelectorCount = 0**: 显示红色警告图标，提示"未配置CSS选择器"
- **enabledCssSelectorCount = 0**: 显示黄色警告图标，提示"所有CSS选择器已禁用"
- **enabledCssSelectorCount > 0**: 显示绿色成功图标，提示"正常"

### 3. 排序功能

建议支持按CSS选择器数量排序：
- 按总数排序
- 按启用数量排序
- 按禁用数量排序

## 使用场景

### 1. 快速识别问题网站

在新闻管理列表页面，管理员可以快速识别：
- 哪些网站没有配置CSS选择器（无法抓取新闻）
- 哪些网站的所有CSS选择器都被禁用（不会抓取新闻）
- 哪些网站配置了多个CSS选择器（可能有冗余配置）

### 2. 批量操作

基于CSS选择器数量，可以：
- 批量启用/禁用CSS选择器
- 批量删除没有CSS选择器的网站
- 批量检查CSS选择器配置

### 3. 统计报表

可以生成统计报表：
- 各网站CSS选择器配置情况
- CSS选择器启用率统计
- 未配置CSS选择器的网站列表

## 性能考虑

### 1. 查询优化

新接口使用了额外的数据库查询来获取CSS选择器数量，建议：
- 对于大量数据，考虑使用缓存
- 定期更新统计信息，避免实时计算

### 2. 分页限制

建议保持合理的分页大小（如10-50条/页），避免一次性查询过多数据。

## 扩展功能

### 1. 导出功能

可以添加导出功能，将包含CSS选择器数量的新闻链接列表导出为Excel或CSV文件。

### 2. 筛选功能

添加筛选功能，可以按CSS选择器数量范围筛选：
- 未配置CSS选择器的网站
- 有1-3个CSS选择器的网站
- 有3个以上CSS选择器的网站

### 3. 批量操作

添加批量操作功能：
- 批量启用/禁用CSS选择器
- 批量删除未配置CSS选择器的网站
- 批量复制CSS选择器配置

## 示例代码

### Java调用示例

```java
// 注入服务
@Autowired
private NewsLinkManagerService newsLinkManagerService;

// 调用新接口
NewsLinkPageRequest request = new NewsLinkPageRequest();
request.setPage(1);
request.setSize(10);
request.setKeyword("news");
request.setLangs(Arrays.asList("en", "zh"));

Page<NewsLinkWithCssCountVO> result = newsLinkManagerService.pageNewsLinksWithCssCount(
    request.getKeyword(),
    request.getLangs(),
    request.getPage(),
    request.getSize()
);

// 处理结果
for (NewsLinkWithCssCountVO vo : result.getRecords()) {
    System.out.println("网站: " + vo.getName());
    System.out.println("CSS选择器总数: " + vo.getCssSelectorCount());
    System.out.println("启用数量: " + vo.getEnabledCssSelectorCount());
    System.out.println("禁用数量: " + vo.getDisabledCssSelectorCount());
    
    if (vo.getCssSelectorCount() == 0) {
        System.out.println("警告: 未配置CSS选择器");
    }
}
```

### REST API调用示例

```bash
# 调用新接口
curl -X POST "http://localhost:9008/api/manager/news-links/page-with-css-count" \
  -H "Content-Type: application/json" \
  -d '{
    "page": 1,
    "size": 10,
    "keyword": "example",
    "langs": ["en", "zh"]
  }'

# 调用旧接口（对比）
curl -X POST "http://localhost:9008/api/manager/news-links/page" \
  -H "Content-Type: application/json" \
  -d '{
    "page": 1,
    "size": 10,
    "keyword": "example",
    "langs": ["en", "zh"]
  }'
```

## 总结

通过添加CSS选择器数量列，新闻管理页面变得更加直观和实用。管理员可以在列表页面直接了解每个网站的CSS配置情况，快速识别和解决问题，提高工作效率。