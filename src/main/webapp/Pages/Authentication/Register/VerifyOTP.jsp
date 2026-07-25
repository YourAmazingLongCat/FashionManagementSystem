<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>Verify OTP - Fashion Store</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        
        <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
        <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
        
        <style>
            <%@ include file="/Pages/Authentication/Register/Register.css" %>
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

        <div class="register-page-wrapper">
            <div class="register-container" style="max-width: 450px; text-align: center;">
                <h1 class="page-title" style="font-size: 1.5rem;">
                    ${param.mode eq 'forgot' ? 'VERIFY OTP - PASSWORD RESET' : 'EMAIL VERIFICATION'}
                </h1>
                <p style="font-size: 0.95rem; margin-bottom: 25px; color: #333;">
                    ${param.mode eq 'forgot'
                        ? 'A 6-digit OTP has been sent to your email. Enter the code to reset your password.'
                        : 'A 6-digit verification code has been sent to your email. Please enter the code to complete your registration.'}
                </p>
                
                <form method="post" action="<%= request.getContextPath() %>/auth/verify-otp">
                    <input type="hidden" name="mode" value="${param.mode}">
                    
                    <div class="form-group">
                        <input type="text" id="otpCode" name="otpCode" placeholder="Enter OTP code..."
                               style="text-align: center; font-size: 1.5rem; letter-spacing: 5px; font-weight: 700; padding: 15px;"
                               maxlength="6" required>
                    </div>

                    <div class="action-row">
                        <button type="submit" class="btn-submit" style="width: 100%;">VERIFY CODE</button>
                    </div>
                </form>

                <c:if test="${param.mode eq 'forgot'}">
                    <div style="margin-top: 20px;">
                        <a href="${pageContext.request.contextPath}/auth/forgot-password" style="color: #666; text-decoration: none; font-size: 0.9rem;">
                            &larr; Back to Forgot Password
                        </a>
                    </div>
                </c:if>
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
