package com.zqf.demo.zookeeperseckillserver;

import org.apache.zookeeper.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

@SpringBootApplication
public class ZookeeperSeckillServerApplication {
    //最原始提供zk地址的方式
    private static String zk_server_addr = "192.168.110.128:2181, 192.168.110.129:2181,192.168.110.130:2181";
    private static int session_timeout = 3000;
    private static String path = "/pNode";
    private static String sub_path = "/seckillServer";
    @Value("${server.host}")
    private String host;
    @Value("${server.post}")
    private String post;


    private ZooKeeper zooKeeper;

    public static void main(String[] args) {
        SpringApplication.run(ZookeeperSeckillServerApplication.class, args);
    }

    //创建一个zk连接

    /**
     *  ZooKeeper(String connectString, int sessionTimeout, Watcher watcher)
     *  connectString:连接地址和端口
     *  sessionTimeout：回话超时时间
     *  watcher：事件监听
     * @return
     */
    @Bean
    public ZooKeeper zkCli() throws IOException {
        zooKeeper = new ZooKeeper(zk_server_addr, session_timeout, new Watcher() {
            //创建回话
            @Override
            public void process(WatchedEvent event) {
                System.out.println("event: " + event);
                //判断是否连接成功
                if (event.getState() == Event.KeeperState.SyncConnected) {
                    System.out.println("zookeeper客户端连接成功");

                    //注册对应的信息
                    try {
                        zooKeeper.create(path + sub_path, (host + ":" + post).getBytes(),
                                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        return zooKeeper;
    }
}
