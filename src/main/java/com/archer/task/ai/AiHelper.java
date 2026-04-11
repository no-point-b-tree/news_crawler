package com.archer.task.ai;

public interface AiHelper {

    /**
     * 调用AI接口处理文本并返回JSON结果
     * @param prompt 输入的提示文本
     * @return JSON格式的响应字符串
     */
    String processWithAi(String prompt);
}
