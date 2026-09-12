package com.casic.assess.dc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * DataCollect 数据解析 Web 服务入口
 * 局域网访问：监听 0.0.0.0:8080，浏览器打开 http://本机IP:8080
 */
@SpringBootApplication
public class DcApplication {

    public static void main(String[] args) {
        SpringApplication.run(DcApplication.class, args);
    }
}
