package com.iu.forum.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iu.forum.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Tìm kiếm người dùng bằng Username phục vụ đăng nhập
    Optional<User> findByUsername(String username);

    // Kiểm tra Email đã tồn tại hay chưa khi đăng ký
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}