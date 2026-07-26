<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="checkoutItems" value="${not empty sessionScope.checkoutCart ? sessionScope.checkoutCart : sessionScope.cart}" />
<c:set var="checkoutTotalValue" value="${0}" />
<c:forEach var="item" items="${checkoutItems}">
    <c:set var="checkoutTotalValue" value="${checkoutTotalValue + (item.unitPrice * item.quantity)}" />
</c:forEach>

<section class="customer-order-page">
    <div class="co-container">
        <div class="co-page-head">
            <div>
                <a class="co-back-link" href="${pageContext.request.contextPath}/cart">
                    <span class="material-symbols-outlined">arrow_back</span>
                    Back to cart
                </a>
                <p class="co-eyebrow">Checkout</p>
                <h1 class="co-page-title">Delivery Information</h1>
                <p class="co-page-subtitle">Enter delivery details and review the products selected from your cart.</p>
            </div>
            <a class="co-secondary-btn" href="${pageContext.request.contextPath}/customer/order-history">
                <span class="material-symbols-outlined">receipt_long</span>
                My Orders
            </a>
        </div>

        <c:choose>
            <c:when test="${empty checkoutItems}">
                <div class="co-empty-card">
                    <span class="material-symbols-outlined">remove_shopping_cart</span>
                    <h2>No products selected</h2>
                    <p>Select one or more products from the cart before checkout.</p>
                    <a class="co-primary-btn" href="${pageContext.request.contextPath}/cart">Return to cart</a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="co-detail-grid">
                    <div class="co-detail-main">
                        <section class="co-card">
                            <div class="co-card-head">
                                <div>
                                    <h2>Shipping information</h2>
                                    <p>These details will be used for delivery.</p>
                                </div>
                                <span class="material-symbols-outlined co-card-head-icon">local_shipping</span>
                            </div>

                            <form class="co-form" action="${pageContext.request.contextPath}/customer/order-review" method="post">
                                <div class="co-form-group">
                                    <label for="shippingAddress">Shipping address</label>
                                    <textarea id="shippingAddress"
                                              name="shippingAddress"
                                              class="co-input co-textarea"
                                              placeholder="Enter your full delivery address"
                                              required><c:out value="${not empty param.shippingAddress ? param.shippingAddress : shippingAddress}" /></textarea>
                                </div>

                                <div class="co-form-group">
                                    <label for="phone">Phone number</label>
                                    <input id="phone"
                                           name="phone"
                                           class="co-input"
                                           type="tel"
                                           value="<c:out value='${not empty param.phone ? param.phone : phone}' />"
                                           placeholder="Example: 0912345678"
                                           required />
                                </div>

                                <button class="co-primary-btn co-full-btn" type="submit">
                                    Review order
                                    <span class="material-symbols-outlined">arrow_forward</span>
                                </button>
                            </form>
                        </section>
                    </div>

                    <aside class="co-detail-side">
                        <section class="co-card co-sticky-card">
                            <div class="co-card-head">
                                <div>
                                    <h2>Order summary</h2>
                                    <p>${fn:length(checkoutItems)} product(s)</p>
                                </div>
                                <span class="material-symbols-outlined co-card-head-icon">shopping_bag</span>
                            </div>

                            <div class="co-form">
                                <div class="co-payment-summary">
                                    <c:forEach var="item" items="${checkoutItems}">
                                        <div>
                                            <span><c:out value="${item.productName}" /> × ${item.quantity}</span>
                                            <strong><fmt:formatNumber value="${item.unitPrice * item.quantity}" type="number" groupingUsed="true" /> VND</strong>
                                        </div>
                                    </c:forEach>
                                    <div>
                                        <span>Total</span>
                                        <strong><fmt:formatNumber value="${checkoutTotalValue}" type="number" groupingUsed="true" /> VND</strong>
                                    </div>
                                </div>
                            </div>
                        </section>
                    </aside>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</section>
