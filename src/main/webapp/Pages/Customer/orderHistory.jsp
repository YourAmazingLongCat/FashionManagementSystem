<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<div class="content-page order-page">
    <div class="order-container">
        <section class="order-hero">
            <div>
                <p class="order-eyebrow">Customer / Orders</p>
                <h1 class="order-title">My Orders</h1>
                <p class="order-subtitle">
                    Track your purchases, view details, and cancel eligible orders before they are shipped.
                </p>
            </div>
            <div class="order-actions-row">
                <a class="order-btn order-btn-primary" href="${pageContext.request.contextPath}/home">
                    <span class="material-symbols-outlined">arrow_back</span>
                    Continue shopping
                </a>
            </div>
        </section>

        <form class="order-search-form" method="get" action="${pageContext.request.contextPath}/customer/order-history">
            <input class="order-search-input" type="text" name="keyword" value="${keyword}" placeholder="Search by order ID, status, or phone..." />
            <button class="order-btn order-btn-primary" type="submit">
                <span class="material-symbols-outlined">search</span>
                Search
            </button>
        </form>

        <c:choose>
            <c:when test="${empty listOrders}">
                <div class="order-panel order-empty">
                    <span class="material-symbols-outlined">receipt_long</span>
                    <h3>No orders found</h3>
                    <p>Your completed checkout orders will appear here.</p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="order-table-wrap">
                    <table class="order-table">
                        <thead>
                            <tr>
                                <th>Order</th>
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
                                    <td>${order.placedAt}</td>
                                    <td>${order.phone}</td>
                                    <td>
                                        <span class="order-status status-${fn:toLowerCase(order.orderStatus)}">${order.orderStatus}</span>
                                    </td>
                                    <td class="order-price">
                                        <fmt:formatNumber value="${order.totalAmount}" type="number" groupingUsed="true" /> đ
                                    </td>
                                    <td>
                                        <a class="order-btn" href="${pageContext.request.contextPath}/customer/order-detail?orderId=${order.orderId}">
                                            View
                                        </a>
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
