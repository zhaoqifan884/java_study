package com.zqf.demo.distributelockdemo;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

@SpringBootApplication
public class DistributeLockDemoApplication {

    //最原始提供zk地址的方式
    private static String ZK_SERVER_ADDR = "192.168.110.128:2181, 192.168.110.129:2181,192.168.110.130:2181";
    private static int SESSION_TIMEOUT = 3000;

    public static void main(String[] args) {
        SpringApplication.run(DistributeLockDemoApplication.class, args);
    }

    //创建一个zk连接
    @Bean
    public ZooKeeper zooKeeper() throws Exception {
        //等待并发
        //ountDownLatch这个类使一个线程等待其他线程各自执行完毕后再执行。
        //是通过一个计数器来实现的，计数器的初始值是线程的数量。每当一个线程执行完毕后，计数器的值就-1，当计数器的值为0时，表示所有线程都执行完毕，然后在闭锁上等待的线程就可以恢复工作了。

        CountDownLatch countDownLatch = new CountDownLatch(1);

        /**
         * 第一个参数: 连接地址和端口 第二个参数: 会话超时时间, 第三个参数: 事件监听程序
         */
        ZooKeeper zooKeeper = new ZooKeeper(ZK_SERVER_ADDR, SESSION_TIMEOUT, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println("event == " + event);
                //判断是否连接成功
                if (event.getState() == Event.KeeperState.SyncConnected) {
                    //等待连接成功返回对象
                    countDownLatch.countDown();;
                }
            }
        });

//        zooKeeper.getData()   状态为connecting，还不是connected,所以需要等待延迟
        countDownLatch.await();
        return zooKeeper;
    }

}
