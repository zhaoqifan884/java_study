package com.zqf.demo.redis.pubsub03;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.util.concurrent.TimeUnit;

/**
 * 发布者
 */
public class PublisherDemo {
    //redis 客户端连接
    private Jedis jedis = null;

    /**
     * 在所有的单元测试执行应加@Before
     * 执行顺序   before ----> test ----> after
     *
     * 开启客户端
     */
    @Before
    public void init() {
        //创建一个jedis连接对象
        jedis = new Jedis("192.168.110.128", 6379);

    }

    /**
     * 关闭客户端
     */
    @After
    public void close() {
        if (jedis != null) {
            jedis.close();
        }
    }

    /**
     * 发布数据
     * @throws InterruptedException
     */
    @Test
    public void produce() throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            jedis.publish("9527", "hello" + i);
            //没发送一条消息，睡10毫秒
            TimeUnit.MICROSECONDS.sleep(10);
        }
    }

}
