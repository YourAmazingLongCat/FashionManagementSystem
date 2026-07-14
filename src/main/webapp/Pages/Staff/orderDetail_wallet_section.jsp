<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<div class="wallet-payment-panel">
    <div class="wallet-form-head">
        <span class="material-symbols-outlined">payments</span>
        <div>
            <h2>Payment Information</h2>
            <p>Wallet payment status for this order.</p>
        </div>
    </div>

    <c:choose>
        <c:when test="${not empty payment}">
            <div class="wallet-payment-row">
                <span>Payment ID</span>
                <strong>${payment.paymentId}</strong>
            </div>
            <div class="wallet-payment-row">
                <span>Type</span>
                <strong>${payment.paymentType}</strong>
            </div>
            <div class="wallet-payment-row">
                <span>Method</span>
                <strong>${payment.paymentMethod}</strong>
            </div>
            <div class="wallet-payment-row">
                <span>Status</span>
                <strong class="payment-status payment-status-${fn:toLowerCase(payment.paymentStatus)}">${payment.paymentStatus}</strong>
            </div>
            <div class="wallet-payment-row">
                <span>Amount</span>
                <strong><fmt:formatNumber value="${payment.amount}" type="number" groupingUsed="true" /> VND</strong>
            </div>
            <div class="wallet-payment-row">
                <span>Paid At</span>
                <strong>${payment.paidAt}</strong>
            </div>
        </c:when>
        <c:otherwise>
            <div class="wallet-empty wallet-empty-small">
                <span class="material-symbols-outlined">money_off</span>
                <h3>No payment found</h3>
                <p>This order has not been paid through wallet yet.</p>
            </div>
        </c:otherwise>
    </c:choose>
</div>
