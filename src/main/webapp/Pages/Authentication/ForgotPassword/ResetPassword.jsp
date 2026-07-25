<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>Reset Password - Fashion Store</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        
        <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
        <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
        
        <style>
            <%@ include file="/Pages/Authentication/Login/Login.css" %>
            
            .input-wrapper {
                position: relative;
            }
            .input-wrapper input {
                padding-right: 45px;
            }
            .password-toggle {
                position: absolute;
                right: 12px;
                top: 50%;
                transform: translateY(-50%);
                cursor: pointer;
                color: #666;
                transition: color 0.2s;
                user-select: none;
            }
            .password-toggle:hover {
                color: #333;
            }
            .password-requirements {
                font-size: 12px;
                color: #888;
                margin-top: 5px;
            }
        </style>
    </head>
    <body>
        
        <c:if test="${not empty errorMessage}">
            <div id="toast" class="toast-notification toast-error">
                <div class="toast-icon">
                    <span class="material-icons">error</span>
                </div>
                <div class="toast-content">
                    <h4 class="toast-title">ERROR</h4>
                    <p class="toast-message">${errorMessage}</p>
                </div>
                <button class="toast-close" onclick="closeToast()">
                    <span class="material-icons" style="font-size:18px;">close</span>
                </button>
            </div>
        </c:if>

        <div class="login-bg-marquee">
            <div class="bg-marquee-track">
                <span>FASHION STORE 2026 • NEW PASSWORD • </span>
                <span>FASHION STORE 2026 • NEW PASSWORD • </span>
                <span>FASHION STORE 2026 • NEW PASSWORD • </span>
                <span>FASHION STORE 2026 • NEW PASSWORD • </span>
            </div>
        </div>

        <div class="login-page-wrapper">
            <div class="login-container">
                <h1 class="page-title">RESET PASSWORD</h1>
                
                <form method="post" action="<%= request.getContextPath() %>/auth/reset-password">
                    
                    <div class="form-group">
                        <label for="email">Email</label>
                        <input type="email" id="email" value="${email}" readonly style="background-color: #f0f0f0; cursor: not-allowed;">
                    </div>

                    <div class="form-group">
                        <label for="newPassword">New Password<span class="required">*</span></label>
                        <div class="input-wrapper">
                            <input type="password" id="newPassword" name="newPassword" placeholder="At least 8 characters" required minlength="8">
                            <span class="material-icons password-toggle" onclick="togglePassword('newPassword')">visibility</span>
                        </div>
                        <p class="password-requirements">Must be at least 8 characters</p>
                    </div>

                    <div class="form-group">
                        <label for="confirmPassword">Confirm Password<span class="required">*</span></label>
                        <div class="input-wrapper">
                            <input type="password" id="confirmPassword" name="confirmPassword" placeholder="Re-enter your password" required>
                            <span class="material-icons password-toggle" onclick="togglePassword('confirmPassword')">visibility</span>
                        </div>
                    </div>

                    <div class="action-row">
                        <button type="submit" class="btn-submit">UPDATE PASSWORD</button>
                        
                        <div class="form-links">
                            <a href="<%= request.getContextPath() %>/auth/login" class="register-link">Back to Login</a>
                        </div>
                    </div>
                    
                </form>
            </div>
        </div>

        <div class="scrolling-ticker">
            <div class="ticker-content">
                <span>FREESHIP ON ALL ORDERS FASHION STORE 2026</span>
                <span>DISCOVER OUR VIBE FASHION STORE 2026</span>
                <span>FREESHIP ON ALL ORDERS FASHION STORE 2026</span>
                <span>DISCOVER OUR VIBE FASHION STORE 2026</span>
                
                <span>FREESHIP ON ALL ORDERS FASHION STORE 2026</span>
                <span>DISCOVER OUR VIBE FASHION STORE 2026</span>
                <span>FREESHIP ON ALL ORDERS FASHION STORE 2026</span>
                <span>DISCOVER OUR VIBE FASHION STORE 2026</span>
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
            
            function togglePassword(inputId) {
                const input = document.getElementById(inputId);
                const icon = input.nextElementSibling;
                if (input.type === "password") {
                    input.type = "text";
                    icon.textContent = "visibility_off";
                } else {
                    input.type = "password";
                    icon.textContent = "visibility";
                }
            }
        </script>
    </body>
</html>
