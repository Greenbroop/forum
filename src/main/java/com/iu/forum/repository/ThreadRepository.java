package com.iu.forum.repository;

import com.iu.forum.model.Thread;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ThreadRepository extends JpaRepository<Thread, Long> {
    
    // 1. Lấy tất cả bài chưa bị xóa (Có Phân trang & Sắp xếp)
    Page<Thread> findByDeletedFalse(Pageable pageable);
    
    // Hàm này giữ nguyên List vì thường dùng cho tính năng dọn dẹp chạy ngầm của Admin
    List<Thread> findByCreatedAtBeforeAndDeletedFalse(LocalDateTime date);

    // 2. Tìm kiếm bằng từ khóa trong Tiêu đề (Có Phân trang)
    Page<Thread> findByTitleContainingIgnoreCaseAndDeletedFalse(String keyword, Pageable pageable);

    // 3. Tìm theo Tên tác giả (Có Phân trang)
    Page<Thread> findByCreatorFullNameContainingIgnoreCaseAndDeletedFalse(String keyword, Pageable pageable);

    // 4. Tìm theo Nội dung bình luận (Có Phân trang)
    Page<Thread> findDistinctByMessagesContentContainingIgnoreCaseAndDeletedFalse(String keyword, Pageable pageable);

    // 5. TÍNH NĂNG MỚI: Siêu bộ lọc 5 tiêu chí kết hợp (Có Phân trang)
    @Query("SELECT DISTINCT t FROM Thread t " +
           "WHERE t.deleted = false " +
           "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
           "AND (:authorId IS NULL OR t.creator.id = :authorId) " +
           "AND (:status IS NULL OR :status = '' OR t.status = :status) " +
           "AND (CAST(:startDate AS date) IS NULL OR t.createdAt >= :startDate) " +
           "AND (CAST(:endDate AS date) IS NULL OR t.createdAt <= :endDate) " +
           "AND (:hasImage IS NULL OR :hasImage = false OR EXISTS (SELECT 1 FROM Message m WHERE m.thread = t AND m.fileUrl IS NOT NULL AND m.fileUrl != ''))")
    Page<Thread> advancedFilter(
            @Param("categoryId") Long categoryId,
            @Param("authorId") Long authorId,
            @Param("status") String status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("hasImage") Boolean hasImage,
            Pageable pageable); // <--- Trùm cuối Pageable nằm ở đây
}