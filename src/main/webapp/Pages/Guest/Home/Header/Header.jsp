<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<header class="header-guest-page">

    <div class="logo-area">
        <a class="logo-link" href="${pageContext.request.contextPath}/home" style="text-decoration: none;">
            <div class="peak-logo">
                <span class="logo-text">FASHION</span>
                <span class="logo-badge">X</span>
                <span class="logo-text">STORE</span>
            </div>
        </a>
    </div>

    <nav class="quick-nav">
        <c:if test="${not empty sessionScope.USER}">
            <a class="quick-nav-link" href="${pageContext.request.contextPath}/customer/order-history">MY ORDERS</a>
        </c:if>

        <c:if test="${not empty sessionScope.USER and sessionScope.USER.role eq 'Admin'}">
            <a class="quick-nav-link quick-nav-link--accent" href="${pageContext.request.contextPath}/admin/products">PRODUCT MANAGEMENT</a>
        </c:if>
    </nav>

    <div class="search-container">
        <form id="headerSearchForm" class="search-form" method="get" action="${pageContext.request.contextPath}/home/search">
            <input id="headerSearchInput" class="search-input" type="text" name="search" placeholder="SEARCH PRODUCTS..." value="${param.search}" autocomplete="off"/>
            <button class="search-submit" type="submit">
                <span class="material-symbols-outlined">search</span>
            </button>
        </form>
    </div>

    <div class="header-actions">
        <a href="${pageContext.request.contextPath}/cart" class="cart-icon-btn" title="View Cart">
            <span class="material-symbols-outlined">shopping_bag</span>
            <span class="cart-badge" id="cartBadge">${empty sessionScope.cartCount ? 0 : sessionScope.cartCount}</span>
        </a>
        <c:if test="${not empty sessionScope.USER}">
            <a href="${pageContext.request.contextPath}/wishlist" class="wishlist-icon-btn" title="My Wishlist">
                <span class="material-symbols-outlined">favorite</span>
            </a>
        </c:if>
        <c:choose>
            <c:when test="${not empty sessionScope.USER}">
                <div class="user-menu">
                    <button class="user-menu-button" type="button">
                        <span class="user-greeting">HI,</span>
                        <span class="user-name">${sessionScope.USER.fullName}</span>
                        <span class="material-symbols-outlined">expand_more</span>
                    </button>
                    <div class="user-dropdown">
                        <a href="${pageContext.request.contextPath}/profile">My Profile</a>
                        <a href="${pageContext.request.contextPath}/change-password">Change Password</a>
                        <a href="${pageContext.request.contextPath}/auth/logout">Logout</a>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <a class="login-btn" href="javascript:void(0);" data-bs-toggle="modal" data-bs-target="#loginModal">LOGIN</a>
            </c:otherwise>
        </c:choose>
    </div>
</header>

<!-- ================= KHUNG MODAL POPUP ĐĂNG NHẬP ================= -->
<div class="modal fade" id="loginModal" tabindex="-1" aria-labelledby="loginModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content" style="border-radius: 0; border: 2px solid #000; box-shadow: 4px 4px 0px rgba(0,0,0,1); background-color: #fff;">
            <div class="modal-header" style="border-bottom: 2px solid #000; padding: 15px 20px;">
                <h5 class="modal-title" id="loginModalLabel" style="font-family: 'Space Grotesk', sans-serif; font-weight: 800; font-size: 1.2rem;">
                    WELCOME BACK
                </h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body" style="padding: 25px;">
                <!-- Vùng thông báo lỗi khi đăng nhập sai -->
                <div id="loginErrorMsg" class="alert alert-danger" style="display: none; border-radius: 0; font-size: 0.9rem; margin-bottom: 20px;"></div>

                <form id="popupLoginForm">
                    <div class="form-group mb-3" style="margin-bottom: 15px;">
                        <label style="font-weight: 500; font-size: 0.9rem; display: block; margin-bottom: 5px;">Email <span style="color:red;">*</span></label>
                        <input type="email" name="email" class="form-control" style="border-radius: 0; border: 1px solid #ccc; padding: 10px 12px; width: 100%;" required>
                    </div>
                    <div class="form-group mb-4" style="margin-bottom: 20px;">
                        <label style="font-weight: 500; font-size: 0.9rem; display: block; margin-bottom: 5px;">Password <span style="color:red;">*</span></label>
                        <input type="password" name="password" class="form-control" style="border-radius: 0; border: 1px solid #ccc; padding: 10px 12px; width: 100%;" required>
                    </div>
                    <button type="submit" class="btn btn-dark w-100" style="border-radius: 0; font-weight: 700; padding: 12px; background-color: #000; color: #fff; width: 100%; border: none; cursor: pointer;">
                        LOGIN
                    </button>

                    <div class="mt-3 text-center" style="font-size: 0.85rem; display: flex; flex-direction: column; gap: 5px; margin-top: 15px;">
                        <a href="${pageContext.request.contextPath}/auth/forgot-password" style="color: #555; text-decoration: none;">Forgot password?</a>
                        <a href="${pageContext.request.contextPath}/auth/register" style="color: #555; text-decoration: none;">Register new account</a>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
<!-- ================= END MODAL ================= -->

<script>
    const headerSearchForm = document.getElementById('headerSearchForm');
    const headerSearchInput = document.getElementById('headerSearchInput');
    const userMenuButton = document.querySelector('.user-menu-button');
    const userDropdown = document.querySelector('.user-dropdown');

    if (headerSearchForm && headerSearchInput) {
        headerSearchInput.addEventListener('keydown', function (event) {
            if (event.key === 'Enter') {
                event.preventDefault();
                headerSearchForm.submit();
            }
        });
    }

    if (userMenuButton && userDropdown) {
        userMenuButton.addEventListener('click', function (event) {
            event.stopPropagation();
            userDropdown.classList.toggle('visible');
        });

        document.addEventListener('click', function () {
            userDropdown.classList.remove('visible');
        });

        userDropdown.addEventListener('click', function (event) {
            event.stopPropagation();
        });
    }

    // --- XỬ LÝ GỬI FORM ĐĂNG NHẬP NGẦM BẰNG AJAX ---
    document.addEventListener('DOMContentLoaded', function () {
        const loginForm = document.getElementById('popupLoginForm');
        if (loginForm) {
            loginForm.addEventListener('submit', function (e) {
                e.preventDefault(); // Chặn tải lại trang

                const formData = new URLSearchParams(new FormData(this));

                fetch('${pageContext.request.contextPath}/auth/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                        'X-Requested-With': 'XMLHttpRequest' // Báo cho Controller biết đây là request AJAX
                    },
                    body: formData
                })
                        .then(response => response.json())
                        .then(data => {
                            if (data.success) {
                                window.location.href = data.redirectUrl; // Đăng nhập đúng -> chuyển hướng theo phân quyền
                            } else {
                                // Đăng nhập sai -> hiện thông báo lỗi màu đỏ ngay trong popup
                                const errorDiv = document.getElementById('loginErrorMsg');
                                errorDiv.textContent = data.message;
                                errorDiv.style.display = 'block';
                            }
                        })
                        .catch(error => console.error('Error:', error));
            });
        }
    });
</script>