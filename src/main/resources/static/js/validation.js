document.addEventListener("DOMContentLoaded", () => {
    const registerForm = document.getElementById("register-form");

    if (registerForm) {
        registerForm.addEventListener("submit", (event) => {
            const username = document.getElementById("username").value.trim();
            const email = document.getElementById("email").value.trim();
            const password = document.getElementById("password").value;

            // 1. Kiểm tra username không được để trống và phải từ 3 ký tự trở lên
            if (username.length < 3) {
                alert("Tên đăng nhập phải có ít nhất 3 ký tự!");
                event.preventDefault(); // Ngăn form gửi đi
                return;
            }

            
            // 2. Kiểm tra định dạng Email bằng Regular Expression (Regex)
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailRegex.test(email)) {
                alert("Định dạng Email không hợp lệ!");
                event.preventDefault();
                return;
            }

            // 3. Kiểm tra mật khẩu phải từ 6 ký tự trở lên
            if (password.length < 6) {
                alert("Mật khẩu phải có ít nhất 6 ký tự!");
                event.preventDefault();
                return;
            }
        });
    }
});