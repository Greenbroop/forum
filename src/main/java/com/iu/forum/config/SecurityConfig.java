package com.iu.forum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 1. Cầu nối bắt buộc để Thymeleaf hiểu được các thẻ phân quyền (Ẩn/hiện menu)
    @Bean
    public SpringSecurityDialect springSecurityDialect() {
        return new SpringSecurityDialect();
    }

    // Bean mã hóa mật khẩu để không lưu mật khẩu thô dưới dạng văn bản
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Tắt CSRF để cho phép đăng xuất bằng thẻ <a> đơn giản trong môi trường Lab
                .csrf(csrf -> csrf.disable())

                // Phân quyền truy cập dựa trên đường dẫn URL
                .authorizeHttpRequests(auth -> auth
                        // ==========================================
                        // ĐÃ SỬA: Bổ sung "/verify" vào danh sách được phép truy cập tự do
                        // ==========================================
                        .requestMatchers("/", "/index", "/thread/**", "/login", "/register", "/verify", "/css/**", "/js/**",
                                "/images/**", "/uploads/**")
                        .permitAll()
                        .requestMatchers("/mod/**").hasAnyRole("MODERATOR", "ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                // Cấu hình trang Đăng nhập
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .failureHandler((request, response, exception) -> {
                            String errorMessage = "bad_credentials";

                            // Nếu lỗi do tài khoản bị disabled (active = false)
                            if (exception instanceof org.springframework.security.authentication.DisabledException) {
                                errorMessage = "disabled";
                            }
                            // Nếu lỗi do tài khoản bị khóa (locked)
                            else if (exception instanceof org.springframework.security.authentication.LockedException) {
                                errorMessage = "locked";
                            }

                            response.sendRedirect("/login?error=" + errorMessage);
                        })
                        .permitAll())
                        
                // TÍNH NĂNG MỚI: Ghi nhớ đăng nhập trong 30 ngày (Dùng Cookie)
                .rememberMe(remember -> remember
                        .key("superSecretKeyForForum") // Mã bí mật để mã hóa cookie
                        .rememberMeParameter("remember-me") // Trùng với thuộc tính name="" của thẻ input checkbox trên HTML
                        .tokenValiditySeconds(30 * 24 * 60 * 60) // Thời gian sống: 30 ngày
                )
                // Cấu hình Đăng xuất
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "remember-me") // Xóa luôn cookie remember-me khi đăng xuất
                        .permitAll());

        return http.build();
    }
}