<%-- 
    Document   : Cart
    Created on : Jun 8, 2026, 10:17:18 AM
    Author     : Admin
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Cart | Shopee Style</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">

    <style>
        body {
            background: #f5f5f5;
        }

        .cart-container {
            max-width: 1100px;
            margin: auto;
        }

        .cart-item {
            background: white;
            border-radius: 8px;
            padding: 15px;
            margin-bottom: 12px;
            display: flex;
            align-items: center;
            gap: 15px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.08);
        }

        .img-box img {
            width: 80px;
            height: 80px;
            object-fit: cover;
            border-radius: 6px;
        }

        .name {
            font-weight: 500;
        }

        .price {
            color: #ee4d2d;
            font-weight: bold;
        }

        .right-panel {
            position: sticky;
            top: 20px;
        }

        .qty-input {
            width: 70px;
            text-align: center;
        }

        .checkout-box {
            background: white;
            padding: 15px;
            border-radius: 8px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.08);
        }

        .btn-shopee {
            background: #ee4d2d;
            color: white;
        }

        .btn-shopee:hover {
            background: #d73211;
            color: white;
        }
    </style>
</head>

<body>
<div class="container mt-3"><a href="${pageContext.request.contextPath}/home" class="btn btn-outline-secondary">&larr; Back to Home</a></div>

<div class="container cart-container py-4">

    <h3 class="mb-4">Shopping Cart</h3>

    <c:choose>

        <c:when test="${empty cartItems}">
            <div class="alert alert-warning text-center">
                Your cart is empty
            </div>
        </c:when>

        <c:otherwise>

            <form action="${pageContext.request.contextPath}/cart/checkout" method="post">

                <div class="row">

                    <!-- LEFT CART -->
                    <div class="col-md-8">

                        <c:forEach items="${cartItems}" var="item">

                            <div class="cart-item">

                                <input type="checkbox"
                                       name="selectedItems"
                                       value="${item.cartItemId}" data-subtotal="${item.subtotal}" onchange="calculateTotal()">

                                <div class="img-box">
                                    <img src="${pageContext.request.contextPath}${item.imageUrl}">
                                </div>

                                <div style="flex:1">
                                    <div class="name">${item.productName}</div>
                                    <small>${item.sizeName} / ${item.colorName}</small>
                                </div>

                                <div class="price">
                                    ${item.price}₫
                                </div>

                                <input type="number"
                                       class="form-control qty-input"
                                       value="${item.quantity}"
                                       min="1"
                                       onchange="updateQty('${item.cartItemId}', this.value)">

                                <div class="price">
                                    ${item.subtotal}₫
                                </div>

                                <a class="btn btn-sm btn-outline-danger"
                                   href="${pageContext.request.contextPath}/cart/delete?id=${item.cartItemId}">
                                    Delete
                                </a>

                            </div>

                        </c:forEach>

                    </div>

                    <!-- RIGHT CHECKOUT -->
                    <div class="col-md-4 right-panel">

                        <div class="checkout-box">

                            <h5>Order Summary</h5>

                            <hr>

                            <h4 class="text-danger">
                                <span id="totalPrice">0₫</span>
                            </h4>

                            <button type="submit" class="btn btn-shopee w-100 mt-3">
                                Checkout
                            </button>

                        </div>

                    </div>

                </div>

            </form>

        </c:otherwise>

    </c:choose>

</div>

<script>
function updateQty(id, qty) {

    fetch('${pageContext.request.contextPath}/cart/update', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: 'cartItemId=' + id + '&quantity=' + qty
    }).then(() => {
        location.reload();
    });

}
function calculateTotal(){let t=0;document.querySelectorAll('input[name="selectedItems"]:checked').forEach(cb=>t+=Number(cb.dataset.subtotal));document.getElementById("totalPrice").innerHTML=t.toLocaleString('vi-VN')+'₫';}
</script>

</body>
</html>