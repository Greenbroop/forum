package com.iu.forum.repository;

import com.iu.forum.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    // =========================================================================
    // HÀM TÌM KIẾM TOKEN XÁC THỰC EMAIL
    // =========================================================================
    // Cơ chế hoạt động: 
    // - Spring Data JPA tự động dịch tên hàm này thành câu lệnh SQL: 
    //   SELECT * FROM verification_token WHERE token = ?
    Optional<VerificationToken> findByToken(String token);
}