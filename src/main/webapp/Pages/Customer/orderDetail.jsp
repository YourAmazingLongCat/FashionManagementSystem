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
            <p class="wallet-subtitle">Review your order, payment status and shipping progress.</p>
        </div>
        <a class="wallet-outline-btn" href="${pageContext.request.contextPath}/customer/order-history">
            <span class="material-symbols-outlined">arrow_back</span>
            Back to orders
        </a>
    </div>

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
                        <span class="material-symbols-outlined">payments</span>
                        <div>
                            <h2>Payment</h2>
                            <p>You can pay by wallet or cash on delivery.</p>
                        </div>
                    </div>

                    <div class="wallet-payment-row">
                        <span>Payment Method</span>
                        <strong>
                            <c:choose>
                                <c:when test="${not empty payment}">${payment.paymentMethod}</c:when>
                                <c:otherwise>Not selected</c:otherwise>
                            </c:choose>
                        </strong>
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

                    <c:if test="${not empty wallet}">
                        <div class="wallet-payment-row">
                            <span>Wallet Balance</span>
                            <strong><fmt:formatNumber value="${wallet.balance}" type="number" groupingUsed="true" /> VND</strong>
                        </div>
                    </c:if>

                    <c:choose>
                        <c:when test="${not empty payment && payment.paymentStatus eq 'Paid'}">
                            <a class="wallet-outline-btn wallet-full-btn" href="${pageContext.request.contextPath}/customer/wallet">
                                View Wallet History
                            </a>
                        </c:when>
                        <c:when test="${not empty payment && payment.paymentMethod eq 'Cash'}">
                            <div class="wallet-alert wallet-alert-success" style="margin-top: 12px;">
                                COD selected. Please pay when the order is delivered.
                            </div>
                        </c:when>
                        <c:when test="${order.orderStatus eq 'Cancelled' or order.orderStatus eq 'Delivered'}">
                            <div class="wallet-alert wallet-alert-error" style="margin-top: 12px;">
                                Payment action is not available for this order status.
                            </div>
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
