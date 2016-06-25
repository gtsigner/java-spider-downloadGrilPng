package org.zhaojun.spider.db;

/**
 * Created by zhaojun on 2016/6/24.
 */


import redis.clients.jedis.Jedis;

/**
 * 高速缓存队列
 */
public class SpiderRedis {

    private String host = "127.0.0.1";
    private int port = 6739;
    private Jedis jedis;

    public SpiderRedis() {
        jedis = new Jedis(host, port);
    }

    public SpiderRedis(String host, int port) {
        this.host = host;
        this.port = port;
        jedis = new Jedis(host, port);
    }

}
