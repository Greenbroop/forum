package com.iu.forum.controller;

import com.iu.forum.model.Message;
import com.iu.forum.model.Thread;
import com.iu.forum.model.Tag;
import com.iu.forum.repository.CategoryRepository;
import com.iu.forum.repository.MessageRepository;
import com.iu.forum.repository.TagRepository;
import com.iu.forum.repository.ThreadRepository;
import com.iu.forum.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile; // THÊM IMPORT NÀY

// THÊM CÁC THƯ VIỆN ĐỂ XỬ LÝ LƯU FILE
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@RequestMapping("/thread")
public class ThreadController {

    @Autowired
    private ThreadRepository threadRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private TagRepository tagRepository;

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        return "common/create-thread";
    }

    @PostMapping("/create")
    public String createThread(Principal principal,
                               @RequestParam("title") String title,
                               @RequestParam("categoryId") Long categoryId,
                               @RequestParam("content") String content,
                               @RequestParam(value = "tags", required = false) String tagString,
                               @RequestParam(value = "file", required = false) MultipartFile file) {

        // 1. CHẶN LỖI ĐĂNG NHẬP (Chưa đăng nhập thì đá về trang login)
        if (principal == null) {
            return "redirect:/login";
        }

        var creator = userRepository.findByUsername(principal.getName()).orElse(null);
        var category = categoryRepository.findById(categoryId).orElse(null);

        if (creator != null && category != null) {

            Thread newThread = new Thread(title, creator);
            newThread.setCategory(category);
            
            // 2. VÁ LỖI DATABASE: Bắt buộc gán updatedAt để không bị lỗi NOT NULL
            newThread.setUpdatedAt(LocalDateTime.now());

            // Xử lý Tags
            if (tagString != null && !tagString.trim().isEmpty()) {
                String[] tagNames = tagString.split(",");
                for (String tagName : tagNames) {
                    String cleanTagName = tagName.trim();
                    if (!cleanTagName.isEmpty()) {
                        Tag tag = tagRepository.findByName(cleanTagName)
                                .orElseGet(() -> {
                                    Tag newTag = new Tag(cleanTagName);
                                    // VÁ LỖI TƯƠNG TỰ CHO TAG: Gán thời gian trước khi lưu
                                    newTag.setCreatedAt(LocalDateTime.now());
                                    newTag.setUpdatedAt(LocalDateTime.now());
                                    return tagRepository.save(newTag);
                                });
                        newThread.getTags().add(tag);
                    }
                }
            }

            Thread savedThread = threadRepository.save(newThread);

            // Tạo nội dung (Message) đầu tiên
            Message firstMessage = new Message(content, creator, savedThread);

            // Xử lý File Upload
            if (file != null && !file.isEmpty()) {
                try {
                    String uploadDir = System.getProperty("user.dir") + "/uploads/";
                    Path uploadPath = Paths.get(uploadDir);

                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }

                    String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                    Path filePath = uploadPath.resolve(uniqueFileName);

                    Files.copy(file.getInputStream(), filePath);

                    firstMessage.setFileUrl("/uploads/" + uniqueFileName);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            messageRepository.save(firstMessage);

            return "redirect:/thread/" + savedThread.getId();
        }

        return "redirect:/?error";
    }
}