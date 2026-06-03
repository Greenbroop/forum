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
        // Đẩy một Object User trống xuống View để Thymeleaf bind (kết nối) dữ liệu form vào Object này
        model.addAttribute("user", new User());
        return "common/register";
    }

    // XỬ LÝ ĐĂNG KÝ TÀI KHOẢN MỚI
    @PostMapping("/register")
    // @Transactional đảm bảo: Nếu có lỗi khi lưu Token thì việc lưu User cũng bị hủy bỏ, tránh rác dữ liệu
    @Transactional 
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult bindingResult, Model model) {
        
        // 1. Kiểm tra tính hợp lệ của dữ liệu (Validate). Bắt các lỗi định dạng email, độ dài pass...
        if (bindingResult.hasErrors()) {
            return "common/register";
        }

        // 2. Chống trùng lặp Username và Email
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            model.addAttribute("error", "Tên đăng nhập này đã tồn tại!");
            return "common/register";
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            model.addAttribute("error", "Email này đã được sử dụng!");
            return "common/register";
        }

        // 3. Khởi tạo các giá trị mặc định hệ thống cho người dùng mới
        // MÃ HÓA MẬT KHẨU NGAY LẬP TỨC (Không bao giờ được lưu plain-text dưới DB)
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER"); // Phân quyền mặc định là User thường
        user.setActive(false);     // TÀI KHOẢN BỊ KHÓA (active = false) cho đến khi xác thực qua email
        user.setFullName(user.getUsername());
        user.setBio("Xin chào, tôi là thành viên mới của IU Forum!");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        // 4. Khởi tạo mã Token xác thực 1 lần (Verification Token) để chuẩn bị gửi mail
        VerificationToken verificationToken = new VerificationToken(user);
        tokenRepository.save(verificationToken);

        // (Chức năng mô phỏng) In link xác thực ra màn hình Console (Server log)
        // Trong dự án thực tế, đoạn này sẽ được thay bằng code gửi Email thật (JavaMailSender)
        System.out.println("\n\n====== HỆ THỐNG GỬI EMAIL TỰ ĐỘNG ======");
        System.out.println("Kích hoạt tại: http://localhost:8080/verify?token=" + verificationToken.getToken());
        System.out.println("========================================\n\n");

        return "redirect:/login?unverified"; // Chuyển về trang đăng nhập với thông báo yêu cầu kiểm tra email
    }

    // XỬ LÝ KÍCH HOẠT TÀI KHOẢN KHI NGƯỜI DÙNG CLICK LINK TRONG EMAIL
    @GetMapping("/verify")
    // Quan trọng: Phải có Transactional để các lệnh Update/Delete xuống DB thực thi đồng bộ
    @Transactional 
    public String verifyAccount(@RequestParam("token") String token, Model model) {
        
        // Tìm Token dưới DB dựa vào mã token trên URL
        VerificationToken verificationToken = tokenRepository.findByToken(token).orElse(null);

        // Kiểm tra xem Token có tồn tại/giả mạo không
        if (verificationToken == null) {
            model.addAttribute("error", "Đường dẫn kích hoạt không hợp lệ!");
            return "common/login";
        }

        // Kiểm tra Token đã hết hạn chưa (Ví dụ: Thường set là 24h)
        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "Đường dẫn kích hoạt đã hết hạn!");
            return "common/login";
        }

        // KÍCH HOẠT THÀNH CÔNG
        User user = verificationToken.getUser();
        user.setActive(true); // Bật cờ active = true -> Cấp phép đăng nhập vào hệ thống
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Dọn dẹp token sau khi sử dụng để tránh token bị tái sử dụng
        tokenRepository.delete(verificationToken);

        return "redirect:/login?verified"; // Trả về form đăng nhập cùng thông báo Thành công
    }
}