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
            <p class="wallet-subtitle">Review your order and shipping progress.</p>
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
                            <h2>Bill</h2>
                            <p>Your order bill information.</p>
                        </div>
                    </div>

                    <div class="wallet-payment-row">
                        <span>Bill ID</span>
                        <strong>
                            <c:choose>
                                <c:when test="${not empty bill}">${bill.billId}</c:when>
                                <c:otherwise>-</c:otherwise>
                            </c:choose>
                        </strong>
                    </div>

                    <div class="wallet-payment-row">
                        <span>Payment Method</span>
                        <strong>
                            <c:choose>
                                <c:when test="${not empty bill}">${bill.paymentMethod}</c:when>
                                <c:otherwise>COD (Cash on Delivery)</c:otherwise>
                            </c:choose>
                        </strong>
                    </div>

                    <div class="wallet-payment-row">
                        <span>Payment Status</span>
                        <c:choose>
                            <c:when test="${not empty bill}">
                                <strong class="payment-status payment-status-${fn:toLowerCase(bill.paymentStatus)}">${bill.paymentStatus}</strong>
                            </c:when>
                            <c:otherwise>
                                <strong class="payment-status payment-status-pending">Pending</strong>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <div class="wallet-payment-row">
                        <span>Amount</span>
                        <strong><fmt:formatNumber value="${order.totalAmount}" type="number" groupingUsed="true" /> VND</strong>
                    </div>

                    <c:if test="${not empty bill && bill.paymentStatus eq 'Pending' && bill.paymentMethod eq 'COD'}">
                        <div class="wallet-alert wallet-alert-success" style="margin-top: 12px;">
                            COD selected. Please pay when the order is delivered.
                        </div>
                    </c:if>

                    <c:if test="${order.orderStatus eq 'Cancelled'}">
                        <div class="wallet-alert wallet-alert-error" style="margin-top: 12px;">
                            This order has been cancelled.
                        </div>
                    </c:if>

                    <c:if test="${order.orderStatus eq 'Delivered'}">
                        <div class="wallet-alert wallet-alert-success" style="margin-top: 12px;">
                            Order has been delivered successfully!
                        </div>
                    </c:if>
                </aside>
            </div>
        </c:otherwise>
    </c:choose>
</section>
