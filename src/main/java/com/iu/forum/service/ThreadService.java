package com.iu.forum.service;

import com.iu.forum.model.Thread;
import com.iu.forum.model.Message;
import com.iu.forum.repository.ThreadRepository;
import com.iu.forum.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ThreadService {

    // Khai báo Logger chuẩn của Spring Boot
    private static final Logger log = LoggerFactory.getLogger(ThreadService.class);

    @Autowired
    private ThreadRepository threadRepository;

    @Autowired
    private MessageRepository messageRepository;

    /**
     * TIẾN TRÌNH 1: TỰ ĐỘNG ẨN (SOFT DELETE) CÁC BÀI VIẾT QUÁ 30 NGÀY
     * cron = "0 0 0 * * ?" nghĩa là: Chạy vào đúng 00:00:00 (12h đêm) mỗi ngày.
     */
    @Scheduled(cron = "0 0 0 * * ?") 
    @Transactional
    public void autoDeleteOldThreads() {
        // Đặt mốc thời gian: Lùi lại 30 ngày
        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(30);
        
        // Tìm các bài hoạt động bình thường nhưng tạo từ trước mốc 30 ngày
        List<Thread> oldThreads = threadRepository.findByCreatedAtBeforeAndDeletedFalse(thresholdDate);
        
        if (!oldThreads.isEmpty()) {
            for (Thread thread : oldThreads) {
                thread.setDeleted(true); // Xóa mềm
                
                List<Message> messages = messageRepository.findByThread(thread);
                for (Message msg : messages) {
                    msg.setDeleted(true);
                }
                messageRepository.saveAll(messages);
            }
            threadRepository.saveAll(oldThreads);
            
            // Sử dụng Logger chuẩn thay vì System.out.println
            log.info("Đã ẨN {} bài viết cũ hơn 30 ngày.", oldThreads.size());
        }
    }

    /**
     * TIẾN TRÌNH 2: TỰ ĐỘNG XÓA VĨNH VIỄN (HARD DELETE) SAU 1 TUẦN TRONG THÙNG RÁC
     * cron = "0 0 0 * * ?" nghĩa là: Chạy vào đúng 00:00:00 (12h đêm) mỗi ngày.
     */
    @Scheduled(cron = "0 0 0 * * ?") 
    @Transactional
    public void autoDeleteOldSoftDeletedThreads() {
        // Đặt mốc thời gian: Lùi lại 7 ngày (1 tuần)
        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(7);

        // Tìm các bài rác (deleted = true) đã bị ẩn từ trước mốc 7 ngày
        List<Thread> trashThreads = threadRepository.findByDeletedTrueAndCreatedAtBefore(thresholdDate);

        if (!trashThreads.isEmpty()) {
            threadRepository.deleteAll(trashThreads); // Xóa cứng
            
            // Ghi log chuyên nghiệp với SLF4J
            log.info("⏰ Quét dọn thùng rác nửa đêm hoàn tất.");
            log.info("✅ Đã xóa VĨNH VIỄN {} bài viết rác tồn đọng quá 1 tuần!", trashThreads.size());
        }
    }
}