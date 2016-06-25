package org.zhaojun.spider.controller;

/**
 * Created by zhaojun on 2016/6/25.
 */
public class LogsController {
    /*写日志*/

    public static final int LOG_ERROR = -1;
    public static final int LOG_SUCCESS = 1;
    public static final int LOG_WARING = 5;//警告
    public static final int LOG_INFO = 2;//信息


    /**
     * 信息，等级
     *
     * @param msg
     * @param level
     */
    public void console(String msg, int level) {

        switch (level) {
            case LOG_ERROR:
                System.err.println(msg);
                break;
            default:
                System.out.println(msg);
                break;
        }
    }

    /**
     * 写入日志
     *
     * @param msg
     * @param level
     */
    public void write(String msg, int level) {

    }
}
