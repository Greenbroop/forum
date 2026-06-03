package com.iu.forum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 1. Hỗ trợ Thymeleaf nhận diện người dùng (Dùng cho các thẻ sec:authorize trên HTML)
    @Bean
    public SpringSecurityDialect springSecurityDialect() {
        return new SpringSecurityDialect();
    }

    // 2. Mã hóa mật khẩu an toàn bằng thuật toán băm (BCrypt) để bảo vệ Database
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 3. Bộ lọc phân quyền trung tâm (Điều hướng và bảo mật toàn bộ request)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Tắt bảo vệ CSRF để cho phép làm nút Đăng xuất bằng thẻ <a> (GET request)
                .csrf(csrf -> csrf.disable())

                // PHÂN QUYỀN TRUY CẬP (AUTHORIZATION)
                .authorizeHttpRequests(auth -> auth
                        // Cấp quyền tự do cho trang chủ, đăng nhập, CSS/JS và xem file đính kèm (uploads)
                        .requestMatchers("/", "/index", "/thread/**", "/login", "/register", "/verify", 
                                         "/css/**", "/js/**", "/images/**", "/uploads/**").permitAll()
                        
                        // Khu vực dành cho Điều phối viên và Quản trị viên
                        .requestMatchers("/mod/**").hasAnyRole("MODERATOR", "ADMIN")
                        
                        // Khu vực thiết lập hệ thống dành riêng cho Admin
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        
                        // Mọi đường dẫn khác đều bắt buộc người dùng phải đăng nhập
                        .anyRequest().authenticated()
                )

                // CẤU HÌNH ĐĂNG NHẬP (AUTHENTICATION)
                .formLogin(form -> form
                        .loginPage("/login") 
                        .defaultSuccessUrl("/", true) 
                        // Xử lý các ngoại lệ khi đăng nhập thất bại để báo lỗi ra UI
                        .failureHandler((request, response, exception) -> {
                            String errorMessage = "bad_credentials"; // Mặc định sai tài khoản/mật khẩu
                            if (exception instanceof DisabledException) {
                                errorMessage = "disabled"; // Lỗi chưa xác thực Email
                            } else if (exception instanceof LockedException) {
                                errorMessage = "locked"; // Lỗi tài khoản bị khóa (Ban)
                            }
                            response.sendRedirect("/login?error=" + errorMessage);
                        })
                        .permitAll()
                )

                // TÍNH NĂNG GHI NHỚ ĐĂNG NHẬP (Lưu trạng thái trên trình duyệt 30 ngày)
                .rememberMe(remember -> remember
                        .key("superSecretKeyForForum") 
                        .rememberMeParameter("remember-me") 
                        .tokenValiditySeconds(30 * 24 * 60 * 60)
                )

                // CẤU HÌNH ĐĂNG XUẤT (Dọn dẹp sạch phiên làm việc)
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true) // Hủy session trên server
                        .deleteCookies("JSESSIONID", "remember-me") // Xóa cookie trên trình duyệt
                        .permitAll()
                );

        return http.build();
    }
}