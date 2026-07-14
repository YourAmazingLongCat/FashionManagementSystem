<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<div class="content-page order-page">
    <div class="order-container">
        <section class="order-hero">
            <div>
                <p class="order-eyebrow">Staff / Order Detail</p>
                <h1 class="order-title">Manage Order</h1>
                <p class="order-subtitle">
                    Confirm the order, update the shipping status, or cancel eligible orders.
                </p>
            </div>
            <div class="order-actions-row">
                <a class="order-btn" href="${pageContext.request.contextPath}/staff/orders">
                    <span class="material-symbols-outlined">arrow_back</span>
                    Back to list
                </a>
            </div>
        </section>

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
                                <div class="order-meta-value"><fmt:formatNumber value="${order.totalAmount}" type="number" groupingUsed="true" /> đ</div>
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
                                                <div class="order-muted">Qty ${item.quantity} × <fmt:formatNumber value="${item.unitPrice}" type="number" groupingUsed="true" /> đ</div>
                                            </div>
                                            <div class="order-price"><fmt:formatNumber value="${item.subTotal}" type="number" groupingUsed="true" /> đ</div>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </section>

                    <aside class="order-grid">
                        <div class="order-panel order-panel-padding">
                            <div class="order-panel-header">
                                <h3 class="order-section-title">Staff Actions</h3>
                            </div>

                            <div class="order-admin-actions">
                                <c:if test="${order.orderStatus eq 'Pending'}">
                                    <form class="order-inline-form" method="post" action="${pageContext.request.contextPath}/staff/confirm-order">
                                        <input type="hidden" name="orderId" value="${order.orderId}" />
                                        <button class="order-btn order-btn-success" type="submit">
                                            <span class="material-symbols-outlined">verified</span>
                                            Confirm order
                                        </button>
                                    </form>
                                </c:if>

                                <c:if test="${order.orderStatus eq 'Confirmed' or order.orderStatus eq 'Processing' or order.orderStatus eq 'Shipping'}">
                                    <form class="order-inline-form" method="post" action="${pageContext.request.contextPath}/staff/change-shipping-status">
                                        <input type="hidden" name="orderId" value="${order.orderId}" />
                                        <label class="order-label" for="newStatus">Next status</label>
                                        <select id="newStatus" name="newStatus" class="order-select" required>
                                            <c:if test="${order.orderStatus eq 'Confirmed'}">
                                                <option value="Processing">Processing</option>
                                            </c:if>
                                            <c:if test="${order.orderStatus eq 'Processing'}">
                                                <option value="Shipping">Shipping</option>
                                            </c:if>
                                            <c:if test="${order.orderStatus eq 'Shipping'}">
                                                <option value="Delivered">Delivered</option>
                                            </c:if>
                                        </select>
                                        <button class="order-btn order-btn-primary" type="submit">
                                            <span class="material-symbols-outlined">local_shipping</span>
                                            Update shipping
                                        </button>
                                    </form>
                                </c:if>

                                <c:if test="${order.orderStatus eq 'Pending' or order.orderStatus eq 'Confirmed' or order.orderStatus eq 'Processing'}">
                                    <form class="order-inline-form" method="post" action="${pageContext.request.contextPath}/staff/cancel-order" onsubmit="return confirm('Cancel this order?');">
                                        <input type="hidden" name="orderId" value="${order.orderId}" />
                                        <button class="order-btn order-btn-danger" type="submit">
                                            <span class="material-symbols-outlined">cancel</span>
                                            Cancel order
                                        </button>
                                    </form>
                                </c:if>

                                <c:if test="${order.orderStatus eq 'Delivered' or order.orderStatus eq 'Cancelled'}">
                                    <div class="order-warning-box">
                                        This order is already ${order.orderStatus}. No further shipping action is available.
                                    </div>
                                </c:if>
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
