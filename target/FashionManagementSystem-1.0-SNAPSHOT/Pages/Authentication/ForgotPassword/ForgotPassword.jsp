<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>Forgot Password - Smartphone Sales</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
        <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/Pages/Authentication/Login/Login.css">
    </head>
    <body>
        <c:if test="${not empty errorMessage}">
            <div id="toast" class="toast-notification toast-error">
                <div class="toast-icon"><span class="material-icons">error</span></div>
                <div class="toast-content">
                    <h4 class="toast-title">Error</h4>
                    <p class="toast-message">${errorMessage}</p>
                </div>
                <button class="toast-close" onclick="closeToast()">
                    <span class="material-icons" style="font-size:18px;">close</span>
                </button>
            </div>
        </c:if>
        <script>
            function closeToast() {
                const toast = document.getElementById("toast");
                if (toast) {
                    toast.classList.add("toast-hide");
                    setTimeout(() => {
                        toast.remove();
                    }, 400);
                }
            }
            document.addEventListener("DOMContentLoaded", function () {
                const toast = document.getElementById("toast");
                if (toast) {
                    setTimeout(() => {
                        closeToast();
                    }, 5000);
                }
            });
        </script>

        <div class="background-effect top"></div>
        <div class="background-effect bottom"></div>

        <div class="login-container">
            <div class="header">
                <div class="logo">
                    <span class="material-icons">lock_reset</span>
                </div>
                <h1>Forgot Password</h1>
                <p>Enter your email to receive an OTP</p>
            </div>

            <div class="login-card">
                <form method="post" action="${pageContext.request.contextPath}/auth/forgot-password">
                    <div class="form-group">
                        <label>Email Address</label>
                        <div class="input-wrapper">
                            <span class="material-icons">mail</span>
                            <input type="email" name="email" placeholder="abc@example.com" required>
                        </div>
                    </div>

                    <button type="submit" class="btn-submit">
                        Send OTP
                    </button>
                </form>

                <div class="footer">
                    <p>Remember your password? 
                        <a href="${pageContext.request.contextPath}/auth/login">Back to Login</a>
                    </p>
                </div>
            </div>
        </div>
    </body>
</html>