<%-- 
    Document   : Profile
    Created on : Jul 2, 2026, 10:38:41 AM
    Author     : ADMIN
--%>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>My Profile - Fashion X Store</title>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;700;800&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    
    <style>
        body {
            font-family: 'Space Grotesk', sans-serif;
            background-color: #f5f5f5;
            margin: 0;
            padding: 0;
        }
        
        .profile-container {
            max-width: 600px;
            margin: 80px auto;
            background: #ffffff;
            padding: 40px;
            border: 2px solid #000;
            box-shadow: 8px 8px 0px rgba(0,0,0,1); 
            border-radius: 0; 
            text-align: center;
        }

        .profile-header h2 {
            margin: 0 0 30px 0;
            text-transform: uppercase;
            letter-spacing: 1px;
            font-weight: 800;
            color: #000;
        }

        /* KHU VỰC AVATAR */
        .avatar-section {
            position: relative;
            width: 150px;
            height: 150px;
            margin: 0 auto 30px auto;
        }

        .avatar-img {
            width: 150px;
            height: 150px;
            border-radius: 50%;
            object-fit: cover;
            border: 3px solid #000;
        }

        .avatar-default {
            font-size: 150px;
            color: #ccc;
        }

        .camera-btn {
            position: absolute;
            bottom: 5px;
            right: 5px;
            background: #000;
            color: #fff;
            width: 40px;
            height: 40px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            border: 2px solid #fff;
            transition: all 0.3s;
        }

        .camera-btn:hover {
            background: #ff3333;
        }

        #avatarInput {
            display: none;
        }

        /* KHU VỰC THÔNG TIN */
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
            letter-spacing: 0.5px;
        }

        .info-group input {
            width: 100%;
            padding: 12px;
            background: #fff;
            border: 2px solid #ccc;
            font-family: 'Space Grotesk', sans-serif;
            font-size: 15px;
            color: #000;
            font-weight: 500;
            box-sizing: border-box;
            outline: none;
            transition: all 0.3s;
        }

        .info-group input:focus {
            border-color: #ff5a00;
        }
        
        .info-group input[readonly] {
            background: #f0f0f0;
            color: #888;
            cursor: not-allowed;
            border-color: #eee;
        }

        .btn-save {
            background-color: #000;
            color: #fff;
            padding: 15px 30px;
            border: 2px solid #000;
            text-transform: uppercase;
            font-weight: 800;
            letter-spacing: 1px;
            cursor: pointer;
            width: 100%;
            margin-top: 10px;
            transition: all 0.3s;
        }

        .btn-save:hover {
            background-color: #ff5a00;
            border-color: #ff5a00;
            box-shadow: 4px 4px 0px rgba(0,0,0,1);
            transform: translateY(-2px);
        }
    </style>
</head>
<body>

    <jsp:include page="/Pages/Guest/Home/Header/Header.jsp" />

    <div class="profile-container">
        <div class="profile-header">
            <h2>MY PROFILE</h2>
        </div>

        <form action="${pageContext.request.contextPath}/profile/update" method="post" enctype="multipart/form-data">
            <div class="avatar-section">
                <c:choose>
                    <c:when test="${not empty sessionScope.USER.avatar}">
                        <img id="avatarPreview" src="${pageContext.request.contextPath}/${sessionScope.USER.avatar}" class="avatar-img" alt="Avatar">
                    </c:when>
                    <c:otherwise>
                        <span id="avatarPreview" class="material-icons avatar-default">account_circle</span>
                    </c:otherwise>
                </c:choose>

                <label for="avatarInput" class="camera-btn" title="Thay đổi ảnh đại diện">
                    <span class="material-icons" style="font-size: 20px;">photo_camera</span>
                </label>
                <input type="file" id="avatarInput" name="avatarFile" accept="image/png, image/jpeg" onchange="previewImage(event)">
            </div>

            <div class="info-group">
                <label>FULL NAME</label>
                <input type="text" name="fullName" value="${sessionScope.USER.fullName}" required>
            </div>

            <div class="info-group">
                <label>EMAIL ADDRESS (READ-ONLY)</label>
                <input type="email" value="${sessionScope.USER.email}" readonly>
            </div>

            <div class="info-group">
                <label>PHONE NUMBER</label>
                <input type="text" name="phone" value="${sessionScope.USER.phone}">
            </div>

            <button type="submit" class="btn-save">SAVE PROFILE</button>
        </form>
    </div>

    <script>
        function previewImage(event) {
            var reader = new FileReader();
            reader.onload = function() {
                var output = document.getElementById('avatarPreview');
                if (output.tagName.toLowerCase() === 'span') {
                    var img = document.createElement('img');
                    img.id = 'avatarPreview';
                    img.className = 'avatar-img';
                    img.src = reader.result;
                    output.parentNode.replaceChild(img, output);
                } else {
                    output.src = reader.result;
                }
            };
            if(event.target.files[0]) {
                reader.readAsDataURL(event.target.files[0]);
            }
        }
    </script>
</body>
</html>