package com.iu.forum.repository;

import com.iu.forum.model.Thread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Sort;

@Repository
public interface ThreadRepository extends JpaRepository<Thread, Long> {
    
    // Lấy tất cả bài chưa bị xóa (Sắp xếp theo ý muốn)
    List<Thread> findByDeletedFalse(Sort sort);
    List<Thread> findByCreatedAtBeforeAndDeletedFalse(LocalDateTime date);

    // Tìm kiếm bằng từ khóa đối với các bài chưa bị xóa
    List<Thread> findByTitleContainingIgnoreCaseAndDeletedFalse(String keyword);

    // 2. MỚI: Tìm theo Tên tác giả (Tìm vào bảng User thông qua biến creator)
    List<Thread> findByCreatorFullNameContainingIgnoreCaseAndDeletedFalse(String keyword);

    // 3. MỚI: Tìm theo Nội dung (Tìm vào bảng Message liên kết với Thread)
    // Lưu ý: Dùng chữ "Distinct" để tránh việc 1 bài viết bị hiển thị lặp lại nhiều lần 
    // nếu có nhiều bình luận bên trong cùng chứa từ khóa đó.
    List<Thread> findDistinctByMessagesContentContainingIgnoreCaseAndDeletedFalse(String keyword);
}