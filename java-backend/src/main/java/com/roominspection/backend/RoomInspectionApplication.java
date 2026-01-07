package com.roominspection.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 机房巡检系统 - 主启动类
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
@MapperScan("com.roominspection.backend.mapper")
public class RoomInspectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoomInspectionApplication.class, args);
        System.out.println("=================================");
        System.out.println("机房巡检系统启动成功！");
        System.out.println("访问地址: http://localhost:8080/api");
        System.out.println("=================================");
    }
}
