<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>
<%@ page import="Models.CartItem" %>
<%!
    private String safe(String value) {
        return value == null ? "" : value;
    }
%>
<%
    List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
    String errorMessage = (String) request.getAttribute("errorMessage");
    NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    BigDecimal totalAmount = BigDecimal.ZERO;
    if (cart != null) {
        for (CartItem item : cart) {
            if (item != null && item.getUnitPrice() != null) {
                totalAmount = totalAmount.add(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }
        }
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Checkout</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/order-ui.css">
</head>
<body class="order-page">
<div class="order-shell">
    <div class="order-topbar">
        <div>
            <div class="order-breadcrumb">Customer / Checkout</div>
            <h1 class="order-title">Checkout</h1>
            <p class="order-subtitle">Confirm your shipping information before placing the order.</p>
        </div>
        <a class="order-btn order-btn-light" href="<%= request.getContextPath() %>/Pages/Customer/Cart.jsp">Back to cart</a>
    </div>

    <% if (errorMessage != null) { %>
        <div class="order-alert order-alert-error"><%= errorMessage %></div>
    <% } %>

    <div class="order-grid order-grid-2">
        <form class="order-card order-card-pad" action="<%= request.getContextPath() %>/customer/checkout" method="post">
            <h2 class="order-section-title">Shipping information</h2>

            <div class="order-form-row">
                <label class="order-label" for="shippingAddress">Shipping address</label>
                <textarea class="order-textarea" id="shippingAddress" name="shippingAddress" required placeholder="Enter your full address..."></textarea>
            </div>

            <div class="order-form-row">
                <label class="order-label" for="phone">Phone number</label>
                <input class="order-input" id="phone" type="tel" name="phone" required placeholder="Example: 0901234567">
            </div>

            <button class="order-btn order-btn-primary" type="submit" style="width: 100%;">Place order</button>
        </form>

        <aside class="order-card order-card-pad">
            <h2 class="order-section-title">Order summary</h2>

            <div class="order-items">
                <% if (cart == null || cart.isEmpty()) { %>
                    <div class="order-empty">
                        <h3>Your cart is empty</h3>
                        <p>Add products before checkout.</p>
                    </div>
                <% } else { %>
                    <% for (CartItem item : cart) { %>
                        <div class="order-item-card">
                            <div class="order-item-thumb">PR</div>
                            <div>
                                <div class="order-item-name">Variant <%= safe(item.getVariantId()) %></div>
                                <div class="order-muted">Quantity: <%= item.getQuantity() %> × <%= currency.format(item.getUnitPrice()) %></div>
                            </div>
                            <div class="order-price"><%= currency.format(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))) %></div>
                        </div>
                    <% } %>
                <% } %>
            </div>

            <div style="height: 18px"></div>
            <div class="order-summary-row">
                <span>Subtotal</span>
                <strong><%= currency.format(totalAmount) %></strong>
            </div>
            <div class="order-summary-row">
                <span>Shipping fee</span>
                <strong>0 ₫</strong>
            </div>
            <div class="order-summary-row">
                <span>Total</span>
                <span class="order-summary-total"><%= currency.format(totalAmount) %></span>
            </div>
        </aside>
    </div>
</div>
</body>
</html>
