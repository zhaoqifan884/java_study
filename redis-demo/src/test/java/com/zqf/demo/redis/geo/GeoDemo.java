package com.zqf.demo.redis.geo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.GeoRadiusResponse;
import redis.clients.jedis.GeoUnit;
import redis.clients.jedis.Jedis;

import java.util.List;

/**
 * 需求： 通过坐标查酒店
 */
public class GeoDemo {

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
    public void initData() {
        HotelPosition h1 = new HotelPosition(113.23121, 23.117933, "如家酒店");
        HotelPosition h2 = new HotelPosition(113.203641,23.382214, "速8酒店");
        HotelPosition h3 = new HotelPosition(113.361532,23.128617, "7天连锁酒店");
        HotelPosition h4 = new HotelPosition(113.258358,23.162526, "广州曼克顿酒店");
        jedis.geoadd("hotel",h1.getLng(),h1.getLat(),h1.getName());
        jedis.geoadd("hotel",h2.getLng(),h2.getLat(),h2.getName());
        jedis.geoadd("hotel",h3.getLng(),h3.getLat(),h3.getName());
        jedis.geoadd("hotel",h4.getLng(),h4.getLat(),h4.getName());
    }

    @Test
    public void handler() {
        //李当前位置10km之内的酒店
        // 当前的位置 113.324981,23.150597
        List<GeoRadiusResponse> hotels = jedis.georadius("hotel", 113.324981, 23.150597, 30, GeoUnit.KM);
        for (GeoRadiusResponse hotel : hotels) {
            //遍历酒店名称
            System.out.println(new String(hotel.getMember()));
        }
        //查看距离信息(两酒店之间)
        System.out.println(jedis.geodist("hotel", "广州曼克顿酒店", "速8酒店", GeoUnit.KM));
    }
}
