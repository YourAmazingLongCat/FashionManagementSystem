<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<div class="content-page order-page staff-ops-page">
    <div class="order-container">
        <c:set var="pagePendingOrders" value="${0}" />
        <c:set var="pageShippingOrders" value="${0}" />
        <c:set var="pageDeliveredOrders" value="${0}" />
        <c:forEach var="order" items="${listOrders}">
            <c:if test="${order.orderStatus eq 'Pending'}">
                <c:set var="pagePendingOrders" value="${pagePendingOrders + 1}" />
            </c:if>
            <c:if test="${order.orderStatus eq 'Shipping'}">
                <c:set var="pageShippingOrders" value="${pageShippingOrders + 1}" />
            </c:if>
            <c:if test="${order.orderStatus eq 'Delivered'}">
                <c:set var="pageDeliveredOrders" value="${pageDeliveredOrders + 1}" />
            </c:if>
        </c:forEach>

        <section class="order-hero staff-ops-hero">
            <div>
                <p class="order-eyebrow">Staff Operations</p>
                <h1 class="order-title">Order Management</h1>
                <p class="order-subtitle">
                    Search customer orders, review payment readiness, and manage fulfilment status.
                </p>
            </div>
        </section>

        <div class="back-nav-row">
            <a class="back-btn" href="${pageContext.request.contextPath}/staff/products">
                <span class="material-symbols-outlined">arrow_back</span>
                Back to Staff
            </a>
        </div>

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

        <div class="order-grid order-grid-4 staff-stat-grid">
            <div class="order-stat-card">
                <span class="order-stat-label">Total records</span>
                <span class="order-stat-value">${empty totalOrders ? fn:length(listOrders) : totalOrders}</span>
            </div>
            <div class="order-stat-card">
                <span class="order-stat-label">Pending on page</span>
                <span class="order-stat-value">${pagePendingOrders}</span>
            </div>
            <div class="order-stat-card">
                <span class="order-stat-label">Shipping on page</span>
                <span class="order-stat-value">${pageShippingOrders}</span>
            </div>
            <div class="order-stat-card">
                <span class="order-stat-label">Delivered on page</span>
                <span class="order-stat-value">${pageDeliveredOrders}</span>
            </div>
        </div>

        <section class="order-panel order-panel-padding staff-list-panel">
            <div class="order-panel-header staff-list-header">
                <div>
                    <h2 class="order-section-title">Order Records</h2>
                    <p class="order-muted">Showing ${fn:length(listOrders)} record(s) on page ${empty currentPage ? 1 : currentPage}.</p>
                </div>
            </div>

            <form class="staff-filter-bar" method="get" action="${pageContext.request.contextPath}/staff/orders">
                <label class="staff-search-field">
                    <span class="material-symbols-outlined">search</span>
                    <input type="text" name="keyword" value="${fn:escapeXml(keyword)}"
                           placeholder="Order ID, customer ID, phone, address, or status" />
                </label>
                <label class="staff-page-size-field">
                    <span>Rows</span>
                    <select name="pageSize">
                        <option value="5" ${pageSize == 5 ? 'selected' : ''}>5</option>
                        <option value="10" ${empty pageSize or pageSize == 10 ? 'selected' : ''}>10</option>
                        <option value="20" ${pageSize == 20 ? 'selected' : ''}>20</option>
                        <option value="50" ${pageSize == 50 ? 'selected' : ''}>50</option>
                    </select>
                </label>
                <button class="order-btn order-btn-primary" type="submit">
                    <span class="material-symbols-outlined">filter_alt</span>
                    Apply
                </button>
                <c:if test="${not empty keyword}">
                    <a class="order-btn" href="${pageContext.request.contextPath}/staff/orders?pageSize=${pageSize}">
                        Clear
                    </a>
                </c:if>
            </form>

            <c:choose>
                <c:when test="${empty listOrders}">
                    <div class="order-empty staff-panel-empty">
                        <span class="material-symbols-outlined">inventory_2</span>
                        <h3>No orders found</h3>
                        <p>Try another search term or wait for customers to create orders.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="order-table-wrap staff-table-wrap">
                        <table class="order-table staff-data-table">
                            <thead>
                                <tr>
                                    <th>Order</th>
                                    <th>Customer</th>
                                    <th>Placed At</th>
                                    <th>Contact</th>
                                    <th>Status</th>
                                    <th>Total</th>
                                    <th class="staff-action-column">Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="order" items="${listOrders}">
                                    <tr>
                                        <td>
                                            <div class="order-code">${order.orderId}</div>
                                            <div class="order-muted staff-cell-secondary">${order.shippingAddress}</div>
                                        </td>
                                        <td>
                                            <div class="staff-cell-primary">${order.customerId}</div>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty order.placedAt}">
                                                    ${fn:replace(order.placedAt, 'T', ' ')}
                                                </c:when>
                                                <c:otherwise>-</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty order.phone}">${order.phone}</c:when>
                                                <c:otherwise>-</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <span class="order-status status-${fn:toLowerCase(order.orderStatus)}">
                                                ${order.orderStatus}
                                            </span>
                                        </td>
                                        <td class="order-price">
                                            <fmt:formatNumber value="${order.totalAmount}" type="number" groupingUsed="true" /> VND
                                        </td>
                                        <td class="staff-action-column">
                                            <a class="order-btn staff-table-action"
                                               href="${pageContext.request.contextPath}/staff/order-detail?orderId=${order.orderId}">
                                                <span class="material-symbols-outlined">visibility</span>
                                                Manage
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>

            <c:if test="${not empty totalPages and totalPages > 1}">
                <div class="staff-pagination">
                    <c:url var="previousPageUrl" value="/staff/orders">
                        <c:param name="page" value="${currentPage - 1}" />
                        <c:param name="pageSize" value="${pageSize}" />
                        <c:param name="keyword" value="${keyword}" />
                    </c:url>
                    <c:url var="nextPageUrl" value="/staff/orders">
                        <c:param name="page" value="${currentPage + 1}" />
                        <c:param name="pageSize" value="${pageSize}" />
                        <c:param name="keyword" value="${keyword}" />
                    </c:url>

                    <c:choose>
                        <c:when test="${currentPage > 1}">
                            <a class="staff-page-button" href="${previousPageUrl}">
                                <span class="material-symbols-outlined">chevron_left</span>
                                Previous
                            </a>
                        </c:when>
                        <c:otherwise>
                            <span class="staff-page-button disabled">
                                <span class="material-symbols-outlined">chevron_left</span>
                                Previous
                            </span>
                        </c:otherwise>
                    </c:choose>

                    <span class="staff-page-summary">Page ${currentPage} of ${totalPages}</span>

                    <c:choose>
                        <c:when test="${currentPage < totalPages}">
                            <a class="staff-page-button" href="${nextPageUrl}">
                                Next
                                <span class="material-symbols-outlined">chevron_right</span>
                            </a>
                        </c:when>
                        <c:otherwise>
                            <span class="staff-page-button disabled">
                                Next
                                <span class="material-symbols-outlined">chevron_right</span>
                            </span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </c:if>
        </section>
    </div>
</div>
