<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:if test="${empty requestScope.contentPage}">
    <c:redirect url="${pageContext.request.contextPath}/customer/order-history" />
</c:if>

<section class="customer-order-page">
    <div class="co-container">
        <div class="co-page-head co-detail-head">
            <div>
                <a class="co-back-link" href="${pageContext.request.contextPath}/customer/order-history">
                    <span class="material-symbols-outlined">arrow_back</span>
                    Back to my orders
                </a>
                <p class="co-eyebrow">Order details</p>
                <h1 class="co-page-title">
                    <c:choose>
                        <c:when test="${not empty order and not orderPlaced and order.orderStatus eq 'Pending'}">Complete your order</c:when>
                        <c:otherwise>Order #${order.orderId}</c:otherwise>
                    </c:choose>
                </h1>
                <p class="co-page-subtitle">
                    <c:choose>
                        <c:when test="${not empty order and not orderPlaced and order.orderStatus eq 'Pending'}">
                            Add delivery details and choose how you would like to pay.
                        </c:when>
                        <c:otherwise>
                            Review your order, payment and delivery information.
                        </c:otherwise>
                    </c:choose>
                </p>
            </div>

            <c:if test="${not empty order}">
                <span class="co-status co-status-large co-status-${fn:toLowerCase(order.orderStatus)}">
                    ${order.orderStatus}
                </span>
            </c:if>
        </div>

        <c:choose>
            <c:when test="${empty order}">
                <div class="co-empty-card">
                    <span class="material-symbols-outlined">error</span>
                    <h2>Order not found</h2>
                    <p><c:out value="${errorMessage}" /></p>
                    <a class="co-primary-btn" href="${pageContext.request.contextPath}/customer/order-history">Back to orders</a>
                </div>
            </c:when>

            <c:otherwise>
                <c:if test="${not orderPlaced and order.orderStatus eq 'Pending'}">
                    <div class="co-notice co-notice-warning">
                        <div class="co-notice-icon">
                            <span class="material-symbols-outlined">schedule</span>
                        </div>
                        <div class="co-notice-content">
                            <strong>Complete your order within</strong>
                            <span id="order-confirmation-countdown"
                                  class="co-notice-countdown"
                                  data-order-expiry="${order.confirmationExpiresAt}">Calculating...</span>
                            <p>The order will be cancelled automatically when this time ends.</p>
                        </div>
                    </div>
                </c:if>

                <c:if test="${orderPlaced and order.orderStatus eq 'Pending'}">
                    <div class="co-notice co-notice-info">
                        <div class="co-notice-icon">
                            <span class="material-symbols-outlined">hourglass_top</span>
                        </div>
                        <div class="co-notice-content">
                            <strong>Your order is waiting for confirmation</strong>
                            <p>We have received your order and will update its status after it is reviewed.</p>
                        </div>
                    </div>
                </c:if>

                <div class="co-progress-card">
                    <c:choose>
                        <c:when test="${order.orderStatus eq 'Cancelled'}">
                            <div class="co-cancelled-message">
                                <span class="material-symbols-outlined">cancel</span>
                                This order has been cancelled.
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="co-progress co-progress-${fn:toLowerCase(order.orderStatus)}">
                                <div class="co-progress-step">
                                    <span class="co-progress-icon"><span class="material-symbols-outlined">receipt_long</span></span>
                                    <span>Pending</span>
                                </div>
                                <div class="co-progress-line"></div>
                                <div class="co-progress-step">
                                    <span class="co-progress-icon"><span class="material-symbols-outlined">task_alt</span></span>
                                    <span>Confirmed</span>
                                </div>
                                <div class="co-progress-line"></div>
                                <div class="co-progress-step">
                                    <span class="co-progress-icon"><span class="material-symbols-outlined">inventory_2</span></span>
                                    <span>Processing</span>
                                </div>
                                <div class="co-progress-line"></div>
                                <div class="co-progress-step">
                                    <span class="co-progress-icon"><span class="material-symbols-outlined">local_shipping</span></span>
                                    <span>Shipping</span>
                                </div>
                                <div class="co-progress-line"></div>
                                <div class="co-progress-step">
                                    <span class="co-progress-icon"><span class="material-symbols-outlined">home</span></span>
                                    <span>Delivered</span>
                                </div>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="co-detail-grid">
                    <div class="co-detail-main">
                        <div class="co-card">
                            <div class="co-card-head">
                                <div>
                                    <h2>Order summary</h2>
                                    <p>Order #${order.orderId}</p>
                                </div>
                                <span class="co-date-chip">
                                    <span class="material-symbols-outlined">calendar_today</span>
                                    ${order.placedAt}
                                </span>
                            </div>

                            <div class="co-summary-grid">
                                <div class="co-summary-item">
                                    <span>Phone number</span>
                                    <strong>
                                        <c:choose>
                                            <c:when test="${not empty order.phone}"><c:out value="${order.phone}" /></c:when>
                                            <c:otherwise>Not provided</c:otherwise>
                                        </c:choose>
                                    </strong>
                                </div>
                                <div class="co-summary-item co-summary-address">
                                    <span>Shipping address</span>
                                    <strong>
                                        <c:choose>
                                            <c:when test="${not empty order.shippingAddress}"><c:out value="${order.shippingAddress}" /></c:when>
                                            <c:otherwise>Not provided</c:otherwise>
                                        </c:choose>
                                    </strong>
                                </div>
                                <div class="co-summary-item co-summary-total">
                                    <span>Order total</span>
                                    <strong><fmt:formatNumber value="${order.totalAmount}" type="number" groupingUsed="true" /> VND</strong>
                                </div>
                            </div>
                        </div>

                        <div class="co-card">
                            <div class="co-card-head">
                                <div>
                                    <h2>Items</h2>
                                    <p>${fn:length(orderItems)} product(s)</p>
                                </div>
                            </div>

                            <c:choose>
                                <c:when test="${empty orderItems}">
                                    <div class="co-inline-empty">
                                        <span class="material-symbols-outlined">inventory_2</span>
                                        <p>No products were found for this order.</p>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="co-table-wrap co-items-table-wrap">
                                        <table class="co-table co-items-table">
                                            <thead>
                                                <tr>
                                                    <th>Product variant</th>
                                                    <th>Quantity</th>
                                                    <th>Unit price</th>
                                                    <th>Discount</th>
                                                    <th>Subtotal</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach var="item" items="${orderItems}">
                                                    <tr>
                                                        <td>
                                                            <div class="co-product-cell">
                                                                <span class="co-product-icon material-symbols-outlined">devices</span>
                                                                <div>
                                                                    <strong>${item.variantId}</strong>
                                                                    <span>Product variant</span>
                                                                </div>
                                                            </div>
                                                        </td>
                                                        <td>${item.quantity}</td>
                                                        <td><fmt:formatNumber value="${item.unitPrice}" type="number" groupingUsed="true" /> VND</td>
                                                        <td><fmt:formatNumber value="${item.discountAmount}" type="number" groupingUsed="true" /> VND</td>
                                                        <td><strong class="co-money"><fmt:formatNumber value="${item.subTotal}" type="number" groupingUsed="true" /> VND</strong></td>
                                                    </tr>
                                                </c:forEach>
                                            </tbody>
                                        </table>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <aside class="co-detail-side">
                        <c:choose>
                            <c:when test="${not orderPlaced and order.orderStatus eq 'Pending'}">
                                <div class="co-card co-sticky-card">
                                    <div class="co-card-head">
                                        <div>
                                            <h2>Place order</h2>
                                            <p>Complete your delivery and payment information.</p>
                                        </div>
                                        <span class="material-symbols-outlined co-card-head-icon">shopping_bag</span>
                                    </div>

                                    <form class="co-form" action="${pageContext.request.contextPath}/customer/order-detail" method="post">
                                        <input type="hidden" name="action" value="placeOrder" />
                                        <input type="hidden" name="orderId" value="${order.orderId}" />

                                        <div class="co-form-group">
                                            <label for="shippingAddress">Shipping address</label>
                                            <textarea id="shippingAddress"
                                                      name="shippingAddress"
                                                      class="co-input co-textarea"
                                                      placeholder="Enter your full delivery address"
                                                      required><c:out value="${order.shippingAddress}" /></textarea>
                                        </div>

                                        <div class="co-form-group">
                                            <label for="phone">Phone number</label>
                                            <input id="phone"
                                                   name="phone"
                                                   class="co-input"
                                                   type="tel"
                                                   value="<c:out value='${order.phone}' />"
                                                   placeholder="Example: 0912345678"
                                                   required />
                                        </div>

                                        <div class="co-form-group">
                                            <label for="paymentMethod">Payment method</label>
                                            <select id="paymentMethod" name="paymentMethod" class="co-input co-select">
                                                <option value="COD" selected>Cash On Delivery</option>
                                                <option value="Wallet">Wallet</option>
                                                <option value="VNPay">VNPay</option>
                                            </select>
                                        </div>

                                        <div class="co-payment-summary">
                                            <div>
                                                <span>Order amount</span>
                                                <strong><fmt:formatNumber value="${order.totalAmount}" type="number" groupingUsed="true" /> VND</strong>
                                            </div>
                                            <div>
                                                <span>Wallet balance</span>
                                                <strong>
                                                    <c:choose>
                                                        <c:when test="${not empty wallet}"><fmt:formatNumber value="${wallet.balance}" type="number" groupingUsed="true" /> VND</c:when>
                                                        <c:otherwise>0 VND</c:otherwise>
                                                    </c:choose>
                                                </strong>
                                            </div>
                                        </div>

                                        <button class="co-primary-btn co-full-btn" type="submit">
                                            Place order
                                            <span class="material-symbols-outlined">arrow_forward</span>
                                        </button>
                                    </form>

                                    <a class="co-secondary-btn co-full-btn" href="${pageContext.request.contextPath}/customer/wallet">
                                        <span class="material-symbols-outlined">account_balance_wallet</span>
                                        Add money to wallet
                                    </a>
                                </div>
                            </c:when>

                            <c:otherwise>
                                <div class="co-card co-sticky-card">
                                    <div class="co-card-head">
                                        <div>
                                            <h2>Payment</h2>
                                            <p>Payment information for this order.</p>
                                        </div>
                                        <span class="material-symbols-outlined co-card-head-icon">payments</span>
                                    </div>

                                    <div class="co-info-list">
                                        <div>
                                            <span>Payment method</span>
                                            <strong>
                                                <c:choose>
                                                    <c:when test="${not empty payment and payment.paymentMethod eq 'COD'}">Cash On Delivery</c:when>
                                                    <c:when test="${not empty payment}">${payment.paymentMethod}</c:when>
                                                    <c:otherwise>Not selected</c:otherwise>
                                                </c:choose>
                                            </strong>
                                        </div>
                                        <div>
                                            <span>Payment status</span>
                                            <strong>
                                                <c:choose>
                                                    <c:when test="${not empty payment}">
                                                        <span class="co-payment-status co-payment-${fn:toLowerCase(payment.paymentStatus)}">${payment.paymentStatus}</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="co-payment-status co-payment-pending">Unpaid</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </strong>
                                        </div>
                                        <div>
                                            <span>Amount</span>
                                            <strong>
                                                <c:choose>
                                                    <c:when test="${not empty payment}"><fmt:formatNumber value="${payment.amount}" type="number" groupingUsed="true" /> VND</c:when>
                                                    <c:otherwise><fmt:formatNumber value="${order.totalAmount}" type="number" groupingUsed="true" /> VND</c:otherwise>
                                                </c:choose>
                                            </strong>
                                        </div>
                                    </div>

                                    <c:choose>
                                        <c:when test="${not empty payment and payment.paymentStatus eq 'Paid'}">
                                            <div class="co-mini-message co-mini-success">Payment completed successfully.</div>
                                        </c:when>
                                        <c:when test="${not empty payment and payment.paymentMethod eq 'COD'}">
                                            <div class="co-mini-message co-mini-info">Payment will be collected when your order is delivered.</div>
                                        </c:when>
                                        <c:when test="${not empty payment and payment.paymentMethod eq 'VNPay' and payment.paymentStatus eq 'Pending'}">
                                            <div class="co-mini-message co-mini-info">Your VNPay payment is being processed.</div>
                                        </c:when>
                                    </c:choose>
                                </div>

                                <div class="co-card">
                                    <div class="co-card-head">
                                        <div>
                                            <h2>Delivery information</h2>
                                            <p>
                                                <c:choose>
                                                    <c:when test="${canEditDelivery}">You can update this before the order is shipped.</c:when>
                                                    <c:otherwise>This information can no longer be changed.</c:otherwise>
                                                </c:choose>
                                            </p>
                                        </div>
                                        <span class="material-symbols-outlined co-card-head-icon">local_shipping</span>
                                    </div>

                                    <c:choose>
                                        <c:when test="${canEditDelivery}">
                                            <form class="co-form" action="${pageContext.request.contextPath}/customer/order-detail" method="post">
                                                <input type="hidden" name="action" value="updateShipping" />
                                                <input type="hidden" name="orderId" value="${order.orderId}" />

                                                <div class="co-form-group">
                                                    <label for="updateShippingAddress">Shipping address</label>
                                                    <textarea id="updateShippingAddress"
                                                              name="shippingAddress"
                                                              class="co-input co-textarea"
                                                              required><c:out value="${order.shippingAddress}" /></textarea>
                                                </div>

                                                <div class="co-form-group">
                                                    <label for="updatePhone">Phone number</label>
                                                    <input id="updatePhone"
                                                           name="phone"
                                                           class="co-input"
                                                           type="tel"
                                                           value="<c:out value='${order.phone}' />"
                                                           required />
                                                </div>

                                                <button class="co-secondary-btn co-full-btn" type="submit">
                                                    <span class="material-symbols-outlined">save</span>
                                                    Save changes
                                                </button>
                                            </form>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="co-info-list">
                                                <div>
                                                    <span>Shipping address</span>
                                                    <strong><c:out value="${order.shippingAddress}" /></strong>
                                                </div>
                                                <div>
                                                    <span>Phone number</span>
                                                    <strong><c:out value="${order.phone}" /></strong>
                                                </div>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </aside>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</section>

<script>
(function () {
    var countdown = document.getElementById('order-confirmation-countdown');
    if (!countdown) {
        return;
    }

    function updateCountdown() {
        var expiryText = countdown.getAttribute('data-order-expiry');
        var expiryTime = new Date(expiryText).getTime();
        var remaining = expiryTime - Date.now();

        if (isNaN(expiryTime)) {
            countdown.textContent = 'Time unavailable';
            return;
        }

        if (remaining <= 0) {
            countdown.textContent = 'Expired';
            return;
        }

        var totalSeconds = Math.floor(remaining / 1000);
        var days = Math.floor(totalSeconds / 86400);
        var hours = Math.floor((totalSeconds % 86400) / 3600);
        var minutes = Math.floor((totalSeconds % 3600) / 60);
        var seconds = totalSeconds % 60;

        countdown.textContent = days + 'd ' + hours + 'h ' + minutes + 'm ' + seconds + 's';
    }

    updateCountdown();
    window.setInterval(updateCountdown, 1000);
})();
</script>
