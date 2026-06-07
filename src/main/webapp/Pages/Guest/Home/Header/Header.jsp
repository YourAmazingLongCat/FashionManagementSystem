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
        <form class="search-form" method="get" action="${pageContext.request.contextPath}/home/search">
            <input class="search-input" type="text" name="search" placeholder="SEARCH PRODUCTS..." value="${param.search}"/>
            <button class="search-submit" type="submit">
                <span class="material-symbols-outlined">search</span>
            </button>
        </form>
    </div>
    
    <div class="header-actions">
        <c:choose>
            <c:when test="${not empty sessionScope.USER}">
                <div class="user-logged-info" style="display: flex; align-items: center; gap: 15px;">
                    <span class="user-name-display" style="font-family: 'Space Grotesk', sans-serif; font-weight: 800; font-size: 0.9rem; text-transform: uppercase; color: #000000; letter-spacing: 0.5px;">
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

        // Click ra ngoài → đóng
        document.addEventListener("click", () => {
            menu.style.display = "none";
            isOpen = false;
        });

        // Click trong menu không bị đóng
        menu.onclick = (e) => {
            e.stopPropagation();
        };
    }
</script>