<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<section class="customer-order-page">
    <div class="co-container">
        <div class="co-page-head">
            <div>
                <a class="co-back-link" href="${pageContext.request.contextPath}/cart">
                    <span class="material-symbols-outlined">arrow_back</span>
                    Back to cart
                </a>
                <p class="co-eyebrow">Checkout</p>
                <h1 class="co-page-title">Order Review</h1>
                <p class="co-page-subtitle">Review delivery information and selected products before continuing.</p>
            </div>
            <a class="co-secondary-btn" href="${pageContext.request.contextPath}/customer/order-history">
                <span class="material-symbols-outlined">receipt_long</span>
                My Orders
            </a>
        </div>

        <c:choose>
            <c:when test="${empty orderPreview}">
                <div class="co-empty-card">
                    <span class="material-symbols-outlined">error</span>
                    <h2>Review information unavailable</h2>
                    <p>Return to the cart and restart checkout.</p>
                    <a class="co-primary-btn" href="${pageContext.request.contextPath}/cart">Return to cart</a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="co-detail-grid">
                    <div class="co-detail-main">
                        <section class="co-card">
                            <div class="co-card-head">
                                <div>
                                    <h2>Delivery information</h2>
                                    <p>Information used to deliver this order.</p>
                                </div>
                                <span class="material-symbols-outlined co-card-head-icon">local_shipping</span>
                            </div>
                            <div class="co-summary-grid" style="padding: 22px;">
                                <div class="co-summary-item">
                                    <span>Phone number</span>
                                    <strong><c:out value="${orderPreview.phone}" /></strong>
                                </div>
                                <div class="co-summary-item co-summary-address">
                                    <span>Shipping address</span>
                                    <strong><c:out value="${orderPreview.shippingAddress}" /></strong>
                                </div>
                                <div class="co-summary-item co-summary-total">
                                    <span>Order total</span>
                                    <strong><fmt:formatNumber value="${orderPreview.totalAmount}" type="number" groupingUsed="true" /> VND</strong>
                                </div>
                            </div>
                        </section>

                        <section class="co-card">
                            <div class="co-card-head">
                                <div>
                                    <h2>Selected products</h2>
                                    <p>${fn:length(cart)} product(s)</p>
                                </div>
                            </div>
                            <div class="co-table-wrap">
                                <table class="co-table co-items-table">
                                    <thead>
                                        <tr>
                                            <th>Product</th>
                                            <th>Variant</th>
                                            <th>Quantity</th>
                                            <th>Unit price</th>
                                            <th>Subtotal</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="item" items="${cart}">
                                            <tr>
                                                <td>
                                                    <div class="co-product-cell">
                                                        <span class="co-product-icon material-symbols-outlined">checkroom</span>
                                                        <div>
                                                            <strong><c:out value="${item.productName}" /></strong>
                                                            <span>${item.sizeName}<c:if test="${not empty item.colorName}"> / ${item.colorName}</c:if></span>
                                                        </div>
                                                    </div>
                                                </td>
                                                <td>${item.variantId}</td>
                                                <td>${item.quantity}</td>
                                                <td><fmt:formatNumber value="${item.unitPrice}" type="number" groupingUsed="true" /> VND</td>
                                                <td>
                                                    <strong class="co-money">
                                                        <fmt:formatNumber value="${item.unitPrice * item.quantity}" type="number" groupingUsed="true" /> VND
                                                    </strong>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </section>
                    </div>

                    <aside class="co-detail-side">
                        <section class="co-card co-sticky-card">
                            <div class="co-card-head">
                                <div>
                                    <h2>Order summary</h2>
                                    <p>Pending order preview</p>
                                </div>
                                <span class="material-symbols-outlined co-card-head-icon">fact_check</span>
                            </div>
                            <div class="co-form">
                                <div class="co-payment-summary">
                                    <div>
                                        <span>Products</span>
                                        <strong>${fn:length(cart)}</strong>
                                    </div>
                                    <div>
                                        <span>Status</span>
                                        <strong>${orderPreview.orderStatus}</strong>
                                    </div>
                                    <div>
                                        <span>Total</span>
                                        <strong><fmt:formatNumber value="${orderPreview.totalAmount}" type="number" groupingUsed="true" /> VND</strong>
                                    </div>
                                </div>
                                <a class="co-primary-btn co-full-btn" href="${pageContext.request.contextPath}/cart">
                                    Continue from cart
                                </a>
                            </div>
                        </section>
                    </aside>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</section>
