package com.iu.forum.controller;

import com.iu.forum.model.Message;
import com.iu.forum.model.Thread;
import com.iu.forum.model.User;
import com.iu.forum.repository.CategoryRepository;
import com.iu.forum.repository.MessageRepository;
import com.iu.forum.repository.ThreadRepository;
import com.iu.forum.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.UUID;

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

    // TÍNH NĂNG MỚI: Tìm kiếm đa tiêu chí, Lọc nâng cao, Sắp xếp và Phân trang
    @GetMapping({ "/", "/index" })
    public String index(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "authorId", required = false) Long authorId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr,
            @RequestParam(value = "hasImage", required = false) Boolean hasImage,
            @RequestParam(value = "sort", required = false, defaultValue = "newest") String sortParam,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        
        // 1. Xử lý chuyển đổi ngày tháng
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        try {
            if (startDateStr != null && !startDateStr.isEmpty()) {
                startDate = java.time.LocalDate.parse(startDateStr).atStartOfDay();
            }
            if (endDateStr != null && !endDateStr.isEmpty()) {
                endDate = java.time.LocalDate.parse(endDateStr).atTime(23, 59, 59);
            }
        } catch (Exception e) {
            // Bỏ qua nếu lỗi format ngày
        }

        // 2. Cấu hình Sắp xếp (Sort)
        Sort sort;
        switch (sortParam) {
            case "oldest":    sort = Sort.by(Sort.Direction.ASC, "createdAt"); break;
            case "titleAsc":  sort = Sort.by(Sort.Direction.ASC, "title"); break;
            case "titleDesc": sort = Sort.by(Sort.Direction.DESC, "title"); break;
            case "viewsDesc": sort = Sort.by(Sort.Direction.DESC, "views"); break;
            case "newest":
            default:          sort = Sort.by(Sort.Direction.DESC, "createdAt"); break;
        }

        // 3. Cấu hình Phân trang (Pageable) kết hợp Sắp xếp
        // Chú ý: Spring JPA đếm trang từ 0, nên page thực tế = page - 1
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        
        // 4. Lấy dữ liệu từ Database
        Page<Thread> threadPage;
        if (categoryId != null || authorId != null || (status != null && !status.isEmpty()) || startDate != null || endDate != null || (hasImage != null && hasImage)) {
            threadPage = threadRepository.advancedFilter(categoryId, authorId, status, startDate, endDate, hasImage, pageable);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            threadPage = threadRepository.findByTitleContainingIgnoreCaseAndDeletedFalse(keyword.trim(), pageable);
        } else {
            threadPage = threadRepository.findByDeletedFalse(pageable);
        }

        // 5. Đẩy dữ liệu danh sách bài viết lên giao diện
        model.addAttribute("threads", threadPage); 
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);

        // 6. Đẩy dữ liệu phụ trợ cho hộp Dropdown
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("users", userRepository.findAll());
        
        // 7. Đẩy lại các biến để hiển thị trạng thái bộ lọc đang được chọn
        model.addAttribute("keyword", keyword); 
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("selectedAuthor", authorId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("startDate", startDateStr);
        model.addAttribute("endDate", endDateStr);
        model.addAttribute("hasImage", hasImage);
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
            @RequestParam(value = "file", required = false) MultipartFile file, 
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

        // XỬ LÝ LOGIC UPLOAD FILE
        if (file != null && !file.isEmpty()) {
            try {
                String uploadDir = System.getProperty("user.dir") + "/uploads/";
                Path uploadPath = Paths.get(uploadDir);

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath); 
                }

                String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path filePath = uploadPath.resolve(uniqueFileName);

                Files.copy(file.getInputStream(), filePath);

                newMessage.setFileUrl("/uploads/" + uniqueFileName);

            } catch (IOException e) {
                e.printStackTrace(); 
            }
        }

        messageRepository.save(newMessage);
        return "redirect:/thread/" + id;
    }
}