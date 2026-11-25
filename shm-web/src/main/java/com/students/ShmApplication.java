package com.students;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.students.mapper")
public class ShmApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShmApplication.class, args);
    }
}
