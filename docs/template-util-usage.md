# TemplateUtil 使用文档

## 功能概述
TemplateUtil 是一个基于 Thymeleaf 的文本模板渲染工具类，提供以下功能：
1. 渲染 resource 目录下的文本模板文件
2. 渲染外部模板字符串
3. 支持 Map 和 Object 两种数据模型
4. 支持普通文本模板（非HTML）

## 依赖配置
已添加以下依赖到 pom.xml：
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

## 使用方法

### 1. 注入 TemplateUtil
```java
@Autowired
private TemplateUtil templateUtil;
```

### 2. 渲染 resource 目录下的文本模板文件
模板文件应放在 `resources/templates/` 目录下，默认文件后缀为 `.txt`

**使用 Map 作为数据模型：**
```java
Map<String, Object> model = new HashMap<>();
model.put("name", "张三");
model.put("age", 25);

String result = templateUtil.renderTemplate("welcome", model);
// 会自动查找 templates/welcome.txt
```

**使用 Object 作为数据模型：**
```java
User user = new User("李四", 30);
String result = templateUtil.renderTemplate("welcome", user);
```

**指定后缀的模板文件：**
```java
// 渲染 templates/email.tpl 文件
String result = templateUtil.renderTemplateWithSuffix("email.tpl", model);
```

### 3. 渲染外部模板字符串
**使用 Map 作为数据模型：**
```java
String template = "欢迎 ${name}！\n年龄：${age}岁";
Map<String, Object> model = new HashMap<>();
model.put("name", "王五");
model.put("age", 28);

String result = templateUtil.renderString(template, model);
```

**使用 Object 作为数据模型：**
```java
String template = "用户：${userName}\n年龄：${userAge}岁";
User user = new User("赵六", 35);
String result = templateUtil.renderString(template, user);
```

### 4. 从指定路径加载模板
**从 classpath 路径：**
```java
String result = templateUtil.renderFromPath("templates/custom/welcome.txt", model);
```

**从文件系统路径：**
```java
String result = templateUtil.renderFromFile("/path/to/template.txt", model);
```

## 数据模型对象要求
对象需要符合 Jackson 的序列化规则：
- 使用 getter 方法或 public 字段
- 可以使用 `@JsonProperty` 注解自定义字段名
- 支持嵌套对象和集合

示例 User 类：
```java
public class User {
    private String userName;
    private int userAge;
    
    // 必须有无参构造函数
    public User() {}
    
    public User(String userName, int userAge) {
        this.userName = userName;
        this.userAge = userAge;
    }
    
    // getter 方法
    public String getUserName() { return userName; }
    public int getUserAge() { return userAge; }
    
    // setter 方法（可选）
    public void setUserName(String userName) { this.userName = userName; }
    public void setUserAge(int userAge) { this.userAge = userAge; }
}
```

## 模板语法
使用 Thymeleaf 的文本模板语法：
- `${variable}` - 输出变量的值
- `[[${variable}]]` - 输出变量的值（HTML转义，在文本模板中效果相同）
- 支持条件判断、循环等高级功能

**文本模板示例：**
```
尊敬的 ${customerName}：

感谢您购买 ${productName}。
订单号：${orderId}
订单金额：${amount}元
下单时间：${orderTime}

${#strings.isEmpty(notes) ? '' : '备注：' + notes}

祝您购物愉快！
${companyName}
${currentDate}
```

## 高级功能
### 条件判断
```
${#strings.isEmpty(address) ? '地址未填写' : '地址：' + address}
```

### 循环
```
购买的商品：
${#lists.each(item : items)}
  - ${item.name} × ${item.quantity}
${/lists.each}
```

### 工具函数
- `${#strings.isEmpty(str)}` - 判断字符串是否为空
- `${#dates.format(date, 'yyyy-MM-dd')}` - 日期格式化
- `${#numbers.formatDecimal(number, 1, 2)}` - 数字格式化

## 错误处理
- 如果模板文件不存在，会抛出 IOException
- 如果数据模型转换失败，会抛出 IllegalArgumentException
- 建议在生产环境中添加适当的异常处理

## 性能优化
- 类路径模板默认启用缓存（1小时）
- 字符串模板不缓存，适合动态内容
- 可以通过修改 TemplateUtil 中的配置调整缓存策略

## 示例模板文件
**templates/welcome.txt:**
```
欢迎 ${name} 加入我们的社区！

您的注册信息：
用户名：${username}
邮箱：${email}
注册时间：${registerTime}

请妥善保管您的账户信息。
如有问题，请联系客服：${supportEmail}

${companyName} 团队
${currentDate}
```