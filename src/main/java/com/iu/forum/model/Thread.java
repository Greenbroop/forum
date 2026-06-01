package com.iu.forum.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "threads")
public class Thread {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = true)
    private Category category;

    // BỔ SUNG QUAN HỆ 1-N: 1 Thread có nhiều Message
    // Biến này tên là 'messages', do đó Spring Boot sẽ hiểu tên hàm 'findDistinctByMessagesContent...'
    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL)
    private java.util.List<Message> messages;

    @ManyToMany
    @JoinTable(
        name = "thread_tags", // Spring Boot sẽ tự động tạo bảng trung gian mang tên này
        joinColumns = @JoinColumn(name = "thread_id"), // Khóa ngoại trỏ về bảng threads
        inverseJoinColumns = @JoinColumn(name = "tag_id") // Khóa ngoại trỏ về bảng tags
    )
    private java.util.Set<Tag> tags = new java.util.HashSet<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Audit trail: Lưu vết thời gian chỉnh sửa cuối cùng

    @Column(name = "views", nullable = false)
    private int views = 0; // Lượt xem

    @Column(name = "is_sticky", nullable = false)
    private boolean isSticky = false; // Ghim bài viết lên đầu

    @Column(name = "status", length = 20, nullable = false)
    private String status = "OPEN"; // Trạng thái: OPEN (Đang mở) hoặc CLOSED (Khóa bình luận)

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false; // TÍNH NĂNG SOFT DELETE: true = đã xóa (ẩn), false = hiển thị

    public Thread() {
        this.createdAt = LocalDateTime.now();
    }

    public Thread(String title, User creator) {
        this.title = title;
        this.creator = creator;
        this.createdAt = LocalDateTime.now();
    }

    // Tự động cập nhật thời gian updatedAt mỗi khi Entity bị thay đổi 
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- GETTER VÀ SETTER ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public boolean isSticky() {
        return isSticky;
    }

    public void setSticky(boolean sticky) {
        this.isSticky = sticky;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
    public java.util.List<Message> getMessages() {
        return messages;
    }

    public void setMessages(java.util.List<Message> messages) {
        this.messages = messages;
    }
    public java.util.Set<Tag> getTags() {
        return tags;
    }

    public void setTags(java.util.Set<Tag> tags) {
        this.tags = tags;
    }
}