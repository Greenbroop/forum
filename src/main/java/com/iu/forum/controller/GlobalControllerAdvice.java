package com.iu.forum.controller;

import com.iu.forum.model.User;
import com.iu.forum.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import java.security.Principal;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private UserRepository userRepository;

    // Hàm này sẽ tự động chạy và gắn thông tin User vào mọi file HTML
    @ModelAttribute("currentUser")
    public User getCurrentUser(Principal principal) {
        if (principal != null) {
            // Trả về toàn bộ thông tin người dùng đang đăng nhập
            return userRepository.findByUsername(principal.getName()).orElse(null);
        }
        return null;
    }
}