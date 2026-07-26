<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>Forgot Password - Fashion Store</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        
        <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
        <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
        
        <style>
            <%@ include file="/Pages/Authentication/Login/Login.css" %>
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

        <c:if test="${not empty successMessage}">
            <div id="toast" class="toast-notification toast-success">
                <div class="toast-icon">
                    <span class="material-icons">check_circle</span>
                </div>
                <div class="toast-content">
                    <h4 class="toast-title">SUCCESS</h4>
                    <p class="toast-message">${successMessage}</p>
                </div>
                <button class="toast-close" onclick="closeToast()">
                    <span class="material-icons" style="font-size:18px;">close</span>
                </button>
            </div>
        </c:if>

        <div class="login-bg-marquee">
            <div class="bg-marquee-track">
                <span>FASHION STORE 2026 • RESET PASSWORD • </span>
                <span>FASHION STORE 2026 • RESET PASSWORD • </span>
                <span>FASHION STORE 2026 • RESET PASSWORD • </span>
                <span>FASHION STORE 2026 • RESET PASSWORD • </span>
            </div>
        </div>

        <div class="login-page-wrapper">
            <div class="login-container">
                <h1 class="page-title">FORGOT PASSWORD</h1>
                
                <form method="post" action="<%= request.getContextPath() %>/auth/forgot-password">
                    
                    <div class="form-group">
                        <label for="email">Email<span class="required">*</span></label>
                        <input type="email" id="email" name="email" placeholder="Enter your email address" required>
                    </div>

                    <div class="action-row">
                        <button type="submit" class="btn-submit">SEND OTP</button>
                        
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
        </script>
    </body>
</html>
