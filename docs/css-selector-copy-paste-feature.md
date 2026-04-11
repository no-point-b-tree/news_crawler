# CSS选择器一键复制/粘贴功能

## 功能概述
在CSS选择器配置页面添加了一键复制和一键粘贴功能，方便用户微调参数后快速添加新的CSS选择器配置。

## 新增功能

### 1. 一键复制（编辑对话框）
- **位置**：编辑CSS选择器配置对话框底部
- **按钮**：灰色"一键复制"按钮
- **功能**：将当前编辑的配置复制到剪贴板
- **格式**：JSON格式，包含所有字段

### 2. 一键同步（列表操作）
- **位置**：已配置CSS选择器列表的操作列
- **按钮**：绿色"一键同步"按钮
- **功能**：将选中的配置同步到添加表单
- **效果**：自动填充表单并滚动到添加区域

### 3. 一键粘贴（添加表单）
- **位置**：添加CSS选择器配置表单底部
- **按钮**：灰色"一键粘贴"按钮
- **功能**：从剪贴板读取配置并填充表单
- **支持格式**：JSON格式或简单文本格式

## 使用场景

### 场景1：微调现有配置后添加新配置
1. 点击"编辑"按钮修改现有配置
2. 微调参数（如修改选择器或日期格式）
3. 点击"一键复制"复制配置
4. 关闭编辑对话框
5. 点击"一键粘贴"填充到添加表单
6. 进一步微调后点击"添加配置"

### 场景2：基于现有配置快速添加相似配置
1. 在已配置列表中找到相似配置
2. 点击"一键同步"按钮
3. 表单自动填充选中配置
4. 微调必要参数
5. 点击"添加配置"

### 场景3：跨网站复制配置
1. 在网站A的CSS选择器配置中点击"一键复制"
2. 切换到网站B的CSS选择器管理页面
3. 点击"一键粘贴"
4. 根据网站B的特点微调配置
5. 点击"添加配置"

## 技术实现

### 复制功能
```javascript
// 使用现代Clipboard API
navigator.clipboard.writeText(configJson)

// 降级方案：使用document.execCommand
const textArea = document.createElement('textarea');
textArea.value = configJson;
document.body.appendChild(textArea);
textArea.select();
document.execCommand('copy');
document.body.removeChild(textArea);
```

### 粘贴功能
```javascript
// 读取剪贴板内容
const clipboardText = await navigator.clipboard.readText();

// 尝试解析JSON格式
try {
  const config = JSON.parse(clipboardText);
  // 更新表单数据
} catch {
  // 降级：尝试文本解析
  // 查找关键词如"cssSelectorGroup"、"日期选择器"等
}
```

### 同步功能
```javascript
// 直接将行数据同步到添加表单
Object.assign(cssSelectorDialog.formData, {
  cssSelectorGroup: row.cssSelectorGroup,
  dateCssSelector: row.dateCssSelector,
  // ...其他字段
});
```

## 配置格式示例

### JSON格式（一键复制生成）
```json
{
  "cssSelectorGroup": ".article-list .item",
  "dateCssSelector": ".date",
  "hyperLinkCssSelector": "a.title",
  "datePattern": "yyyy-MM-dd HH:mm:ss",
  "enabled": "ON"
}
```

### 文本格式（也支持粘贴）
```
cssSelectorGroup: .article-list .item
dateCssSelector: .date
hyperLinkCssSelector: a.title
datePattern: yyyy-MM-dd HH:mm:ss
enabled: ON
```

## 用户体验优化

### 1. 视觉反馈
- 成功操作显示绿色提示消息
- 失败操作显示红色错误消息
- 按钮加载状态指示

### 2. 交互优化
- 同步后自动滚动到添加表单
- 粘贴后自动聚焦到第一个输入框
- 响应式设计，移动端适配

### 3. 错误处理
- 剪贴板为空时友好提示
- 无效JSON格式时尝试文本解析
- 最终降级方案：手动输入提示

## 浏览器兼容性

### 支持的浏览器
- Chrome 66+ ✅
- Firefox 63+ ✅
- Safari 13.1+ ✅
- Edge 79+ ✅

### 功能降级
1. **Clipboard API不可用**：使用document.execCommand
2. **读取剪贴板权限被拒绝**：显示手动输入提示
3. **JSON解析失败**：尝试文本解析

## 测试用例

### 测试1：一键复制
1. 打开CSS选择器编辑对话框
2. 修改一些参数
3. 点击"一键复制"
4. 验证剪贴板内容是否正确

### 测试2：一键同步
1. 在已配置列表中找到一条记录
2. 点击"一键同步"
3. 验证添加表单是否被正确填充
4. 验证页面是否滚动到添加区域

### 测试3：一键粘贴
1. 复制一个有效的配置JSON
2. 点击"一键粘贴"
3. 验证表单是否被正确填充
4. 尝试粘贴文本格式配置

### 测试4：错误处理
1. 清空剪贴板后点击"一键粘贴"
2. 复制无效JSON后点击"一键粘贴"
3. 在无权限环境中测试复制/粘贴

## 维护说明

### 新增字段处理
如果未来添加新的CSS选择器配置字段：
1. 在复制函数中添加新字段
2. 在粘贴函数中添加新字段解析
3. 在同步函数中添加新字段同步

### 样式调整
CSS样式已模块化，可通过修改以下类名调整：
- `.action-buttons`：操作按钮组
- `.dialog-footer`：对话框底部按钮
- 响应式断点：`@media (max-width: 768px)`

## 性能考虑
- 复制/粘贴操作是纯前端操作，无网络请求
- 使用异步API避免阻塞主线程
- 内存使用：临时创建textarea元素后立即清理