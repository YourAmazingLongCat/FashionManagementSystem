<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<div class="content-page order-page">
    <div class="order-container">
        <c:set var="totalOrders" value="${0}" />
        <c:set var="pendingOrders" value="${0}" />
        <c:set var="shippingOrders" value="${0}" />
        <c:forEach var="order" items="${listOrders}">
            <c:set var="totalOrders" value="${totalOrders + 1}" />
            <c:if test="${order.orderStatus eq 'Pending'}">
                <c:set var="pendingOrders" value="${pendingOrders + 1}" />
            </c:if>
            <c:if test="${order.orderStatus eq 'Shipping'}">
                <c:set var="shippingOrders" value="${shippingOrders + 1}" />
            </c:if>
        </c:forEach>

        <section class="order-hero">
            <div>
                <p class="order-eyebrow">Staff / Orders</p>
                <h1 class="order-title">Order Management</h1>
                <p class="order-subtitle">
                    Review customer orders, confirm pending orders, and update shipping progress.
                </p>
            </div>
            <div class="order-actions-row">
                <a class="order-btn" href="${pageContext.request.contextPath}/home">
                    <span class="material-symbols-outlined">storefront</span>
                    Storefront
                </a>
            </div>
        </section>

        <div class="order-grid order-grid-3" style="margin-bottom: 28px;">
            <div class="order-stat-card">
                <span class="order-stat-label">Total orders</span>
                <span class="order-stat-value">${totalOrders}</span>
            </div>
            <div class="order-stat-card">
                <span class="order-stat-label">Pending</span>
                <span class="order-stat-value">${pendingOrders}</span>
            </div>
            <div class="order-stat-card">
                <span class="order-stat-label">Shipping</span>
                <span class="order-stat-value">${shippingOrders}</span>
            </div>
        </div>

        <form class="order-search-form" method="get" action="${pageContext.request.contextPath}/staff/search-orders">
            <input class="order-search-input" type="text" name="keyword" value="${keyword}" placeholder="Search by order ID, customer ID, phone, or status..." />
            <button class="order-btn order-btn-primary" type="submit">
                <span class="material-symbols-outlined">search</span>
                Search
            </button>
        </form>

        <c:choose>
            <c:when test="${empty listOrders}">
                <div class="order-panel order-empty">
                    <span class="material-symbols-outlined">inventory_2</span>
                    <h3>No orders found</h3>
                    <p>Orders will appear here after customers complete checkout.</p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="order-table-wrap">
                    <table class="order-table">
                        <thead>
                            <tr>
                                <th>Order</th>
                                <th>Customer</th>
                                <th>Date</th>
                                <th>Phone</th>
                                <th>Status</th>
                                <th>Total</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="order" items="${listOrders}">
                                <tr>
                                    <td>
                                        <div class="order-code">${order.orderId}</div>
                                        <div class="order-muted">${order.shippingAddress}</div>
                                    </td>
                                    <td>${order.customerId}</td>
                                    <td>${order.placedAt}</td>
                                    <td>${order.phone}</td>
                                    <td><span class="order-status status-${fn:toLowerCase(order.orderStatus)}">${order.orderStatus}</span></td>
                                    <td class="order-price"><fmt:formatNumber value="${order.totalAmount}" type="number" groupingUsed="true" /> đ</td>
                                    <td>
                                        <a class="order-btn" href="${pageContext.request.contextPath}/staff/order-detail?orderId=${order.orderId}">Manage</a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>
