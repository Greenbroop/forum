package com.iu.forum.repository;

import com.iu.forum.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // Lấy tất cả chuyên mục chưa bị xóa (Soft Delete = false), 
    // và sắp xếp theo thứ tự hiển thị (Display Order) tăng dần (Ascending).
    List<Category> findByDeletedFalseOrderByDisplayOrderAsc();

    // Hàm thống kê số lượng bài viết chưa bị xóa của mỗi chuyên mục (Dùng cho Chart.js)
    // Dùng JPQL (Java Persistence Query Language) thay vì SQL thuần để tương tác trực tiếp với các Thực thể (Entities).
    @Query("SELECT c.name, (SELECT COUNT(t) FROM Thread t WHERE t.category = c AND t.deleted = false) FROM Category c")
    List<Object[]> getCategoryStats(); // Trả về mảng Object (Do chứa 2 kiểu dữ liệu khác nhau là String và Long)
    
}