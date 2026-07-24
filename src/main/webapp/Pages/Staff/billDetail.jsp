<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Bill Details ${bill.billId}</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background:#f6f7fb; }
        .card { border:none; border-radius:14px; box-shadow:0 2px 10px rgba(0,0,0,.06); }
        .item-img { width:56px; height:56px; object-fit:cover; border-radius:8px; }
    </style>
</head>
<body class="p-4">
<div class="container-fluid">

    <div class="d-flex justify-content-between align-items-center mb-2">
        <div>
            <a href="${pageContext.request.contextPath}/BillServlet?action=list" class="btn btn-link ps-0">&larr; Back to Bill List</a>
        </div>
        <div>
            <a href="${pageContext.request.contextPath}/staff/products" class="btn btn-outline-secondary btn-sm">Back to Product Management</a>
        </div>
    </div>

    <h3 class="mb-4">Bill Details #${bill.billId}</h3>

    <!-- ===== Bill Information ===== -->
    <div class="card p-3 mb-4">
        <div class="row">
            <div class="col-md-4">
                <p><strong>Order ID:</strong> ${bill.orderId}</p>
                <p><strong>Customer:</strong> ${bill.customerName}</p>
                <p><strong>Phone:</strong> ${bill.customerPhone}</p>
                <p><strong>Shipping Address:</strong> ${bill.shippingAddress}</p>
            </div>
            <div class="col-md-4">
                <p><strong>Order Date:</strong>
                    <fmt:formatDate value="${bill.placedAt}" pattern="dd/MM/yyyy HH:mm"/></p>
                <p><strong>Issued Date:</strong>
                    <fmt:formatDate value="${bill.issuedDate}" pattern="dd/MM/yyyy HH:mm"/></p>
                <p><strong>Order Status:</strong> ${bill.orderStatus}</p>
            </div>
            <div class="col-md-4">
                <p><strong>Payment Method:</strong> ${bill.paymentMethod}</p>
                <p><strong>Payment Status:</strong> ${bill.paymentStatus}</p>
                <p><strong>Total Amount:</strong>
                    <span class="fw-bold text-primary">
                        <fmt:formatNumber value="${bill.totalAmount}" type="number" groupingUsed="true"/> VND
                    </span>
                </p>
            </div>
        </div>
    </div>

    <!-- ===== Search Products in Bill ===== -->
    <div class="card p-3 mb-4">
        <form method="get" action="${pageContext.request.contextPath}/BillServlet" class="row g-2 align-items-end">
            <input type="hidden" name="action" value="detail">
            <input type="hidden" name="billId" value="${bill.billId}">

            <div class="col-md-6">
                <label class="form-label">Search product in bill (product name / SKU)</label>
                <input type="text" name="keyword" value="${keyword}" class="form-control"
                       placeholder="Enter product name or SKU...">
            </div>
            <div class="col-md-2 d-grid">
                <button type="submit" class="btn btn-primary">Search</button>
            </div>
            <div class="col-md-2 d-grid">
                <a class="btn btn-outline-secondary"
                   href="${pageContext.request.contextPath}/BillServlet?action=detail&billId=${bill.billId}">Clear</a>
            </div>
        </form>
    </div>

    <!-- ===== Products in Bill ===== -->
    <div class="card p-3">
        <h5 class="mb-3">Products in Bill (${items.size()} items)</h5>

        <table class="table table-hover align-middle">
            <thead class="table-light">
            <tr>
                <th>Product</th>
                <th>SKU</th>
                <th>Size</th>
                <th>Color</th>
                <th class="text-end">Quantity</th>
                <th class="text-end">Unit Price</th>
                <th class="text-end">Subtotal</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="it" items="${items}">
                <tr>
                    <td>${it.productName}</td>
                    <td>${it.sku}</td>
                    <td>${it.sizeName}</td>
                    <td>${it.colorName}</td>
                    <td class="text-end">${it.quantity}</td>
                    <td class="text-end"><fmt:formatNumber value="${it.unitPrice}" type="number" groupingUsed="true"/> VND</td>
                    <td class="text-end fw-semibold"><fmt:formatNumber value="${it.subtotal}" type="number" groupingUsed="true"/> VND</td>
                </tr>
            </c:forEach>
            <c:if test="${empty items}">
                <tr><td colspan="7" class="text-center text-muted">No products found.</td></tr>
            </c:if>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>
