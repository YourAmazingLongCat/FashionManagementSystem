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

<!-- ================= 1. MODAL ĐĂNG NHẬP ================= -->
<div class="modal fade" id="loginModal" tabindex="-1" aria-labelledby="loginModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content" style="border-radius: 0; border: 2px solid #000; box-shadow: 4px 4px 0px rgba(0,0,0,1); background-color: #fff;">
            <div class="modal-header" style="border-bottom: 2px solid #000; padding: 15px 20px;">
                <h5 class="modal-title" id="loginModalLabel" style="font-family: 'Space Grotesk', sans-serif; font-weight: 800; font-size: 1.2rem;">WELCOME BACK</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body" style="padding: 25px;">
                <div id="loginErrorMsg" class="alert alert-danger" style="display: none; border-radius: 0; font-size: 0.9rem; margin-bottom: 20px;"></div>

                <form id="popupLoginForm" onsubmit="event.preventDefault();">
                    <div class="form-group mb-3">
                        <label style="font-weight: 500; font-size: 0.9rem; display: block; margin-bottom: 5px;">Email <span style="color:red;">*</span></label>
                        <input type="email" name="email" class="form-control" style="border-radius: 0; border: 1px solid #ccc; padding: 10px 12px; width: 100%;" required>
                    </div>
                    <div class="form-group mb-4">
                        <label style="font-weight: 500; font-size: 0.9rem; display: block; margin-bottom: 5px;">Password <span style="color:red;">*</span></label>
                        <input type="password" name="password" class="form-control" style="border-radius: 0; border: 1px solid #ccc; padding: 10px 12px; width: 100%;" required>
                    </div>
                    <button type="submit" class="btn btn-dark w-100" style="border-radius: 0; font-weight: 700; padding: 12px; background-color: #000; color: #fff; width: 100%; border: none;">LOGIN</button>

                    <div class="mt-3 text-center" style="font-size: 0.85rem; display: flex; flex-direction: column; gap: 5px; margin-top: 15px;">
                        <a href="javascript:void(0);" data-bs-dismiss="modal" data-bs-toggle="modal" data-bs-target="#forgotPasswordModal" style="color: #555; text-decoration: none;">Forgot password?</a>
                        <!-- Đã đổi link Register để mở Popup -->
                        <a href="javascript:void(0);" data-bs-dismiss="modal" data-bs-toggle="modal" data-bs-target="#registerModal" style="color: #555; text-decoration: none;">Register new account</a>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<!-- ================= 2. MODAL QUÊN MẬT KHẨU ================= -->
