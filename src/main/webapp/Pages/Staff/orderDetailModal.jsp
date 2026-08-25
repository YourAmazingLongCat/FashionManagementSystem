<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="modalSuccessMessage" value="${sessionScope.successMessage}" />
<c:set var="modalErrorMessage" value="${not empty errorMessage ? errorMessage : sessionScope.errorMessage}" />
<c:remove var="successMessage" scope="session" />
<c:remove var="errorMessage" scope="session" />

<div class="order-modal-detail" data-order-id="${empty order ? '' : fn:escapeXml(order.orderId)}">
    <c:if test="${not empty modalSuccessMessage}">
        <div class="order-modal-alert order-modal-alert-success" role="status">
            <i class="fas fa-circle-check me-2" aria-hidden="true"></i>${fn:escapeXml(modalSuccessMessage)}
        </div>
    </c:if>
    <c:if test="${not empty modalErrorMessage}">
        <div class="order-modal-alert order-modal-alert-error" role="alert">
            <i class="fas fa-circle-exclamation me-2" aria-hidden="true"></i>${fn:escapeXml(modalErrorMessage)}
        </div>
    </c:if>

    <c:choose>
        <c:when test="${empty order}">
            <div class="order-modal-card">
                <div class="order-modal-empty">
                    <i class="fas fa-triangle-exclamation fa-2x mb-3" aria-hidden="true"></i>
                    <div><strong>Order not found.</strong></div>
                    <div>Please close this popup and choose another order.</div>
                </div>
            </div>
        </c:when>
        <c:otherwise>
            <div class="order-modal-overview">
                <div>
                    <p class="order-modal-eyebrow">Order overview</p>
                    <h4 class="order-modal-order-id">${fn:escapeXml(order.orderId)}</h4>
                    <p class="order-modal-overview-meta">
                        Placed ${empty order.placedAt ? '-' : fn:replace(order.placedAt, 'T', ' ')}
                    </p>
                </div>
                <span class="order-modal-status status-${fn:toLowerCase(order.orderStatus)}">
                    ${fn:escapeXml(order.orderStatus)}
                </span>
            </div>

            <div class="order-modal-layout">
                <div class="order-modal-column">
                    <section class="order-modal-card">
                        <div class="order-modal-card-header">
                            <div>
                                <h4 class="order-modal-card-title">Customer & Shipping</h4>
                                <p class="order-modal-card-subtitle">Delivery information attached to this order.</p>
                            </div>
                            <span class="order-modal-card-icon"><i class="fas fa-truck-fast" aria-hidden="true"></i></span>
                        </div>
                        <div class="order-modal-meta-grid">
                            <div class="order-modal-meta-item">
                                <span class="order-modal-meta-label">Customer name</span>
                                <div class="order-modal-meta-value">
                                    <c:choose>
                                        <c:when test="${not empty customer and not empty customer.fullName}">${fn:escapeXml(customer.fullName)}</c:when>
                                        <c:when test="${not empty bill and not empty bill.customerName}">${fn:escapeXml(bill.customerName)}</c:when>
                                        <c:otherwise>-</c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                            <div class="order-modal-meta-item">
                                <span class="order-modal-meta-label">Customer ID</span>
                                <div class="order-modal-meta-value">${fn:escapeXml(order.customerId)}</div>
                            </div>
                            <div class="order-modal-meta-item">
                                <span class="order-modal-meta-label">Phone</span>
                                <div class="order-modal-meta-value">
                                    <c:choose>
                                        <c:when test="${not empty order.phone}">${fn:escapeXml(order.phone)}</c:when>
                                        <c:otherwise>-</c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                            <div class="order-modal-meta-item">
                                <span class="order-modal-meta-label">Placed at</span>
                                <div class="order-modal-meta-value">
                                    <c:choose>
                                        <c:when test="${not empty order.placedAt}">${fn:replace(order.placedAt, 'T', ' ')}</c:when>
                                        <c:otherwise>-</c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                            <div class="order-modal-meta-item">
                                <span class="order-modal-meta-label">Order total</span>
                                <div class="order-modal-meta-value">
                                    <fmt:formatNumber value="${order.totalAmount}" type="number" groupingUsed="true" /> VND
                                </div>
                            </div>
                            <div class="order-modal-meta-item full-width">
                                <span class="order-modal-meta-label">Shipping address</span>
                                <div class="order-modal-meta-value">
                                    <c:choose>
                                        <c:when test="${not empty order.shippingAddress}">${fn:escapeXml(order.shippingAddress)}</c:when>
                                        <c:otherwise>-</c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </div>
                    </section>

                    <section class="order-modal-card">
                        <div class="order-modal-card-header">
                            <div>
                                <h4 class="order-modal-card-title">Order Items</h4>
                                <p class="order-modal-card-subtitle">${empty orderItems ? 0 : fn:length(orderItems)} line item(s) in this order.</p>
                            </div>
                            <span class="order-modal-card-icon"><i class="fas fa-box-open" aria-hidden="true"></i></span>
                        </div>

                        <c:choose>
                            <c:when test="${empty orderItems}">
                                <div class="order-modal-empty">No items found for this order.</div>
                            </c:when>
                            <c:otherwise>
                                <div class="order-modal-items-wrap">
                                    <table class="order-modal-items-table">
                                        <thead>
                                            <tr>
                                                <th>Product</th>
                                                <th>Variant</th>
                                                <th>Quantity</th>
                                                <th>Unit price</th>
                                                <th class="text-end">Subtotal</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="item" items="${orderItems}">
                                                <tr>
                                                    <td>
                                                        <strong>
                                                            <c:choose>
                                                                <c:when test="${not empty item.productName}">${fn:escapeXml(item.productName)}</c:when>
                                                                <c:otherwise>-</c:otherwise>
                                                            </c:choose>
                                                        </strong>
                                                    </td>
                                                    <td>
                                                        <div>
                                                            <c:choose>
                                                                <c:when test="${not empty item.colorName or not empty item.sizeName}">
                                                                    <c:if test="${not empty item.colorName}">${fn:escapeXml(item.colorName)}</c:if>
                                                                    <c:if test="${not empty item.colorName and not empty item.sizeName}"> / </c:if>
                                                                    <c:if test="${not empty item.sizeName}">${fn:escapeXml(item.sizeName)}</c:if>
                                                                </c:when>
                                                                <c:otherwise>${fn:escapeXml(item.variantId)}</c:otherwise>
                                                            </c:choose>
                                                        </div>
                                                        <small class="text-muted">
                                                            <c:if test="${not empty item.sku}">SKU: ${fn:escapeXml(item.sku)} &middot; </c:if>
                                                            ID: ${fn:escapeXml(item.variantId)}
                                                        </small>
                                                    </td>
                                                    <td>${item.quantity}</td>
                                                    <td><fmt:formatNumber value="${item.unitPrice}" type="number" groupingUsed="true" /> VND</td>
                                                    <td class="text-end order-modal-price"><fmt:formatNumber value="${item.subTotal}" type="number" groupingUsed="true" /> VND</td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </section>
                </div>

                <aside class="order-modal-column">
                    <section class="order-modal-card">
                        <div class="order-modal-card-header">
                            <div>
                                <h4 class="order-modal-card-title">Payment</h4>
                                <p class="order-modal-card-subtitle">Payment information is managed as part of the order.</p>
                            </div>
                            <span class="order-modal-card-icon"><i class="fas fa-credit-card" aria-hidden="true"></i></span>
                        </div>

                        <c:choose>
                            <c:when test="${not empty payment}">
                                <div class="order-modal-detail-list">
                                    <div class="order-modal-detail-row"><span>Payment ID</span><strong>${fn:escapeXml(payment.paymentId)}</strong></div>
                                    <div class="order-modal-detail-row"><span>Type</span><strong>${fn:escapeXml(payment.paymentType)}</strong></div>
                                    <div class="order-modal-detail-row"><span>Method</span><strong>${fn:escapeXml(payment.paymentMethod)}</strong></div>
                                    <div class="order-modal-detail-row">
                                        <span>Status</span>
                                        <strong><span class="order-modal-status status-${fn:toLowerCase(payment.paymentStatus)}">${fn:escapeXml(payment.paymentStatus)}</span></strong>
                                    </div>
                                    <div class="order-modal-detail-row"><span>Amount</span><strong><fmt:formatNumber value="${payment.amount}" type="number" groupingUsed="true" /> VND</strong></div>
                                    <div class="order-modal-detail-row">
                                        <span>Paid at</span>
                                        <strong>
                                            <c:choose>
                                                <c:when test="${not empty payment.paidAt}">${fn:replace(payment.paidAt, 'T', ' ')}</c:when>
                                                <c:otherwise>-</c:otherwise>
                                            </c:choose>
                                        </strong>
                                    </div>
                                </div>
                                <c:if test="${payment.paymentMethod eq 'COD' and payment.paymentStatus eq 'Pending'}">
                                    <div class="order-modal-info success mt-3">
                                        <i class="fas fa-circle-info mt-1" aria-hidden="true"></i>
                                        <span>COD becomes Paid automatically when the order reaches Delivered.</span>
                                    </div>
                                </c:if>
                            </c:when>
                            <c:otherwise>
                                <div class="order-modal-empty">
                                    The customer has not completed Place order yet, so no payment record exists.
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </section>

                    <section class="order-modal-card">
                        <div class="order-modal-card-header">
                            <div>
                                <h4 class="order-modal-card-title">Invoice</h4>
                                <p class="order-modal-card-subtitle">Billing summary linked to this order.</p>
                            </div>
                            <span class="order-modal-card-icon"><i class="fas fa-file-invoice-dollar" aria-hidden="true"></i></span>
                        </div>
                        <c:choose>
                            <c:when test="${not empty bill}">
                                <div class="order-modal-detail-list">
                                    <div class="order-modal-detail-row"><span>Bill ID</span><strong>${fn:escapeXml(bill.billId)}</strong></div>
                                    <div class="order-modal-detail-row"><span>Method</span><strong>${fn:escapeXml(bill.paymentMethod)}</strong></div>
                                    <div class="order-modal-detail-row"><span>Status</span><strong>${fn:escapeXml(bill.paymentStatus)}</strong></div>
                                    <div class="order-modal-detail-row"><span>Issued date</span><strong>${bill.issuedDate}</strong></div>
                                    <div class="order-modal-detail-row"><span>Total</span><strong><fmt:formatNumber value="${bill.totalAmount}" type="number" groupingUsed="true" /> VND</strong></div>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="order-modal-empty">No invoice information is available for this order.</div>
                            </c:otherwise>
                        </c:choose>
                    </section>

                    <section class="order-modal-card">
                        <div class="order-modal-card-header">
                            <div>
                                <h4 class="order-modal-card-title">Manage Order</h4>
                                <p class="order-modal-card-subtitle">Confirm, update shipping status, or cancel an eligible order.</p>
                            </div>
                            <span class="order-modal-card-icon"><i class="fas fa-sliders" aria-hidden="true"></i></span>
                        </div>

                        <c:set var="nextStatus" value="" />
                        <c:choose>
                            <c:when test="${order.orderStatus eq 'Confirmed'}">
                                <c:set var="nextStatus" value="Processing" />
                            </c:when>
                            <c:when test="${order.orderStatus eq 'Processing'}">
                                <c:set var="nextStatus" value="Shipping" />
                            </c:when>
                            <c:when test="${order.orderStatus eq 'Shipping'}">
                                <c:set var="nextStatus" value="Delivered" />
                            </c:when>
                        </c:choose>

                        <div class="order-modal-actions">
                            <c:choose>
                                <c:when test="${empty payment}">
                                    <div class="order-modal-info warning">
                                        <i class="fas fa-lock mt-1" aria-hidden="true"></i>
                                        <span>Waiting for the customer to complete Place order. Staff actions are locked until a payment record exists.</span>
                                    </div>
                                    <button type="button" class="order-modal-action-btn secondary" disabled>
                                        <i class="fas fa-hourglass-half" aria-hidden="true"></i>Waiting for customer
                                    </button>
                                </c:when>
                                <c:otherwise>
                                    <c:if test="${order.orderStatus eq 'Pending'}">
                                        <form class="order-modal-action-form" method="post" action="${pageContext.request.contextPath}/staff/confirm-order"
                                              data-confirm="Confirm this order for processing?">
                                            <input type="hidden" name="orderId" value="${fn:escapeXml(order.orderId)}" />
                                            <button type="submit" class="order-modal-action-btn">
                                                <i class="fas fa-circle-check" aria-hidden="true"></i>Confirm Order
                                            </button>
                                        </form>
                                    </c:if>

                                    <c:if test="${not empty nextStatus}">
                                        <form class="order-modal-action-form" method="post" action="${pageContext.request.contextPath}/staff/change-shipping-status"
                                              data-confirm="Move order status from ${fn:escapeXml(order.orderStatus)} to ${fn:escapeXml(nextStatus)}?">
                                            <input type="hidden" name="orderId" value="${fn:escapeXml(order.orderId)}" />
                                            <input type="hidden" name="newStatus" value="${fn:escapeXml(nextStatus)}" />
                                            <button type="submit" class="order-modal-action-btn">
                                                <i class="fas fa-arrow-right" aria-hidden="true"></i>Move to ${fn:escapeXml(nextStatus)}
                                            </button>
                                        </form>
                                    </c:if>

                                    <c:if test="${order.orderStatus eq 'Pending' or order.orderStatus eq 'Confirmed' or order.orderStatus eq 'Processing'}">
                                        <form class="order-modal-action-form" method="post" action="${pageContext.request.contextPath}/staff/cancel-order"
                                              data-confirm="Cancel this order? A completed VNPay payment will be marked for refund when applicable.">
                                            <input type="hidden" name="orderId" value="${fn:escapeXml(order.orderId)}" />
                                            <button type="submit" class="order-modal-action-btn danger">
                                                <i class="fas fa-ban" aria-hidden="true"></i>Cancel Order
                                            </button>
                                        </form>
                                    </c:if>

                                    <c:if test="${order.orderStatus eq 'Delivered'}">
                                        <div class="order-modal-info success">
                                            <i class="fas fa-circle-check mt-1" aria-hidden="true"></i>
                                            <span>This order is Delivered. No further shipping action is required.</span>
                                        </div>
                                    </c:if>
                                    <c:if test="${order.orderStatus eq 'Cancelled'}">
                                        <div class="order-modal-info warning">
                                            <i class="fas fa-ban mt-1" aria-hidden="true"></i>
                                            <span>This order is Cancelled. Status changes are no longer available.</span>
                                        </div>
                                    </c:if>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </section>

                    <section class="order-modal-card">
                        <div class="order-modal-card-header">
                            <div>
                                <h4 class="order-modal-card-title">Shipping Progress</h4>
                                <p class="order-modal-card-subtitle">Current fulfilment stage for this order.</p>
                            </div>
                            <span class="order-modal-card-icon"><i class="fas fa-route" aria-hidden="true"></i></span>
                        </div>

                        <c:if test="${order.orderStatus eq 'Cancelled'}">
                            <div class="order-modal-cancelled-banner">ORDER CANCELLED</div>
                        </c:if>
                        <div class="order-modal-progress" aria-label="Order shipping progress">
                            <div class="order-modal-progress-step ${order.orderStatus ne 'Cancelled' ? 'is-complete' : ''}">
                                <span class="order-modal-progress-dot">1</span><span>Pending</span>
                            </div>
                            <div class="order-modal-progress-step ${order.orderStatus eq 'Confirmed' or order.orderStatus eq 'Processing' or order.orderStatus eq 'Shipping' or order.orderStatus eq 'Delivered' ? 'is-complete' : ''}">
                                <span class="order-modal-progress-dot">2</span><span>Confirmed</span>
                            </div>
                            <div class="order-modal-progress-step ${order.orderStatus eq 'Processing' or order.orderStatus eq 'Shipping' or order.orderStatus eq 'Delivered' ? 'is-complete' : ''}">
                                <span class="order-modal-progress-dot">3</span><span>Processing</span>
                            </div>
                            <div class="order-modal-progress-step ${order.orderStatus eq 'Shipping' or order.orderStatus eq 'Delivered' ? 'is-complete' : ''}">
                                <span class="order-modal-progress-dot">4</span><span>Shipping</span>
                            </div>
                            <div class="order-modal-progress-step ${order.orderStatus eq 'Delivered' ? 'is-complete' : ''}">
                                <span class="order-modal-progress-dot">5</span><span>Delivered</span>
                            </div>
                        </div>
                    </section>
                </aside>
            </div>
        </c:otherwise>
    </c:choose>
</div>
