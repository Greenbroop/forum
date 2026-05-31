package com.iu.forum.repository;

import com.iu.forum.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // Lấy tất cả chuyên mục chưa bị xóa, sắp xếp theo thứ tự hiển thị
    List<Category> findByDeletedFalseOrderByDisplayOrderAsc();
    
}