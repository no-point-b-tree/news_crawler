package com.archer.task.util;

import java.net.URI;
import java.net.URISyntaxException;

public class UrlUtils {

    /**
     * 提取URL的协议和主机部分（包括端口，若为非默认端口）
     * 例如：输入 "https://cnpd.public.lu/fr/decisions-avis/..."
     *       返回 "https://cnpd.public.lu"
     *
     * @param url 完整的URL字符串
     * @return 协议 + "://" + 主机名（含端口），若URL无效则返回null
     */
    public static String extractBaseUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if (scheme == null || host == null) {
                return null; // 不是有效的绝对URL
            }
            int port = uri.getPort();
            // 构建基础部分
            StringBuilder base = new StringBuilder();
            base.append(scheme).append("://").append(host);
            if (port != -1 && !isDefaultPort(scheme, port)) {
                base.append(":").append(port);
            }
            return base.toString();
        } catch (URISyntaxException e) {
            // 可选：记录日志
            return null;
        }
    }

    /**
     * 判断端口是否为对应协议的默认端口
     */
    private static boolean isDefaultPort(String scheme, int port) {
        if ("http".equalsIgnoreCase(scheme)) {
            return port == 80;
        } else if ("https".equalsIgnoreCase(scheme)) {
            return port == 443;
        }
        return false; // 其他协议视为非默认，保留端口
    }
}