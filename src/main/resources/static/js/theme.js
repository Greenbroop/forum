document.addEventListener("DOMContentLoaded", () => {
    const themeToggleBtn = document.getElementById("theme-toggle");
    const currentTheme = localStorage.getItem("theme");

    // 1. Kiểm tra trạng thái đã lưu trước đó của người dùng
    if (currentTheme === "dark") {
        document.body.classList.add("dark-mode");
    }

    // 2. Bắt sự kiện khi click chuột vào nút đổi giao diện
    themeToggleBtn.addEventListener("click", () => {
        // Đảo trạng thái class .dark-mode tại thẻ <body>
        document.body.classList.toggle("dark-mode");
        
        // Lưu lại tùy chọn hiện tại vào bộ nhớ trình duyệt (localStorage)
        let theme = "light";
        if (document.body.classList.contains("dark-mode")) {
            theme = "dark";
        }
        localStorage.setItem("theme", theme);
    });
});