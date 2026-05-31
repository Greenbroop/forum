package com.iu.forum.controller;

import com.iu.forum.model.Message;
import com.iu.forum.model.Thread;
import com.iu.forum.model.User;
import com.iu.forum.repository.ThreadRepository;
import com.iu.forum.repository.CategoryRepository;
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
    private CategoryRepository categoryRepository;

    @Autowired
    private ThreadRepository threadRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    // TÍNH NĂNG MỚI: Tích hợp Tìm kiếm (Keyword) và Sắp xếp bài mới nhất lên đầu
    // TÍNH NĂNG MỚI: Tìm kiếm đa tiêu chí (Title, Content, Author)
    @GetMapping({ "/", "/index" })
    public String index(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "authorId", required = false) Long authorId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr,
            @RequestParam(value = "hasImage", required = false) Boolean hasImage,
            // BỔ SUNG: Tham số hứng giá trị sắp xếp (mặc định là mới nhất)
            @RequestParam(value = "sort", required = false, defaultValue = "newest") String sortParam,
            Model model) {
        
        List<Thread> threads;

        // Xử lý chuyển đổi ngày tháng... (Giữ nguyên đoạn try-catch parse ngày của bạn)
        java.time.LocalDateTime startDate = null;
        java.time.LocalDateTime endDate = null;
        try {
            if (startDateStr != null && !startDateStr.isEmpty()) {
                startDate = java.time.LocalDate.parse(startDateStr).atStartOfDay();
            }
            if (endDateStr != null && !endDateStr.isEmpty()) {
                endDate = java.time.LocalDate.parse(endDateStr).atTime(23, 59, 59);
            }
        } catch (Exception e) {}

        // BỔ SUNG: Dịch chuỗi "sortParam" thành đối tượng Sort của Spring Data
        Sort sort;
        switch (sortParam) {
            case "oldest":    sort = Sort.by(Sort.Direction.ASC, "createdAt"); break;
            case "titleAsc":  sort = Sort.by(Sort.Direction.ASC, "title"); break;
            case "titleDesc": sort = Sort.by(Sort.Direction.DESC, "title"); break;
            case "viewsDesc": sort = Sort.by(Sort.Direction.DESC, "views"); break; // Sắp xếp theo lượt xem
            case "newest":
            default:          sort = Sort.by(Sort.Direction.DESC, "createdAt"); break;
        }

        // Truyền đối tượng "sort" vào cuối các hàm gọi Database
        if (categoryId != null || authorId != null || (status != null && !status.isEmpty()) || startDate != null || endDate != null || (hasImage != null && hasImage)) {
            threads = threadRepository.advancedFilter(categoryId, authorId, status, startDate, endDate, hasImage, sort);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            threads = threadRepository.findByTitleContainingIgnoreCaseAndDeletedFalse(keyword.trim(), sort);
        } else {
            threads = threadRepository.findByDeletedFalse(sort);
        }

        model.addAttribute("threads", threads);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("users", userRepository.findAll());
        
        // Trả lại các biến trạng thái
        model.addAttribute("keyword", keyword); 
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("selectedAuthor", authorId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("startDate", startDateStr);
        model.addAttribute("endDate", endDateStr);
        model.addAttribute("hasImage", hasImage);
        
        // BỔ SUNG: Trả lại trạng thái sắp xếp để giao diện HTML biết đường in đậm
        model.addAttribute("currentSort", sortParam);
        
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