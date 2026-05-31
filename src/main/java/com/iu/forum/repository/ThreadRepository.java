package com.iu.forum.repository;

import com.iu.forum.model.Thread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // TÍNH NĂNG MỚI: Siêu bộ lọc 5 tiêu chí kết hợp
    @Query("SELECT DISTINCT t FROM Thread t " +
           "WHERE t.deleted = false " +
           "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
           "AND (:authorId IS NULL OR t.creator.id = :authorId) " +
           "AND (:status IS NULL OR :status = '' OR t.status = :status) " +
           "AND (CAST(:startDate AS date) IS NULL OR t.createdAt >= :startDate) " +
           "AND (CAST(:endDate AS date) IS NULL OR t.createdAt <= :endDate) " +
           "AND (:hasImage IS NULL OR :hasImage = false OR EXISTS (SELECT 1 FROM Message m WHERE m.thread = t AND m.fileUrl IS NOT NULL AND m.fileUrl != ''))")
    List<Thread> advancedFilter(
            @Param("categoryId") Long categoryId,
            @Param("authorId") Long authorId,
            @Param("status") String status,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate,
            @Param("hasImage") Boolean hasImage);
}