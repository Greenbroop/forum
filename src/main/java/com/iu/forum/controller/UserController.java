package com.iu.forum.controller;

import com.iu.forum.model.User;
import com.iu.forum.repository.UserRepository;
import jakarta.validation.Valid; // Import thư viện Validation
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult; // Cầu nối lấy lỗi
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.iu.forum.model.VerificationToken;
import com.iu.forum.repository.VerificationTokenRepository;
import java.time.LocalDateTime;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @GetMapping("/login")
    public String showLoginForm() {
        return "common/login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "common/register";
    }

    // TÍNH NĂNG MỚI: Dùng @Valid và BindingResult để hứng lỗi nhập liệu
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult bindingResult, Model model) {
        // 1. Nếu vi phạm các @NotBlank, @Size đã cài ở model User
        if (bindingResult.hasErrors()) {
            return "common/register";
        }

        // 2. Bắt lỗi trùng lặp dữ liệu (Database Constraints)
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            model.addAttribute("error", "Tên đăng nhập này đã tồn tại!");
            return "common/register";
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            model.addAttribute("error", "Email này đã được sử dụng!");
            return "common/register";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER");
        user.setActive(false); // Chưa kích hoạt

        // ==========================================
        // VÁ LỖI DATABASE: Bơm dữ liệu cho các cột NOT NULL
        // ==========================================
        user.setFullName(user.getUsername()); // Lấy tạm username làm họ tên
        user.setBio("Xin chào, tôi là thành viên mới của IU Forum!"); // Cung cấp tiểu sử mặc định
        user.setCreatedAt(LocalDateTime.now());
        // ==========================================

        // Lưu xuống DB (Lúc này MySQL sẽ chấp nhận vì đã đủ các trường bắt buộc)
        userRepository.save(user);

        // Tạo token xác thực
        VerificationToken verificationToken = new VerificationToken(user);
        tokenRepository.save(verificationToken);

        // Mô phỏng gửi email
        System.out.println("\n\n====== HỆ THỐNG GỬI EMAIL TỰ ĐỘNG ======");
        System.out.println("Gửi đến Email: " + user.getEmail());
        System.out.println("Vui lòng click vào link sau để kích hoạt tài khoản:");
        System.out.println("http://localhost:8080/verify?token=" + verificationToken.getToken());
        System.out.println("========================================\n\n");

        return "redirect:/login?unverified";     
    }
}