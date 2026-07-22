<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:if test="${empty requestScope.contentPage}">
    <c:redirect url="${pageContext.request.contextPath}/customer/wallet" />
</c:if>

<section class="customer-wallet-page">
    <div class="cw-container">
        <div class="cw-page-head">
            <div>
                <a class="cw-back-link" href="${pageContext.request.contextPath}/home">
                    <span class="material-symbols-outlined">arrow_back</span>
                    Continue shopping
                </a>
                <p class="cw-eyebrow">My account</p>
                <h1 class="cw-page-title">My Wallet</h1>
                <p class="cw-page-subtitle">Manage your wallet balance and review recent transactions.</p>
            </div>
            <a class="cw-secondary-btn" href="${pageContext.request.contextPath}/customer/order-history">
                <span class="material-symbols-outlined">receipt_long</span>
                My Orders
            </a>
        </div>

        <div class="cw-top-grid">
            <div class="cw-balance-card">
                <div class="cw-balance-head">
                    <div>
                        <span>Available balance</span>
                        <strong><fmt:formatNumber value="${wallet.balance}" type="number" groupingUsed="true" /> VND</strong>
                    </div>
                    <span class="material-symbols-outlined">account_balance_wallet</span>
                </div>

                <div class="cw-balance-footer">
                    <span class="cw-status cw-status-${fn:toLowerCase(wallet.walletStatus)}">${wallet.walletStatus}</span>
                    <span>Wallet ID: ${wallet.walletId}</span>
                </div>
            </div>

            <div class="cw-card cw-deposit-card">
                <div class="cw-card-head">
                    <div>
                        <h2>Add money</h2>
                        <p>Create a VNPay deposit request for your wallet.</p>
                    </div>
                    <span class="material-symbols-outlined cw-card-icon">add_card</span>
                </div>

                <form class="cw-form" action="${pageContext.request.contextPath}/customer/wallet/deposit" method="post">
                    <div class="cw-form-group">
                        <label for="amount">Amount</label>
                        <div class="cw-amount-field">
                            <input id="amount"
                                   name="amount"
                                   type="number"
                                   min="1000"
                                   step="1000"
                                   placeholder="Example: 100000"
                                   required />
                            <span>VND</span>
                        </div>
                    </div>

                    <div class="cw-form-group">
                        <label for="paymentMethod">Payment method</label>
                        <select id="paymentMethod" name="paymentMethod">
                            <option value="VNPay">VNPay</option>
                        </select>
                    </div>

                    <button class="cw-primary-btn cw-full-btn" type="submit">
                        Create deposit request
                        <span class="material-symbols-outlined">arrow_forward</span>
                    </button>
                </form>
            </div>
        </div>

        <div class="cw-card cw-history-card">
            <div class="cw-card-head">
                <div>
                    <h2>Transaction history</h2>
                    <p>${fn:length(paymentHistory)} transaction(s)</p>
                </div>
                <span class="material-symbols-outlined cw-card-icon">history</span>
            </div>

            <c:choose>
                <c:when test="${empty paymentHistory}">
                    <div class="cw-empty">
                        <span class="material-symbols-outlined">payments</span>
                        <h3>No transactions yet</h3>
                        <p>Your deposits, purchases and refunds will appear here.</p>
                    </div>
                </c:when>

                <c:otherwise>
                    <div class="cw-table-wrap">
                        <table class="cw-table">
                            <thead>
                                <tr>
                                    <th>Transaction</th>
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
                                        <td><strong class="cw-transaction-id">#${payment.paymentId}</strong></td>
                                        <td>${payment.paymentType}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${payment.paymentMethod eq 'COD'}">Cash On Delivery</c:when>
                                                <c:otherwise>${payment.paymentMethod}</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <span class="cw-payment-status cw-payment-${fn:toLowerCase(payment.paymentStatus)}">
                                                ${payment.paymentStatus}
                                            </span>
                                        </td>
                                        <td><strong class="cw-money"><fmt:formatNumber value="${payment.amount}" type="number" groupingUsed="true" /> VND</strong></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty payment.orderId}">
                                                    <a class="cw-order-link" href="${pageContext.request.contextPath}/customer/order-detail?orderId=${payment.orderId}">
                                                        #${payment.orderId}
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
    </div>
</section>
