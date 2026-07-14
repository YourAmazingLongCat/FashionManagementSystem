<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:if test="${empty requestScope.contentPage}">
    <c:redirect url="${pageContext.request.contextPath}/customer/order-history" />
</c:if>

<section class="wallet-page">
    <div class="wallet-hero">
        <div>
            <p class="wallet-breadcrumb">Customer / Order Detail</p>
            <h1 class="wallet-title">Order Detail</h1>
            <p class="wallet-subtitle">Review your order and pay with wallet balance.</p>
        </div>
        <a class="wallet-outline-btn" href="${pageContext.request.contextPath}/customer/order-history">
            <span class="material-symbols-outlined">arrow_back</span>
            Back to orders
        </a>
    </div>

    <c:if test="${not empty sessionScope.successMessage}">
        <div class="wallet-alert wallet-alert-success">${sessionScope.successMessage}</div>
        <c:remove var="successMessage" scope="session" />
    </c:if>

    <c:if test="${not empty sessionScope.errorMessage}">
        <div class="wallet-alert wallet-alert-error">${sessionScope.errorMessage}</div>
        <c:remove var="errorMessage" scope="session" />
    </c:if>

    <c:choose>
        <c:when test="${empty order}">
            <div class="wallet-empty">
                <span class="material-symbols-outlined">error</span>
                <h3>Order not found</h3>
                <p>${errorMessage}</p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="wallet-order-grid">
                <div class="wallet-history-card">
                    <div class="wallet-section-head">
                        <h2>${order.orderId}</h2>
                        <span class="payment-status payment-status-${fn:toLowerCase(order.orderStatus)}">${order.orderStatus}</span>
                    </div>

                    <div class="wallet-info-list">
                        <div><span>Shipping Address</span><strong>${order.shippingAddress}</strong></div>
                        <div><span>Phone</span><strong>${order.phone}</strong></div>
                        <div><span>Placed At</span><strong>${order.placedAt}</strong></div>
                        <div>
                            <span>Total Amount</span>
                            <strong><fmt:formatNumber value="${order.totalAmount}" type="number" groupingUsed="true" /> VND</strong>
                        </div>
                    </div>

                    <div class="wallet-table-wrap wallet-table-space">
                        <table class="wallet-table">
                            <thead>
                                <tr>
                                    <th>Variant</th>
                                    <th>Quantity</th>
                                    <th>Unit Price</th>
                                    <th>Discount</th>
                                    <th>Subtotal</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="item" items="${orderItems}">
                                    <tr>
                                        <td><strong>${item.variantId}</strong></td>
                                        <td>${item.quantity}</td>
                                        <td><fmt:formatNumber value="${item.unitPrice}" type="number" groupingUsed="true" /> VND</td>
                                        <td><fmt:formatNumber value="${item.discountAmount}" type="number" groupingUsed="true" /> VND</td>
                                        <td class="wallet-money"><fmt:formatNumber value="${item.subTotal}" type="number" groupingUsed="true" /> VND</td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>

                <aside class="wallet-payment-panel">
                    <div class="wallet-form-head">
                        <span class="material-symbols-outlined">account_balance_wallet</span>
                        <div>
                            <h2>Wallet Payment</h2>
                            <p>Use your wallet balance to pay this order.</p>
                        </div>
                    </div>

                    <div class="wallet-payment-row">
                        <span>Payment Status</span>
                        <c:choose>
                            <c:when test="${not empty payment}">
                                <strong class="payment-status payment-status-${fn:toLowerCase(payment.paymentStatus)}">${payment.paymentStatus}</strong>
                            </c:when>
                            <c:otherwise>
                                <strong class="payment-status payment-status-pending">Unpaid</strong>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <div class="wallet-payment-row">
                        <span>Amount</span>
                        <strong><fmt:formatNumber value="${order.totalAmount}" type="number" groupingUsed="true" /> VND</strong>
                    </div>

                    <c:choose>
                        <c:when test="${not empty payment && payment.paymentStatus == 'Paid'}">
                            <a class="wallet-outline-btn wallet-full-btn" href="${pageContext.request.contextPath}/customer/wallet">
                                View Wallet History
                            </a>
                        </c:when>
                        <c:otherwise>
                            <form action="${pageContext.request.contextPath}/customer/pay-with-wallet" method="post">
                                <input type="hidden" name="orderId" value="${order.orderId}" />
                                <button class="wallet-primary-btn wallet-full-btn" type="submit">
                                    <span class="material-symbols-outlined">wallet</span>
                                    Pay With Wallet
                                </button>
                            </form>
                            <a class="wallet-outline-btn wallet-full-btn" href="${pageContext.request.contextPath}/customer/wallet">
                                Deposit More Money
                            </a>
                        </c:otherwise>
                    </c:choose>
                </aside>
            </div>
        </c:otherwise>
    </c:choose>
</section>
