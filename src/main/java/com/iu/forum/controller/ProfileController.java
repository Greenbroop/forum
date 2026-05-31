package com.iu.forum.controller;

import com.iu.forum.model.User;
import com.iu.forum.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public String viewProfile(Principal principal, Model model) {
        User user = userRepository.findByUsername(principal.getName()).get();
        model.addAttribute("user", user);
        return "common/profile";
    }

    // ĐÃ SỬA: Gộp chung việc cập nhật Email và Thông tin (Họ tên, Bio, Avatar) vào 1 hàm duy nhất
    @PostMapping("/update")
    public String updateProfile(
            Principal principal,
            @RequestParam("email") String email,
            @RequestParam(value = "fullName", required = false) String fullName,
            @RequestParam(value = "bio", required = false) String bio,
            @RequestParam(value = "avatar", required = false) String avatar) {

        userRepository.findByUsername(principal.getName()).ifPresent(user -> {
            // Cập nhật tất cả các trường
            user.setEmail(email);
            user.setFullName(fullName);
            user.setBio(bio);
            user.setAvatar(avatar);
            
            // Chốt hạ lưu xuống DB
            userRepository.save(user);
        });

        return "redirect:/profile?success";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).get();
        
        // Kiểm tra mật khẩu cũ có khớp trong DB không
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return "redirect:/profile?error=wrongpass";
        }

        // Đổi mật khẩu mới (phải mã hóa)
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return "redirect:/profile?success";
    }
}