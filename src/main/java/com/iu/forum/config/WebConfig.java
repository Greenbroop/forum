package com.iu.forum.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration // Đánh dấu đây là file cấu hình của Spring Boot, hệ thống sẽ tự động quét và chạy lúc khởi động
public class WebConfig implements WebMvcConfigurer {

    // =========================================================================
    // CẤU HÌNH TÀI NGUYÊN TĨNH (STATIC RESOURCES)
    // Mục đích: Ánh xạ (Mapping) một đường dẫn ảo trên web vào một thư mục vật lý chứa file thật trên ổ cứng.
    // Nếu không có hàm này, khi truy cập link ảnh, Spring Security sẽ chặn lại hoặc báo lỗi 404 Not Found.
    // =========================================================================
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        
        // Bước 1: Lấy đường dẫn gốc của thư mục dự án đang chạy trên máy chủ
        String uploadDir = System.getProperty("user.dir") + "/uploads/";
        Path uploadPath = Paths.get(uploadDir);
        
        // Bước 2: Tạo quy tắc Ánh xạ (Mapping)
        registry.addResourceHandler("/uploads/**") // Đường dẫn ảo trên URL web (VD: web.com/uploads/avatar.png)
                .addResourceLocations("file:/" + uploadPath.toAbsolutePath().toString() + "/"); // Trỏ thẳng tới thư mục vật lý trên ổ cứng
    }
}