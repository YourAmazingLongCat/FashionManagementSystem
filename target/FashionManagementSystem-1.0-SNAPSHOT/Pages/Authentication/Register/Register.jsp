<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>Register - Fashion Store</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        
        <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
        <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
        
        <style>
            <%@ include file="/Pages/Authentication/Register/Register.css" %>
        </style>
    </head>
    <body>
        
        <c:if test="${not empty successMessage or not empty errorMessage}">
            <div id="toast" class="toast-notification ${not empty successMessage ? 'toast-success' : 'toast-error'}">
                <div class="toast-icon">
                    <span class="material-icons">
                        ${not empty successMessage ? 'check_circle' : 'error'}
                    </span>
                </div>
                <div class="toast-content">
                    <h4 class="toast-title">
                        ${not empty successMessage ? 'Success' : 'Error'}
                    </h4>
                    <p class="toast-message">
                        ${not empty successMessage ? successMessage : errorMessage}
                    </p>
                </div>
                <button class="toast-close" onclick="closeToast()">
                    <span class="material-icons" style="font-size:18px;">close</span>
                </button>
            </div>
        </c:if>

        <div class="register-bg-marquee">
            <div class="bg-marquee-track">
                <span>FASHION STORE 2026 • JOIN US • </span>
                <span>FASHION STORE 2026 • JOIN US • </span>
                <span>FASHION STORE 2026 • JOIN US • </span>
                <span>FASHION STORE 2026 • JOIN US • </span>
            </div>
        </div>

        <div class="register-page-wrapper">
            <div class="register-container">
                <h1 class="page-title">ĐĂNG KÝ TÀI KHOẢN</h1>
                
                <form method="post" action="<%= request.getContextPath() %>/auth/register">
                    
                    <div class="form-group">
                        <label for="name">Họ và Tên<span class="required">*</span></label>
                        <input type="text" id="name" name="name" required>
                    </div>

                    <div class="form-group">
                        <label for="email">Email<span class="required">*</span></label>
                        <input type="email" id="email" name="email" required>
                    </div>

                    <div class="form-group">
                        <label for="phoneNumber">Số điện thoại<span class="required">*</span></label>
                        <input type="text" id="phoneNumber" name="phoneNumber" required>
                    </div>

                    <div class="form-group">
                        <label for="password">Mật khẩu<span class="required">*</span></label>
                        <input type="password" id="password" name="password" required 
                               pattern="^[A-Z](?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{};':&quot;\\|,.<>\/?]).+$"
                               title="Mật khẩu phải bắt đầu bằng chữ cái viết hoa, chứa ít nhất 1 chữ số và 1 ký tự đặc biệt.">
                    </div>

                    <div class="form-group">
                        <label for="confirmPassword">Xác nhận mật khẩu<span class="required">*</span></label>
                        <input type="password" id="confirmPassword" name="confirmPassword" required>
                    </div>
                    <div class="action-row">
                        <button type="submit" class="btn-submit">ĐĂNG KÝ</button>
                        
                        <div class="form-links">
                            <span class="separator">Đã có tài khoản?</span>
                            <a href="<%= request.getContextPath() %>/auth/login" class="login-link">Đăng nhập ngay</a>
                        </div>
                    </div>
                    
                </form>
            </div>
        </div>

        <div class="scrolling-ticker">
            <div class="ticker-content">
                <span>FREESHIP CHO MỌI ĐƠN HÀNG FASHION STORE™ 2026</span>
                <span>GET TO KNOW ABOUT OUR VIBE FASHION STORE™ 2026</span>
                <span>FREESHIP CHO MỌI ĐƠN HÀNG FASHION STORE™ 2026</span>
                <span>GET TO KNOW ABOUT OUR VIBE FASHION STORE™ 2026</span>
                
                <span>FREESHIP CHO MỌI ĐƠN HÀNG FASHION STORE™ 2026</span>
                <span>GET TO KNOW ABOUT OUR VIBE FASHION STORE™ 2026</span>
                <span>FREESHIP CHO MỌI ĐƠN HÀNG FASHION STORE™ 2026</span>
                <span>GET TO KNOW ABOUT OUR VIBE FASHION STORE™ 2026</span>
            </div>
        </div>

        <script>
            function closeToast() {
                const toast = document.getElementById("toast");
                if (toast) {
                    toast.classList.add("toast-hide");
                    setTimeout(() => toast.remove(), 400);
                }
            }
            window.addEventListener('pageshow', function (event) {
                const toast = document.getElementById("toast");
                if (event.persisted || (window.performance && window.performance.navigation.type === 2)) {
                    if (toast) { toast.style.display = 'none'; toast.remove(); }
                    window.location.reload();
                } else if (toast) {
                    setTimeout(() => closeToast(), 5000);
                }
            });
        </script>
    </body>
</html>