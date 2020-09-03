package com.zqf.demo.distributelockdemo;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * zk分布式锁-----创建同名节点
 */
@RestController
public class OrderController {

    //创建http请求
    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private ZooKeeper zooKeeper;

    private String path = "/locks";
    private String node = "/orderIdLock";

    @RequestMapping("createOrder")
    public String createOrder() {

        //获取id
        //尝试获取锁
        if (tryLock()) {
            //调用业务方法，生成订单标号
            String id = restTemplate.getForObject("http://localhost:8080/getId", String.class);
            System.out.println("获取到的id   " + id);

            //释放锁
            unlock();
        } else {
            //等待锁
            waitLock();
        }

        return "success";
    }

    /**
     * 获取锁
     * @return false
     */
    public boolean tryLock() {
        try {
            zooKeeper.create(path + node, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 释放锁
     */
    public void unlock() {
        //删除指定节点
        try {
            Stat stat = zooKeeper.exists(path + node, false);
            if (stat != null) {
                zooKeeper.delete(path + node, stat.getVersion());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 等待锁
     */
    public void waitLock() {
        //绑定监听事件
        try {
            //绑定一次性事件
            zooKeeper.getChildren(path, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getType() == Event.EventType.NodeChildrenChanged) {
                        createOrder();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
