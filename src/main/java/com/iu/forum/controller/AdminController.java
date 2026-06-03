package com.iu.forum.controller;

import com.iu.forum.model.User;
import com.iu.forum.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin") // Tất cả các đường dẫn trong class này đều bắt đầu bằng /admin
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    // Xem danh sách và quản lý các Moderator hệ thống
    @GetMapping("/mods")
    public String manageMods(Model model) {
        // Truy vấn lấy danh sách toàn bộ người dùng từ Database
        List<User> users = userRepository.findAll();
        // Đẩy danh sách người dùng xuống giao diện HTML (View) với biến tên là "users"
        model.addAttribute("users", users);
        return "admin/manage-mods"; // Trả về file giao diện: resources/templates/admin/manage-mods.html
    }

    // Cấp quyền nâng cấp một User thông thường lên làm Moderator
    @GetMapping("/promote/{id}")
    public String promoteToMod(@PathVariable Long id) {
        // Tìm người dùng theo ID, nếu tồn tại (ifPresent) thì thực hiện đổi quyền
        userRepository.findById(id).ifPresent(user -> {
            user.setRole("ROLE_MODERATOR"); // Set cứng quyền Moderator
            userRepository.save(user); // Lưu (Cập nhật) lại thông tin vào Database
        });
        // Sau khi xử lý xong, điều hướng (redirect) trình duyệt quay lại trang danh sách Mods
        return "redirect:/admin/mods";
    }

    // TÍNH NĂNG MỚI: Hạ quyền Moderator xuống làm User thường
    @GetMapping("/demote/{id}")
    public String demoteToUser(@PathVariable Long id) {
        userRepository.findById(id).ifPresent(user -> {
            // Kiểm tra bảo mật: Không ai có thể tự hạ quyền của chính Admin (Bảo vệ hệ thống tối đa)
            if (!user.getRole().equals("ROLE_ADMIN")) {
                user.setRole("ROLE_USER"); // Đưa về làm người dùng bình thường
                userRepository.save(user);
            }
        });
        return "redirect:/admin/mods";
    }

    // TÍNH NĂNG MỚI: Khóa / Mở khóa tài khoản (Ban/Unban)
    @GetMapping("/toggle-status/{id}")
    public String toggleStatus(@PathVariable Long id) {
        userRepository.findById(id).ifPresent(user -> {
            // Kiểm tra bảo mật: Không cho phép tự khóa tài khoản ADMIN để tránh tự hủy hệ thống
            if (!user.getRole().equals("ROLE_ADMIN")) {
                // Đảo ngược trạng thái hiện tại (Nếu đang khóa thì mở, đang mở thì khóa)
                user.setActive(!user.isActive()); 
                userRepository.save(user);
            }
        });
        return "redirect:/admin/mods";
    }

    // Hiển thị trang cài đặt thông số hệ thống chung của Admin
    @GetMapping("/settings")
    public String systemSettings() {
        return "admin/settings";
    }
}