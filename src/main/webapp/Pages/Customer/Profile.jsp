<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<style>
    .profile-wrapper {
        padding: 40px 0;
        background-color: #f3f4f6;
        font-family: 'Space Grotesk', 'Inter', sans-serif;
        min-height: 85vh;
    }
    
    /* STYLE CHO SIDEBAR (CỘT TRÁI) */
    .profile-sidebar {
        background: #fff;
        border-radius: 16px;
        padding: 24px;
        border: 1px solid #e5e7eb;
        box-shadow: 0 10px 30px rgba(0,0,0,0.03);
    }
    .profile-sidebar .user-info {
        text-align: center;
        border-bottom: 1px solid #f3f4f6;
        padding-bottom: 20px;
        margin-bottom: 20px;
    }
    .profile-sidebar .avatar-circle {
        width: 72px;
        height: 72px;
        background-color: #111827;
        color: #fff;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 28px;
        font-weight: 800;
        margin: 0 auto 12px;
    }
    .profile-sidebar .nav-link {
        color: #475569;
        font-weight: 600;
        padding: 12px 16px;
        border-radius: 10px;
        margin-bottom: 4px;
        transition: all 0.2s ease;
        display: flex;
        align-items: center;
    }
    .profile-sidebar .nav-link:hover, .profile-sidebar .nav-link.active {
        background-color: #f8fafc;
        color: #111827;
    }
    .profile-sidebar .nav-link span.material-symbols-outlined {
        margin-right: 12px;
        font-size: 22px;
    }
    .profile-sidebar .logout-link {
        color: #ef4444;
        margin-top: 10px;
    }
    .profile-sidebar .logout-link:hover {
        background-color: #fef2f2;
        color: #dc2626;
    }
    
    /* STYLE CHO FORM BÊN PHẢI */
    .profile-content-card {
        background: #fff;
        border-radius: 16px;
        padding: 32px;
        border: 1px solid #e5e7eb;
        box-shadow: 0 10px 30px rgba(0,0,0,0.03);
    }
    .profile-content-card h3 {
        font-weight: 800;
        margin-bottom: 24px;
        padding-bottom: 16px;
        border-bottom: 1px solid #f3f4f6;
        color: #111827;
        text-transform: uppercase;
        letter-spacing: 0.05em;
        font-size: 1.25rem;
    }
    .form-label {
        font-weight: 700;
        font-size: 0.75rem;
        text-transform: uppercase;
        color: #6b7280;
        letter-spacing: 0.1em;
        margin-bottom: 8px;
    }
    .form-control {
        border-radius: 12px;
        padding: 14px 16px;
        border: 1px solid #d1d5db;
        background-color: #f8fafc;
        color: #111827;
        font-weight: 500;
        font-size: 0.95rem;
    }
    .form-control:focus {
        background-color: #ffffff;
        border-color: #111827;
        box-shadow: 0 0 0 4px rgba(17, 24, 39, 0.1);
    }
    .form-control:disabled, .form-control[readonly] {
        background-color: #e5e7eb;
        color: #6b7280;
        cursor: not-allowed;
    }
    .btn-save {
        background-color: #111827;
        color: #fff;
        padding: 14px 32px;
        font-weight: 700;
        border-radius: 999px;
        border: none;
        transition: all 0.2s ease;
        text-transform: uppercase;
        letter-spacing: 0.05em;
    }
    .btn-save:hover {
        background-color: #374151;
        transform: translateY(-1px);
    }
</style>

<div class="profile-wrapper">
    <div class="container" style="max-width: 1100px;">
        <div class="row">
            
            <!-- CỘT TRÁI: SIDEBAR MENU -->
            <div class="col-lg-3 col-md-4 mb-4">
                <div class="profile-sidebar">
                    <div class="user-info">
                        <div class="avatar-circle">
                            ${empty sessionScope.USER.avatar ? sessionScope.USER.fullName.substring(0,1).toUpperCase() : '<img src="'.concat(sessionScope.USER.avatar).concat('" style="width:100%; height:100%; object-fit:cover; border-radius:50%;">')}
                        </div>
                        <h5 class="mb-1" style="font-weight: 800; font-size: 1.1rem;">${sessionScope.USER.fullName}</h5>
                        <small style="color: #6b7280; font-weight: 600;">${sessionScope.USER.role}</small>
                    </div>
                    
                    <nav class="nav flex-column">
                        <a class="nav-link active" href="${pageContext.request.contextPath}/profile">
                            <span class="material-symbols-outlined">person</span> Account Details
                        </a>
                        
                        <!-- Chỉ hiện Đơn hàng và Yêu thích cho Customer -->
                        <c:if test="${sessionScope.USER.role ne 'Staff' && sessionScope.USER.role ne 'Admin'}">
                            <a class="nav-link" href="${pageContext.request.contextPath}/customer/order-history">
                                <span class="material-symbols-outlined">receipt_long</span> My Orders
                            </a>
                            <a class="nav-link" href="${pageContext.request.contextPath}/wishlist">
                                <span class="material-symbols-outlined">favorite</span> Wishlist
                            </a>
                        </c:if>

                        <a class="nav-link" href="${pageContext.request.contextPath}/change-password">
                            <span class="material-symbols-outlined">lock</span> Change Password
                        </a>
                        
                        <hr style="border-color: #e5e7eb; margin: 10px 0;">
                        
                        <a class="nav-link logout-link" href="${pageContext.request.contextPath}/auth/logout">
                            <span class="material-symbols-outlined">logout</span> Log Out
                        </a>
                    </nav>
                </div>
            </div>

            <!-- CỘT PHẢI: FORM CHỈNH SỬA THÔNG TIN -->
            <div class="col-lg-9 col-md-8">
                <div class="profile-content-card">
                    <h3>Account Details</h3>
                    
                    <form action="${pageContext.request.contextPath}/profile/update" method="POST">
                        <div class="row">
                            <div class="col-md-6 mb-4">
                                <label class="form-label">Full Name</label>
                                <input type="text" name="fullName" class="form-control" value="${sessionScope.USER.fullName}" required>
                            </div>
                            
                            <div class="col-md-6 mb-4">
                                <label class="form-label">Email Address</label>
                                <input type="email" class="form-control" value="${sessionScope.USER.email}" disabled>
                            </div>
                            
                            <div class="col-md-12 mb-4">
                                <label class="form-label">Phone Number</label>
                                <input type="tel" name="phone" class="form-control" value="${sessionScope.USER.phone}" pattern="0[0-9]{9}" placeholder="0912345678" title="Phone number must be exactly 10 digits starting with 0 (e.g., 0912345678)">
                            </div>
                            
                            <!-- Chỉ hiện Địa chỉ giao hàng nếu không phải là Staff/Admin -->
                            <c:if test="${sessionScope.USER.role ne 'Staff' && sessionScope.USER.role ne 'Admin'}">
                                <div class="col-md-12 mb-4">
                                    <label class="form-label">Shipping Address</label>
                                    <input type="text" name="address" class="form-control" value="${sessionScope.USER.address}" placeholder="Enter your full address">
                                </div>
                            </c:if>
                        </div>
                        
                        <div class="text-end mt-2">
                            <button type="submit" class="btn-save">Save Changes</button>
                        </div>
                    </form>
                </div>
            </div>

        </div>
    </div>
</div>