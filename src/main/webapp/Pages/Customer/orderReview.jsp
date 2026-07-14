<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="Models.Order"%>
<%@page import="Models.CartItem"%>
<%@page import="java.util.List"%>
<%@page import="java.math.BigDecimal"%>

<%
    Order orderPreview = (Order) request.getAttribute("orderPreview");
    List<CartItem> cart = (List<CartItem>) request.getAttribute("cart");
    String errorMessage = (String) request.getAttribute("errorMessage");
%>

<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Order Review</title>
        <link rel="stylesheet" href="<%= request.getContextPath() %>/Pages/Customer/checkout.css">
    </head>

    <body>
        <div class="page-wrapper">

            <header class="top-header">
                <div class="logo-box">
                    <div class="logo-mark">S</div>
                    <div>
                        <h2>Shop</h2>
                        <p>Online store</p>
                    </div>
                </div>

                <nav class="nav-menu">
                    <a href="<%= request.getContextPath() %>/home">Home</a>
                    <a href="<%= request.getContextPath() %>/shop">Shop</a>
                    <a href="<%= request.getContextPath() %>/cart">Cart</a>
                    <a class="active" href="<%= request.getContextPath() %>/customer/checkout">Checkout</a>
                    <a href="<%= request.getContextPath() %>/customer/order-history">Order History</a>
                </nav>
            </header>

            <section class="hero-section">
                <div class="title-slash">
                    <h1>Order Review</h1>
                    <p>Please check your order before placing it.</p>
                </div>

                <div class="order-tabs">
                    <span class="tab">Checkout</span>
                    <span class="tab active-tab">Order Review</span>
                    <span class="tab">Order Detail</span>
                </div>
            </section>

            <% if (errorMessage != null) { %>
                <div class="error-box">
                    <%= errorMessage %>
                </div>
            <% } %>

            <% if (orderPreview != null) { %>

                <main class="checkout-container">

                    <section class="checkout-panel form-panel">
                        <div class="panel-title">
                            <span>Shipping Information</span>
                        </div>

                        <div class="detail-info-box">
                            <div class="detail-line">
                                <span>Customer ID</span>
                                <strong><%= orderPreview.getCustomerId() %></strong>
                            </div>

                            <div class="detail-line">
                                <span>Phone</span>
                                <strong><%= orderPreview.getPhone() %></strong>
                            </div>

                            <div class="detail-line address-line">
                                <span>Shipping Address</span>
                                <strong><%= orderPreview.getShippingAddress() %></strong>
                            </div>

                            <div class="detail-line">
                                <span>Status</span>
                                <strong>
                                    <span class="status-badge status-pending">
                                        <%= orderPreview.getOrderStatus() %>
                                    </span>
                                </strong>
                            </div>
                        </div>

                        <a class="place-order-btn link-btn"
                           href="<%= request.getContextPath() %>/customer/checkout">
                            Edit Information
                        </a>
                    </section>

                    <section class="checkout-panel item-panel">
                        <div class="panel-title">
                            <span>Review Items</span>
                        </div>

                        <div class="item-table">
                            <div class="item-row item-header">
                                <div>Item</div>
                                <div>Qty</div>
                                <div>Unit Price</div>
                                <div>Total</div>
                            </div>

                            <% if (cart != null && !cart.isEmpty()) { %>
                                <% for (CartItem item : cart) { %>
                                    <%
                                        BigDecimal subTotal = item.getUnitPrice()
                                                .multiply(BigDecimal.valueOf(item.getQuantity()));
                                    %>

                                    <div class="item-row">
                                        <div class="item-info">
                                            <div class="item-image">
                                                <span>ITEM</span>
                                            </div>

                                            <div>
                                                <h4><%= item.getVariantId() %></h4>
                                                <p>Variant ID: <%= item.getVariantId() %></p>
                                            </div>
                                        </div>

                                        <div class="item-qty">
                                            <%= item.getQuantity() %>
                                        </div>

                                        <div>
                                            <%= item.getUnitPrice() %>
                                        </div>

                                        <div class="item-total">
                                            <%= subTotal %>
                                        </div>
                                    </div>
                                <% } %>
                            <% } else { %>
                                <div class="empty-cart">
                                    Your cart is empty.
                                </div>
                            <% } %>
                        </div>
                    </section>

                    <aside class="summary-panel">
                        <div class="summary-title">
                            Order Summary
                        </div>

                        <div class="summary-line">
                            <span>Items</span>
                            <strong><%= cart == null ? 0 : cart.size() %></strong>
                        </div>

                        <div class="summary-line">
                            <span>Status</span>
                            <strong><%= orderPreview.getOrderStatus() %></strong>
                        </div>

                        <div class="summary-divider"></div>

                        <div class="summary-total">
                            <span>Total</span>
                            <strong><%= orderPreview.getTotalAmount() %></strong>
                        </div>

                        <form action="<%= request.getContextPath() %>/customer/checkout" method="post">
                            <input type="hidden" name="shippingAddress" value="<%= orderPreview.getShippingAddress() %>">
                            <input type="hidden" name="phone" value="<%= orderPreview.getPhone() %>">

                            <button type="submit" class="place-order-btn">
                                Place Order
                            </button>
                        </form>

                        <p class="secure-text">Please confirm your order information.</p>
                    </aside>

                </main>

            <% } %>

        </div>
    </body>
</html>