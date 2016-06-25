package org.zhaojun.spider.controller;

import com.sun.xml.internal.fastinfoset.util.CharArray;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.DefaultHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.util.EntityUtils;
import org.zhaojun.spider.conf.Conf;
import org.zhaojun.spider.inter.CallBackFunction;
import org.zhaojun.spider.inter.DownloadImgCallBack;
import org.zhaojun.spider.utils.ImageUtil;
import org.zhaojun.spider.utils.StreamUtil;
import org.zhaojun.spider.utils.XxOoUtil;
import redis.clients.jedis.Jedis;
import sun.net.www.http.HttpClient;

import javax.security.auth.callback.CallbackHandler;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhaojun on 2016/6/24.
 */
public class SpiderController {


    protected String xxooRoot = "http://www.mzitu.com/xinggan";//妹子图根目录
    protected String downloadRoot = "http://pic.mmfile.net";//下载图片的根路径


    protected Jedis jedis;//临时存
    protected String prefix_Page = "xx_p_";//前缀
    protected String prefix_Src = "xx_s_";//src前缀
    protected String prefix_img = "xx_img_";//图片源地址前缀

    /*redis 数据库中的key*/
    protected String redis_img_key = prefix_img + "list";
    protected String redis_pages_key = prefix_Page + "list";
    protected String redis_src_key = prefix_Src + "list";

    protected LogsController logsControl;
    protected CloseableHttpAsyncClient httpClient;

    //但是默认了2
    protected int currentPageNum = 2;//当前应该是在第一页
    protected int maxPageNum = 67;//这个需要我们去采集得到

    //页面的默认根路径
    protected String pageRoot = "http://www.mzitu.com/xinggan/page/";

    protected boolean isImgDownloading = false;//是否正在下载图片
    protected boolean isSrcParsing = false;//是否正在解析图片源
    protected boolean isPagesParsing = false;//是否正在解析分页源
    protected boolean isImagesParsing = false;//正在解析图片

    protected Timer timer;

    protected String userAgent = "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:47.0) Gecko/20100101 Firefox/47.0";

