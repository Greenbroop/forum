package com.iu.forum.controller;

import com.iu.forum.model.User;
import com.iu.forum.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import java.security.Principal;

@ControllerAdvice // Annotation này biến class thành một "chiếc dù" bao phủ mọi Controller khác
public class GlobalControllerAdvice {

    @Autowired
    private UserRepository userRepository;

    // Hàm này sẽ tự động chạy TRƯỚC MỌI request và gắn thông tin User vào mọi file HTML (Thymeleaf)
    @ModelAttribute("currentUser")
    public User getCurrentUser(Principal principal) {
        // Principal chứa thông tin danh tính (thường là username) của người đang đăng nhập
        if (principal != null) {
            // Truy vấn Database để lấy toàn bộ thông tin người dùng (Avatar, Email, Role...)
            return userRepository.findByUsername(principal.getName()).orElse(null);
        }
        // Nếu người dùng chưa đăng nhập (Khách vãng lai), trả về null
        return null;
    }
}