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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

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

    // Hiển thị giao diện Đăng bài viết mới
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        return "common/create-thread";
    }

    // XỬ LÝ LOGIC ĐĂNG BÀI VIẾT
    @PostMapping("/create")
    public String createThread(Principal principal,
                               @RequestParam("title") String title,
                               @RequestParam("categoryId") Long categoryId,
                               @RequestParam("content") String content,
                               @RequestParam(value = "tags", required = false) String tagString,
                               @RequestParam(value = "file", required = false) MultipartFile file) {

        if (principal == null) {
            return "redirect:/login";
        }

        var creator = userRepository.findByUsername(principal.getName()).orElse(null);
        var category = categoryRepository.findById(categoryId).orElse(null);

        if (creator != null && category != null) {

            Thread newThread = new Thread(title, creator);
            newThread.setCategory(category);
            newThread.setUpdatedAt(LocalDateTime.now());

            if (tagString != null && !tagString.trim().isEmpty()) {
                String[] tagNames = tagString.split(",");
                for (String tagName : tagNames) {
                    String cleanTagName = tagName.trim();
                    if (!cleanTagName.isEmpty()) {
                        Tag tag = tagRepository.findByName(cleanTagName)
                                .orElseGet(() -> {
                                    Tag newTag = new Tag(cleanTagName);
                                    newTag.setCreatedAt(LocalDateTime.now());
                                    newTag.setUpdatedAt(LocalDateTime.now());
                                    return tagRepository.save(newTag);
                                });
                        newThread.getTags().add(tag);
                    }
                }
            }

            Thread savedThread = threadRepository.save(newThread);
            Message firstMessage = new Message(content, creator, savedThread);

            // ====================================================================
            // XỬ LÝ FILE UPLOAD (ĐÃ FIX LỖI TIẾNG VIỆT CHO RAILWAY)
            // ====================================================================
            if (file != null && !file.isEmpty()) {
                try {
                    String uploadDir = System.getProperty("user.dir") + "/uploads/";
                    Path uploadPath = Paths.get(uploadDir);

                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }

                    // 1. Tách tên file và đuôi file
                    String originalFilename = file.getOriginalFilename();
                    String fileExtension = "";
                    String baseName = originalFilename;
                    if (originalFilename != null && originalFilename.lastIndexOf(".") != -1) {
                        fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                        baseName = originalFilename.substring(0, originalFilename.lastIndexOf("."));
                    }

                    // 2. Chuyển tiếng Việt có dấu thành không dấu
                    String temp = Normalizer.normalize(baseName, Normalizer.Form.NFD);
                    Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
                    String noAccentName = pattern.matcher(temp).replaceAll("")
                            .replace("Đ", "D").replace("đ", "d");

                    // 3. Xóa các ký tự đặc biệt, thay khoảng trắng bằng gạch dưới
                    String safeBaseName = noAccentName.replaceAll("[^a-zA-Z0-9-]", "_");

                    // 4. Tạo tên file cuối cùng: UUID + Tên đã làm sạch + Đuôi file
                    String uniqueFileName = UUID.randomUUID().toString() + "_" + safeBaseName + fileExtension;
                    
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