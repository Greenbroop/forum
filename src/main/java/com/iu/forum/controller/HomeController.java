package com.iu.forum.controller;

import com.iu.forum.model.Message;
import com.iu.forum.model.Thread;
import com.iu.forum.model.User;
import com.iu.forum.repository.ThreadRepository;
import com.iu.forum.repository.MessageRepository;
import com.iu.forum.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort; 
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private ThreadRepository threadRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    // TÍNH NĂNG MỚI: Tích hợp Tìm kiếm (Keyword) và Sắp xếp bài mới nhất lên đầu
    @GetMapping({ "/", "/index" })
    public String index(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        List<Thread> threads;

        if (keyword != null && !keyword.trim().isEmpty()) {
            // SỬA LỖI Ở ĐÂY: Cập nhật tên hàm mới có chữ AndDeletedFalse
            threads = threadRepository.findByTitleContainingIgnoreCaseAndDeletedFalse(keyword.trim());
        } else {
            // CẬP NHẬT LUÔN Ở ĐÂY: Chỉ lấy những bài chưa bị xóa mềm
            threads = threadRepository.findByDeletedFalse(Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        model.addAttribute("threads", threads);
        model.addAttribute("keyword", keyword); 
        return "common/index";
    }

    // Xem chi tiết một Thread
    @GetMapping("/thread/{id}")
    public String threadDetail(@PathVariable Long id, Model model) {
        Thread thread = threadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chủ đề với ID: " + id));

        model.addAttribute("thread", thread);
        model.addAttribute("messages", messageRepository.findByThread(thread));
        return "common/thread-detail";
    }

    // 1. Hiển thị trang đăng bài
    @GetMapping("/thread/create")
    public String showCreateThreadForm(Model model) {
        model.addAttribute("thread", new com.iu.forum.model.Thread());
        return "common/create-thread"; 
    }

    // 2. Xử lý lưu bài viết mới
    @PostMapping("/thread/create")
    public String createThread(@RequestParam("title") String title, Principal principal) {
        if (principal == null)
            return "redirect:/login";

        User creator = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại"));

        com.iu.forum.model.Thread newThread = new com.iu.forum.model.Thread(title, creator);
        threadRepository.save(newThread);

        return "redirect:/"; // Quay về trang chủ sau khi đăng thành công
    }

    // Xử lý gửi bình luận (Đã gộp xử lý text và xử lý upload file đính kèm)
    @PostMapping("/thread/{id}/reply")
    public String replyToThread(@PathVariable Long id,
            @RequestParam("content") String content,
            @RequestParam(value = "file", required = false) MultipartFile file, // required = false để không bắt buộc phải có ảnh
            Principal principal) {
        
        if (principal == null) {
            return "redirect:/login";
        }

        Thread thread = threadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chủ đề"));

        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));

        Message newMessage = new Message();
        newMessage.setContent(content);
        newMessage.setThread(thread);
        newMessage.setUser(currentUser);
        newMessage.setCreatedAt(LocalDateTime.now());

        // XỬ LÝ LOGIC UPLOAD FILE (Nếu người dùng có chọn file)
        if (file != null && !file.isEmpty()) {
            try {
                // Định nghĩa thư mục lưu file (Tạo folder "uploads" nằm ngay ngoài thư mục dự án)
                String uploadDir = System.getProperty("user.dir") + "/uploads/";
                Path uploadPath = Paths.get(uploadDir);

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath); 
                }

                // Tránh trùng tên file bằng cách thêm chuỗi ngẫu nhiên UUID 
                String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path filePath = uploadPath.resolve(uniqueFileName);

                // Sao chép (Lưu) file từ máy tính người dùng vào folder uploads trên Server
                Files.copy(file.getInputStream(), filePath);

                // Lưu đường dẫn URL ảo vào database để sau này HTML có thể gọi ra hiển thị
                newMessage.setFileUrl("/uploads/" + uniqueFileName);

            } catch (IOException e) {
                e.printStackTrace(); // Ghi nhận lỗi nếu lưu file thất bại
            }
        }

        messageRepository.save(newMessage);
        return "redirect:/thread/" + id;
    }
}