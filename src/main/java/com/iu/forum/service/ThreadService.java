package com.iu.forum.service;

import com.iu.forum.model.Thread;
import com.iu.forum.repository.ThreadRepository;
import com.iu.forum.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.iu.forum.model.Message;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ThreadService {

    @Autowired
    private ThreadRepository threadRepository;

    @Autowired
    private MessageRepository messageRepository;

    // Tự động chạy quét hệ thống định kỳ 
    // Áp dụng tính năng tự động dọn dẹp các threads cũ 
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void autoDeleteOldThreads(int daysConfiguredByAdmin) {
        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(daysConfiguredByAdmin);
        
        // Tìm các bài thảo luận được tạo trước thời hạn quy định
        List<Thread> oldThreads = threadRepository.findByCreatedAtBeforeAndDeletedFalse(thresholdDate);
        
        for (Thread thread : oldThreads) {
            // SỬA LỖI 2: ÁP DỤNG SOFT DELETE (Xóa mềm)
            thread.setDeleted(true);
            threadRepository.save(thread); // Lưu trạng thái ẩn của Thread
            
            // Xóa mềm luôn cả các bình luận bên trong Thread đó
            List<Message> messages = messageRepository.findByThread(thread);
            for (Message msg : messages) {
                msg.setDeleted(true);
            }
            messageRepository.saveAll(messages);
        }
        
        System.out.println("[HỆ THỐNG] Đã chạy tiến trình ẩn tự động các bài viết cũ hơn " + daysConfiguredByAdmin + " ngày.");
    }
}