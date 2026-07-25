<%-- 
    Document   : manage-voucher
    Created on : Jul 25, 2026, 7:31:37 AM
    Author     : ADMIN
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %> <%-- Thêm thư viện format số ở đây --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Quản Lý Voucher</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
</head>
<body class="bg-light">
<div class="container mt-5">
    <!-- Header của trang -->
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2><i class="fas fa-ticket-alt text-primary"></i> Quản Lý Voucher</h2>
        <div>
            <!-- Nút quay lại trang Dashboard -->
            <a href="${pageContext.request.contextPath}/Admin?section=overview" class="btn btn-secondary me-2">
                <i class="fas fa-arrow-left"></i> Quay lại Admin
            </a>
            <!-- Nút thêm mới -->
            <a href="${pageContext.request.contextPath}/Pages/Admin/add-voucher.jsp" class="btn btn-success">
                <i class="fas fa-plus"></i> Thêm Voucher mới
            </a>
        </div>
    </div>

    <!-- Bảng danh sách Voucher -->
    <div class="card shadow-sm">
        <div class="card-body">
            <div class="table-responsive">
                <table class="table table-hover table-striped align-middle">
                    <thead class="table-dark">
                        <tr>
                            <th>Mã Code</th>
                            <th>Loại Giảm</th>
                            <th>Mức Giảm</th>
                            <th>Tối thiểu</th>
                            <th>Giới hạn dùng</th>
                            <th>Đã dùng</th>
                            <th>Trạng thái</th>
                            <th class="text-center">Hành động</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty vouchers}">
                                <c:forEach items="${vouchers}" var="v">
                                    <tr>
                                        <td>
                                            <span class="badge bg-primary fs-6">${v.voucherCode}</span>
                                        </td>
                                        <td>${v.discountType}</td>
                                        
                                        <%-- FORMAT CỘT MỨC GIẢM --%>
                                        <td class="fw-bold text-success">
                                            <c:choose>
                                                <%-- Nếu là giảm theo phần trăm thì in ra % --%>
                                                <c:when test="${v.discountType == 'Percentage' || v.discountType == 'PERCENTAGE'}">
                                                    <fmt:formatNumber value="${v.discountValue}" maxFractionDigits="0"/>%
                                                </c:when>
                                                <%-- Nếu là giảm tiền mặt thì in ra dấu phẩy và VNĐ --%>
                                                <c:otherwise>
                                                    <fmt:formatNumber value="${v.discountValue}" pattern="#,###"/> VNĐ
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        
                                        <%-- FORMAT CỘT TỐI THIỂU --%>
                                        <td>
                                            <fmt:formatNumber value="${v.minOrderValue}" pattern="#,###"/> VNĐ
                                        </td>
                                        
                                        <td>${v.usageLimit}</td>
                                        <td>${v.usedCount}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${v.status == 'Active'}">
                                                    <span class="badge bg-success">Đang hoạt động</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-danger">Đã khóa</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="text-center">
                                            <c:choose>
                                                <c:when test="${v.status == 'Active'}">
                                                    <!-- Nút Khóa: Bo tròn, viền đỏ tinh tế -->
                                                    <a href="${pageContext.request.contextPath}/manage-voucher?action=toggleStatus&id=${v.voucherCode}&status=${v.status}" 
                                                       class="btn btn-sm btn-outline-danger rounded-pill px-3 fw-semibold shadow-sm">
                                                       <i class="fas fa-lock me-1"></i> Khóa
                                                    </a>
                                                </c:when>
                                                <c:otherwise>
                                                    <!-- Nút Mở khóa: Nền Dark-theme, Icon xanh lá nổi bật -->
                                                    <a href="${pageContext.request.contextPath}/manage-voucher?action=toggleStatus&id=${v.voucherCode}&status=${v.status}" 
                                                       class="btn btn-sm btn-dark rounded-pill px-3 fw-bold shadow-sm border-0">
                                                       <i class="fas fa-unlock text-success me-1"></i> Mở khóa
                                                    </a>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="8" class="text-center text-muted py-4">
                                        <i class="fas fa-box-open fa-2x mb-2"></i><br>
                                        Chưa có voucher nào trong hệ thống!
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
