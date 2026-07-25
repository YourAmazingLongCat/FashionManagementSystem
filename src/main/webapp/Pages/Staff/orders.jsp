<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<style>
.order-pagination { display: flex; justify-content: center; margin-top: 24px; }
.pagination { display: flex; gap: 6px; list-style: none; padding: 0; margin: 0; }
.page-item { }
.page-link { display: flex; align-items: center; justify-content: center; min-width: 38px; height: 38px; padding: 0 10px; border: 1.5px solid #e2e8f0; border-radius: 10px; background: #fff; color: #334155; font-weight: 600; font-size: 0.9rem; text-decoration: none; transition: all 0.2s; }
.page-link:hover { background: #f1f5f9; border-color: #4338ca; color: #4338ca; }
.page-item.active .page-link { background: #4338ca; border-color: #4338ca; color: #fff; }
.page-item.disabled .page-link { color: #94a3b8; cursor: not-allowed; pointer-events: none; }
</style>

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
                <a class="order-btn" href="${pageContext.request.contextPath}/staff/products">
                    <span class="material-symbols-outlined">inventory_2</span>
                    Back to Product Management
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

                <c:if test="${totalPages > 1}">
                    <div class="order-pagination">
                        <nav>
                            <ul class="pagination">
                                <c:if test="${currentPage > 1}">
                                    <li class="page-item">
                                        <a class="page-link" href="?page=${currentPage - 1}&keyword=${keyword}">‹</a>
                                    </li>
                                </c:if>
                                <c:forEach var="i" begin="1" end="${totalPages}">
                                    <c:choose>
                                        <c:when test="${i == currentPage}">
                                            <li class="page-item active"><span class="page-link">${i}</span></li>
                                        </c:when>
                                        <c:when test="${i <= 3 || i > totalPages - 3 || (i >= currentPage - 1 && i <= currentPage + 1)}">
                                            <li class="page-item"><a class="page-link" href="?page=${i}&keyword=${keyword}">${i}</a></li>
                                        </c:when>
                                        <c:when test="${i == 4 && currentPage > 5}">
                                            <li class="page-item disabled"><span class="page-link">...</span></li>
                                        </c:when>
                                        <c:when test="${i == totalPages - 3 && currentPage < totalPages - 4}">
                                            <li class="page-item disabled"><span class="page-link">...</span></li>
                                        </c:when>
                                    </c:choose>
                                </c:forEach>
                                <c:if test="${currentPage < totalPages}">
                                    <li class="page-item">
                                        <a class="page-link" href="?page=${currentPage + 1}&keyword=${keyword}">›</a>
                                    </li>
                                </c:if>
                            </ul>
                        </nav>
                    </div>
                </c:if>
            </c:otherwise>
        </c:choose>
    </div>
</div>
