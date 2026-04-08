# rss网站检索工具

1. 作为一名程序开发者，我需要你使用playwright打开网站搜索这个网站的所有rss链接，并保存到集合里面。用于订阅最新的新闻。
2. 方法签名可以符合 List<String> findRssUrlList(String url); 要求在com.archer.task.service.impl.LatestSpiderTaskServiceImpl类下
3. 业务要求如下，首先打开提供的网站，检索该页面的rss链接并保存，考虑rss链接不一定刚好在首页，需要加入检索该页面所有导航栏的资讯页。资讯页一般是news,blog，Artical这些。并在资讯类检索rss链接
4. 检索rss链接的时候注意直接使用playwright打开页面后，后面提取rss的链接直接使用jsoup去提取rss链接。请确保尽可能匹配更多的rss链接。如果当前工程没有jsoup，请加入相关依赖