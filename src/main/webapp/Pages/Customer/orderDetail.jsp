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

    private int statusIndex(String status) {
        if ("Pending".equals(status)) return 0;
        if ("Confirmed".equals(status)) return 1;
        if ("Processing".equals(status)) return 2;
        if ("Shipping".equals(status)) return 3;
        if ("Delivered".equals(status)) return 4;
        if ("Cancelled".equals(status)) return -1;
        return 0;
    }
%>
<%
    Order order = (Order) request.getAttribute("order");
    List<OrderItem> orderItems = (List<OrderItem>) request.getAttribute("orderItems");
    String errorMessage = (String) request.getAttribute("errorMessage");
    String successMessage = (String) request.getAttribute("successMessage");
    NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    String[] steps = {"Pending", "Confirmed", "Processing", "Shipping", "Delivered"};
    int currentStep = order == null ? 0 : statusIndex(order.getOrderStatus());
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Order Detail</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/order-ui.css">
</head>
<body class="order-page">
<div class="order-shell">
    <div class="order-topbar">
        <div>
            <div class="order-breadcrumb">Customer / Order detail</div>
            <h1 class="order-title">Order Detail</h1>
            <p class="order-subtitle">Review your order information and shipping progress.</p>
        </div>
        <a class="order-btn order-btn-light" href="<%= request.getContextPath() %>/customer/order-history">Back to orders</a>
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
            <p>Please return to your order history and try again.</p>
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
                        <div class="order-meta-label">Placed at</div>
                        <div class="order-meta-value"><%= order.getPlacedAt() == null ? "N/A" : order.getPlacedAt().format(dateFormat) %></div>
                    </div>
                    <div class="order-meta-box">
                        <div class="order-meta-label">Phone</div>
                        <div class="order-meta-value"><%= safe(order.getPhone()) %></div>
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

            <div class="order-grid">
                <div class="order-card order-card-pad">
                    <h3 class="order-section-title">Shipping progress</h3>
                    <div class="order-timeline">
                        <% if ("Cancelled".equals(order.getOrderStatus())) { %>
                            <div class="order-timeline-step is-cancelled">
                                <div class="order-timeline-dot">!</div>
                                <div>
                                    <strong>Cancelled</strong>
                                    <div class="order-muted">This order has been cancelled.</div>
                                </div>
                            </div>
                        <% } else { %>
                            <% for (int i = 0; i < steps.length; i++) { %>
                                <div class="order-timeline-step <%= i < currentStep ? "is-done" : (i == currentStep ? "is-active" : "") %>">
                                    <div class="order-timeline-dot"><%= i + 1 %></div>
                                    <div>
                                        <strong><%= steps[i] %></strong>
                                        <div class="order-muted"><%= i <= currentStep ? "Completed or current step" : "Waiting" %></div>
                                    </div>
                                </div>
                            <% } %>
                        <% } %>
                    </div>
                </div>

                <div class="order-card order-card-pad">
                    <h3 class="order-section-title">Summary</h3>
                    <div class="order-summary-row">
                        <span>Subtotal</span>
                        <strong><%= order.getTotalAmount() == null ? "0 ₫" : currency.format(order.getTotalAmount()) %></strong>
                    </div>
                    <div class="order-summary-row">
                        <span>Shipping fee</span>
                        <strong>0 ₫</strong>
                    </div>
                    <div class="order-summary-row">
                        <span>Total</span>
                        <span class="order-summary-total"><%= order.getTotalAmount() == null ? "0 ₫" : currency.format(order.getTotalAmount()) %></span>
                    </div>

                    <% if (!("Shipping".equals(order.getOrderStatus()) || "Delivered".equals(order.getOrderStatus()) || "Cancelled".equals(order.getOrderStatus()))) { %>
                        <div style="height: 16px"></div>
                        <form action="<%= request.getContextPath() %>/customer/cancel-order" method="post" onsubmit="return confirm('Are you sure you want to cancel this order?');">
                            <input type="hidden" name="orderId" value="<%= order.getOrderId() %>">
                            <button class="order-btn order-btn-danger" type="submit" style="width: 100%;">Cancel order</button>
                        </form>
                    <% } %>
                </div>
            </div>
        </div>
    <% } %>
</div>
</body>
</html>
