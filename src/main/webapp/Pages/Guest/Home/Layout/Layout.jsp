<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Fashion Store Management</title>
        
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet"/>
        <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@300;400;500;600;700;800&display=swap" rel="stylesheet"/>
        <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
        <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet"/>
        
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
        
        <style>
            <%@ include file="/Pages/Guest/Home/Layout/Layout.css" %>
            <%@ include file="/Pages/Guest/Home/Header/Header.css" %>
            <%@ include file="/Pages/Guest/Home/Content/Content.css" %>
            <%@ include file="/Pages/Guest/Home/SearchAndFilter/SearchAndFilter.css" %>
            <%@ include file="/Pages/Guest/Home/Order/Order.css" %>
        </style>
    </head>
    <body>
        <c:set var="displaySuccessMessage" value="${not empty successMessage ? successMessage : sessionScope.successMessage}" />
        <c:set var="displayErrorMessage" value="${not empty errorMessage ? errorMessage : sessionScope.errorMessage}" />

        <c:if test="${not empty displaySuccessMessage or not empty displayErrorMessage}">
            <div id="toast" class="toast-notification ${not empty displaySuccessMessage ? 'toast-success' : 'toast-error'}">
                <div class="toast-icon">
                    <span class="material-symbols-outlined">
                        ${not empty displaySuccessMessage ? 'check_circle' : 'error'}
                    </span>
                </div>
                <div class="toast-content">
                    <h4 class="toast-title">
                        ${not empty displaySuccessMessage ? 'SUCCESS' : 'ERROR'}
                    </h4>
                    <p class="toast-message">
                        ${not empty displaySuccessMessage ? displaySuccessMessage : displayErrorMessage}
                    </p>
                </div>
                <button class="toast-close" onclick="closeToast()">
                    <span class="material-symbols-outlined" style="font-size:20px;">close</span>
                </button>
            </div>
            <c:remove var="successMessage" scope="session" />
            <c:remove var="errorMessage" scope="session" />
        </c:if>

        <header>
            <c:choose>
                <c:when test="${not empty sessionScope.USER}">                  
                    <jsp:include page="/Pages/Guest/Home/Header/Header.jsp" />
                </c:when>
                <c:otherwise>                  
                    <jsp:include page="/Pages/Guest/Home/Header/Header.jsp" />
                </c:otherwise>
            </c:choose>
        </header>

        <div class="main-container">
            <main>
                <c:catch var="contentError">
                    <jsp:include page="${contentPage}" />
                </c:catch>
                <c:if test="${not empty contentError}">
                    <h3 style="color:red; text-align:center; padding: 50px;">
                        Đường dẫn trang nội dung (${contentPage}) không tồn tại! <br> Lỗi: ${contentError.message}
                    </h3>
                </c:if>
            </main>
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

        <footer>
            <div class="footer-container">
                <p class="copyright">
                    © 2026 FASHION MANAGEMENT SYSTEM.<br>ALL RIGHTS RESERVED.
                </p>
            </div>
        </footer>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
        
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

            window.addEventListener('pageshow', function (event) {
                const toast = document.getElementById("toast");                
                if (event.persisted || (window.performance && window.performance.navigation.type === 2)) {
                    if (toast) {
                        toast.style.display = 'none';
                        toast.remove();
                    }                  
                    window.location.reload();
                }                
                else if (toast) {
                    setTimeout(() => {
                        closeToast();
                    }, 5000);
                }
            });
        </script>
    </body>
</html>