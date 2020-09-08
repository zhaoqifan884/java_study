package com.zqf.demo.distributelockdemo;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * zk分布式锁-------异名节点解决方案
 */
@RestController
public class Order02Controller {

    private RestTemplate restTemplate = new RestTemplate();

    @Resource
    private ZooKeeper zooKeeper;

    private String path = "/locks02";
    private String node = "/orderIdLock";

    @RequestMapping("createOrder02")
    public String createOrder02() {
        //创建一个临时顺序节点  /locks02/orderIdLock0000000001
        try {
            String currentPath = zooKeeper.create(this.path + this.node, null, ZooDefs.Ids.OPEN_ACL_UNSAFE,
                                                          CreateMode.EPHEMERAL_SEQUENTIAL);
            //orderIdLock0000000001 字符串处理
            currentPath = currentPath.substring(currentPath.lastIndexOf("/") + 1);
            //获取id
            if (tryLock(currentPath)) {
                //调用业务方法
                String id = restTemplate.getForObject("http://localhost:8080/getId", String.class);
                System.out.println("获取到的id:" + id);

                //释放锁
                unlock(currentPath);

            } else {
                //等待锁
                waitLock(currentPath);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 尝试获取id , 如果获取到了， 返回 true 否则 返回false
     * 竞争锁资源
     * @param currentPath
     * @return
     */
    public boolean tryLock(String currentPath) {
        try {
            //1.获取所有的子节点列表
            List<String> children = zooKeeper.getChildren(path, false);
            //将子节点排序
            Collections.sort(children);
            //2.判断当前的currentPath是否是最小节点
            if (StringUtils.pathEquals(currentPath, children.get(0))) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 释放锁
     * @param currentPath  跟路径
     */
    public void unlock(String currentPath) {
        try {
            Stat stat = zooKeeper.exists(path + "/" + currentPath, false);
            if (stat != null) {
                zooKeeper.delete(path + "/" + currentPath, stat.getVersion());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 等待锁
     * @param currentPath
     */
    public void waitLock(String currentPath) {
        try {
            //获取所有子节点列表
            List<String> children = zooKeeper.getChildren(path, false);
            //对子节点列表进行排序
            Collections.sort(children);
            //获取当前节点位置
            int index = children.indexOf(currentPath);
            if (index > 0) {
                String preNode = children.get(index - 1);
                //对于前一个节点绑定节点删除事件
                zooKeeper.getData(path + "/" + preNode, new Watcher() {
                    @Override
                    public void process(WatchedEvent event) {
                        if (event.getType() == Event.EventType.NodeDeleted) {
                            //调用业务方法
                            String id = restTemplate.getForObject("http://localhost:8080/getId", String.class);
                            System.out.println("获取到的id" + id);
                            //释放锁
                            unlock(currentPath);
                        }
                    }
                }, new Stat());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
