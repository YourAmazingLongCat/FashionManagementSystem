<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<div class="content-page order-page staff-ops-page">
    <div class="order-container">
        <section class="order-hero staff-ops-hero">
            <div>
                <p class="order-eyebrow">Staff Operations</p>
                <h1 class="order-title">Order Detail</h1>
                <p class="order-subtitle">
                    Review fulfilment and payment information, then manage the order through its allowed status flow.
                </p>
            </div>
            <div class="order-actions-row">
                <a class="order-btn" href="${pageContext.request.contextPath}/staff/orders">
                    <span class="material-symbols-outlined">arrow_back</span>
                    Orders
                </a>
                <a class="order-btn" href="${pageContext.request.contextPath}/staff/payments">
                    <span class="material-symbols-outlined">payments</span>
                    Payments
                </a>
            </div>
        </section>

        <nav class="staff-module-tabs" aria-label="Order and payment management">
            <a class="staff-module-tab active" href="${pageContext.request.contextPath}/staff/orders">
                <span class="material-symbols-outlined">receipt_long</span>
                Orders
            </a>
            <a class="staff-module-tab" href="${pageContext.request.contextPath}/staff/payments">
                <span class="material-symbols-outlined">payments</span>
                Payments
            </a>
        </nav>

        <c:choose>
            <c:when test="${empty order}">
                <div class="order-panel order-empty">
                    <span class="material-symbols-outlined">error</span>
                    <h3>Order not found</h3>
                    <p>Please return to order management and try again.</p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="order-grid order-grid-2">
                    <section class="order-panel order-panel-padding">
                        <div class="order-panel-header">
                            <div>
                                <span class="order-muted">Order ID</span>
                                <h2 class="order-section-title" style="font-size: 1.35rem; margin-top: 4px;">${order.orderId}</h2>
                            </div>
                            <span class="order-status status-${fn:toLowerCase(order.orderStatus)}">${order.orderStatus}</span>
                        </div>

                        <div class="order-meta-grid">
                            <div class="order-meta-card">
                                <span class="order-meta-label">Customer</span>
                                <div class="order-meta-value">${order.customerId}</div>
                            </div>
                            <div class="order-meta-card">
                                <span class="order-meta-label">Phone</span>
                                <div class="order-meta-value">${order.phone}</div>
                            </div>
                            <div class="order-meta-card">
                                <span class="order-meta-label">Placed at</span>
                                <div class="order-meta-value">${order.placedAt}</div>
                            </div>
                            <div class="order-meta-card">
                                <span class="order-meta-label">Total</span>
                                <div class="order-meta-value"><fmt:formatNumber value="${order.totalAmount}" type="number" groupingUsed="true" /> VND</div>
                            </div>
                            <div class="order-meta-card" style="grid-column: 1 / -1;">
                                <span class="order-meta-label">Shipping address</span>
                                <div class="order-meta-value">${order.shippingAddress}</div>
                            </div>
                        </div>

                        <div style="height: 28px;"></div>
                        <div class="order-panel-header">
                            <h3 class="order-section-title">Products</h3>
                            <span class="order-muted">${empty orderItems ? 0 : orderItems.size()} items</span>
                        </div>

                        <c:choose>
                            <c:when test="${empty orderItems}">
                                <div class="order-empty" style="padding: 30px 10px;">No items found for this order.</div>
                            </c:when>
                            <c:otherwise>
                                <div class="order-summary-list">
                                    <c:forEach var="item" items="${orderItems}">
                                        <div class="order-item-row">
                                            <div class="order-thumb">PR</div>
                                            <div>
                                                <div class="order-item-name">Variant ${item.variantId}</div>
                                                <div class="order-muted">Qty ${item.quantity} × <fmt:formatNumber value="${item.unitPrice}" type="number" groupingUsed="true" /> VND</div>
                                            </div>
                                            <div class="order-price"><fmt:formatNumber value="${item.subTotal}" type="number" groupingUsed="true" /> VND</div>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </section>

                    <aside class="order-grid">
                        <section class="order-panel order-panel-padding staff-payment-detail-card">
                            <div class="order-panel-header">
                                <div>
                                    <h3 class="order-section-title">Payment Information</h3>
                                    <p class="order-muted">Staff actions remain locked until the customer completes Place order.</p>
                                </div>
                                <span class="staff-panel-icon material-symbols-outlined">payments</span>
                            </div>

                            <c:choose>
                                <c:when test="${not empty payment}">
                                    <div class="staff-detail-list">
                                        <div class="staff-detail-row">
                                            <span>Payment ID</span>
                                            <strong>${payment.paymentId}</strong>
                                        </div>
                                        <div class="staff-detail-row">
                                            <span>Type</span>
                                            <strong>${payment.paymentType}</strong>
                                        </div>
                                        <div class="staff-detail-row">
                                            <span>Method</span>
                                            <strong class="staff-method-badge">${payment.paymentMethod}</strong>
                                        </div>
                                        <div class="staff-detail-row">
                                            <span>Status</span>
                                            <strong class="order-status status-${fn:toLowerCase(payment.paymentStatus)}">${payment.paymentStatus}</strong>
                                        </div>
                                        <div class="staff-detail-row">
                                            <span>Amount</span>
                                            <strong class="order-price"><fmt:formatNumber value="${payment.amount}" type="number" groupingUsed="true" /> VND</strong>
                                        </div>
                                        <div class="staff-detail-row">
                                            <span>Paid At</span>
                                            <strong>
                                                <c:choose>
                                                    <c:when test="${empty payment.paidAt}">-</c:when>
                                                    <c:otherwise>${fn:replace(payment.paidAt, 'T', ' ')}</c:otherwise>
                                                </c:choose>
                                            </strong>
                                        </div>
                                    </div>
                                    <c:if test="${payment.paymentMethod eq 'COD' and payment.paymentStatus eq 'Pending'}">
                                        <div class="staff-info-box staff-info-success">
                                            <span class="material-symbols-outlined">info</span>
                                            <span>COD payment will be marked Paid automatically when the order becomes Delivered.</span>
                                        </div>
                                    </c:if>
                                </c:when>
                                <c:otherwise>
                                    <div class="order-empty staff-panel-empty staff-panel-empty-compact">
                                        <span class="material-symbols-outlined">money_off</span>
                                        <h3>Order not placed</h3>
                                        <p>The customer created a Pending reservation but has not completed Place order.</p>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </section>

                        <div class="order-panel order-panel-padding">
                            <div class="order-panel-header">
                                <h3 class="order-section-title">Staff Actions</h3>
                            </div>

                            <c:set var="nextStatus" value="" />
                            <c:set var="previousStatus" value="" />

                            <c:choose>
                                <c:when test="${order.orderStatus eq 'Pending'}">
                                    <c:set var="nextStatus" value="Confirmed" />
                                </c:when>
                                <c:when test="${order.orderStatus eq 'Confirmed'}">
                                    <c:set var="nextStatus" value="Processing" />
                                    <c:set var="previousStatus" value="Pending" />
                                </c:when>
                                <c:when test="${order.orderStatus eq 'Processing'}">
                                    <c:set var="nextStatus" value="Shipping" />
                                    <c:set var="previousStatus" value="Confirmed" />
                                </c:when>
                                <c:when test="${order.orderStatus eq 'Shipping'}">
                                    <c:set var="nextStatus" value="Delivered" />
                                </c:when>
                            </c:choose>

                            <div class="order-admin-actions">
                                <c:choose>
                                    <c:when test="${empty payment}">
                                        <div class="order-warning-box">
                                            The customer has not pressed <strong>Place order</strong> yet.
                                            Staff cannot confirm, move backward, move forward or cancel this Pending order.
                                        </div>
                                        <button class="order-btn" type="button" disabled
                                                style="opacity: 0.45; cursor: not-allowed;">
                                            <span class="material-symbols-outlined">lock</span>
                                            Waiting for customer
                                        </button>
                                    </c:when>
                                    <c:otherwise>
                                        <c:if test="${not empty nextStatus}">
                                            <form class="order-inline-form" method="post"
                                                  action="${pageContext.request.contextPath}/staff/change-shipping-status"
                                                  onsubmit="return confirmOrderStatusChange('forward', '${order.orderStatus}', '${nextStatus}');">
                                                <input type="hidden" name="orderId" value="${order.orderId}" />
                                                <input type="hidden" name="newStatus" value="${nextStatus}" />
                                                <button class="order-btn order-btn-primary" type="submit">
                                                    <span class="material-symbols-outlined">arrow_forward</span>
                                                    Move forward to ${nextStatus}
                                                </button>
                                            </form>
                                        </c:if>

                                        <c:choose>
                                            <c:when test="${order.orderStatus eq 'Shipping' or order.orderStatus eq 'Delivered'}">
                                                <button class="order-btn" type="button" disabled
                                                        style="opacity: 0.45; cursor: not-allowed;">
                                                    <span class="material-symbols-outlined">lock</span>
                                                    Status locked after Shipping
                                                </button>
                                            </c:when>
                                            <c:when test="${not empty previousStatus}">
                                                <form class="order-inline-form" method="post"
                                                      action="${pageContext.request.contextPath}/staff/change-shipping-status"
                                                      onsubmit="return confirmOrderStatusChange('backward', '${order.orderStatus}', '${previousStatus}');">
                                                    <input type="hidden" name="orderId" value="${order.orderId}" />
                                                    <input type="hidden" name="newStatus" value="${previousStatus}" />
                                                    <button class="order-btn" type="submit">
                                                        <span class="material-symbols-outlined">arrow_back</span>
                                                        Move backward to ${previousStatus}
                                                    </button>
                                                </form>
                                            </c:when>
                                            <c:when test="${order.orderStatus ne 'Cancelled'}">
                                                <button class="order-btn" type="button" disabled
                                                        style="opacity: 0.45; cursor: not-allowed;">
                                                    <span class="material-symbols-outlined">block</span>
                                                    No previous status
                                                </button>
                                            </c:when>
                                        </c:choose>

                                        <c:if test="${order.orderStatus eq 'Pending' or order.orderStatus eq 'Confirmed' or order.orderStatus eq 'Processing'}">
                                            <form class="order-inline-form" method="post"
                                                  action="${pageContext.request.contextPath}/staff/cancel-order"
                                                  onsubmit="return confirm('Cancel this order? A completed VNPay payment will be marked for refund if applicable.');">
                                                <input type="hidden" name="orderId" value="${order.orderId}" />
                                                <button class="order-btn order-btn-danger" type="submit">
                                                    <span class="material-symbols-outlined">cancel</span>
                                                    Cancel order
                                                </button>
                                            </form>
                                        </c:if>

                                        <c:if test="${order.orderStatus eq 'Cancelled'}">
                                            <div class="order-warning-box">
                                                This order is already Cancelled. Status movement is not available.
                                            </div>
                                        </c:if>

                                        <div class="order-warning-box">
                                            Forward and backward movement must be one status level each time before Shipping.
                                            Once an order reaches Shipping, it cannot move backward or be cancelled.
                                            VNPay orders must be Paid before moving forward.
                                            COD orders can move forward while payment is Pending and become Paid automatically when Delivered.
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>

                        <div class="order-panel order-panel-padding">
                            <div class="order-panel-header">
                                <h3 class="order-section-title">Shipping Progress</h3>
                            </div>
                            <div class="order-progress order-progress-${fn:toLowerCase(order.orderStatus)}">
                                <div class="order-progress-step"><span class="order-progress-dot">1</span><span class="order-progress-label">Pending</span></div>
                                <div class="order-progress-step"><span class="order-progress-dot">2</span><span class="order-progress-label">Confirmed</span></div>
                                <div class="order-progress-step"><span class="order-progress-dot">3</span><span class="order-progress-label">Processing</span></div>
                                <div class="order-progress-step"><span class="order-progress-dot">4</span><span class="order-progress-label">Shipping</span></div>
                                <div class="order-progress-step"><span class="order-progress-dot">5</span><span class="order-progress-label">Delivered</span></div>
                            </div>
                        </div>
                    </aside>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>


<script>
    function confirmOrderStatusChange(direction, currentStatus, targetStatus) {
        var actionText = direction === 'backward' ? 'move backward' : 'move forward';
        return confirm("Confirm to " + actionText + " order status from " + currentStatus + " to " + targetStatus + "?");
    }
</script>
