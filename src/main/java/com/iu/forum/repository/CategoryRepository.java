package com.iu.forum.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iu.forum.model.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // Lấy tất cả chuyên mục chưa bị xóa, sắp xếp theo thứ tự hiển thị
    List<Category> findByDeletedFalseOrderByDisplayOrderAsc();
    
}