package org.zhaojun.spider.inter;

/**
 * Created by zhaojun on 2016/6/25.
 */
public interface DownloadImgCallBack {

    public void success(String fileSaveName, String savePath);

    public void error(String msg);
}
