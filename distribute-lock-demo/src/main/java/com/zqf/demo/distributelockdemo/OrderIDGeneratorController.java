package com.zqf.demo.distributelockdemo;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 获取id
 */

@RestController
public class OrderIDGeneratorController {

    private int count = 0;

    @RequestMapping("getId")
    public String getId() {

        String id = null;
        try {
            TimeUnit.MICROSECONDS.sleep(50);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            //并发的原子性，有序性， 可见性
            count = count + 1;
            id = sdf.format(new Date()) + "-" + count;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return id;
    }
}
