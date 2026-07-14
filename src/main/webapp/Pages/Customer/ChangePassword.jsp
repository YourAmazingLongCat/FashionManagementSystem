<%-- 
    Document   : ChangePassword
    Created on : Jul 6, 2026, 3:20:19 PM
    Author     : ADMIN
--%>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Change Password - Fashion X Store</title>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;700;800&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    
<style>
    body {
        font-family: 'Space Grotesk', sans-serif;
        background-color: #f5f5f5; /* Nền xám nhạt */
        margin: 0;
        padding: 0;
    }
    
    .password-container {
        max-width: 500px;
        margin: 80px auto;
        background: #ffffff; /* Nền trắng */
        padding: 40px;
        border: 2px solid #000; /* Viền đen dày */
        box-shadow: 8px 8px 0px rgba(0,0,0,1); /* Đổ bóng cứng */
        border-radius: 0; 
        text-align: center;
    }

    .password-header h2 {
        margin: 0 0 30px 0;
        text-transform: uppercase;
        letter-spacing: 1px;
        font-weight: 800;
        color: #000; /* Chữ đen */
    }

    .info-group {
        margin-bottom: 20px;
        text-align: left;
    }

    .info-group label {
        display: block;
        font-size: 12px;
        color: #555;
        text-transform: uppercase;
        margin-bottom: 8px;
        font-weight: 700;
    }

    .info-group input {
        width: 100%;
        padding: 12px;
        background: #fff;
        border: 2px solid #000; /* Viền đen */
        font-family: 'Space Grotesk', sans-serif;
        font-size: 15px;
        color: #000;
        font-weight: 500;
        box-sizing: border-box;
        outline: none;
    }

    .info-group input:focus {
        border-color: #ff5a00; /* Nhấn cam khi focus */
    }

    .btn-save {
        background-color: #000; /* Nền đen */
        color: #fff;
        padding: 15px 30px;
        border: 2px solid #000;
        text-transform: uppercase;
        font-weight: 800;
        letter-spacing: 1px;
        cursor: pointer;
        width: 100%;
        margin-top: 20px;
        transition: all 0.3s;
    }

    .btn-save:hover {
        background-color: #ff5a00;
        border-color: #ff5a00;
        box-shadow: 4px 4px 0px rgba(0,0,0,1);
        transform: translateY(-2px);
    }

    .error-msg {
        color: #ff3333;
        font-size: 14px;
        font-weight: 700;
        margin-bottom: 15px;
        text-align: left;
    }
    .success-msg {
        color: #008000;
        font-size: 14px;
        font-weight: 700;
        margin-bottom: 15px;
        text-align: left;
    }
</style>
</head>
<body>

    <jsp:include page="/Pages/Guest/Home/Header/Header.jsp" />

    <div class="password-container">
        <div class="password-header">
            <h2>CHANGE PASSWORD</h2>
        </div>

        <c:if test="${not empty requestScope.error}">
            <div class="error-msg">${requestScope.error}</div>
        </c:if>
        <c:if test="${not empty requestScope.success}">
            <div class="success-msg">${requestScope.success}</div>
        </c:if>

        <form action="${pageContext.request.contextPath}/change-password" method="post">
            
            <div class="info-group">
                <label>CURRENT PASSWORD</label>
                <input type="password" name="oldPassword" placeholder="Enter current password" required>
            </div>

            <div class="info-group">
                <label>NEW PASSWORD</label>
                <input type="password" name="newPassword" placeholder="Enter new password" required>
            </div>

            <div class="info-group">
                <label>CONFIRM NEW PASSWORD</label>
                <input type="password" name="confirmPassword" placeholder="Confirm new password" required>
            </div>

            <button type="submit" class="btn-save">UPDATE PASSWORD</button>
        </form>
    </div>

</body>
</html>