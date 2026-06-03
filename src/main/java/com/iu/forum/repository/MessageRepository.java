package com.iu.forum.repository;

import com.iu.forum.model.Message;
import com.iu.forum.model.Thread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    // 1. Đếm tổng số bình luận đang hoạt động (Hiển thị cho bảng điều khiển Dashboard)
    long countByDeletedFalse();
    
    // 2. Lấy danh sách bình luận của một Thread, nhưng CHỈ lấy những bình luận CHƯA bị xóa
    List<Message> findByThreadAndDeletedFalse(Thread thread);
    
    // 3. XÓA MỀM (SOFT-DELETE): Chuẩn xác và an toàn.
    // Chỉ cập nhật cột deleted = true đối với những bình luận thuộc về cái Thread đang bị xóa.
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.deleted = true WHERE m.thread = :thread")
    void softDeleteByThread(@Param("thread") Thread thread);
}