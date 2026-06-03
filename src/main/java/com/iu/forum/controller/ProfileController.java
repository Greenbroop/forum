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

    // Lấy thông tin cá nhân hiện tại để hiển thị lên form Profile
    @GetMapping
    public String viewProfile(Principal principal, Model model) {
        // Principal chứa định danh của user đang đăng nhập (nhờ Spring Security cung cấp)
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

        // Lấy User đang đăng nhập lên từ DB và tiến hành cập nhật
        userRepository.findByUsername(principal.getName()).ifPresent(user -> {
            // Cập nhật tất cả các trường dữ liệu mà user nhập vào form
            user.setEmail(email);
            user.setFullName(fullName);
            user.setBio(bio);
            user.setAvatar(avatar);
            
            // Chốt hạ lưu thông tin mới xuống DB
            userRepository.save(user);
        });

        // Redirect kèm tham số success để HTML hiển thị hộp thoại thông báo thành công
        return "redirect:/profile?success";
    }

    // XỬ LÝ ĐỔI MẬT KHẨU AN TOÀN
    @PostMapping("/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 Principal principal) {
                                     
        User user = userRepository.findByUsername(principal.getName()).get();
        
        // 1. KIỂM TRA BẢO MẬT: So sánh mật khẩu cũ người dùng nhập vào với mật khẩu đã mã hóa trong DB
        // (Không thể dùng == vì mật khẩu DB đã bị băm (Hash), phải dùng passwordEncoder.matches)
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            // Nếu không khớp, đá về trang profile kèm báo lỗi
            return "redirect:/profile?error=wrongpass";
        }

        // 2. Nếu mật khẩu cũ đúng -> Mã hóa mật khẩu mới và lưu vào DB
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        return "redirect:/profile?success";
    }
}