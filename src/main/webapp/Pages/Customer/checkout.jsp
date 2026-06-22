<%-- 
    Document   : checkout
    Created on : Jun 22, 2026, 10:51:06 PM
    Author     : CE181629 - Ngo Manh Quan
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Thanh toán | Shopee Style</title>
    
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    
    <link href="${pageContext.request.contextPath}/Pages/Customer/checkout.css" rel="stylesheet">
</head>
<body>

<div class="container py-5">
    <h3 class="mb-4">💳 Thanh Toán</h3>

    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            ⚠️ ${error}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>

    <form id="checkoutForm" action="${pageContext.request.contextPath}/CheckoutServlet" method="post">
        <div class="row">
            
            <div class="col-md-7">
                
                <div class="checkout-section">
                    <div class="section-title text-shopee">
                        📍 Thông Tin Nhận Hàng
                    </div>
                    <div class="mb-3">
                        <label class="form-label small text-muted">Số điện thoại nhận hàng</label>
                        <input type="tel" name="phone" class="form-control" placeholder="Nhập số điện thoại..." required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small text-muted">Địa chỉ chi tiết</label>
                        <textarea name="shippingAddress" class="form-control" rows="3" placeholder="Số nhà, tên đường, phường/xã, quận/huyện..." required></textarea>
                    </div>
                </div>

                <div class="checkout-section">
                    <div class="section-title">
                        📦 Sản phẩm đã chọn
                    </div>
                    <div class="product-list">
                        <c:forEach items="${checkoutItems}" var="item">
                            <div class="product-item">
                                <img src="${item.imageUrl}" class="product-img">
                                <div style="flex: 1;">
                                    <div class="fw-medium text-truncate" style="max-width: 300px;">${item.productName}</div>
                                    <small class="text-muted">Phân loại: ${item.sizeName} / ${item.colorName}</small>
                                </div>
                                <div class="text-muted small">x${item.quantity}</div>
                                <div class="fw-semibold text-end" style="min-width: 90px;">${item.subtotal}₫</div>
                            </div>
                        </c:forEach>
                    </div>
                </div>

            </div>

            <div class="col-md-5">
                <div class="checkout-section sticky-panel">
                    <div class="section-title border-bottom pb-2">
                        💰 Tổng kết đơn hàng
                    </div>
                    
                    <div class="mt-3">
                        <div class="price-row">
                            <span>Tổng tiền hàng:</span>
                            <span>${total}₫</span>
                        </div>
                        <div class="price-row">
                            <span>Phí vận chuyển:</span>
                            <span class="text-success">Miễn phí</span>
                        </div>
                        <hr>
                        <div class="d-flex justify-content-between align-items-center mb-4">
                            <span class="fw-bold">Tổng thanh toán:</span>
                            <h3 class="text-shopee m-0 fw-bold">${total}₫</h3>
                        </div>
                    </div>

                    <button type="submit" id="btnSubmitOrder" class="btn btn-shopee w-100 rounded">
                        Đặt Hàng Ngay
                    </button>
                    
                    <div class="text-center mt-3">
                        <a href="${pageContext.request.contextPath}/Pages/Customer/Cart.jsp" class="text-decoration-none small text-muted">
                            ← Quay lại chỉnh sửa giỏ hàng
                        </a>
                    </div>
                </div>
            </div>

        </div>
    </form>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

<script>
    document.getElementById('checkoutForm').addEventListener('submit', function() {
        const btn = document.getElementById('btnSubmitOrder');
        btn.disabled = true;
        btn.innerHTML = `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Đang xử lý đơn hàng...`;
    });
</script>
</body>
</html>