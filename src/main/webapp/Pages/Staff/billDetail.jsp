<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Chi tiết hóa đơn ${bill.billId}</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background:#f6f7fb; }
        .card { border:none; border-radius:14px; box-shadow:0 2px 10px rgba(0,0,0,.06); }
        .item-img { width:56px; height:56px; object-fit:cover; border-radius:8px; }
    </style>
</head>
<body class="p-4">
<div class="container-fluid">

    <a href="${pageContext.request.contextPath}/BillServlet?action=list" class="btn btn-link ps-0 mb-2">&larr; Quay lại danh sách hóa đơn</a>

    <h3 class="mb-4">Chi tiết hóa đơn #${bill.billId}</h3>

    <!-- ===== Thông tin hóa đơn ===== -->
    <div class="card p-3 mb-4">
        <div class="row">
            <div class="col-md-4">
                <p><strong>Mã đơn hàng:</strong> ${bill.orderId}</p>
                <p><strong>Khách hàng:</strong> ${bill.customerName}</p>
                <p><strong>SĐT:</strong> ${bill.customerPhone}</p>
                <p><strong>Địa chỉ giao hàng:</strong> ${bill.shippingAddress}</p>
            </div>
            <div class="col-md-4">
                <p><strong>Ngày đặt hàng:</strong>
                    <fmt:formatDate value="${bill.placedAt}" pattern="dd/MM/yyyy HH:mm"/></p>
                <p><strong>Ngày lập hóa đơn:</strong>
                    <fmt:formatDate value="${bill.issuedDate}" pattern="dd/MM/yyyy HH:mm"/></p>
                <p><strong>Trạng thái đơn hàng:</strong> ${bill.orderStatus}</p>
            </div>
            <div class="col-md-4">
                <p><strong>Phương thức thanh toán:</strong> ${bill.paymentMethod}</p>
                <p><strong>Trạng thái thanh toán:</strong> ${bill.paymentStatus}</p>
                <p><strong>Tổng tiền hóa đơn:</strong>
                    <span class="fw-bold text-primary">
                        <fmt:formatNumber value="${bill.totalAmount}" type="number" groupingUsed="true"/> đ
                    </span>
                </p>
            </div>
        </div>
    </div>

    <!-- ===== Tìm kiếm chi tiết hóa đơn (theo sản phẩm) ===== -->
    <div class="card p-3 mb-4">
        <form method="get" action="${pageContext.request.contextPath}/BillServlet" class="row g-2 align-items-end">
            <input type="hidden" name="action" value="detail">
            <input type="hidden" name="billId" value="${bill.billId}">

            <div class="col-md-6">
                <label class="form-label">Tìm sản phẩm trong hóa đơn (tên sản phẩm / SKU)</label>
                <input type="text" name="keyword" value="${keyword}" class="form-control"
                       placeholder="Nhập tên sản phẩm hoặc SKU...">
            </div>
            <div class="col-md-2 d-grid">
                <button type="submit" class="btn btn-primary">Tìm</button>
            </div>
            <div class="col-md-2 d-grid">
                <a class="btn btn-outline-secondary"
                   href="${pageContext.request.contextPath}/BillServlet?action=detail&billId=${bill.billId}">Xóa lọc</a>
            </div>
        </form>
    </div>

    <!-- ===== Danh sách sản phẩm trong hóa đơn ===== -->
    <div class="card p-3">
        <h5 class="mb-3">Sản phẩm trong hóa đơn (${items.size()} dòng)</h5>

        <table class="table table-hover align-middle">
            <thead class="table-light">
            <tr>
                <th>Ảnh</th>
                <th>Sản phẩm</th>
                <th>SKU</th>
                <th>Size</th>
                <th>Màu</th>
                <th class="text-end">Số lượng</th>
                <th class="text-end">Đơn giá</th>
                <th class="text-end">Giảm giá</th>
                <th class="text-end">Thành tiền</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="it" items="${items}">
                <tr>
                    <td>
                        <c:if test="${not empty it.imageUrl}">
                            <img src="${it.imageUrl}" class="item-img" alt="${it.productName}">
                        </c:if>
                    </td>
                    <td>${it.productName}</td>
                    <td>${it.sku}</td>
                    <td>${it.sizeName}</td>
                    <td>${it.colorName}</td>
                    <td class="text-end">${it.quantity}</td>
                    <td class="text-end"><fmt:formatNumber value="${it.unitPrice}" type="number" groupingUsed="true"/> đ</td>
                    <td class="text-end"><fmt:formatNumber value="${it.discountAmount}" type="number" groupingUsed="true"/> đ</td>
                    <td class="text-end fw-semibold"><fmt:formatNumber value="${it.subtotal}" type="number" groupingUsed="true"/> đ</td>
                </tr>
            </c:forEach>
            <c:if test="${empty items}">
                <tr><td colspan="9" class="text-center text-muted">Không tìm thấy sản phẩm nào phù hợp.</td></tr>
            </c:if>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>
