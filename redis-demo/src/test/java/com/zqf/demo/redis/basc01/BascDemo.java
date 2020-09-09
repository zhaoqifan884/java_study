package com.zqf.demo.redis.basc01;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;

public class BascDemo {
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
     * redis的数据类型---String---get/set/del
     */
    @Test
    public void test01() {
        String s = jedis.set("hello", "java");
        System.out.println("val = " + s);
    }
}
