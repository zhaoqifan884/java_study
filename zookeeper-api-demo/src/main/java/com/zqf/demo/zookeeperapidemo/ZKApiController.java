package com.zqf.demo.zookeeperapidemo;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ZKApiController {
    @Autowired
    private ZooKeeper zooKeeper;

    /**
     * 1.创建节点
     * @param path  路径
     * @param data  数据
     *        acl   设置权限
     *
     *        createMode   创建节点类型    枚举类型
     * @return
     */
    @RequestMapping("createNode")
    public String createNode(String path, String data,  String type) throws KeeperException, InterruptedException {
        String result = zooKeeper.create(path, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.valueOf(type));
        return result;
    }

    /**
     * 2.获取数据（同步）
     * @param path  路径
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    @RequestMapping("getData")
    public String getData(String path) throws KeeperException, InterruptedException {
        //1.先去查询版本信息，若没有数据，则返回null
        Stat stat = zooKeeper.exists(path, false);
        //同步获取数据
        //等到返回结果同步时，才会继续执行
        byte[] data = zooKeeper.getData(path, false, stat);
        System.out.println("new String(data) = " + new String(data));
        return new String(data);
    }

    /**
     * 2.获取数据（异步）
     * @param path  路径
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    @RequestMapping("getDataAsync")
    public String getDataAsync(String path) throws KeeperException, InterruptedException {
        //1.先去查询版本信息
        Stat stat = zooKeeper.exists(path, false);
        //获取数据
        //异步获取会通过回调的方式返回数据
        zooKeeper.getData(path, false, new AsyncCallback.DataCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
                System.out.println("异步处理回调数据");
                System.out.println("收到的数据：" + new String(data));
                System.out.println("环境上下文 ctx" + ctx);
            }
        }, "测试数据");
        return "异步获取数据";
    }

    /**
     * 获取子节点信息
     * @param path 路径
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    @RequestMapping("getChildren")
    public List<String> getChildren(String path) throws KeeperException, InterruptedException {
        List<String> children = zooKeeper.getChildren(path, false);
        return children;
    }

    /**
     * 删除节点
     * @param path
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    @RequestMapping("delete")
    public String delete(String path) throws KeeperException, InterruptedException {
        Stat stat = zooKeeper.exists(path, false);
        if (stat != null) {
            zooKeeper.delete(path, stat.getVersion());
        }
        return "删除成功";
    }

    /**
     * 更新
     * @param path
     * @param data
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    @RequestMapping("update")
    public String update(String path, String data) throws KeeperException, InterruptedException {
        Stat stat = zooKeeper.exists(path, false);
        if (stat != null) {
            zooKeeper.setData(path, data.getBytes(), stat.getVersion());
        }
        return "更新成功";
    }

    /**
     * 绑定一次事件（该事件只能监听数据的改变，不能监听节点的改变）
     * 实时获取更新数据
     * @param path
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    @RequestMapping("addWatch1")
    public String addWatch1(String path) throws KeeperException, InterruptedException {
        Stat stat = zooKeeper.exists(path, false);
        //定义一个监视器对象
        Watcher watcher = new Watcher() {
            //数据改变事件，而且还是一次性的
            @Override
        public void process(WatchedEvent event) {
            System.out.println("事件类型" + event.getType());
            System.out.println("数据发生改变，请及时更新");
                try {
                    byte[] data = zooKeeper.getData(path, this, stat);
                    System.out.println("更新后的数据：" + new String(data));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
    };
        if (stat != null) {
            zooKeeper.getData(path, watcher, stat);
        }
        return "success";
    }

    /**
     * 绑定永久事件
     * @param path
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    @RequestMapping("addWatch2")
    public String addWatch2(String path) throws KeeperException, InterruptedException {
        Stat stat = zooKeeper.exists(path, false);
        //只是获取数据，没有绑定事件
        byte[] data = zooKeeper.getData(path, null, stat);
        System.out.println("获取到数据" + new String(data));
        //绑定永久事件 -----> 1.数据变化事件   2.子节点变化事件
        zooKeeper.addWatch(path, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println("event==" + event);
                if (event.getType() == Event.EventType.NodeDataChanged) {
                    //重新获取数据
                    try {
                        //重新获取数据
                        Stat stat = zooKeeper.exists(path, false);
                        //只是获取数据, 没有绑定事件
                        byte[] data = zooKeeper.getData(path, null, stat);
                        System.out.println("更新的数据:"+new String(data));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (event.getType() == Event.EventType.NodeChildrenChanged) {
                    //重新获取子节点列表
                    System.out.println("子节点数据发生改变");
                }
            }
        }, AddWatchMode.PERSISTENT);
        return "success";
    }

    /**
     * 递归绑定事件
     * @param path
     * @return
     * @throws Exception
     */
    @RequestMapping("addWatch3")
    public List<String> addWatch3(String path) throws Exception{
        //1 先获取所有的子节点
        List<String> children = zooKeeper.getChildren(path, false);
        //2 绑定一个监听事件
        zooKeeper.addWatch(path, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if(event.getType()== Event.EventType.NodeChildrenChanged){
                    System.out.println("子节点数据发送改变");
                    System.out.println("重新获取子节点数据");
                    try {

                        List<String> children1 = zooKeeper.getChildren(path, false);
                        System.out.println("children1 = " + children1);
                        System.out.println("=========================");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }else if(event.getType()== Event.EventType.NodeDataChanged){
                    System.out.println("节点数据发生改变");
                    try {

                        byte[] data = zooKeeper.getData(path, false, new Stat());
                        System.out.println("获取到的数据是:"+new String(data));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, AddWatchMode.PERSISTENT_RECURSIVE);
        return children;
    }
}
