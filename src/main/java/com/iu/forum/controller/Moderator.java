package com.iu.forum.controller;

import com.iu.forum.repository.ThreadRepository;
import com.iu.forum.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Bổ sung import Model
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.data.domain.Sort;
import com.iu.forum.model.Message;
import java.util.List;

@Controller
@RequestMapping("/mod") // Áp dụng tiền tố URL để phục vụ cho Security chặn quyền
public class Moderator {

    @Autowired
    private ThreadRepository threadRepository;

    @Autowired
    private MessageRepository messageRepository;

    @GetMapping("/dashboard")
    public String modDashboard(Model model) {
        // Lấy số liệu thống kê từ CSDL
        long totalThreads = threadRepository.count();
        long totalMessages = messageRepository.count();

        // Đẩy dữ liệu sang HTML
        model.addAttribute("totalThreads", totalThreads);
        model.addAttribute("totalMessages", totalMessages);

        // CẬP NHẬT: Lấy 10 bài viết mới nhất (Trang 0, kích thước 10) để hiển thị lên Dashboard Mod
        model.addAttribute("recentThreads", threadRepository.findByDeletedFalse(
                org.springframework.data.domain.PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent());

        return "mod/dashboard";
    }

    // Tính năng xóa bình luận vi phạm
    @GetMapping("/delete-message/{id}")
    @Transactional
    public String deleteMessage(@PathVariable Long id) {
        messageRepository.findById(id).ifPresent(msg -> {
            msg.setDeleted(true); // Cắm cờ xóa mềm
            messageRepository.save(msg); // Lưu trạng thái
        });
        return "redirect:/mod/dashboard?successDeleted";
    }

    // Xóa nguyên một Thread vi phạm
    @GetMapping("/delete-thread/{id}")
    @Transactional
    public String deleteThread(@PathVariable Long id) {
        threadRepository.findById(id).ifPresent(thread -> {
            // 1. Xóa mềm Thread cha
            thread.setDeleted(true);
            threadRepository.save(thread);

            // 2. Xóa mềm luôn sạch sẽ các tin nhắn con bên trong
            List<Message> messages = messageRepository.findByThread(thread);
            for (Message msg : messages) {
                msg.setDeleted(true);
            }
            messageRepository.saveAll(messages);
        });

        return "redirect:/mod/dashboard?successDeleted";
    }
}