package com.zqf.demo.redis.pipeline02;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

public class PipelineDemo {
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
     * 模拟发100000条数据
     */
    @Test
    public void testBasic() {
        //开始时间
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 100000; i++) {
            jedis.set("base:key" + i, "value");
        }
        //结束时间
        long endTime = System.currentTimeMillis();
        //12s
        System.out.println("耗时时长: " + (endTime - startTime));
    }

    /**
     * 模拟发100000条数据
     */
    @Test
    public void testPipeline() {
        //开始时间
        long startTime = System.currentTimeMillis();

        //创建一个pipeline管道
        Pipeline pipelined = jedis.pipelined();

        for (int i = 0; i < 100000; i++) {
//            把需要执行的命令放在管道中
            pipelined.set("base:key" + i, "value");
        }
//        同步执行命令
        pipelined.sync();
        //结束时间
        long endTime = System.currentTimeMillis();
        //0.7s
        System.out.println("耗时时长: " + (endTime - startTime));
    }
}
