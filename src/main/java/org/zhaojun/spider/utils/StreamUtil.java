package org.zhaojun.spider.utils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zhaojun on 2016/6/25.
 */
public class StreamUtil {

    /**
     * @param inputStream
     * @return
     */
    public static String streamToString(InputStream inputStream) {
        try {
            StringBuilder ret = new StringBuilder();
            byte[] bytes = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(bytes)) != -1) {
                ret.append(String.valueOf(bytes));
                System.out.println(bytes.toString());
            }
            return ret.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
