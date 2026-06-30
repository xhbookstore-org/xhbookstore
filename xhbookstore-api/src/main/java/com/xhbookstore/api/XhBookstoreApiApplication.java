package com.xhbookstore.api;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"com.xhbookstore.api", "com.xhbookstore.system", "com.xhbookstore.framework", "com.xhbookstore.common"})
@MapperScan(basePackages = {"com.xhbookstore.api.mapper", "com.xhbookstore.system.mapper"})
public class XhBookstoreApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(XhBookstoreApiApplication.class, args);
    }
}