<div class="modal fade" id="forgotPasswordModal" tabindex="-1" aria-labelledby="forgotPasswordModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content" style="border-radius: 0; border: 2px solid #000; box-shadow: 4px 4px 0px rgba(0,0,0,1); background-color: #fff;">
            <div class="modal-header" style="border-bottom: 2px solid #000; padding: 15px 20px;">
                <h5 class="modal-title" id="forgotPasswordModalLabel" style="font-family: 'Space Grotesk', sans-serif; font-weight: 800; font-size: 1.2rem;">FORGOT PASSWORD</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body" style="padding: 25px;">
                <div id="forgotPwdMsg" class="alert alert-danger" style="display: none; border-radius: 0; font-size: 0.9rem; margin-bottom: 20px;"></div>
                
                <form id="popupForgotPwdForm" onsubmit="event.preventDefault();">
                    <input type="hidden" name="mode" value="forgot">
                    <div class="form-group mb-4" id="emailGroup">
                        <label style="font-weight: 500; font-size: 0.9rem; display: block; margin-bottom: 5px;">Email <span style="color:red;">*</span></label>
                        <input type="email" id="forgotEmailInput" name="email" class="form-control" style="border-radius: 0; border: 1px solid #ccc; padding: 10px 12px; width: 100%; background-color: #f9f9f9;" placeholder="Enter your email address">
                    </div>

                    <div class="form-group mb-4" id="otpGroup" style="display: none;">
                        <label style="font-weight: 500; font-size: 0.9rem; display: block; margin-bottom: 5px;">6-Digit OTP Code <span style="color:red;">*</span></label>
                        <input type="text" id="forgotOtpInput" name="otp" class="form-control" style="border-radius: 0; border: 1px solid #000; padding: 10px 12px; width: 100%; letter-spacing: 5px; font-size: 1.2rem; font-weight: bold; text-align: center;" placeholder="------" maxlength="6">
                        <small style="color: #15803d; font-weight: 600; display: block; margin-top: 8px;">✔️ OTP has been sent to your email!</small>
                    </div>

                    <div id="newPasswordGroup" style="display: none;">
                        <div class="form-group mb-3">
                            <label style="font-weight: 500; font-size: 0.9rem; display: block; margin-bottom: 5px;">New Password <span style="color:red;">*</span></label>
                            <input type="password" id="resetNewPwd" name="newPassword" class="form-control" style="border-radius: 0; border: 1px solid #000; padding: 10px 12px; width: 100%;">
                        </div>
                        <div class="form-group mb-4">
                            <label style="font-weight: 500; font-size: 0.9rem; display: block; margin-bottom: 5px;">Confirm New Password <span style="color:red;">*</span></label>
                            <input type="password" id="resetConfirmPwd" name="confirmPassword" class="form-control" style="border-radius: 0; border: 1px solid #000; padding: 10px 12px; width: 100%;">
                        </div>
                    </div>
                    
                    <div style="display: flex; gap: 15px; align-items: center;">
                        <button type="button" id="btnActionOtp" class="btn btn-dark" style="border-radius: 0; font-weight: 700; padding: 12px 24px; background-color: #000; color: #fff; border: none; cursor: pointer;">SEND OTP</button>
                        <a href="javascript:void(0);" id="btnBackToLogin" data-bs-dismiss="modal" data-bs-toggle="modal" data-bs-target="#loginModal" style="color: #555; text-decoration: none; font-size: 0.9rem; font-weight: 500;">Back to Login</a>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<!-- ================= 3. MODAL ĐĂNG KÝ (2 BƯỚC) ================= -->