    public void start() {
        //初始化redis
        jedis = new Jedis(Conf.Redis_Host, Conf.Redis_Port);
        logsControl = new LogsController();
        httpClient = HttpAsyncClients.createDefault();
        this.httpClient.start();
        /**
         * 相当于心跳函数
         *
         */


        /**
         * 10秒一个源请求包
         */
        this.timer = new Timer(10000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                if (isPagesParsing == false && currentPageNum <= maxPageNum) {
                    isPagesParsing = true;
                    initPages(xxooRoot);//解析分页数据
                }
                if (isSrcParsing == false) {
                    isSrcParsing = true;
                    initSrc(pageRoot);//从分页数据中解析详细的妹子图数据
                }

                if (isImagesParsing == false) {
                    isImagesParsing = true;
                    initImages();
                }


            }
        });
        timer.start();



        /*3秒心跳下载一个图片*/
        Timer downTimer = new Timer(2000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //定时器去发送函数心跳包
                if (isImgDownloading == false) {
                    String url = jedis.lpop(redis_img_key);//获取一个源
                    if (url != null) {
                        System.out.println("下载图片：" + url);
                        isImgDownloading = true;
                        ImageUtil.downloadImg(url, ImageUtil.createFileName() + ".jpg", "./download/" + ImageUtil.createSavePath() + "/", new DownloadImgCallBack() {
                            public void success(String fileSaveName, String savePath) {
                                isImgDownloading = false;
                                System.out.println("图片下载成功保存路径:" + savePath + fileSaveName);
                            }

                            public void error(String msg) {
                                isImgDownloading = false;
                            }
                        });
                        System.out.println("1");

                    } else {
                        System.out.println("redis列表里面没有图片了");
                    }
                }
            }
        });
        downTimer.start();

    }

    /**
     * 加载列表数据分页，其实只是需要判断最大页面数量
     *
     * @param url
     */
    private void initPages(final String url) {
        System.out.println("请求分页数据");
        HttpGet get = new HttpGet(url);//一个获取页面的请求
        get.setHeader("User-Agent", userAgent);
        /**
         * 请求页面成功
         */
        Future<HttpResponse> future = httpClient.execute(get, new FutureCallback<HttpResponse>() {
            public void completed(HttpResponse httpResponse) {

                StatusLine statusLine = httpResponse.getStatusLine();
                if (statusLine.getStatusCode() != 200) {
                    //请求失败，切换代理模式
                    logsControl.console("请求失败-Url:" + url + "，Code:" + statusLine.getStatusCode() + "", LogsController.LOG_ERROR);
                } else {
                    HttpEntity entity = httpResponse.getEntity();
                    try {
                        String html = EntityUtils.toString(entity);//获取到静态页面
                        //进行缓存
                        html = html.replace("\n", "");
                        Pattern p = Pattern.compile("<span(.*?)class=\"meta-nav(.*?)screen(.*?)></span>[1-9]{0,5}");
                        Matcher matcher = p.matcher(html);
                        int maxPage = 0;
                        while (matcher.find()) {
                            String str = matcher.group();
                            str = str.replace("<span class=\"meta-nav screen-reader-text\"></span>", "");
                            str = str.replace("\n", "");
                            System.out.println(str);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }

            public void failed(Exception e) {

            }

            public void cancelled() {

            }
        });
    }

    /**
     * 加载分页解析后的链接
     *
     * @param urlSrc
     */
    public void initSrc(String urlSrc) {
        urlSrc = urlSrc + currentPageNum;//加上页号
        HttpGet get = new HttpGet(urlSrc);
        System.out.println("src=" + urlSrc);
        get.setHeader("User-Agent", userAgent);
        Future<HttpResponse> futureSrc = httpClient.execute(get, new FutureCallback<HttpResponse>() {
            /**
             * 完成数据
             * @param httpResponse
             */
            public void completed(HttpResponse httpResponse) {
                StatusLine statusLine = httpResponse.getStatusLine();
                if (statusLine.getStatusCode() != 200) {
                    //请求失败，切换代理模式
                    System.out.println("请求失败");
                } else {
                    HttpEntity entity = httpResponse.getEntity();
                    System.out.println("开始解析Html---");
                    try {
                        String htmlDom = EntityUtils.toString(entity);//获取到静态页面
                        //进行缓存
                        htmlDom = htmlDom.replace("\n", "");
                        System.out.println(htmlDom);
                        Pattern p = Pattern.compile("<ul id=\"pins\"([\\s\\S]*)</ul>");//匹配所有图片信息
                        Matcher matcher = p.matcher(htmlDom);
                        System.out.println("Doing：匹配妹子图列列表");
                        while (matcher.find()) {
                            String str = matcher.group();
                            Pattern pli = Pattern.compile("<li>([\\s\\S]*?)</li>");
                            Matcher matcherLi = pli.matcher(str);
                            while (matcherLi.find()) {
                                String srcc = XxOoUtil.matchSource(matcherLi.group());
                                System.out.println("Doing：解析妹子图路径" + srcc);
                                jedis.rpush(redis_src_key, srcc);//加入redis右边
                            }
                            //ul中
                        }
                        System.out.println("结束页面采集：current-" + currentPageNum);
                        currentPageNum++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //这里触发调用下载
                }

            }

            public void failed(Exception e) {

            }

            public void cancelled() {

            }
        });
    }


    /**
     * 这才是获取高清大图的函数，这里还没有下载，只是加载数据到redis
     */
    public void initImages() {
        String url = jedis.lpop(redis_src_key);//获取路径
        if (url == null) {
            System.out.println("等待Src数据解析");
            return;
        }
        HttpGet get = new HttpGet(url);
        System.out.println("采集高清大图:" + url);
        get.setHeader("User-Agent", userAgent);
        Future<HttpResponse> futureSrc = httpClient.execute(get, new FutureCallback<HttpResponse>() {
            /**
             * 完成数据
             * @param httpResponse
             */
            public void completed(HttpResponse httpResponse) {
                StatusLine statusLine = httpResponse.getStatusLine();
                if (statusLine.getStatusCode() != 200) {
                    //请求失败，切换代理模式
                    System.out.println("请求失败");
                } else {
                    HttpEntity entity = httpResponse.getEntity();
                    try {
                        String htmlDom = EntityUtils.toString(entity);//获取到静态页面
                        //进行缓存
                        htmlDom = htmlDom.replace("\n", "");
                        Pattern p = Pattern.compile("http://pic.mmfile.net/2016/(.*?).jpg");//匹配大图地址路径和根
                        Matcher matcher = p.matcher(htmlDom);
                        String ful = "";
                        while (matcher.find()) {
                            ful = matcher.group();
                            System.out.println(ful);
                        }
                        p = Pattern.compile("class=['|\"]pagenavi([\\s\\S]*?)</div>");
                        Matcher matcher1 = p.matcher(htmlDom);
                        int max = 10;
                        String pagesF = "";
                        while (matcher1.find()) {
                            pagesF = matcher1.group();
                        }
                        p = Pattern.compile("<span>[\\d]{1,2}</span>");
                        Matcher matcherPage = p.matcher(pagesF);
                        while (matcherPage.find()) {
                            String pageSS = matcherPage.group();
                            pageSS = pageSS.replace("<span>", "").replace("</span>", "").replace(" ", "");
                            try {
                                max = Integer.parseInt(pageSS);
                            } catch (Exception ex) {

                            }
                        }

                        //加入redis
                        for (int i = 1; i <= max; i++) {
                            StringBuilder builder = new StringBuilder(ful);
                            //开始替换
                            String page = i + "";
                            if (i <= 9) {
                                page = "0" + i;
                            }
                            int at = builder.length() - 6;
                            builder.replace(at, at + 2, page);
                            //array[at] = page.substring(0, 1).toCharArray()[0];//第一个字符
                            //array[at + 1] = page.substring(1, 2).toCharArray()[0];//第二个字符
                            System.out.println("成功获取图片：" + builder.toString());
                            jedis.rpush(redis_img_key, builder.toString());//加入
                        }

                        System.out.println("这次找到该页妹子图：" + max + "张");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //这里触发调用下载
                }

            }

            public void failed(Exception e) {

            }

            public void cancelled() {

            }
        });
    }

    /**
     * STOP
     */
    public void stop() {
        this.timer.stop();
    }

    /**
     * 销毁资源
     */
    public void destroy() {
        jedis = null;
        try {
            httpClient.close();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
