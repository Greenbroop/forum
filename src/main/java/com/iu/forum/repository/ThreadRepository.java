package com.iu.forum.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iu.forum.model.Thread;

@Repository
public interface ThreadRepository extends JpaRepository<Thread, Long> {
    
    // Lấy tất cả bài chưa bị xóa (Sắp xếp theo ý muốn)
    List<Thread> findByDeletedFalse(Sort sort);
    List<Thread> findByCreatedAtBeforeAndDeletedFalse(LocalDateTime date);

    // Tìm kiếm bằng từ khóa đối với các bài chưa bị xóa
    List<Thread> findByTitleContainingIgnoreCaseAndDeletedFalse(String keyword);
}