<div class="modal fade" id="registerModal" tabindex="-1" aria-labelledby="registerModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content" style="border-radius: 0; border: 2px solid #000; box-shadow: 4px 4px 0px rgba(0,0,0,1); background-color: #fff;">
            <div class="modal-header" style="border-bottom: 2px solid #000; padding: 15px 20px;">
                <h5 class="modal-title" id="registerModalLabel" style="font-family: 'Space Grotesk', sans-serif; font-weight: 800; font-size: 1.2rem;">CREATE ACCOUNT</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body" style="padding: 25px;">
                <div id="registerMsg" class="alert alert-danger" style="display: none; border-radius: 0; font-size: 0.9rem; margin-bottom: 20px;"></div>
                
                <form id="popupRegisterForm" onsubmit="event.preventDefault();">
                    <input type="hidden" name="mode" value="register">

                    <!-- Bước 1: Thông tin đăng ký -->
                    <div id="registerInfoGroup">
                        <div class="form-group mb-3">
                            <label style="font-weight: 500; font-size: 0.9rem; display: block; margin-bottom: 5px;">Full Name <span style="color:red;">*</span></label>
                            <input type="text" id="regFullName" name="fullName" class="form-control" style="border-radius: 0; border: 1px solid #ccc; padding: 10px 12px; width: 100%;" required>
                        </div>
                        <div class="form-group mb-3">
                            <label style="font-weight: 500; font-size: 0.9rem; display: block; margin-bottom: 5px;">Email <span style="color:red;">*</span></label>
                            <input type="email" id="regEmail" name="email" class="form-control" style="border-radius: 0; border: 1px solid #ccc; padding: 10px 12px; width: 100%;" required>
                        </div>
                        <div class="form-group mb-3">
                            <label style="font-weight: 500; font-size: 0.9rem; display: block; margin-bottom: 5px;">Phone Number <span style="color:red;">*</span></label>
                            <input type="text" id="regPhone" name="phone" class="form-control" style="border-radius: 0; border: 1px solid #ccc; padding: 10px 12px; width: 100%;" pattern="0[0-9]{9}" placeholder="0912345678" required>
                        </div>
                        <div class="form-group mb-3">
                            <label style="font-weight: 500; font-size: 0.9rem; display: block; margin-bottom: 5px;">Password <span style="color:red;">*</span></label>
                            <input type="password" id="regPassword" name="password" class="form-control" style="border-radius: 0; border: 1px solid #ccc; padding: 10px 12px; width: 100%;" required>
                        </div>
                        <div class="form-group mb-4">
                            <label style="font-weight: 500; font-size: 0.9rem; display: block; margin-bottom: 5px;">Confirm Password <span style="color:red;">*</span></label>
                            <input type="password" id="regConfirmPassword" name="confirmPassword" class="form-control" style="border-radius: 0; border: 1px solid #ccc; padding: 10px 12px; width: 100%;" required>
                        </div>
                    </div>

                    <!-- Bước 2: Nhập OTP -->
                    <div class="form-group mb-4" id="regOtpGroup" style="display: none;">
                        <label style="font-weight: 500; font-size: 0.9rem; display: block; margin-bottom: 5px;">6-Digit OTP Code <span style="color:red;">*</span></label>
                        <input type="text" id="regOtpInput" name="otp" class="form-control" style="border-radius: 0; border: 1px solid #000; padding: 10px 12px; width: 100%; letter-spacing: 5px; font-size: 1.2rem; font-weight: bold; text-align: center;" placeholder="------" maxlength="6">
                        <small style="color: #15803d; font-weight: 600; display: block; margin-top: 8px;">✔️ OTP has been sent to your email!</small>
                    </div>
                    
                    <button type="submit" id="btnActionRegister" class="btn btn-dark w-100" style="border-radius: 0; font-weight: 700; padding: 12px; background-color: #000; color: #fff; border: none;">CREATE ACCOUNT</button>
                    
                    <div class="mt-3 text-center" style="font-size: 0.85rem;">
                        <span style="color: #666;">Already have an account? </span>
                        <a href="javascript:void(0);" id="btnSwitchToLogin" data-bs-dismiss="modal" data-bs-toggle="modal" data-bs-target="#loginModal" style="color: #000; text-decoration: underline; font-weight: 600;">Login here</a>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
