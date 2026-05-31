package com.iu.forum.repository;

import com.iu.forum.model.Message;
import com.iu.forum.model.Thread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    // Lấy danh sách toàn bộ tin nhắn thuộc về một Thread cụ thể
    List<Message> findByThread(Thread thread);
    
    // Tự động xóa tất cả các tin nhắn thuộc một Thread (Dùng khi xóa Thread)
    void deleteByThread(Thread thread);
}