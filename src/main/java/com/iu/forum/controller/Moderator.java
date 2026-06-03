package com.iu.forum.controller;

import com.iu.forum.repository.ThreadRepository;
import com.iu.forum.repository.MessageRepository;
import com.iu.forum.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.data.domain.Sort;
import com.iu.forum.model.Message;

import java.util.List;
import java.util.ArrayList;

@Controller
@RequestMapping("/mod") // Áp dụng tiền tố URL để phục vụ cho Security chặn quyền
public class Moderator {

    @Autowired
    private ThreadRepository threadRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/dashboard")
    public String modDashboard(Model model) {
        
        // 1. SỬA LỖI ĐẾM SAI: Dùng hàm đếm loại trừ các bài đã xóa mềm
        long totalThreads = threadRepository.countByDeletedFalse();
        long totalRawMessages = messageRepository.countByDeletedFalse();

        // 2. TỐI ƯU UX: Tính ra số lượng bình luận phản hồi thực tế
        long actualReplies = totalRawMessages - totalThreads;
        if (actualReplies < 0) {
            actualReplies = 0;
        }

        // Truyền biến ra giao diện
        model.addAttribute("totalThreads", totalThreads);
        model.addAttribute("totalMessages", actualReplies);

        // Lấy 10 bài viết mới nhất để hiển thị lên Dashboard Mod
        model.addAttribute("recentThreads", threadRepository.findByDeletedFalse(
                org.springframework.data.domain.PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent());

        // Lấy dữ liệu cho Biểu đồ Chart.js
        List<Object[]> stats = categoryRepository.getCategoryStats();
        List<String> chartLabels = new ArrayList<>();
        List<Long> chartData = new ArrayList<>();
        
        if (stats != null) {
            for (Object[] row : stats) {
                chartLabels.add((String) row[0]); 
                chartData.add((Long) row[1]);     
            }
        }
        model.addAttribute("chartLabels", chartLabels);
        model.addAttribute("chartData", chartData);

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
            List<Message> messages = messageRepository.findByThreadAndDeletedFalse(thread);
            for (Message msg : messages) {
                msg.setDeleted(true);
            }
            messageRepository.saveAll(messages);
        });

        return "redirect:/mod/dashboard?successDeleted";
    }
}