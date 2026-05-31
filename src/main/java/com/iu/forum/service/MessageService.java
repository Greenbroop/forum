package com.iu.forum.service;

import com.iu.forum.model.Message;
import com.iu.forum.model.Thread;
import com.iu.forum.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    // Lấy danh sách toàn bộ bình luận của một chủ đề thảo luận cụ thể
    public List<Message> getMessagesByThread(Thread thread) {
        return messageRepository.findByThread(thread);
    }

    // Nghiệp vụ đăng bài viết/bình luận mới
    public Message postNewMessage(Message message) {
        // Bạn có thể bổ sung các logic lọc từ ngữ nhạy cảm hoặc spam tại đây trước khi lưu
        if (message.getContent() == null || message.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung bài viết không được để trống!");
        }
        return messageRepository.save(message);
    }

    // Xóa một tin nhắn cụ thể (Dành cho chức năng xử lý vi phạm của Moderator) [cite: 52]
    public void deleteMessageById(Long id) {
        if (!messageRepository.existsById(id)) {
            throw new IllegalArgumentException("Bài viết cần xóa không tồn tại trên hệ thống!");
        }
        messageRepository.deleteById(id);
    }
}