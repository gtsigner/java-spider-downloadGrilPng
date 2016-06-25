package org.zhaojun.spider.utils;

/**
 * Created by zhaojun on 2016/6/25.
 */

import org.zhaojun.spider.inter.CallBackFunction;
import org.zhaojun.spider.inter.DownloadImgCallBack;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;

/**
 * 下载图片
 */
public class ImageUtil {

    /**
     * 自动文件名称
     *
     * @return
     */
    public static String createFileName() {
        Calendar calendar = Calendar.getInstance();
        String time = String.valueOf(calendar.getTime().getTime());
        return time;
    }

    public static String createSavePath() {
        Calendar cal = Calendar.getInstance();
        int y, m, d;
        y = cal.get(Calendar.YEAR);
        m = cal.get(Calendar.MONTH);
        d = cal.get(Calendar.DATE);
        return y + "-" + m + "-" + d;
    }

    /**
     * 下载图片
     *
     * @param uri
     * @param filename
     * @param savePath
     * @param callBack
     */
    public static void downloadImg(String uri, String filename, String savePath, DownloadImgCallBack callBack) {

        try {


            URL url = new URL(uri);
            // 打开连接
            URLConnection con = url.openConnection();
            //设置请求超时为5s
            con.setConnectTimeout(3 * 1000);
            // 输入流
            InputStream is = con.getInputStream();

            // 1K的数据缓冲
            byte[] bs = new byte[1024];
            // 读取到的数据长度
            int len;
            // 输出的文件流
            File sf = new File(savePath);
            if (!sf.exists()) {
                sf.mkdirs();
            }
            OutputStream os = new FileOutputStream(sf.getPath() + "\\" + filename);
            // 开始读取
            while ((len = is.read(bs)) != -1) {
                os.write(bs, 0, len);
            }
            callBack.success(filename, savePath);
            // 完毕，关闭所有链接
            is.close();
            os.close();


        } catch (Exception e) {
            callBack.error(e.getMessage());
        }
    }

}
