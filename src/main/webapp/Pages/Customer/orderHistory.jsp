<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<section class="customer-order-page">
    <div class="co-container">
        <div class="co-page-head">
            <div>
                <a class="co-back-link" href="${pageContext.request.contextPath}/home">
                    <span class="material-symbols-outlined">arrow_back</span>
                    Continue shopping
                </a>
                <p class="co-eyebrow">My account</p>
                <h1 class="co-page-title">My Orders</h1>
                <p class="co-page-subtitle">Track orders, review order details and complete unfinished orders.</p>
            </div>
            <a class="co-secondary-btn" href="${pageContext.request.contextPath}/customer/wallet">
                <span class="material-symbols-outlined">account_balance_wallet</span>
                My Wallet
            </a>
        </div>

        <form class="co-search-card" method="get" action="${pageContext.request.contextPath}/customer/order-history">
            <div class="co-search-field">
                <span class="material-symbols-outlined">search</span>
                <input type="text"
                       name="keyword"
                       value="<c:out value='${keyword}' />"
                       placeholder="Search by order ID, status or phone number" />
            </div>
            <button class="co-primary-btn" type="submit">Search</button>
            <c:if test="${not empty keyword}">
                <a class="co-text-btn" href="${pageContext.request.contextPath}/customer/order-history">Clear</a>
            </c:if>
        </form>

        <c:choose>
            <c:when test="${empty listOrders}">
                <div class="co-empty-card">
                    <span class="material-symbols-outlined">shopping_bag</span>
                    <h2>No orders found</h2>
                    <p>Your orders will appear here after you checkout from the cart.</p>
                    <a class="co-primary-btn" href="${pageContext.request.contextPath}/home">Start shopping</a>
                </div>
            </c:when>

            <c:otherwise>
                <div class="co-card co-orders-card">
                    <div class="co-card-head">
                        <div>
                            <h2>Order history</h2>
                            <p>${fn:length(listOrders)} order(s)</p>
                        </div>
                    </div>

                    <div class="co-table-wrap">
                        <table class="co-table">
                            <thead>
                                <tr>
                                    <th>Order</th>
                                    <th>Placed date</th>
                                    <th>Delivery</th>
                                    <th>Status</th>
                                    <th>Total</th>
                                    <th></th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="order" items="${listOrders}">
                                    <tr>
                                        <td>
                                            <div class="co-order-code">#${order.orderId}</div>
                                            <c:if test="${order.awaitingConfirmation}">
                                                <div class="co-countdown-wrap">
                                                    <span class="material-symbols-outlined">schedule</span>
                                                    <span class="co-countdown"
                                                          data-order-expiry="${order.confirmationExpiresAt}">Calculating...</span>
                                                </div>
                                            </c:if>
                                        </td>
                                        <td>
                                            <span class="co-cell-main">${order.placedAt}</span>
                                        </td>
                                        <td>
                                            <span class="co-cell-main">
                                                <c:choose>
                                                    <c:when test="${not empty order.phone}"><c:out value="${order.phone}" /></c:when>
                                                    <c:otherwise>Not provided</c:otherwise>
                                                </c:choose>
                                            </span>
                                            <span class="co-cell-sub co-address-preview">
                                                <c:choose>
                                                    <c:when test="${not empty order.shippingAddress}"><c:out value="${order.shippingAddress}" /></c:when>
                                                    <c:otherwise>Delivery information pending</c:otherwise>
                                                </c:choose>
                                            </span>
                                        </td>
                                        <td>
                                            <span class="co-status co-status-${fn:toLowerCase(order.orderStatus)}">
                                                ${order.orderStatus}
                                            </span>
                                        </td>
                                        <td>
                                            <strong class="co-money">
                                                <fmt:formatNumber value="${order.totalAmount}" type="number" groupingUsed="true" /> VND
                                            </strong>
                                        </td>
                                        <td class="co-action-cell">
                                            <a class="co-view-btn"
                                               href="${pageContext.request.contextPath}/customer/order-detail?orderId=${order.orderId}">
                                                <c:choose>
                                                    <c:when test="${order.awaitingConfirmation}">Complete order</c:when>
                                                    <c:otherwise>View details</c:otherwise>
                                                </c:choose>
                                                <span class="material-symbols-outlined">arrow_forward</span>
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</section>

<script>
(function () {
    var elements = document.querySelectorAll('[data-order-expiry]');

    function updateCountdown(element) {
        var expiryText = element.getAttribute('data-order-expiry');
        var expiryTime = new Date(expiryText).getTime();
        var remaining = expiryTime - Date.now();

        if (isNaN(expiryTime)) {
            element.textContent = 'Time unavailable';
            return;
        }

        if (remaining <= 0) {
            element.textContent = 'Expired';
            return;
        }

        var totalSeconds = Math.floor(remaining / 1000);
        var days = Math.floor(totalSeconds / 86400);
        var hours = Math.floor((totalSeconds % 86400) / 3600);
        var minutes = Math.floor((totalSeconds % 3600) / 60);
        var seconds = totalSeconds % 60;

        element.textContent = days + 'd ' + hours + 'h ' + minutes + 'm ' + seconds + 's';
    }

    function updateAll() {
        for (var i = 0; i < elements.length; i++) {
            updateCountdown(elements[i]);
        }
    }

    updateAll();
    if (elements.length > 0) {
        window.setInterval(updateAll, 1000);
    }
})();
</script>