<!-- ================= END MODALS ================= -->

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

    // 1. XỬ LÝ LOGIN NGẦM
    document.addEventListener('DOMContentLoaded', function () {
        const loginForm = document.getElementById('popupLoginForm');
        if (loginForm) {
            loginForm.addEventListener('submit', function (e) {
                e.preventDefault();
                fetch('${pageContext.request.contextPath}/auth/login', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'X-Requested-With': 'XMLHttpRequest' },
                    body: new URLSearchParams(new FormData(this))
                })
                .then(response => response.json())
                .then(data => {
                    if (data.success) { window.location.href = data.redirectUrl; } 
                    else {
                        const errorDiv = document.getElementById('loginErrorMsg');
                        errorDiv.textContent = data.message;
                        errorDiv.style.display = 'block';
                    }
                }).catch(error => console.error('Error:', error));
            });
        }
    });

    // 2. XỬ LÝ QUÊN MẬT KHẨU NGẦM (3 BƯỚC)
    document.addEventListener('DOMContentLoaded', function () {
        const popupForgotPwdForm = document.getElementById('popupForgotPwdForm');
        const btnActionOtp = document.getElementById('btnActionOtp');
        const forgotPwdMsg = document.getElementById('forgotPwdMsg');
        let currentStep = 1; 

        if (popupForgotPwdForm) {
            popupForgotPwdForm.addEventListener('submit', function(e) { e.preventDefault(); });
        }

        const forgotOtpInput = document.getElementById('forgotOtpInput');
        if (forgotOtpInput) {
            forgotOtpInput.addEventListener('keypress', function(e) {
                if (e.key === 'Enter') { e.preventDefault(); btnActionOtp.click(); }
            });
        }

        if (btnActionOtp) {
            btnActionOtp.addEventListener('click', function () {
                forgotPwdMsg.style.display = 'none';
                forgotPwdMsg.classList.remove('alert-success');
                forgotPwdMsg.classList.add('alert-danger');

                // XIN MÃ OTP
                if (currentStep === 1) {
                    const emailValue = document.getElementById('forgotEmailInput').value;
                    if (!emailValue) {
                        forgotPwdMsg.textContent = "Please enter your email address.";
                        forgotPwdMsg.style.display = 'block';
                        return;
                    }

                    const originalText = btnActionOtp.textContent;
                    btnActionOtp.textContent = 'SENDING...';
                    btnActionOtp.disabled = true;

                    fetch('${pageContext.request.contextPath}/auth/forgot-password', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'X-Requested-With': 'XMLHttpRequest' },
                        body: new URLSearchParams({ email: emailValue })
                    })
                    .then(response => response.json())
                    .then(data => {
                        if (data.success) {
                            currentStep = 2;
                            document.getElementById('otpGroup').style.display = 'block';
                            document.getElementById('forgotEmailInput').readOnly = true;
                            document.getElementById('forgotEmailInput').style.backgroundColor = '#e5e7eb';
                            btnActionOtp.textContent = 'VERIFY OTP';
                            btnActionOtp.disabled = false;
                        } else {
                            forgotPwdMsg.textContent = data.message;
                            forgotPwdMsg.style.display = 'block';
                            btnActionOtp.textContent = originalText;
                            btnActionOtp.disabled = false;
                        }
                    }).catch(error => { 
                        forgotPwdMsg.textContent = "System error!";
                        forgotPwdMsg.style.display = 'block';
                        btnActionOtp.disabled = false; 
                        btnActionOtp.textContent = originalText;
                    });

                } 
                // KIỂM TRA MÃ OTP
                else if (currentStep === 2) {
                    const otpValue = forgotOtpInput.value;
                    if (!otpValue) {
                        forgotPwdMsg.textContent = "Please enter the OTP code.";
                        forgotPwdMsg.style.display = 'block';
                        return;
                    }
                    
                    const originalText = btnActionOtp.textContent;
                    btnActionOtp.textContent = 'VERIFYING...';
                    btnActionOtp.disabled = true;

                    const formData = new URLSearchParams();
                    formData.append('otp', otpValue);
                    formData.append('mode', 'forgot');

                    fetch('${pageContext.request.contextPath}/auth/verify-otp', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'X-Requested-With': 'XMLHttpRequest' },
                        body: formData
                    })
                    .then(response => {
                        const ct = response.headers.get("content-type");
                        if (ct && ct.indexOf("application/json") !== -1) return response.json();
                        throw new Error("SERVER_NOT_JSON");
                    })
                    .then(data => {
                        if (data.success) {
                            currentStep = 3;
                            document.getElementById('emailGroup').style.display = 'none';
                            document.getElementById('otpGroup').style.display = 'none';
                            document.getElementById('newPasswordGroup').style.display = 'block';
                            document.getElementById('forgotPasswordModalLabel').textContent = 'CREATE NEW PASSWORD';
                            btnActionOtp.textContent = 'RESET PASSWORD';
                            btnActionOtp.disabled = false;
                        } else {
                            forgotPwdMsg.textContent = data.message;
                            forgotPwdMsg.style.display = 'block';
                            btnActionOtp.textContent = originalText;
                            btnActionOtp.disabled = false;
                        }
                    }).catch(error => { 
                        forgotPwdMsg.innerHTML = "<b>Lỗi:</b> Mã OTP không hợp lệ hoặc Server lỗi!";
                        forgotPwdMsg.style.display = 'block';
                        btnActionOtp.textContent = originalText;
                        btnActionOtp.disabled = false; 
                    });
                }
                // ĐẶT MẬT KHẨU MỚI
                else if (currentStep === 3) {
                    const newPwd = document.getElementById('resetNewPwd').value;
                    const confirmPwd = document.getElementById('resetConfirmPwd').value;

                    if (!newPwd || !confirmPwd) {
                        forgotPwdMsg.textContent = "Please fill in all fields.";
                        forgotPwdMsg.style.display = 'block'; return;
                    }
                    if (newPwd !== confirmPwd) {
                        forgotPwdMsg.textContent = "Passwords do not match!";
                        forgotPwdMsg.style.display = 'block'; return;
                    }

                    const originalText = btnActionOtp.textContent;
                    btnActionOtp.textContent = 'UPDATING...';
                    btnActionOtp.disabled = true;

                    fetch('${pageContext.request.contextPath}/auth/reset-password', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'X-Requested-With': 'XMLHttpRequest' },
                        body: newSearchParams({ newPassword: newPwd, confirmPassword: confirmPwd })
                    })
                    .then(response => response.json())
                    .then(data => {
                        if (data.success) {
                            forgotPwdMsg.classList.remove('alert-danger');
                            forgotPwdMsg.classList.add('alert-success');
                            forgotPwdMsg.textContent = "Password updated successfully! Please login.";
                            forgotPwdMsg.style.display = 'block';
                            
                            setTimeout(() => {
                                currentStep = 1;
                                popupForgotPwdForm.reset();
                                document.getElementById('emailGroup').style.display = 'block';
                                document.getElementById('forgotEmailInput').readOnly = false;
                                document.getElementById('forgotEmailInput').style.backgroundColor = '#f9f9f9';
                                document.getElementById('otpGroup').style.display = 'none';
                                document.getElementById('newPasswordGroup').style.display = 'none';
                                document.getElementById('forgotPasswordModalLabel').textContent = 'FORGOT PASSWORD';
                                btnActionOtp.textContent = 'SEND OTP';
                                forgotPwdMsg.style.display = 'none';
                                document.getElementById('btnBackToLogin').click();
                            }, 2000);
                        } else {
                            forgotPwdMsg.textContent = data.message;
                            forgotPwdMsg.style.display = 'block';
                            btnActionOtp.disabled = false;
                            btnActionOtp.textContent = originalText;
                        }
                    }).catch(error => { 
                        forgotPwdMsg.textContent = "Lỗi hệ thống!";
                        forgotPwdMsg.style.display = 'block';
                        btnActionOtp.disabled = false; 
                        btnActionOtp.textContent = originalText;
                    });
                }
            });
        }
    });

    // 3. XỬ LÝ REGISTER NGẦM (2 BƯỚC)
    document.addEventListener('DOMContentLoaded', function () {
        const popupRegisterForm = document.getElementById('popupRegisterForm');
        const btnActionRegister = document.getElementById('btnActionRegister');
        const registerMsg = document.getElementById('registerMsg');
        
        let regStep = 1;

        if (popupRegisterForm) {
            popupRegisterForm.addEventListener('submit', function(e) {
                e.preventDefault();
                registerMsg.style.display = 'none';
                registerMsg.classList.remove('alert-success');
                registerMsg.classList.add('alert-danger');

                // BƯỚC 1: NHẬP THÔNG TIN
                if (regStep === 1) {
                    const pass = document.getElementById('regPassword').value;
                    const confirmPass = document.getElementById('regConfirmPassword').value;

                    if (pass !== confirmPass) {
                        registerMsg.textContent = "Passwords do not match!";
                        registerMsg.style.display = 'block';
                        return;
                    }

                    const originalText = btnActionRegister.textContent;
                    btnActionRegister.textContent = 'SENDING OTP...';
                    btnActionRegister.disabled = true;

                    fetch('${pageContext.request.contextPath}/auth/register', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'X-Requested-With': 'XMLHttpRequest' },
                        body: new URLSearchParams(new FormData(popupRegisterForm))
                    })
                    .then(response => {
                        const ct = response.headers.get("content-type");
                        if (ct && ct.indexOf("application/json") !== -1) return response.json();
                        throw new Error("SERVER_NOT_JSON");
                    })
                    .then(data => {
                        if (data.success) {
                            regStep = 2;
                            document.getElementById('registerInfoGroup').style.display = 'none';
                            document.getElementById('regOtpGroup').style.display = 'block';
                            document.getElementById('registerModalLabel').textContent = 'VERIFY EMAIL';
                            btnActionRegister.textContent = 'VERIFY OTP';
                            btnActionRegister.disabled = false;
                        } else {
                            registerMsg.textContent = data.message;
                            registerMsg.style.display = 'block';
                            btnActionRegister.textContent = originalText;
                            btnActionRegister.disabled = false;
                        }
                    }).catch(error => {
                        console.error(error);
                        registerMsg.innerHTML = "<b>Đang tải...</b> Hãy cập nhật RegisterServlet.java để hỗ trợ AJAX!";
                        registerMsg.style.display = 'block';
                        btnActionRegister.textContent = originalText;
                        btnActionRegister.disabled = false;
                    });

                } 
                // BƯỚC 2: XÁC THỰC OTP ĐĂNG KÝ
                else if (regStep === 2) {
                    const otpVal = document.getElementById('regOtpInput').value;
                    if (!otpVal) {
                        registerMsg.textContent = "Please enter OTP code!";
                        registerMsg.style.display = 'block';
                        return;
                    }

                    const originalText = btnActionRegister.textContent;
                    btnActionRegister.textContent = 'VERIFYING...';
                    btnActionRegister.disabled = true;

                    const formData = new URLSearchParams();
                    formData.append('otp', otpVal);
                    formData.append('mode', 'register');

                    fetch('${pageContext.request.contextPath}/auth/verify-otp', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'X-Requested-With': 'XMLHttpRequest' },
                        body: formData
                    })
                    .then(response => {
                        const ct = response.headers.get("content-type");
                        if (ct && ct.indexOf("application/json") !== -1) return response.json();
                        throw new Error("SERVER_NOT_JSON");
                    })
                    .then(data => {
                        if (data.success) {
                            registerMsg.classList.remove('alert-danger');
                            registerMsg.classList.add('alert-success');
                            registerMsg.textContent = "Registration successful! You can now login.";
                            registerMsg.style.display = 'block';
                            
                            setTimeout(() => {
                                document.getElementById('btnSwitchToLogin').click();
                                // Reset form
                                regStep = 1;
                                popupRegisterForm.reset();
                                document.getElementById('registerInfoGroup').style.display = 'block';
                                document.getElementById('regOtpGroup').style.display = 'none';
                                document.getElementById('registerModalLabel').textContent = 'CREATE ACCOUNT';
                                btnActionRegister.textContent = 'CREATE ACCOUNT';
                                registerMsg.style.display = 'none';
                            }, 2000);
                        } else {
                            registerMsg.textContent = data.message;
                            registerMsg.style.display = 'block';
                            btnActionRegister.textContent = originalText;
                            btnActionRegister.disabled = false;
                        }
                    }).catch(error => {
                        console.error(error);
                        registerMsg.innerHTML = "<b>Lỗi:</b> Mã OTP không đúng!";
                        registerMsg.style.display = 'block';
                        btnActionRegister.textContent = originalText;
                        btnActionRegister.disabled = false;
                    });
                }
            });
        }
    });
</script>