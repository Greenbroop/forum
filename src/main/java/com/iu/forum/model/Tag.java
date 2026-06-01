package com.iu.forum.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tags")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tên tag không được trùng lặp
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    // KHAI BÁO NGƯỢC LẠI CHO QUAN HỆ NHIỀU-NHIỀU (mappedBy trỏ tới biến 'tags' trong Thread)
    @ManyToMany(mappedBy = "tags")
    private Set<Thread> threads = new HashSet<>();

    public Tag() {
    }

    public Tag(String name) {
        this.name = name;
    }

    // --- GETTER VÀ SETTER ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Thread> getThreads() {
        return threads;
    }

    public void setThreads(Set<Thread> threads) {
        this.threads = threads;
    }
}