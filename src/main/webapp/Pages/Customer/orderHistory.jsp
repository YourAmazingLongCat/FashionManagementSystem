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
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Order History</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/order-ui.css">
</head>
<body class="order-page">
<div class="order-shell">
    <div class="order-topbar">
        <div>
            <div class="order-breadcrumb">Customer / Orders</div>
            <h1 class="order-title">My Orders</h1>
            <p class="order-subtitle">Track your purchases, review details, and cancel orders when allowed.</p>
        </div>
        <a class="order-btn order-btn-light" href="<%= request.getContextPath() %>/Pages/Customer/Cart.jsp">Back to cart</a>
    </div>

    <% if (errorMessage != null) { %>
        <div class="order-alert order-alert-error"><%= errorMessage %></div>
    <% } %>
    <% if (successMessage != null) { %>
        <div class="order-alert order-alert-success"><%= successMessage %></div>
    <% } %>

    <form class="order-search" action="<%= request.getContextPath() %>/customer/order-history" method="get">
        <input type="text" name="keyword" value="<%= safe(keyword) %>" placeholder="Search by order ID, status, or phone...">
        <button class="order-btn order-btn-primary" type="submit">Search</button>
    </form>

    <div style="height: 20px"></div>

    <div class="order-table-wrap">
        <% if (listOrders == null || listOrders.isEmpty()) { %>
            <div class="order-empty">
                <h3>No orders found</h3>
                <p>Your orders will appear here after checkout.</p>
            </div>
        <% } else { %>
            <table class="order-table">
                <thead>
                    <tr>
                        <th>Order</th>
                        <th>Date</th>
                        <th>Phone</th>
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
                            <div class="order-muted"><%= safe(order.getShippingAddress()) %></div>
                        </td>
                        <td><%= order.getPlacedAt() == null ? "N/A" : order.getPlacedAt().format(dateFormat) %></td>
                        <td><%= safe(order.getPhone()) %></td>
                        <td><span class="order-badge <%= badgeClass(order.getOrderStatus()) %>"><%= order.getOrderStatus() %></span></td>
                        <td class="order-price"><%= order.getTotalAmount() == null ? "0 ₫" : currency.format(order.getTotalAmount()) %></td>
                        <td>
                            <a class="order-btn order-btn-light" href="<%= request.getContextPath() %>/customer/order-detail?orderId=<%= order.getOrderId() %>">View</a>
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
