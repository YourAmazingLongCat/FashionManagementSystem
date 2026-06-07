<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>Reset Password - Smartphone Sales</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
        <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/Pages/Authentication/Login/Login.css">
    </head>
    <body>
        <c:if test="${not empty successMessage or not empty errorMessage}">
            <div id="toast"
                 class="toast-notification
                 ${not empty successMessage ? 'toast-success' : 'toast-error'}">
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
                    <span class="material-icons">password</span>
                </div>
                <h1>Reset Password</h1>
                <p>Enter the 6-digit OTP and your new password</p>
            </div>

            <div class="login-card">
                <form method="post" action="${pageContext.request.contextPath}/auth/reset-password">

                    <div class="form-group">
                        <label>OTP Code</label>
                        <div class="input-wrapper">
                            <span class="material-icons">pin</span>
                            <input type="text" name="otp" placeholder="123456" pattern="\d{6}" title="Please enter 6 digits" required>
                        </div>
                    </div>

                    <div class="form-group">
                        <label>New Password</label>
                        <div class="input-wrapper">
                            <span class="material-icons">lock</span>
                            <input type="password" name="newPassword" placeholder="••••••••" required
                                   minlength="8"
                                   maxlength="20"
                                   pattern="(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9]).{8,20}">
                        </div>
                    </div>

                    <div class="form-group">
                        <label>Confirm Password</label>
                        <div class="input-wrapper">
                            <span class="material-icons">lock_outline</span>
                            <input type="password" name="confirmPassword" placeholder="••••••••" required
                                   minlength="8"
                                   maxlength="20"
                                   pattern="(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9]).{8,20}">
                        </div>
                    </div>

                    <button type="submit" class="btn-submit">
                        Change Password
                    </button>
                </form>

                <div class="footer">
                    <p>Remembered your password? 
                        <a href="${pageContext.request.contextPath}/auth/login">Back to Login</a>
                    </p>
                </div>
            </div>
        </div>
    </body>
</html>