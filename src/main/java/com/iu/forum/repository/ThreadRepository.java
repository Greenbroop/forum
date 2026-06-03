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
       long countByDeletedFalse();

       // Phân trang danh sách các bài viết ĐANG HOẠT ĐỘNG (Chưa bị xóa mềm)
       Page<Thread> findByDeletedFalse(Pageable pageable);

       // Tìm bài viết tạo trước một mốc thời gian (Thường dùng cho tính năng dọn dẹp hệ thống)
       List<Thread> findByCreatedAtBeforeAndDeletedFalse(LocalDateTime date);

       // TÌM KIẾM CƠ BẢN: Tìm theo Tiêu đề (Bỏ qua hoa thường: IgnoreCase)
       Page<Thread> findByTitleContainingIgnoreCaseAndDeletedFalse(String keyword, Pageable pageable);

       // TÌM KIẾM THEO TÁC GIẢ (Sử dụng dấu _ để chui vào thuộc tính của bảng User)
       Page<Thread> findByCreatorFullNameContainingIgnoreCaseAndDeletedFalse(String keyword, Pageable pageable);

       // HÀM CHUẨN ĐỂ TÌM TRONG NỘI DUNG (Xuyên qua bảng Message)
       // Vì Thread và Message là quan hệ 1-Nhiều, dùng Distinct để tránh bài viết bị nhân bản nếu trùng nhiều Message
       Page<Thread> findDistinctByMessagesContentContainingIgnoreCaseAndDeletedFalse(String keyword, Pageable pageable);

       // Tìm theo tên đầy đủ HOẶC (Or) username của tác giả (Xuyên vào bảng User)
       Page<Thread> findByCreator_FullNameContainingIgnoreCaseOrCreator_UsernameContainingIgnoreCaseAndDeletedFalse(
                     String fullName, String username, Pageable pageable);

       // Tìm các bài viết ĐÃ bị xóa mềm (deleted = true) và tạo từ TRƯỚC một mốc thời gian (VD: Dọn rác sau 30 ngày)
       List<Thread> findByDeletedTrueAndCreatedAtBefore(LocalDateTime date);

       // =========================================================================
       // BỘ LỌC NÂNG CAO (ADVANCED FILTER) - TRÁI TIM CỦA TÍNH NĂNG TÌM KIẾM
       // Dùng JPQL (Java Persistence Query Language) để xây dựng câu truy vấn động.
       // =========================================================================
       // BƯỚC QUAN TRỌNG: Dùng LEFT JOIN t.tags tag để móc nối dữ liệu qua bảng trung gian (Many-to-Many).
       @Query("SELECT DISTINCT t FROM Thread t " +
                     "LEFT JOIN t.tags tag " +
                     "WHERE t.deleted = false " +
                     // Kỹ thuật Dynamic Query: Nếu tham số truyền vào bị NULL, điều kiện đó tự động bị bỏ qua (True)
                     "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
                     "AND (:authorId IS NULL OR t.creator.id = :authorId) " +
                     "AND (:tagId IS NULL OR tag.id = :tagId) " + // <--- BỔ SUNG ĐIỀU KIỆN LỌC THEO TAG
                     "AND (:status IS NULL OR :status = '' OR t.status = :status) " +
                     "AND (CAST(:startDate AS date) IS NULL OR t.createdAt >= :startDate) " +
                     "AND (CAST(:endDate AS date) IS NULL OR t.createdAt <= :endDate) " +
                     // Kỹ thuật Subquery (EXISTS): Kiểm tra bài viết này có tồn tại ít nhất 1 tin nhắn đính kèm File hay không
                     "AND (:hasImage IS NULL OR :hasImage = false OR EXISTS (SELECT 1 FROM Message m WHERE m.thread = t AND m.fileUrl IS NOT NULL AND m.fileUrl != ''))")
       Page<Thread> advancedFilter(
                     @Param("categoryId") Long categoryId,
                     @Param("authorId") Long authorId,
                     @Param("tagId") Long tagId, // <--- THÊM THAM SỐ NÀY
                     @Param("status") String status,
                     @Param("startDate") LocalDateTime startDate,
                     @Param("endDate") LocalDateTime endDate,
                     @Param("hasImage") Boolean hasImage,
                     Pageable pageable);
}