<%-- 
    Document   : Cart
    Created on : Jun 8, 2026, 10:17:18 AM
    Author     : Admin
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
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

        .cart-header {
            background: white;
            border-radius: 8px;
            padding: 12px 15px;
            margin-bottom: 12px;
            display: flex;
            align-items: center;
            gap: 15px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.08);
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

        .select-all-checkbox {
            display: flex;
            align-items: center;
            gap: 8px;
            cursor: pointer;
            user-select: none;
        }

        .select-all-checkbox input {
            width: 18px;
            height: 18px;
            cursor: pointer;
        }

        .item-checkbox {
            width: 18px;
            height: 18px;
            cursor: pointer;
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

            <form id="checkoutForm" action="${pageContext.request.contextPath}/customer/checkout" method="post">
                <!-- Hidden fields for selected items -->
                <input type="hidden" name="selectedItemsList" id="selectedItemsList" value="">

                <div class="row">

                    <!-- LEFT CART -->
                    <div class="col-md-8">

                        <!-- Select All Header -->
                        <div class="cart-header">
                            <label class="select-all-checkbox">
                                <input type="checkbox" id="selectAll" onchange="toggleSelectAll()">
                                <span>Select All (${cartItems.size()} items)</span>
                            </label>
                        </div>

                        <c:forEach items="${cartItems}" var="item">

                            <div class="cart-item">

                                <input type="checkbox"
                                       class="item-checkbox"
                                       name="selectedItems"
                                       value="${item.cartItemId}" data-subtotal="${item.subtotal}" onchange="updateSelectAllState(); calculateTotal()">

                                <div class="img-box">
                                    <img src="${pageContext.request.contextPath}${item.imageUrl}">
                                </div>

                                <div style="flex:1">
                                    <div class="name">${item.productName}</div>
                                    <small>${item.sizeName} / ${item.colorName}</small>
                                </div>

                                <div class="price">
                                    <fmt:formatNumber value="${item.price}" pattern="#,##0"/> VND
                                </div>

                                <input type="number"
                                       class="form-control qty-input"
                                       value="${item.quantity}"
                                       min="1"
                                       onchange="updateQty('${item.cartItemId}', this.value)">

                                <div class="price">
                                    <fmt:formatNumber value="${item.subtotal}" pattern="#,##0"/> VND
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

                            <div class="mb-2">
                                <div class="d-flex justify-content-between">
                                    <span class="text-muted">Selected items:</span>
                                    <span id="selectedCount">0</span>
                                </div>
                            </div>

                            <div class="mb-2">
                                <div class="d-flex justify-content-between">
                                    <span class="text-muted">Total:</span>
                                </div>
                            </div>

                            <h4 class="text-danger">
                                <span id="totalPrice">0 VND</span>
                            </h4>

                            <button type="button" class="btn btn-shopee w-100 mt-3" onclick="proceedToCheckout()">
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
function saveCheckedItems() {
    let checked = [];

    document.querySelectorAll("input[name='selectedItems']:checked")
        .forEach(cb => checked.push(cb.value));

    localStorage.setItem("checkedItems", JSON.stringify(checked));
    return checked;
}

function updateQty(id, qty) {
    saveCheckedItems();

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

function toggleSelectAll() {
    const selectAllCheckbox = document.getElementById('selectAll');
    const itemCheckboxes = document.querySelectorAll("input[name='selectedItems']");
    
    itemCheckboxes.forEach(cb => {
        cb.checked = selectAllCheckbox.checked;
    });
    
    calculateTotal();
    updateSelectedCount();
}

function updateSelectAllState() {
    const selectAllCheckbox = document.getElementById('selectAll');
    const itemCheckboxes = document.querySelectorAll("input[name='selectedItems']");
    const checkedCount = document.querySelectorAll("input[name='selectedItems']:checked").length;
    
    selectAllCheckbox.checked = itemCheckboxes.length > 0 && checkedCount === itemCheckboxes.length;
    selectAllCheckbox.indeterminate = checkedCount > 0 && checkedCount < itemCheckboxes.length;
    
    updateSelectedCount();
}

function updateSelectedCount() {
    const count = document.querySelectorAll("input[name='selectedItems']:checked").length;
    document.getElementById('selectedCount').textContent = count;
}

window.onload = function () {
    let checked = JSON.parse(localStorage.getItem("checkedItems") || "[]");

    document.querySelectorAll("input[name='selectedItems']").forEach(cb => {
        if (checked.includes(cb.value)) {
            cb.checked = true;
        }
    });

    updateSelectAllState();
    calculateTotal();

    // Listen for individual checkbox changes
    document.querySelectorAll("input[name='selectedItems']").forEach(cb => {
        cb.addEventListener('change', function() {
            updateSelectAllState();
            calculateTotal();
        });
    });
};

function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN').format(amount);
}

function calculateTotal() {
    let total = 0;

    document.querySelectorAll('input[name="selectedItems"]:checked')
        .forEach(cb => {
            total += Number(cb.dataset.subtotal);
        });

    document.getElementById("totalPrice").innerHTML =
        formatCurrency(total) + " VND";
    
    updateSelectedCount();
}

function proceedToCheckout() {
    let selected = [];
    document.querySelectorAll("input[name='selectedItems']:checked")
        .forEach(cb => selected.push(cb.value));

    if (selected.length === 0) {
        alert('Please select at least one product to checkout.');
        return;
    }

    // Set hidden field with comma-separated IDs
    document.getElementById("selectedItemsList").value = selected.join(",");
    document.getElementById("checkoutForm").submit();
}
</script>

</body>
</html>
