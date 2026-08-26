<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="currentActive" value="${not empty param.activeMenu ? param.activeMenu : activeMenu}" />
<div class="col-md-3 col-lg-2 sidebar">
    <div class="brand">Management</div>
    <ul class="nav flex-column">
        <li class="nav-item">
            <a class="nav-link ${currentActive eq 'orders' ? 'active' : ''}" href="${pageContext.request.contextPath}/staff/orders">Manage Orders</a>
        </li>
        <li class="nav-item">
            <a class="nav-link ${currentActive eq 'products' ? 'active' : ''}" href="${pageContext.request.contextPath}/staff/products">Manage Products</a>
        </li>
        <li class="nav-item">
            <a class="nav-link ${currentActive eq 'variants' ? 'active' : ''}" href="${pageContext.request.contextPath}/staff/products?action=manageVariants">Manage Variants</a>
        </li>
        <li class="nav-item">
            <a class="nav-link ${currentActive eq 'warehouse' ? 'active' : ''}" href="${pageContext.request.contextPath}/staff/warehouse/inventory">Manage Warehouse</a>
        </li>
        <li class="nav-item mt-auto">
            <a class="nav-link ${currentActive eq 'profile' ? 'active' : ''}" href="${pageContext.request.contextPath}/profile">Profile</a>
        </li>
        <li class="nav-item">
            <a class="nav-link" href="${pageContext.request.contextPath}/auth/logout">Logout</a>
        </li>
    </ul>
</div>
