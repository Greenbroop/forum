package com.iu.forum.controller;

import com.iu.forum.model.Message;
import com.iu.forum.model.Thread;
import com.iu.forum.repository.CategoryRepository;
import com.iu.forum.repository.MessageRepository;
import com.iu.forum.repository.ThreadRepository;
import com.iu.forum.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
@RequestMapping("/thread")
public class ThreadController {

    @Autowired
    private ThreadRepository threadRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    // 1. MỞ TRANG ĐĂNG BÀI VÀ TRUYỀN DANH SÁCH CHUYÊN MỤC
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        // Lấy danh sách chuyên mục từ DB đưa lên giao diện
        model.addAttribute("categories", categoryRepository.findAll());
        return "common/create-thread"; // Đảm bảo tên file HTML của bạn khớp ở đây
    }

    // 2. XỬ LÝ KHI NGƯỜI DÙNG BẤM NÚT "ĐĂNG BÀI NGAY"
    @PostMapping("/create")
    public String createThread(Principal principal,
                               @RequestParam("title") String title,
                               @RequestParam("categoryId") Long categoryId,
                               @RequestParam("content") String content) {
        
        // Bước 1: Lấy thông tin người đăng
        var creator = userRepository.findByUsername(principal.getName()).orElse(null);
        var category = categoryRepository.findById(categoryId).orElse(null);

        if (creator != null && category != null) {
            // Bước 2: Tạo Chủ đề (Thread) mới
            Thread newThread = new Thread(title, creator);
            newThread.setCategory(category);
            Thread savedThread = threadRepository.save(newThread); // Lưu để lấy ID

            // Bước 3: Tạo Bình luận đầu tiên (Message) chính là nội dung bài viết
            Message firstMessage = new Message(content, creator, savedThread);
            messageRepository.save(firstMessage);
            
            // Bước 4: Chuyển hướng về trang bài viết vừa tạo (hoặc trang chủ)
            return "redirect:/thread/" + savedThread.getId();
        }

        return "redirect:/?error";
    }
}