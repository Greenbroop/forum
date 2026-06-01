package com.iu.forum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling; // 1. BỔ SUNG THƯ VIỆN NÀY

@SpringBootApplication
@EnableScheduling // 2. BỔ SUNG ANNOTATION NÀY ĐỂ BẬT TÍNH NĂNG CHẠY NGẦM
public class ForumApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForumApplication.class, args);
    }
}