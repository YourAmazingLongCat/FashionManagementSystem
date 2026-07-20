<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:if test="${empty requestScope.contentPage}">
    <c:redirect url="${pageContext.request.contextPath}/customer/checkout" />
</c:if>

<div class="content-page order-page">
    <div class="order-container">
        <section class="order-hero">
            <div>
                <p class="order-eyebrow">Customer / Checkout</p>
                <h1 class="order-title">Complete Order</h1>
                <p class="order-subtitle">
                    Confirm your shipping information, choose a payment method, and review your cart before placing the order.
                </p>
            </div>
            <div class="order-actions-row">
                <a class="order-btn" href="${pageContext.request.contextPath}/cart">
                    <span class="material-symbols-outlined">shopping_bag</span>
                    Back to cart
                </a>
            </div>
        </section>

        <c:set var="checkoutTotal" value="${0}" />
        <c:forEach var="item" items="${sessionScope.cart}">
            <c:set var="lineTotal" value="${item.unitPrice * item.quantity}" />
            <c:set var="checkoutTotal" value="${checkoutTotal + lineTotal}" />
        </c:forEach>

        <div class="order-grid order-grid-2">
            <section class="order-panel order-panel-padding">
                <div class="order-panel-header">
                    <h2 class="order-section-title">Checkout Information</h2>
                    <span class="order-muted">Required</span>
                </div>

                <form action="${pageContext.request.contextPath}/customer/checkout" method="post">
                    <div class="order-form-group">
                        <label class="order-label" for="shippingAddress">Shipping address</label>
                        <textarea id="shippingAddress" name="shippingAddress" class="order-textarea" placeholder="Enter your full address..." required>${param.shippingAddress}</textarea>
                    </div>

                    <div class="order-form-group">
                        <label class="order-label" for="phone">Phone number</label>
                        <input id="phone" name="phone" class="order-input" type="tel" value="${param.phone}" placeholder="Example: 0912345678" required />
                    </div>

                    <div class="wallet-deposit-card wallet-checkout-payment-box" style="box-shadow: none; margin: 18px 0;">
                        <div class="wallet-form-head">
                            <span class="material-symbols-outlined">payments</span>
                            <div>
                                <h2>Payment Method</h2>
                                <p>Choose VNPay, Wallet or Cash On Delivery.</p>
                            </div>
                        </div>

                        <label class="wallet-label" for="paymentMethod">Payment Method</label>
                        <select class="wallet-input" id="paymentMethod" name="paymentMethod">
                            <option value="VNPay">VNPay</option>
                            <option value="Wallet">Wallet</option>
                            <option value="COD" selected>Cash On Delivery</option>
                        </select>

                        <div class="wallet-info-list">
                            <div>
                                <span>Wallet Balance</span>
                                <strong>
                                    <c:choose>
                                        <c:when test="${not empty wallet}">
                                            <fmt:formatNumber value="${wallet.balance}" type="number" groupingUsed="true" /> VND
                                        </c:when>
                                        <c:otherwise>0 VND</c:otherwise>
                                    </c:choose>
                                </strong>
                            </div>
                            <div>
                                <span>Order Total</span>
                                <strong><fmt:formatNumber value="${checkoutTotal}" type="number" groupingUsed="true" /> VND</strong>
                            </div>
                        </div>
                    </div>

                    <button class="order-btn order-btn-primary" type="submit" style="width: 100%;" <c:if test="${empty sessionScope.cart}">disabled</c:if>>
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

                                <div class="order-summary-item">
                                    <div class="order-thumb">
                                        <c:choose>
                                            <c:when test="${not empty item.productImageUrl}">
                                                <img src="${pageContext.request.contextPath}${item.productImageUrl}" alt="${item.productName}" />
                                            </c:when>
                                            <c:otherwise>PR</c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div>
                                        <div class="order-item-name">
                                            <c:choose>
                                                <c:when test="${not empty item.productName}">${item.productName}</c:when>
                                                <c:otherwise>Variant ${item.variantId}</c:otherwise>
                                            </c:choose>
                                        </div>
                                        <div class="order-muted">
                                            ${item.sizeName}<c:if test="${not empty item.colorName}"> / ${item.colorName}</c:if>
                                            · Qty ${item.quantity} × <fmt:formatNumber value="${item.unitPrice}" type="number" groupingUsed="true" /> VND
                                        </div>
                                    </div>
                                    <div class="order-price"><fmt:formatNumber value="${lineTotal}" type="number" groupingUsed="true" /> VND</div>
                                </div>
                            </c:forEach>
                        </div>

                        <div class="order-total-box">
                            <div class="order-total-row">
                                <span>Subtotal</span>
                                <strong><fmt:formatNumber value="${checkoutTotal}" type="number" groupingUsed="true" /> VND</strong>
                            </div>
                            <div class="order-total-row">
                                <span>Shipping</span>
                                <strong>Free</strong>
                            </div>
                            <div class="order-total-row final">
                                <span>Total</span>
                                <span><fmt:formatNumber value="${checkoutTotal}" type="number" groupingUsed="true" /> VND</span>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </aside>
        </div>
    </div>
</div>
