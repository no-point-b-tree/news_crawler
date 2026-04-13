package com.archer.task.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.microsoft.playwright.options.LoadState;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import com.archer.task.entity.Article;
import com.archer.task.service.LatestSpiderTaskService;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Playwright.CreateOptions;
import com.microsoft.playwright.Response;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LatestSpiderTaskServiceImpl implements LatestSpiderTaskService {

    private static final ArrayBlockingQueue<LatestSpiderTaskServiceImpl> queue = new ArrayBlockingQueue<>(10);

    static Properties properties;

    public static LatestSpiderTaskServiceImpl getInstance(String active) {
        synchronized (LatestSpiderTaskServiceImpl.class) {
            if (properties == null) {
                synchronized (LatestSpiderTaskServiceImpl.class) {
                    properties = new Properties();
                    try {
                        properties.load(LatestSpiderTaskServiceImpl.class.getResourceAsStream(String.format("/playwright/config-%s.properties", active)));
                        int count = Integer.parseInt(properties.getProperty("instanceNum", "1"));
                        for (int i = 0; i < count; i++) {
                            queue.add(new LatestSpiderTaskServiceImpl());
                        }
                    } catch (IOException e) {
                        log.error("加载配置失败", e);
                    }
                }
            }
        }
        try {
            return queue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void release() {
        // 把自己还回到池子
        try {
            queue.put(this);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private LatestSpiderTaskServiceImpl() {

    }

    private final Playwright playwright;

    {
        Map<String, String> env = new HashMap<>();
        env.put("PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD", "1");
        CreateOptions createOptions = new CreateOptions();
        createOptions.setEnv(env);
        playwright = Playwright.create(createOptions);
    }


    @Override
    public Browser getBrowser() {
        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions();
        String executablePath = properties.getProperty("executablePath");
        if (executablePath != null) {
            options.setExecutablePath(Paths.get(executablePath));
        }
        String headlessStr = properties.getProperty("headless", "false");
        boolean headless = Boolean.parseBoolean(headlessStr);
        options.setHeadless(headless);
        return playwright.chromium().launch(options);
    }

    @Override
    public List<String> findRssUrlList(String url) {
        Set<String> rssUrls = new HashSet<>();
        Browser browser = getBrowser();
        Page page = null;

        try {

            page = browser.newPage();
            page.setViewportSize(1920, 1080);

            page.navigate(url, new Page.NavigateOptions());
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);

            String html = page.content();
            rssUrls.addAll(extractRssLinksWithJsoup(html, url));

            List<String> newsPageUrls = findNewsPageUrlsFromHtml(html, url);
            for (String newsUrl : newsPageUrls) {
                try {
                    page.navigate(newsUrl, new Page.NavigateOptions());
                    page.waitForLoadState(LoadState.DOMCONTENTLOADED);
                    String newsHtml = page.content();
                    rssUrls.addAll(extractRssLinksWithJsoup(newsHtml, newsUrl));
                } catch (Exception e) {
                    log.debug("Failed to fetch news page: {}", newsUrl);
                }
            }

        } catch (Exception e) {
            log.error("Failed to find RSS URLs from {}: {}", url, e.getMessage());
        } finally {
            page.close();
        }

        return new ArrayList<>(rssUrls);
    }

    private List<String> findNewsPageUrlsFromHtml(String html, String baseUrl) {
        Set<String> newsUrls = new HashSet<>();
        try {
            Document doc = Jsoup.parse(html);

            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String href = link.attr("href");
                if (href != null && !href.isEmpty()) {
                    String fullUrl = resolveUrl(baseUrl, href);
                    if (isNewsPage(fullUrl) && isSameDomain(baseUrl, fullUrl)) {
                        newsUrls.add(fullUrl);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Failed to find news page URLs: {}", e.getMessage());
        }
        return new ArrayList<>(newsUrls);
    }

    private List<String> extractRssLinksWithJsoup(String html, String baseUrl) {
        List<String> rssLinks = new ArrayList<>();
        try {
            Document doc = Jsoup.parse(html);

            Elements rssElements = doc.select("link[type='application/rss+xml'], link[type='application/atom+xml']");
            for (Element link : rssElements) {
                String href = link.attr("href");
                if (href != null && !href.isEmpty()) {
                    String fullUrl = resolveUrl(baseUrl, href);
                    if (!rssLinks.contains(fullUrl)) {
                        rssLinks.add(fullUrl);
                    }
                }
            }

            Elements anchorLinks = doc.select("a[href]");
            for (Element link : anchorLinks) {
                String href = link.attr("href");
                String text = link.text().toLowerCase();
                if (href != null && !href.isEmpty() && isLikelyRssAnchor(href, text)) {
                    String fullUrl = resolveUrl(baseUrl, href);
                    if (!rssLinks.contains(fullUrl)) {
                        rssLinks.add(fullUrl);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Failed to extract RSS links with Jsoup: {}", e.getMessage());
        }
        return rssLinks;
    }

    private boolean isLikelyRssAnchor(String href, String text) {
        String lowerHref = href.toLowerCase();
        String[] rssKeywords = {"rss", "feed", "atom", "subscribe"};
        for (String keyword : rssKeywords) {
            if (lowerHref.contains(keyword) || text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private boolean isNewsPage(String url) {
        String lowerUrl = url.toLowerCase();
        String[] newsKeywords = {"news", "blog", "article", "articles", "post", "posts", "report", "reports", "updates", "latest"};
        for (String keyword : newsKeywords) {
            if (lowerUrl.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSameDomain(String url1, String url2) {
        try {
            java.net.URL a1 = new java.net.URL(url1);
            java.net.URL a2 = new java.net.URL(url2);
            return a1.getHost().equals(a2.getHost());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<String> findNewsPageUrlList(String url) {
        List<String> newsUrls = new ArrayList<>();
        Playwright playwright = null;
        BrowserContext context = null;
        Page page = null;

        try {
            playwright = Playwright.create();
            context = playwright.chromium().launch().newContext();
            page = context.newPage();

            page.navigate(url, new Page.NavigateOptions().setTimeout(10000));

            List<String> keywords = List.of("news", "blog", "report", "articles", "posts", "latest");

            List<Locator> navLinks = page.locator("nav a, header a, .nav a, .menu a, [class*='nav'] a, [class*='menu'] a").all();

            for (Locator link : navLinks) {
                try {
                    String href = link.getAttribute("href");
                    if (href != null && !href.isEmpty()) {
                        String fullUrl = resolveUrl(url, href);
                        if (isValidUrl(fullUrl) && !newsUrls.contains(fullUrl)) {
                            newsUrls.add(fullUrl);
                        }
                    }
                } catch (Exception e) {
                    log.debug("Nav link check failed");
                }
            }

            for (String keyword : keywords) {
                List<Locator> keywordLinks = page.locator("a[href*='" + keyword + "'], button[data-href*='" + keyword + "']").all();
                for (Locator link : keywordLinks) {
                    try {
                        String href = link.getAttribute("href");
                        if (href == null) {
                            href = link.getAttribute("data-href");
                        }
                        if (href != null && !href.isEmpty()) {
                            String fullUrl = resolveUrl(url, href);
                            if (isValidUrl(fullUrl) && !newsUrls.contains(fullUrl)) {
                                newsUrls.add(fullUrl);
                            }
                        }
                    } catch (Exception e) {
                        log.debug("Keyword link check failed");
                    }
                }
            }

        } catch (Exception e) {
            log.error("Failed to find news page URLs from {}: {}", url, e.getMessage());
        } finally {
            closeQuietly(page, context, playwright);
        }

        return newsUrls;
    }

    @Override
    public List<Article> getRssResult(List<String> rssUrlList, Date startDate, Date endDate) {
        List<Article> articles = new ArrayList<>();
        if (rssUrlList == null || rssUrlList.isEmpty()) {
            return articles;
        }

        try {
            for (String rssUrl : rssUrlList) {
                try {
                    List<Article> articalList = parseRss(rssUrl);
                    for (Article article : articalList) {
                        if (article.getPublishDate().compareTo(startDate) >= 0 && article.getPublishDate().compareTo(endDate) <= 0) {
                            articles.add(article);
                        }
                    }
                } catch (Exception e) {
                    log.debug("Failed to fetch RSS from {}: {}", rssUrl, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Failed to get RSS results: {}", e.getMessage());
        }

        return articles;
    }

    @Override
    public List<Article> getNewsPageResult(List<String> pageUrlList) {
        List<Article> articles = new ArrayList<>();

        if (pageUrlList == null || pageUrlList.isEmpty()) {
            return articles;
        }

        Playwright playwright = null;

        try {
            playwright = Playwright.create();
            Browser browser = playwright.chromium().launch();

            for (String pageUrl : pageUrlList) {
                BrowserContext context = null;
                Page page = null;
                try {
                    context = browser.newContext();
                    page = context.newPage();
                    page.setViewportSize(1920, 1080);

                    Response response = page.navigate(pageUrl);

                    if (response != null && response.ok()) {
                        String title = page.title();
                        String content = page.locator("body").innerText();

                        Article article = new Article();
                        article.setTitle(title);
                        article.setContent(content);
                        article.setPublishDate(new Date());
                        articles.add(article);
                    }
                } catch (Exception e) {
                    log.debug("Failed to fetch page {}: {}", pageUrl, e.getMessage());
                } finally {
                    closeQuietly(page, context, null);
                }
            }

            browser.close();
        } catch (Exception e) {
            log.error("Failed to get news page results: {}", e.getMessage());
        } finally {
            if (playwright != null) {
                playwright.close();
            }
        }

        return articles;
    }

    @Override
    public List<Article> parseRss(String rssUrl) {
        List<Article> articles = new ArrayList<>();
        try (Browser browser = getBrowser()) {
            Page page = browser.newPage();
            page.setViewportSize(1920, 1080);
            Response response = page.navigate(rssUrl, new Page.NavigateOptions());
            String xmlContent = response.text();
            Document doc = Jsoup.parse(xmlContent, "", org.jsoup.parser.Parser.xmlParser());
            Elements items = doc.select("item, entry");
            for (Element item : items) {
                Article article = new Article();

                String title = item.selectFirst("title").text();
                article.setTitle(title);

                String link = "";
                Element linkElement = item.selectFirst("link");
                if (linkElement != null) {
                    link = linkElement.text();
                    if (link.isEmpty()) {
                        link = linkElement.attr("href");
                    }
                }
                if (link.isEmpty()) {
                    Elements links = item.select("link[href]");
                    for (Element le : links) {
                        String href = le.attr("href");
                        if (href != null && !href.isEmpty()) {
                            link = href;
                            break;
                        }
                    }
                }
                article.setUrl(link);

                String description = "";
                Element descElement = item.selectFirst("description, summary, content\\:encoded, content");
                if (descElement != null) {
                    description = descElement.text();
                }
                article.setDescription(description);

                String pubDate = "";
                Element dateElement = item.selectFirst("pubDate, published, updated, dc\\:date, date");
                if (dateElement != null) {
                    pubDate = dateElement.text();
                    article.setPublishDate(parseRssDate(pubDate));
                }
                article.setContent(getArticleContent(article.getUrl()).getContent());
                articles.add(article);
            }

        } catch (Exception e) {
            log.error("Failed to parse RSS from {}: ", rssUrl, e);
        }
        return articles;
    }

    @Override
    public Article getArticleContent(String articleUrl) {
        Article article = new Article();

        try (Browser browser = getBrowser()) {
            Page page = browser.newPage();
            page.setViewportSize(1920, 1080);
            page.navigate(articleUrl, new Page.NavigateOptions());
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
            String title = page.title();
            article.setUrl(articleUrl);
            article.setTitle(title);
            article.setContent(page.content());

        } catch (Exception e) {
            log.error("Failed to get article content from {}", articleUrl, e);
        }
        return article;
    }

    private Date parseRssDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return new Date();
        }
        try {
            java.text.SimpleDateFormat[] formats = {
                    new java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", java.util.Locale.ENGLISH),
                    new java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", java.util.Locale.ENGLISH),
                    new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX"),
                    new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"),
                    new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"),
                    new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
                    new java.text.SimpleDateFormat("yyyy-MM-dd")
            };
            for (java.text.SimpleDateFormat format : formats) {
                try {
                    return format.parse(dateStr);
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            log.debug("Failed to parse date: {}", dateStr);
        }
        return new Date();
    }

    private String buildFullUrl(String baseUrl, String path) {
        String result = baseUrl;
        if (!baseUrl.endsWith("/")) {
            result += "/";
        }
        if (path.startsWith("/")) {
            result += path.substring(1);
        } else {
            result += path;
        }
        return result;
    }

    private boolean isRssContent(APIResponse response) {
        if (response.headers() == null) {
            return false;
        }
        String contentType = response.headers().get("content-type");
        return contentType != null && (contentType.contains("xml") || contentType.contains("rss") || contentType.contains("atom"));
    }

    private String resolveUrl(String baseUrl, String relativeUrl) {
        if (relativeUrl == null || relativeUrl.isEmpty()) {
            return baseUrl;
        }
        if (relativeUrl.startsWith("http://") || relativeUrl.startsWith("https://")) {
            return relativeUrl;
        }
        try {
            return java.net.URLEncoder.encode(relativeUrl, "UTF-8");
        } catch (Exception e) {
            return relativeUrl;
        }
    }

    private boolean isValidUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }

    private String extractRssTitle(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        int titleStart = content.indexOf("<title>");
        int titleEnd = content.indexOf("</title>");
        if (titleStart != -1 && titleEnd != -1 && titleEnd > titleStart) {
            return content.substring(titleStart + 7, titleEnd).trim();
        }
        return "";
    }

    private Date extractRssDate(String content) {
        if (content == null || content.isEmpty()) {
            return new Date();
        }
        String[] patterns = {"<pubDate>", "<dc:date>", "<published>", "<updated>"};
        for (String pattern : patterns) {
            int dateStart = content.indexOf(pattern);
            if (dateStart != -1) {
                int dateEnd = content.indexOf("</", dateStart);
                if (dateEnd != -1) {
                    String dateStr = content.substring(dateStart + pattern.length(), dateEnd).trim();
                    try {
                        return parseDate(dateStr);
                    } catch (Exception e) {
                        log.debug("Failed to parse date: {}", dateStr);
                    }
                }
            }
        }
        return new Date();
    }

    private Date parseDate(String dateStr) {
        try {
            java.text.SimpleDateFormat[] formats = {
                    new java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", java.util.Locale.ENGLISH),
                    new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX"),
                    new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
                    new java.text.SimpleDateFormat("yyyy-MM-dd")
            };
            for (java.text.SimpleDateFormat format : formats) {
                try {
                    return format.parse(dateStr);
                } catch (Exception e) {
                    log.error("Failed to parse date: {}", dateStr);
                    // continue to next format
                }
            }
        } catch (Exception e) {
            log.debug("Date parsing failed: {}", dateStr);
        }
        return new Date();
    }

    private void closeQuietly(Page page, BrowserContext context, Playwright playwright) {
        try {
            if (page != null) page.close();
        } catch (Exception e) {
            log.debug("Error closing page");
        }
        try {
            if (context != null) context.close();
        } catch (Exception e) {
            log.debug("Error closing context");
        }
        try {
            if (playwright != null) playwright.close();
        } catch (Exception e) {
            log.debug("Error closing playwright");
        }
    }

}
