package com.iu.forum.controller;

import com.iu.forum.model.Message;
import com.iu.forum.model.Thread;
import com.iu.forum.model.User;
import com.iu.forum.repository.CategoryRepository;
import com.iu.forum.repository.MessageRepository;
import com.iu.forum.repository.TagRepository;
import com.iu.forum.repository.ThreadRepository;
import com.iu.forum.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.UUID;

@Controller
public class HomeController {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ThreadRepository threadRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    // Xử lý hiển thị trang chủ và chức năng Lọc/Tìm kiếm nâng cao
    @GetMapping({ "/", "/index" })
    public String index(
            // TÍNH NĂNG MỚI: Hứng tham số searchBy từ menu thả xuống (Tìm theo Tiêu đề, Tác giả, Nội dung)
            @RequestParam(value = "searchBy", required = false, defaultValue = "title") String searchBy,
            @RequestParam(value = "keyword", required = false) String keyword, // Từ khóa người dùng nhập vào ô tìm kiếm
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "authorId", required = false) Long authorId,
            @RequestParam(value = "tagId", required = false) Long tagId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr,
            @RequestParam(value = "hasImage", required = false) Boolean hasImage,
            @RequestParam(value = "sort", required = false, defaultValue = "newest") String sortParam,
            @RequestParam(value = "page", defaultValue = "1") int page, // Trang hiện tại (Mặc định là trang 1)
            @RequestParam(value = "size", defaultValue = "10") int size, // Số lượng bài viết trên 1 trang
            Model model) {

        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        // Chuyển đổi định dạng ngày tháng từ chuỗi String (do HTML gửi lên) sang kiểu LocalDateTime của Java
        try {
            if (startDateStr != null && !startDateStr.isEmpty()) {
                startDate = java.time.LocalDate.parse(startDateStr).atStartOfDay(); // Bắt đầu từ 00:00:00 của ngày đó
            }

            if (endDateStr != null && !endDateStr.isEmpty()) {
                endDate = java.time.LocalDate.parse(endDateStr).atTime(23, 59, 59); // Kết thúc vào 23:59:59 của ngày đó
            }
        } catch (Exception e) {
            // Bỏ qua lỗi parse ngày tháng (Giữ null)
        }

        // ==========================================
        // 1. CROSS-FIELD VALIDATION (Kiểm tra logic chéo)
        // ==========================================
        // Đảm bảo người dùng không nhập ngày bắt đầu lớn hơn ngày kết thúc
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {

            model.addAttribute("dateError",
                    "⚠️ Lỗi: 'Từ ngày' không được diễn ra sau 'Đến ngày'!");

            // Vô hiệu hóa bộ lọc ngày bị lỗi để tránh gây lỗi (Crash) cho Database khi query
            startDate = null;
            endDate = null;
        }

        Sort sort;

        // Xử lý logic sắp xếp (Sorting) dựa vào tham số truyền từ HTML
        switch (sortParam) {

            case "oldest":
                sort = Sort.by(Sort.Direction.ASC, "createdAt");
                break;

            case "titleAsc":
                sort = Sort.by(Sort.Direction.ASC, "title"); // Xếp theo bảng chữ cái A-Z
                break;

            case "titleDesc":
                sort = Sort.by(Sort.Direction.DESC, "title"); // Xếp theo bảng chữ cái Z-A
                break;

            case "viewsDesc":
                sort = Sort.by(Sort.Direction.DESC, "views"); // Xếp theo lượt xem cao nhất
                break;

            case "newest":
            default:
                sort = Sort.by(Sort.Direction.DESC, "createdAt"); // Mặc định: Bài viết mới nhất lên đầu
                break;
        }

        // Đóng gói thông tin Phân trang (Pagination) và Sắp xếp (Sorting)
        Pageable pageable = PageRequest.of(page - 1, size, sort); // Core Java: Index trang bắt đầu từ 0, nên phải trừ 1

        Page<Thread> threadPage;

        // KIỂM TRA ĐIỀU KIỆN LỌC: 
        // Nếu người dùng sử dụng BẤT KỲ một bộ lọc Nâng cao nào (Category, Author, Tag, Date...)
        if (categoryId != null
                || authorId != null
                || tagId != null
                || (status != null && !status.isEmpty())
                || startDate != null
                || endDate != null
                || (hasImage != null && hasImage)) {

            // Gọi hàm advancedFilter (đã được viết bằng custom query trong ThreadRepository)
            threadPage = threadRepository.advancedFilter(
                    categoryId,
                    authorId,
                    tagId,
                    status,
                    startDate,
                    endDate,
                    hasImage,
                    pageable);

        // KIỂM TRA TÌM KIẾM CƠ BẢN: Nếu người dùng chỉ gõ Text vào ô tìm kiếm
        } else if (keyword != null && !keyword.trim().isEmpty()) {

            String kw = keyword.trim(); // Cắt khoảng trắng dư thừa
            // Rẽ nhánh tìm kiếm dựa trên tham số searchBy từ dropdown menu
            if ("content".equals(searchBy)) {
                // ĐÃ SỬA: Dùng hàm tìm kiếm xuyên qua danh sách Message bên trong Thread
                threadPage = threadRepository.findDistinctByMessagesContentContainingIgnoreCaseAndDeletedFalse(kw, pageable);
                
            } else if ("author".equals(searchBy)) {
                // Tìm theo tên hiển thị (FullName) HOẶC tên tài khoản (Username)
                threadPage = threadRepository.findByCreator_FullNameContainingIgnoreCaseOrCreator_UsernameContainingIgnoreCaseAndDeletedFalse(kw, kw, pageable);
                
            } else {
                // Mặc định: Tìm kiếm theo Tiêu đề bài viết (Title)
                threadPage = threadRepository.findByTitleContainingIgnoreCaseAndDeletedFalse(kw, pageable);
            }

        } else {
            // TRẠNG THÁI MẶC ĐỊNH (Khi vừa vào trang web): Lấy toàn bộ bài viết (ngoại trừ các bài đã bị xóa mềm)
            threadPage = threadRepository.findByDeletedFalse(pageable);
        }

        // Đẩy toàn bộ dữ liệu xuống View (HTML) để render ra màn hình
        model.addAttribute("threads", threadPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);

        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("tags", tagRepository.findAll());

        // Đẩy lại các giá trị lọc ra View để giao diện HTML "nhớ" và giữ đúng lựa chọn của người dùng trên form
        model.addAttribute("searchBy", searchBy);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("selectedAuthor", authorId);
        model.addAttribute("selectedTag", tagId);
        model.addAttribute("selectedStatus", status);

        model.addAttribute("startDate", startDateStr);
        model.addAttribute("endDate", endDateStr);

        model.addAttribute("hasImage", hasImage);
        model.addAttribute("currentSort", sortParam);

        return "common/index";
    }

    // Xử lý xem chi tiết một bài viết (Bao gồm nội dung chính và các bình luận bên dưới)
    @GetMapping("/thread/{id}")
    public String threadDetail(
            @PathVariable Long id,
            @RequestParam(value = "error", required = false) String error, // Bắt tham số báo lỗi (nếu có) trên thanh URL
            Model model) {

        // Tìm bài viết theo ID. Nếu không thấy sẽ ném ra lỗi (Exception)
        Thread thread = threadRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy chủ đề với ID: " + id));

        // Xử lý hiển thị thông báo lỗi dựa trên tham số error truyền về từ hàm post reply
        if (error != null) {

            if (error.equals("ThreadIsClosed")) {
                model.addAttribute(
                        "errorMessage",
                        "Chủ đề này đã bị khóa, không thể bình luận!");
            }

            if (error.equals("FileTooLarge")) {
                model.addAttribute(
                        "errorMessage",
                        "Tệp đính kèm quá lớn. Vui lòng tải lên tệp dưới 5MB.");
            }

            if (error.equals("InvalidFileType")) {
                model.addAttribute(
                        "errorMessage",
                        "Định dạng tệp không hợp lệ. Chỉ chấp nhận ảnh hoặc tài liệu cơ bản.");
            }
        }

        model.addAttribute("thread", thread);
        // Tải toàn bộ danh sách bình luận (Messages) thuộc về bài viết này
        model.addAttribute("messages", messageRepository.findByThreadAndDeletedFalse(thread));

        return "common/thread-detail";
    }

    // Xử lý hành động người dùng Đăng bình luận (Reply) vào một bài viết
    @PostMapping("/thread/{id}/reply")
    public String replyToThread(
            @PathVariable Long id, // ID của bài viết đang được bình luận
            @RequestParam("content") String content, // Nội dung text của bình luận
            @RequestParam(value = "file", required = false) MultipartFile file, // Hứng file đính kèm (nếu có) từ input type="file"
            Principal principal) {

        // Kiểm tra bảo mật: Nếu chưa đăng nhập thì đá về trang Login
        if (principal == null) {
            return "redirect:/login";
        }

        Thread thread = threadRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy chủ đề"));

        // Kiểm tra trạng thái: Nếu bài viết đã bị Mod khóa (CLOSED) thì từ chối bình luận
        if ("CLOSED".equals(thread.getStatus())) {
            // Gắn tham số error lên URL và redirect lại trang chi tiết để hiển thị thông báo lỗi
            return "redirect:/thread/" + id + "?error=ThreadIsClosed";
        }

        // Lấy thực thể (Entity) người dùng đang thao tác
        User currentUser = userRepository
                .findByUsername(principal.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy tài khoản"));

        // Khởi tạo một đối tượng Message (Bình luận) mới và gán dữ liệu
        Message newMessage = new Message();

        newMessage.setContent(content);
        newMessage.setThread(thread); // Móc nối quan hệ khóa ngoại (Foreign key) với bài viết
        newMessage.setUser(currentUser); // Móc nối quan hệ với tác giả bình luận
        newMessage.setCreatedAt(LocalDateTime.now()); // Đóng dấu thời gian hiện tại

        // ==========================================
        // XỬ LÝ UPLOAD FILE ĐÍNH KÈM (Nếu người dùng có chọn file)
        // ==========================================
        if (file != null && !file.isEmpty()) {

            // Ràng buộc dung lượng: Tối đa 5MB (5 * 1024 * 1024 bytes = 5,242,880)
            if (file.getSize() > 5242880) {
                return "redirect:/thread/" + id + "?error=FileTooLarge";
            }

            String fileName = file.getOriginalFilename();

            if (fileName != null) {

                // Lấy phần đuôi mở rộng của file (Ví dụ: .jpg, .png)
                String ext = fileName
                        .substring(fileName.lastIndexOf("."))
                        .toLowerCase();

                // Ràng buộc định dạng: Chỉ cho phép các file ảnh và file văn bản an toàn
                if (!ext.matches("\\.(jpg|jpeg|png|gif|pdf|docx|zip)")) {
                    return "redirect:/thread/" + id + "?error=InvalidFileType";
                }
            }

            try {

                // Đường dẫn tới thư mục lưu trữ thực tế trên máy chủ
                String uploadDir =
                        System.getProperty("user.dir") + "/uploads/";

                Path uploadPath = Paths.get(uploadDir);

                // Nếu thư mục uploads chưa tồn tại (lần đầu chạy hệ thống) thì tự động tạo mới
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Chống trùng lặp tên file bằng cách gắn chuỗi mã hóa ngẫu nhiên UUID lên trước tên file
                String uniqueFileName =
                        UUID.randomUUID().toString() + "_" + fileName;

                Path filePath =
                        uploadPath.resolve(uniqueFileName);

                // Copy dữ liệu nhị phân từ file upload (RAM) vào ổ cứng máy chủ
                Files.copy(file.getInputStream(), filePath);

                // Lưu đường dẫn ảo vào Database để HTML có thể gọi ra hiển thị ảnh
                newMessage.setFileUrl(
                        "/uploads/" + uniqueFileName);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Lưu toàn bộ dữ liệu bình luận xuống Database
        messageRepository.save(newMessage);

        // Đăng xong thì làm mới lại trang chi tiết bài viết
        return "redirect:/thread/" + id;
    }
}