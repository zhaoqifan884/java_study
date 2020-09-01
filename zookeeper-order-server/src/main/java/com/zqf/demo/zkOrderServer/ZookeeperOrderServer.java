package com.zqf.demo.zkOrderServer;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class ZookeeperOrderServer {

    //最原始提供zk地址的方式
    private static String ZK_SERVER_ADDR = "192.168.110.128:2181, 192.168.110.129:2181,192.168.110.130:2181";
    private static int SESSION_TIMEOUT = 3000;
    private static String PATH = "/pNode";
//    private static String sub_path = "/seckillServer";
//用来接收节点数据的列表
    public static List<String> addrList;

//volatile: 保证在多线程之间的变量的可变性
    private volatile ZooKeeper zooKeeper;

    public static void main(String[] args) {
        SpringApplication.run(ZookeeperOrderServer.class, args);
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
        zooKeeper = new ZooKeeper(ZK_SERVER_ADDR, SESSION_TIMEOUT, new Watcher() {
            //创建回话
            @Override
            public void process(WatchedEvent event) {
                System.out.println("event: " + event);
                //判断是否连接成功
                if (event.getState() == Event.KeeperState.SyncConnected) {
                    System.out.println("zookeeper客户端连接成功");

                    try {
                        //获取数据
                        getData();

                        // 2.绑定永久事件监听
                        zooKeeper.addWatch(PATH, new Watcher() {
                            //事件回调（异步处理，在另一个线程处理）
                            @Override
                            public void process(WatchedEvent event) { //// 开启另外的线程处理
                                try {
                                    getData();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }, AddWatchMode.PERSISTENT);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }

            /**
             * 获取数据
             * @throws KeeperException
             * @throws InterruptedException
             */
            private void getData() throws KeeperException, InterruptedException {
                //1.获取对应的地址列表
                List<String> serverAddr = zooKeeper.getChildren(PATH, null);
                List<String> tempList=new ArrayList<>();
                for (String path : serverAddr) {
                    //获取节点路径数据
                    byte[] data = zooKeeper.getData(PATH+"/"+path, null, new Stat());
                    String addrInfo = new String(data);
                    // 把数据添加到临时列表
                    tempList.add(addrInfo);
                }
                addrList=tempList;
                System.out.println("获取到秒杀服务的最新地址\n"+addrList);
            }
        });
        return zooKeeper;
    }
}
