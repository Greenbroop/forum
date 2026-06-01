package com.iu.forum.controller;

import com.iu.forum.model.User;
import com.iu.forum.model.VerificationToken;
import com.iu.forum.repository.UserRepository;
import com.iu.forum.repository.VerificationTokenRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    @PostMapping("/register")
    @Transactional // Đảm bảo cả User và Token đều được lưu hoặc cùng bị hủy nếu lỗi
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "common/register";
        }

        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            model.addAttribute("error", "Tên đăng nhập này đã tồn tại!");
            return "common/register";
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            model.addAttribute("error", "Email này đã được sử dụng!");
            return "common/register";
        }

        // Cấu hình thông tin User
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER");
        user.setActive(false);
        user.setFullName(user.getUsername());
        user.setBio("Xin chào, tôi là thành viên mới của IU Forum!");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        // Tạo và lưu token xác thực
        VerificationToken verificationToken = new VerificationToken(user);
        tokenRepository.save(verificationToken);

        System.out.println("\n\n====== HỆ THỐNG GỬI EMAIL TỰ ĐỘNG ======");
        System.out.println("Kích hoạt tại: http://localhost:8080/verify?token=" + verificationToken.getToken());
        System.out.println("========================================\n\n");

        return "redirect:/login?unverified";
    }

    @GetMapping("/verify")
    @Transactional // Quan trọng: Phải có Transactional để lệnh update xuống DB có hiệu lực
    public String verifyAccount(@RequestParam("token") String token, Model model) {
        VerificationToken verificationToken = tokenRepository.findByToken(token).orElse(null);

        if (verificationToken == null) {
            model.addAttribute("error", "Đường dẫn kích hoạt không hợp lệ!");
            return "common/login";
        }

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "Đường dẫn kích hoạt đã hết hạn!");
            return "common/login";
        }

        User user = verificationToken.getUser();
        user.setActive(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Dọn dẹp token sau khi sử dụng
        tokenRepository.delete(verificationToken);

        return "redirect:/login?verified";
    }
}