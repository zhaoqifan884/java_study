package com.zqf.demo.redis.pubsub03;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

/**
 * 消费者   订阅者
 */
public class SubscribeDemo {

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

    @Test
    public void cousmer() {
        JedisPubSub jedisPubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                //那个频道
                System.out.println("channel = " + channel);
                //消息内容
                System.out.println("message = " + message);
            }
        };
        jedis.subscribe(jedisPubSub, "9527");
    }
}
