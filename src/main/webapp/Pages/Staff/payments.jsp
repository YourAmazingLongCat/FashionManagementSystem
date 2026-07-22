<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<style>
.wallet-pagination { display: flex; justify-content: center; margin-top: 24px; }
.pagination { display: flex; gap: 6px; list-style: none; padding: 0; margin: 0; }
.page-item { }
.page-link { display: flex; align-items: center; justify-content: center; min-width: 38px; height: 38px; padding: 0 10px; border: 1.5px solid #e2e8f0; border-radius: 10px; background: #fff; color: #334155; font-weight: 600; font-size: 0.9rem; text-decoration: none; transition: all 0.2s; }
.page-link:hover { background: #f1f5f9; border-color: #4338ca; color: #4338ca; }
.page-item.active .page-link { background: #4338ca; border-color: #4338ca; color: #fff; }
.page-item.disabled .page-link { color: #94a3b8; cursor: not-allowed; pointer-events: none; }
</style>

<section class="wallet-page">
    <div class="wallet-hero">
        <div>
            <p class="wallet-breadcrumb">Staff / Payments</p>
            <h1 class="wallet-title">Payment Management</h1>
            <p class="wallet-subtitle">
                Review wallet deposits, wallet payments, COD records and refunds.
            </p>
        </div>
        <a class="wallet-outline-btn" href="${pageContext.request.contextPath}/staff/products">
            <span class="material-symbols-outlined">inventory_2</span>
            Back to Product Management
        </a>
    </div>

    <div class="wallet-history-card">
        <div class="wallet-section-head">
            <h2>Pending Deposit Requests</h2>
            <span>${fn:length(pendingDeposits)} pending</span>
        </div>

        <c:choose>
            <c:when test="${empty pendingDeposits}">
                <div class="wallet-empty wallet-empty-small">
                    <span class="material-symbols-outlined">task_alt</span>
                    <h3>No pending deposit</h3>
                    <p>All deposit requests have been handled.</p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="wallet-table-wrap">
                    <table class="wallet-table">
                        <thead>
                            <tr>
                                <th>Payment ID</th>
                                <th>Wallet</th>
                                <th>Method</th>
                                <th>Amount</th>
                                <th>Created At</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="payment" items="${pendingDeposits}">
                                <tr>
                                    <td><strong>${payment.paymentId}</strong></td>
                                    <td>${payment.walletId}</td>
                                    <td>${payment.paymentMethod}</td>
                                    <td class="wallet-money"><fmt:formatNumber value="${payment.amount}" type="number" groupingUsed="true" /> VND</td>
                                    <td>${payment.createdAt}</td>
                                    <td>
                                        <form method="post" action="${pageContext.request.contextPath}/staff/complete-deposit" onsubmit="return confirm('Confirm this deposit and add balance to the wallet?');">
                                            <input type="hidden" name="paymentId" value="${payment.paymentId}" />
                                            <button class="wallet-primary-btn" type="submit" style="min-height: 38px; padding: 0 12px;">
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
    </div>

    <div class="wallet-history-card">
        <div class="wallet-section-head">
            <h2>All Payments</h2>
            <span>${fn:length(payments)} records</span>
        </div>

        <c:choose>
            <c:when test="${empty payments}">
                <div class="wallet-empty">
                    <span class="material-symbols-outlined">payments</span>
                    <h3>No payment record</h3>
                    <p>Payment records will appear here.</p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="wallet-table-wrap">
                    <table class="wallet-table">
                        <thead>
                            <tr>
                                <th>Payment ID</th>
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
                                    <td><strong>${payment.paymentId}</strong></td>
                                    <td>${payment.paymentType}</td>
                                    <td>${payment.paymentMethod}</td>
                                    <td>
                                        <span class="payment-status payment-status-${fn:toLowerCase(payment.paymentStatus)}">
                                            ${payment.paymentStatus}
                                        </span>
                                    </td>
                                    <td class="wallet-money"><fmt:formatNumber value="${payment.amount}" type="number" groupingUsed="true" /> VND</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty payment.orderId}">
                                                <a class="wallet-table-link" href="${pageContext.request.contextPath}/staff/order-detail?orderId=${payment.orderId}">
                                                    ${payment.orderId}
                                                </a>
                                            </c:when>
                                            <c:otherwise>-</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>${payment.createdAt}</td>
                                    <td><c:choose><c:when test="${empty payment.paidAt}">-</c:when><c:otherwise>${payment.paidAt}</c:otherwise></c:choose></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>

                    <c:if test="${totalPages > 1}">
                        <div class="wallet-pagination">
                            <nav>
                                <ul class="pagination">
                                    <c:if test="${currentPage > 1}">
                                        <li class="page-item">
                                            <a class="page-link" href="?page=${currentPage - 1}">‹</a>
                                        </li>
                                    </c:if>
                                    <c:forEach var="i" begin="1" end="${totalPages}">
                                        <c:choose>
                                            <c:when test="${i == currentPage}">
                                                <li class="page-item active"><span class="page-link">${i}</span></li>
                                            </c:when>
                                            <c:when test="${i <= 3 || i > totalPages - 3 || (i >= currentPage - 1 && i <= currentPage + 1)}">
                                                <li class="page-item"><a class="page-link" href="?page=${i}">${i}</a></li>
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
                                            <a class="page-link" href="?page=${currentPage + 1}">›</a>
                                        </li>
                                    </c:if>
                                </ul>
                            </nav>
                        </div>
                    </c:if>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</section>
