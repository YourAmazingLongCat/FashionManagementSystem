<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="content-page order-page">
    <div class="order-container">
        <section class="order-hero">
            <div>
                <p class="order-eyebrow">Customer / Checkout</p>
                <h1 class="order-title">Complete Order</h1>
                <p class="order-subtitle">
                    Confirm your shipping information and review your cart before placing the order.
                </p>
            </div>
            <div class="order-actions-row">
                <a class="order-btn" href="${pageContext.request.contextPath}/cart">
                    <span class="material-symbols-outlined">shopping_bag</span>
                    Back to cart
                </a>
            </div>
        </section>

        <div class="order-grid order-grid-2">
            <section class="order-panel order-panel-padding">
                <div class="order-panel-header">
                    <h2 class="order-section-title">Shipping Information</h2>
                    <span class="order-muted">Required</span>
                </div>

                <form action="<%= request.getContextPath() %>/customer/order-review" method="post">
                    <div class="order-form-group">
                        <label class="order-label" for="shippingAddress">Shipping address</label>
                        <textarea id="shippingAddress" name="shippingAddress" class="order-textarea" placeholder="Enter your full address..." required>${param.shippingAddress}</textarea>
                    </div>

                    <div class="order-form-group">
                        <label class="order-label" for="phone">Phone number</label>
                        <input id="phone" name="phone" class="order-input" type="tel" value="${param.phone}" placeholder="Example: 0912345678" required />
                    </div>

                    <button class="order-btn order-btn-primary" type="submit" style="width: 100%;">
                        <span class="material-symbols-outlined">lock</span>
                        Place order
                    </button>
                </form>
            </section>

            <aside class="order-panel order-panel-padding">
                <div class="order-panel-header">
                    <h2 class="order-section-title">Order Summary</h2>
                    <span class="order-muted">${empty sessionScope.cart ? 0 : sessionScope.cart.size()} items</span>
                </div>

                <c:set var="checkoutTotal" value="${0}" />
                <c:choose>
                    <c:when test="${empty sessionScope.cart}">
                        <div class="order-empty" style="padding: 40px 10px;">
                            <span class="material-symbols-outlined">remove_shopping_cart</span>
                            <h3>Your cart is empty</h3>
                            <p>Add products before checkout.</p>
                            <a class="order-btn order-btn-primary" href="${pageContext.request.contextPath}/home">Continue shopping</a>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="order-summary-list">
                            <c:forEach var="item" items="${sessionScope.cart}">
                                <c:set var="lineTotal" value="${item.unitPrice * item.quantity}" />
                                <c:set var="checkoutTotal" value="${checkoutTotal + lineTotal}" />

                                <div class="order-summary-item">
                                    <div class="order-thumb">PR</div>
                                    <div>
                                        <div class="order-item-name">Variant ${item.variantId}</div>
                                        <div class="order-muted">Qty ${item.quantity} × <fmt:formatNumber value="${item.unitPrice}" type="number" groupingUsed="true" /> đ</div>
                                    </div>
                                    <div class="order-price"><fmt:formatNumber value="${lineTotal}" type="number" groupingUsed="true" /> đ</div>
                                </div>
                            </c:forEach>
                        </div>

                        <div class="order-total-box">
                            <div class="order-total-row">
                                <span>Subtotal</span>
                                <strong><fmt:formatNumber value="${checkoutTotal}" type="number" groupingUsed="true" /> đ</strong>
                            </div>
                            <div class="order-total-row">
                                <span>Shipping</span>
                                <strong>Free</strong>
                            </div>
                            <div class="order-total-row final">
                                <span>Total</span>
                                <span><fmt:formatNumber value="${checkoutTotal}" type="number" groupingUsed="true" /> đ</span>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </aside>
        </div>
    </div>
</div>
