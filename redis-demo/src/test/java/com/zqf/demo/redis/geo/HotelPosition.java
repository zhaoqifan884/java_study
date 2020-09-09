package com.zqf.demo.redis.geo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelPosition {
    //经度
    private double lng;
    //维度
    private double lat;
    //名字
    private String name;

}
