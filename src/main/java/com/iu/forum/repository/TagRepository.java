package com.iu.forum.repository;

import com.iu.forum.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    
    // Tìm tag theo tên để kiểm tra xem nó đã tồn tại chưa trước khi tạo mới
    Optional<Tag> findByName(String name);
}