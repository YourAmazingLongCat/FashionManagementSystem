<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>Login - Fashion Store</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        
        <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
        <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
        
        <style>
            <%@ include file="/Pages/Authentication/Login/Login.css" %>
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

        <div class="login-bg-marquee">
            <div class="bg-marquee-track">
                <span>FASHION STORE 2026 • WELCOME BACK • </span>
                <span>FASHION STORE 2026 • WELCOME BACK • </span>
                <span>FASHION STORE 2026 • WELCOME BACK • </span>
                <span>FASHION STORE 2026 • WELCOME BACK • </span>
            </div>
        </div>
        <div class="login-page-wrapper">
            <div class="login-container">
                <h1 class="page-title">ĐĂNG NHẬP</h1>
                
                <form method="post" action="<%= request.getContextPath() %>/auth/login">
                    
                    <div class="form-group">
                        <label for="email">Email<span class="required">*</span></label>
                        <input type="email" id="email" name="email" required>
                    </div>

                    <div class="form-group">
                        <label for="password">Mật khẩu<span class="required">*</span></label>
                        <div class="password-wrapper">
                            <input type="password" id="password" name="password" required>
                            <span class="material-icons toggle-password" onclick="togglePasswordVisibility()">
                                visibility_off
                            </span>
                        </div>
                    </div>

                    <div class="action-row">
                        <button type="submit" class="btn-submit">ĐĂNG NHẬP</button>
                        
                        <div class="form-links">
                            <a href="<%= request.getContextPath() %>/auth/forgot-password" class="forgot-link">Quên mật khẩu?</a>
                            <span class="separator">hoặc</span>
                            <a href="<%= request.getContextPath() %>/auth/register" class="register-link">Đăng ký</a>
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
            function togglePasswordVisibility() {
                const passwordInput = document.getElementById("password");
                const toggleIcon = document.querySelector(".toggle-password");
                
                if (passwordInput.type === "password") {
                    passwordInput.type = "text";
                    toggleIcon.textContent = "visibility";
                    toggleIcon.style.color = "#000";
                } else {
                    passwordInput.type = "password";
                    toggleIcon.textContent = "visibility_off";
                    toggleIcon.style.color = "#555";
                }
            }

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