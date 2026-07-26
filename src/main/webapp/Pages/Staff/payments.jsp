<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<div class="content-page order-page staff-ops-page">
    <div class="order-container">
        <c:set var="pagePaidPayments" value="${0}" />
        <c:set var="pagePendingPayments" value="${0}" />
        <c:forEach var="payment" items="${payments}">
            <c:if test="${payment.paymentStatus eq 'Paid'}">
                <c:set var="pagePaidPayments" value="${pagePaidPayments + 1}" />
            </c:if>
            <c:if test="${payment.paymentStatus eq 'Pending'}">
                <c:set var="pagePendingPayments" value="${pagePendingPayments + 1}" />
            </c:if>
        </c:forEach>

        <section class="order-hero staff-ops-hero">
            <div>
                <p class="order-eyebrow">Staff Operations</p>
                <h1 class="order-title">Payment Management</h1>
                <p class="order-subtitle">
                    Approve wallet deposits and review purchase, refund, COD, wallet, and VNPay transactions.
                </p>
            </div>
            <div class="order-actions-row">
                <a class="order-btn" href="${pageContext.request.contextPath}/staff/dashboard">
                    <span class="material-symbols-outlined">dashboard</span>
                    Dashboard
                </a>
            </div>
        </section>

        <nav class="staff-module-tabs" aria-label="Order and payment management">
            <a class="staff-module-tab" href="${pageContext.request.contextPath}/staff/orders">
                <span class="material-symbols-outlined">receipt_long</span>
                Orders
            </a>
            <a class="staff-module-tab active" href="${pageContext.request.contextPath}/staff/payments">
                <span class="material-symbols-outlined">payments</span>
                Payments
            </a>
        </nav>

        <div class="order-grid order-grid-4 staff-stat-grid">
            <div class="order-stat-card">
                <span class="order-stat-label">Total records</span>
                <span class="order-stat-value">${empty totalPayments ? fn:length(payments) : totalPayments}</span>
            </div>
            <div class="order-stat-card">
                <span class="order-stat-label">Pending deposits</span>
                <span class="order-stat-value">${fn:length(pendingDeposits)}</span>
            </div>
            <div class="order-stat-card">
                <span class="order-stat-label">Paid on page</span>
                <span class="order-stat-value">${pagePaidPayments}</span>
            </div>
            <div class="order-stat-card">
                <span class="order-stat-label">Pending on page</span>
                <span class="order-stat-value">${pagePendingPayments}</span>
            </div>
        </div>

        <section class="order-panel order-panel-padding staff-list-panel staff-priority-panel">
            <div class="order-panel-header staff-list-header">
                <div>
                    <h2 class="order-section-title">Pending Deposit Requests</h2>
                    <p class="order-muted">Complete a request only after the deposit has been verified.</p>
                </div>
                <span class="order-status status-pending">${fn:length(pendingDeposits)} Pending</span>
            </div>

            <c:choose>
                <c:when test="${empty pendingDeposits}">
                    <div class="order-empty staff-panel-empty staff-panel-empty-compact">
                        <span class="material-symbols-outlined">task_alt</span>
                        <h3>No pending deposit</h3>
                        <p>All wallet deposit requests have been handled.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="order-table-wrap staff-table-wrap">
                        <table class="order-table staff-data-table">
                            <thead>
                                <tr>
                                    <th>Payment</th>
                                    <th>Wallet</th>
                                    <th>Method</th>
                                    <th>Amount</th>
                                    <th>Created At</th>
                                    <th class="staff-action-column">Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="payment" items="${pendingDeposits}">
                                    <tr>
                                        <td><div class="order-code">${payment.paymentId}</div></td>
                                        <td>${payment.walletId}</td>
                                        <td><span class="staff-method-badge">${payment.paymentMethod}</span></td>
                                        <td class="order-price"><fmt:formatNumber value="${payment.amount}" type="number" groupingUsed="true" /> VND</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty payment.createdAt}">
                                                    ${fn:replace(payment.createdAt, 'T', ' ')}
                                                </c:when>
                                                <c:otherwise>-</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="staff-action-column">
                                            <form method="post" action="${pageContext.request.contextPath}/staff/complete-deposit"
                                                  onsubmit="return confirm('Confirm this deposit and add balance to the wallet?');">
                                                <input type="hidden" name="paymentId" value="${payment.paymentId}" />
                                                <button class="order-btn order-btn-success staff-table-action" type="submit">
                                                    <span class="material-symbols-outlined">check_circle</span>
                                                    Complete
                                                </button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

        <section class="order-panel order-panel-padding staff-list-panel">
            <div class="order-panel-header staff-list-header">
                <div>
                    <h2 class="order-section-title">All Payment Records</h2>
                    <p class="order-muted">Showing ${fn:length(payments)} record(s) on page ${empty currentPage ? 1 : currentPage}.</p>
                </div>
                <form class="staff-inline-page-size" method="get" action="${pageContext.request.contextPath}/staff/payments">
                    <label>
                        <span>Rows</span>
                        <select name="pageSize" onchange="this.form.submit()">
                            <option value="5" ${pageSize == 5 ? 'selected' : ''}>5</option>
                            <option value="10" ${empty pageSize or pageSize == 10 ? 'selected' : ''}>10</option>
                            <option value="20" ${pageSize == 20 ? 'selected' : ''}>20</option>
                            <option value="50" ${pageSize == 50 ? 'selected' : ''}>50</option>
                        </select>
                    </label>
                </form>
            </div>

            <c:choose>
                <c:when test="${empty payments}">
                    <div class="order-empty staff-panel-empty">
                        <span class="material-symbols-outlined">payments</span>
                        <h3>No payment record</h3>
                        <p>Payment records will appear here after customers place orders or request wallet deposits.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="order-table-wrap staff-table-wrap">
                        <table class="order-table staff-data-table staff-payment-table">
                            <thead>
                                <tr>
                                    <th>Payment</th>
                                    <th>Type</th>
                                    <th>Method</th>
                                    <th>Status</th>
                                    <th>Amount</th>
                                    <th>Order</th>
                                    <th>Created At</th>
                                    <th>Paid At</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="payment" items="${payments}">
                                    <tr>
                                        <td><div class="order-code">${payment.paymentId}</div></td>
                                        <td>${payment.paymentType}</td>
                                        <td><span class="staff-method-badge">${payment.paymentMethod}</span></td>
                                        <td>
                                            <span class="order-status status-${fn:toLowerCase(payment.paymentStatus)}">
                                                ${payment.paymentStatus}
                                            </span>
                                        </td>
                                        <td class="order-price"><fmt:formatNumber value="${payment.amount}" type="number" groupingUsed="true" /> VND</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty payment.orderId}">
                                                    <a class="staff-record-link" href="${pageContext.request.contextPath}/staff/order-detail?orderId=${payment.orderId}">
                                                        ${payment.orderId}
                                                        <span class="material-symbols-outlined">open_in_new</span>
                                                    </a>
                                                </c:when>
                                                <c:otherwise>-</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty payment.createdAt}">
                                                    ${fn:replace(payment.createdAt, 'T', ' ')}
                                                </c:when>
                                                <c:otherwise>-</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty payment.paidAt}">
                                                    ${fn:replace(payment.paidAt, 'T', ' ')}
                                                </c:when>
                                                <c:otherwise>-</c:otherwise>
                                            </c:choose>
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
                    <c:url var="previousPageUrl" value="/staff/payments">
                        <c:param name="page" value="${currentPage - 1}" />
                        <c:param name="pageSize" value="${pageSize}" />
                    </c:url>
                    <c:url var="nextPageUrl" value="/staff/payments">
                        <c:param name="page" value="${currentPage + 1}" />
                        <c:param name="pageSize" value="${pageSize}" />
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
