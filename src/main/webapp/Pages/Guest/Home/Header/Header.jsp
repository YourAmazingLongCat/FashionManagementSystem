<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<header class="header-guest-page">
    
    <div class="logo-area">
        <a class="logo-link" href="${pageContext.request.contextPath}/home" style="text-decoration: none;">
            <div class="peak-logo">
                <span class="logo-text">FASHION</span>
                <span class="logo-badge">X</span>
                <span class="logo-text">STORE</span>
            </div>
        </a>
    </div>

    <nav class="quick-nav">
        <a class="quick-nav-link" href="${pageContext.request.contextPath}/home">HOME</a>
<<<<<<< HEAD
        <c:if test="${not empty sessionScope.USER}">
            <a class="quick-nav-link" href="${pageContext.request.contextPath}/customer/order-history">MY ORDERS</a>
        </c:if>
        <c:if test="${not empty sessionScope.USER and sessionScope.USER.role eq 'Admin'}">
            <a class="quick-nav-link quick-nav-link--accent" href="${pageContext.request.contextPath}/admin/products">PRODUCT MANAGEMENT</a>
        </c:if>
        <c:if test="${not empty sessionScope.USER and (sessionScope.USER.role eq 'Staff' or sessionScope.USER.role eq 'Admin')}">
            <a class="quick-nav-link quick-nav-link--accent order-nav-accent" href="${pageContext.request.contextPath}/staff/orders">ORDER MANAGEMENT</a>
        </c:if>
=======
>>>>>>> e3e54207b4a5ae9c33f0518079c2c8ea883ea308
    </nav>
    
    <div class="categories-dropdown">
        <button class="categories-btn">
            <span class="material-symbols-outlined">menu</span>
            <span>CATEGORIES</span>
            <span class="material-symbols-outlined chevron">expand_more</span>
        </button>

        <div class="mega-menu">
            <div class="mega-grid">
                <c:forEach var="c" items="${categories}" varStatus="status">
                    <c:if test="${status.index % 5 == 0}">
                        <div>
                            <ul class="mega-list">
                    </c:if>
                                <li>
                                    <a class="category-card" href="${pageContext.request.contextPath}/home/search?category=${c.categoryId}">
                                        <div class="category-content">
                                            <span class="material-symbols-outlined category-icon">local_mall</span>
                                            <span class="category-name">${c.name}</span>
                                        </div>
                                    </a>
                                </li>
                    <c:if test="${status.index % 5 == 4 || status.last}">
                            </ul>
                        </div>
                    </c:if>
                </c:forEach>
            </div>
        </div>
    </div>

    <div class="search-container">
        <form id="headerSearchForm" class="search-form" method="get" action="${pageContext.request.contextPath}/home/search">
            <input id="headerSearchInput" class="search-input" type="text" name="search" placeholder="SEARCH PRODUCTS..." value="${param.search}" autocomplete="off"/>
            <button class="search-submit" type="submit">
                <span class="material-symbols-outlined">search</span>
            </button>
        </form>
    </div>
    
    <div class="header-actions">
        <a href="${pageContext.request.contextPath}/cart" class="cart-icon-btn" title="View Cart">
            <span class="material-symbols-outlined">shopping_bag</span>
            <span class="cart-badge" id="cartBadge">${empty sessionScope.cartCount ? 0 : sessionScope.cartCount}</span>
        </a>
        <c:if test="${not empty sessionScope.USER}">
            <a href="${pageContext.request.contextPath}/wishlist" class="wishlist-icon-btn" title="My Wishlist">
                <span class="material-symbols-outlined">favorite</span>
            </a>
        </c:if>
        <c:choose>
            <c:when test="${not empty sessionScope.USER}">
                <div class="user-logged-info" style="display: flex; align-items: center; gap: 15px;">
                    <span class="user-name-display" style="font-family: 'Space Grotesk', sans-serif; font-weight: 800; font-size: 0.9rem; text-transform: uppercase; color: #ffffff; letter-spacing: 0.5px;">
                        HI, ${sessionScope.USER.fullName}
                    </span>
                    <a class="login-btn" href="${pageContext.request.contextPath}/auth/logout" style="background-color: #ff3333; color: #ffffff; border-color: #000000;">
                        LOGOUT
                    </a>
                </div>
            </c:when>
            <c:otherwise>
                <a class="login-btn" href="${pageContext.request.contextPath}/auth/login">LOGIN</a>
            </c:otherwise>
        </c:choose>
    </div>
</header>

<script>
    const btn = document.querySelector(".categories-btn");
    const menu = document.querySelector(".mega-menu");
    let isOpen = false;

    if (btn && menu) {
        btn.onclick = (e) => {
            e.stopPropagation();
            isOpen = !isOpen;
            menu.style.display = isOpen ? "block" : "none";
        };

        document.addEventListener("click", () => {
            menu.style.display = "none";
            isOpen = false;
        });

        menu.onclick = (e) => {
            e.stopPropagation();
        };
    }

    const headerSearchForm = document.getElementById('headerSearchForm');
    const headerSearchInput = document.getElementById('headerSearchInput');

    if (headerSearchForm && headerSearchInput) {
        headerSearchInput.addEventListener('keydown', function (event) {
            if (event.key === 'Enter') {
                event.preventDefault();
                headerSearchForm.submit();
            }
        });
    }
</script>
