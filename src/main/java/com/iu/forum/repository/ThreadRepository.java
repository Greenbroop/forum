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

       Page<Thread> findByDeletedFalse(Pageable pageable);

       List<Thread> findByCreatedAtBeforeAndDeletedFalse(LocalDateTime date);

       Page<Thread> findByTitleContainingIgnoreCaseAndDeletedFalse(String keyword, Pageable pageable);

       Page<Thread> findByCreatorFullNameContainingIgnoreCaseAndDeletedFalse(String keyword, Pageable pageable);

       // HÀM CHUẨN ĐỂ TÌM TRONG NỘI DUNG (Xuyên qua bảng Message)
       Page<Thread> findDistinctByMessagesContentContainingIgnoreCaseAndDeletedFalse(String keyword, Pageable pageable);

       // Tìm theo tên đầy đủ hoặc username của tác giả
       Page<Thread> findByCreator_FullNameContainingIgnoreCaseOrCreator_UsernameContainingIgnoreCaseAndDeletedFalse(
                     String fullName, String username, Pageable pageable);
       // Tìm các bài viết ĐÃ bị xóa mềm (deleted = true) và tạo từ TRƯỚC một mốc thời
       // gian
       List<Thread> findByDeletedTrueAndCreatedAtBefore(LocalDateTime date);

       // BƯỚC QUAN TRỌNG: Dùng LEFT JOIN t.tags tag để móc nối dữ liệu qua bảng trung
       // gian
       @Query("SELECT DISTINCT t FROM Thread t " +
                     "LEFT JOIN t.tags tag " +
                     "WHERE t.deleted = false " +
                     "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
                     "AND (:authorId IS NULL OR t.creator.id = :authorId) " +
                     "AND (:tagId IS NULL OR tag.id = :tagId) " + // <--- BỔ SUNG ĐIỀU KIỆN TAG
                     "AND (:status IS NULL OR :status = '' OR t.status = :status) " +
                     "AND (CAST(:startDate AS date) IS NULL OR t.createdAt >= :startDate) " +
                     "AND (CAST(:endDate AS date) IS NULL OR t.createdAt <= :endDate) " +
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