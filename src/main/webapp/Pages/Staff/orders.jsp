<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.util.Locale" %>
<%@ page import="Models.Order" %>
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
    List<Order> listOrders = (List<Order>) request.getAttribute("listOrders");
    String keyword = (String) request.getAttribute("keyword");
    String errorMessage = (String) request.getAttribute("errorMessage");
    String successMessage = (String) request.getAttribute("successMessage");
    NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    int total = listOrders == null ? 0 : listOrders.size();
    int pending = 0;
    int shipping = 0;
    int delivered = 0;
    if (listOrders != null) {
        for (Order order : listOrders) {
            if ("Pending".equals(order.getOrderStatus())) pending++;
            if ("Shipping".equals(order.getOrderStatus())) shipping++;
            if ("Delivered".equals(order.getOrderStatus())) delivered++;
        }
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Staff Orders</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/order-ui.css">
</head>
<body class="order-page">
<div class="order-shell">
    <div class="order-topbar">
        <div>
            <div class="order-breadcrumb">Staff / Order Management</div>
            <h1 class="order-title">Orders</h1>
            <p class="order-subtitle">Manage customer orders, confirm requests, and update shipping status.</p>
        </div>
        <a class="order-btn order-btn-light" href="<%= request.getContextPath() %>/staff/orders">Refresh</a>
    </div>

    <% if (errorMessage != null) { %>
        <div class="order-alert order-alert-error"><%= errorMessage %></div>
    <% } %>
    <% if (successMessage != null) { %>
        <div class="order-alert order-alert-success"><%= successMessage %></div>
    <% } %>

    <div class="order-grid order-grid-3">
        <div class="order-stat-card">
            <div class="order-stat-label">Total orders</div>
            <div class="order-stat-value"><%= total %></div>
        </div>
        <div class="order-stat-card">
            <div class="order-stat-label">Pending</div>
            <div class="order-stat-value"><%= pending %></div>
        </div>
        <div class="order-stat-card">
            <div class="order-stat-label">Shipping / Delivered</div>
            <div class="order-stat-value"><%= shipping + delivered %></div>
        </div>
    </div>

    <div style="height: 20px"></div>

    <form class="order-search" action="<%= request.getContextPath() %>/staff/search-orders" method="get">
        <input type="text" name="keyword" value="<%= safe(keyword) %>" placeholder="Search by order ID, customer ID, status, or phone...">
        <button class="order-btn order-btn-primary" type="submit">Search</button>
    </form>

    <div style="height: 20px"></div>

    <div class="order-table-wrap">
        <% if (listOrders == null || listOrders.isEmpty()) { %>
            <div class="order-empty">
                <h3>No orders found</h3>
                <p>Orders matching your filters will appear here.</p>
            </div>
        <% } else { %>
            <table class="order-table">
                <thead>
                    <tr>
                        <th>Order</th>
                        <th>Customer</th>
                        <th>Date</th>
                        <th>Status</th>
                        <th>Total</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>
                <% for (Order order : listOrders) { %>
                    <tr>
                        <td>
                            <div class="order-id"><%= order.getOrderId() %></div>
                            <div class="order-muted"><%= safe(order.getPhone()) %></div>
                        </td>
                        <td><%= safe(order.getCustomerId()) %></td>
                        <td><%= order.getPlacedAt() == null ? "N/A" : order.getPlacedAt().format(dateFormat) %></td>
                        <td><span class="order-badge <%= badgeClass(order.getOrderStatus()) %>"><%= order.getOrderStatus() %></span></td>
                        <td class="order-price"><%= order.getTotalAmount() == null ? "0 ₫" : currency.format(order.getTotalAmount()) %></td>
                        <td>
                            <a class="order-btn order-btn-light" href="<%= request.getContextPath() %>/staff/order-detail?orderId=<%= order.getOrderId() %>">Manage</a>
                        </td>
                    </tr>
                <% } %>
                </tbody>
            </table>
        <% } %>
    </div>
</div>
</body>
</html>
