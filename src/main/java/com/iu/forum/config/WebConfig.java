package com.iu.forum.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Cấu hình link ảo /uploads/** trỏ thẳng vào folder uploads thật ngoài ổ đĩa máy tính
        String uploadDir = System.getProperty("user.dir") + "/uploads/";
        Path uploadPath = Paths.get(uploadDir);
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:/" + uploadPath.toAbsolutePath().toString() + "/");
    }
}