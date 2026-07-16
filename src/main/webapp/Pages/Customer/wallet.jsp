<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:if test="${empty requestScope.contentPage}">
    <c:redirect url="${pageContext.request.contextPath}/customer/wallet" />
</c:if>

<section class="wallet-page">
    <div class="wallet-hero">
        <div>
            <p class="wallet-breadcrumb">Customer / Wallet</p>
            <h1 class="wallet-title">My Wallet</h1>
            <p class="wallet-subtitle">
                Deposit money into your web wallet and use the balance to pay orders.
            </p>
        </div>
        <a class="wallet-outline-btn" href="${pageContext.request.contextPath}/customer/order-history">
            <span class="material-symbols-outlined">receipt_long</span>
            My Orders
        </a>
    </div>

    <div class="wallet-grid">
        <div class="wallet-balance-card">
            <span class="wallet-card-label">Current Balance</span>
            <strong class="wallet-balance">
                <fmt:formatNumber value="${wallet.balance}" type="number" groupingUsed="true" /> VND
            </strong>
            <div class="wallet-status-row">
                <span class="wallet-status wallet-status-${fn:toLowerCase(wallet.walletStatus)}">${wallet.walletStatus}</span>
                <span class="wallet-id">ID: ${wallet.walletId}</span>
            </div>
        </div>

        <form class="wallet-deposit-card" action="${pageContext.request.contextPath}/customer/wallet/deposit" method="post">
            <div class="wallet-form-head">
                <span class="material-symbols-outlined">account_balance_wallet</span>
                <div>
                    <h2>Deposit Money</h2>
                    <p>Deposit requests are now pending until Staff/Admin confirms the received payment.</p>
                </div>
            </div>

            <label class="wallet-label" for="amount">Amount</label>
            <input class="wallet-input" id="amount" name="amount" type="number" min="1000" step="1000" placeholder="Example: 100000" required />

            <label class="wallet-label" for="paymentMethod">Payment Method</label>
            <select class="wallet-input" id="paymentMethod" name="paymentMethod">
                <option value="Bank Transfer">Bank Transfer</option>
                <option value="VNPay">VNPay</option>
            </select>

            <button class="wallet-primary-btn" type="submit">
                <span class="material-symbols-outlined">add_card</span>
                Create Deposit Request
            </button>
        </form>
    </div>

    <div class="wallet-history-card">
        <div class="wallet-section-head">
            <h2>Payment History</h2>
            <span>${fn:length(paymentHistory)} transactions</span>
        </div>

        <c:choose>
            <c:when test="${empty paymentHistory}">
                <div class="wallet-empty">
                    <span class="material-symbols-outlined">payments</span>
                    <h3>No payment yet</h3>
                    <p>Your deposits, wallet purchases, COD records and refunds will appear here.</p>
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
                                <th>Date</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="payment" items="${paymentHistory}">
                                <tr>
                                    <td><strong>${payment.paymentId}</strong></td>
                                    <td>${payment.paymentType}</td>
                                    <td>${payment.paymentMethod}</td>
                                    <td>
                                        <span class="payment-status payment-status-${fn:toLowerCase(payment.paymentStatus)}">
                                            ${payment.paymentStatus}
                                        </span>
                                    </td>
                                    <td class="wallet-money">
                                        <fmt:formatNumber value="${payment.amount}" type="number" groupingUsed="true" /> VND
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty payment.orderId}">
                                                <a class="wallet-table-link" href="${pageContext.request.contextPath}/customer/order-detail?orderId=${payment.orderId}">
                                                    ${payment.orderId}
                                                </a>
                                            </c:when>
                                            <c:otherwise>-</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>${payment.createdAt}</td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</section>
