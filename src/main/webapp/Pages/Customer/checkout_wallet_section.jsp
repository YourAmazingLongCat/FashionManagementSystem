<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%-- Optional: place this inside checkout.jsp form if you want users to choose a payment method during checkout. --%>
<div class="wallet-deposit-card wallet-checkout-payment-box">
    <div class="wallet-form-head">
        <span class="material-symbols-outlined">payments</span>
        <div>
            <h2>Payment Method</h2>
            <p>Choose VNPay, Wallet or Cash On Delivery.</p>
        </div>
    </div>

    <label class="wallet-label" for="paymentMethod">Payment Method</label>
    <select class="wallet-input" id="paymentMethod" name="paymentMethod">
        <option value="VNPay">VNPay</option>
        <option value="Wallet">Wallet</option>
        <option value="COD">Cash On Delivery</option>
    </select>
</div>
