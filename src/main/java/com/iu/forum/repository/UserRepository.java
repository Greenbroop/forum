package com.iu.forum.repository;

import com.iu.forum.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Tìm kiếm người dùng bằng Username phục vụ đăng nhập
    Optional<User> findByUsername(String username);

    // Kiểm tra Email đã tồn tại hay chưa khi đăng ký
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}