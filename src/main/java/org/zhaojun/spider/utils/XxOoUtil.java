package org.zhaojun.spider.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhaojun on 2016/6/25.
 */
public class XxOoUtil {


    /**
     * 解析li里的数据
     *
     * @param liHtml
     * @return
     */
    public static String matchSource(String liHtml) {
        Pattern pattern = Pattern.compile("http://www.mzitu.com/[1-9][0-9]{0,10}[^'|\"]");
        Matcher matcher = pattern.matcher(liHtml);
        String str = "";
        while (matcher.find()) {
            str = matcher.group();
            break;
        }
        return str;
    }

    public static String matchImages(String html) {

        return "";
    }
}
