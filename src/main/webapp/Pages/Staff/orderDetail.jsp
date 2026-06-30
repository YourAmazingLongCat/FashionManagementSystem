<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.util.Locale" %>
<%@ page import="Models.Order" %>
<%@ page import="Models.OrderItem" %>
<%!
    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String badgeClass(String status) {
        if (status == null) return "order-badge-pending";
        return "order-badge-" + status.toLowerCase();
    }
%>
<%
    Order order = (Order) request.getAttribute("order");
    List<OrderItem> orderItems = (List<OrderItem>) request.getAttribute("orderItems");
    String errorMessage = (String) request.getAttribute("errorMessage");
    String successMessage = (String) request.getAttribute("successMessage");
    NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Staff Order Detail</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/order-ui.css">
</head>
<body class="order-page">
<div class="order-shell">
    <div class="order-topbar">
        <div>
            <div class="order-breadcrumb">Staff / Order detail</div>
            <h1 class="order-title">Manage Order</h1>
            <p class="order-subtitle">Confirm order and update shipping progress from this screen.</p>
        </div>
        <a class="order-btn order-btn-light" href="<%= request.getContextPath() %>/staff/orders">Back to list</a>
    </div>

    <% if (errorMessage != null) { %>
        <div class="order-alert order-alert-error"><%= errorMessage %></div>
    <% } %>
    <% if (successMessage != null) { %>
        <div class="order-alert order-alert-success"><%= successMessage %></div>
    <% } %>

    <% if (order == null) { %>
        <div class="order-card order-card-pad order-empty">
            <h3>Order not found</h3>
            <p>Please return to order management and try again.</p>
        </div>
    <% } else { %>
        <div class="order-grid order-grid-2">
            <div class="order-card order-card-pad">
                <div class="order-detail-header">
                    <div>
                        <div class="order-muted">Order ID</div>
                        <h2 class="order-id"><%= order.getOrderId() %></h2>
                    </div>
                    <span class="order-badge <%= badgeClass(order.getOrderStatus()) %>"><%= order.getOrderStatus() %></span>
                </div>

                <div style="height: 22px"></div>

                <div class="order-detail-meta">
                    <div class="order-meta-box">
                        <div class="order-meta-label">Customer ID</div>
                        <div class="order-meta-value"><%= safe(order.getCustomerId()) %></div>
                    </div>
                    <div class="order-meta-box">
                        <div class="order-meta-label">Placed at</div>
                        <div class="order-meta-value"><%= order.getPlacedAt() == null ? "N/A" : order.getPlacedAt().format(dateFormat) %></div>
                    </div>
                    <div class="order-meta-box">
                        <div class="order-meta-label">Phone</div>
                        <div class="order-meta-value"><%= safe(order.getPhone()) %></div>
                    </div>
                    <div class="order-meta-box">
                        <div class="order-meta-label">Total</div>
                        <div class="order-meta-value"><%= order.getTotalAmount() == null ? "0 ₫" : currency.format(order.getTotalAmount()) %></div>
                    </div>
                    <div class="order-meta-box" style="grid-column: 1 / -1;">
                        <div class="order-meta-label">Shipping address</div>
                        <div class="order-meta-value"><%= safe(order.getShippingAddress()) %></div>
                    </div>
                </div>

                <div style="height: 24px"></div>
                <h3 class="order-section-title">Products</h3>
                <div class="order-items">
                    <% if (orderItems == null || orderItems.isEmpty()) { %>
                        <div class="order-empty">No items found for this order.</div>
                    <% } else { %>
                        <% for (OrderItem item : orderItems) { %>
                            <div class="order-item-card">
                                <div class="order-item-thumb">PR</div>
                                <div>
                                    <div class="order-item-name">Variant <%= safe(item.getVariantId()) %></div>
                                    <div class="order-muted">Quantity: <%= item.getQuantity() %> × <%= currency.format(item.getUnitPrice()) %></div>
                                </div>
                                <div class="order-price"><%= currency.format(item.getSubTotal()) %></div>
                            </div>
                        <% } %>
                    <% } %>
                </div>
            </div>

            <aside class="order-grid">
                <div class="order-card order-card-pad">
                    <h3 class="order-section-title">Staff actions</h3>

                    <% if ("Pending".equals(order.getOrderStatus())) { %>
                        <form action="<%= request.getContextPath() %>/staff/confirm-order" method="post">
                            <input type="hidden" name="orderId" value="<%= order.getOrderId() %>">
                            <button class="order-btn order-btn-success" type="submit" style="width: 100%;">Confirm order</button>
                        </form>
                        <div style="height: 12px"></div>
                    <% } %>

                    <% if (!("Pending".equals(order.getOrderStatus()) || "Delivered".equals(order.getOrderStatus()) || "Cancelled".equals(order.getOrderStatus()))) { %>
                        <form action="<%= request.getContextPath() %>/staff/change-shipping-status" method="post">
                            <input type="hidden" name="orderId" value="<%= order.getOrderId() %>">
                            <label class="order-label" for="newStatus">Next shipping status</label>
                            <select class="order-select" id="newStatus" name="newStatus" required>
                                <option value="Processing">Processing</option>
                                <option value="Shipping">Shipping</option>
                                <option value="Delivered">Delivered</option>
                            </select>
                            <div style="height: 12px"></div>
                            <button class="order-btn order-btn-primary" type="submit" style="width: 100%;">Update status</button>
                        </form>
                    <% } %>

                    <% if ("Pending".equals(order.getOrderStatus()) || "Confirmed".equals(order.getOrderStatus()) || "Processing".equals(order.getOrderStatus())) { %>
                        <div style="height: 12px"></div>
                        <form action="<%= request.getContextPath() %>/customer/cancel-order" method="post" onsubmit="return confirm('Cancel this order?');">
                            <input type="hidden" name="orderId" value="<%= order.getOrderId() %>">
                            <button class="order-btn order-btn-danger" type="submit" style="width: 100%;">Cancel order</button>
                        </form>
                    <% } %>
                </div>

                <div class="order-card order-card-pad">
                    <h3 class="order-section-title">Summary</h3>
                    <div class="order-summary-row">
                        <span>Status</span>
                        <span class="order-badge <%= badgeClass(order.getOrderStatus()) %>"><%= order.getOrderStatus() %></span>
                    </div>
                    <div class="order-summary-row">
                        <span>Total amount</span>
                        <span class="order-summary-total"><%= order.getTotalAmount() == null ? "0 ₫" : currency.format(order.getTotalAmount()) %></span>
                    </div>
                </div>
            </aside>
        </div>
    <% } %>
</div>
</body>
</html>
