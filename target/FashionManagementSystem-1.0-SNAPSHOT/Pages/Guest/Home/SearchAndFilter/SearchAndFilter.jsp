<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<form id="searchFilterForm" method="get" action="${pageContext.request.contextPath}/home/search">
    <main class="search-and-filter-page">
        <input type="hidden" name="search" value="${param.search}">

        <aside class="sidebar">
            <section class="filter-section">
                <div class="filter-group">
                    <c:forEach var="c" items="${categories}">
                        <c:choose>
                            <c:when test="${selectedCategoryIds.contains(c.categoryId)}">
                                <label class="filter-item">
                                    <input type="checkbox" name="category" value="${c.categoryId}" checked="checked" onchange="submitSearchFilterForm()">
                                    <span class="custom-checkbox"></span>
                                    <span class="filter-label">${c.name}</span>
                                </label>
                            </c:when>
                            <c:otherwise>
                                <label class="filter-item">
                                    <input type="checkbox" name="category" value="${c.categoryId}" onchange="submitSearchFilterForm()">
                                    <span class="custom-checkbox"></span>
                                    <span class="filter-label">${c.name}</span>
                                </label>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>
                </div>
            </section>

            <section class="filter-section">
                <h3 class="filter-section-title">PRICE RANGE</h3>
                <div class="price-inputs">
                    <div class="price-input-wrapper">
                        <label>Min (đ)</label>
                        <input type="number" class="price-input-field" name="minPrice" value="${selectedMinPrice}" placeholder="0" />
                    </div>
                    <span class="price-separator">-</span>
                    <div class="price-input-wrapper">
                        <label>Max (đ)</label>
                        <input type="number" class="price-input-field" name="maxPrice" value="${selectedMaxPrice}" placeholder="0" />
                    </div>
                </div>
            </section>

            <button type="submit" class="btn btn-apply">APPLY FILTERS</button>
            <a href="${pageContext.request.contextPath}/home/search" class="btn btn-reset">RESET ALL</a>
        </aside>

        <div class="content-area">
            <div class="results-header">
                <div>
                    <h2 class="results-title">
                        <c:choose>
                            <c:when test="${not empty param.search}">SEARCH RESULTS FOR <span>"${param.search}"</span></c:when>
                            <c:otherwise>ALL PRODUCTS</c:otherwise>
                        </c:choose>
                    </h2>
                </div>
                <div class="sort-container">
                    <span class="results-count">SORT BY:</span>
                    <select class="sort-select" name="sort" onchange="submitSearchFilterForm()">
                        <option value="latest" <c:if test="${isLatestSort}">selected="selected"</c:if>>Latest Arrivals</option>
                        <option value="priceAsc" <c:if test="${isPriceAscSort}">selected="selected"</c:if>>Price: Low to High</option>
                        <option value="priceDesc" <c:if test="${isPriceDescSort}">selected="selected"</c:if>>Price: High to Low</option>
                    </select>
                </div>
            </div>

            <c:choose>
                <c:when test="${not empty products}">
                    <div class="products-grid">
                        <c:forEach var="p" items="${products}">
                            <a href="${pageContext.request.contextPath}/home/view-detail-product?productId=${p.productId}" class="product-link">
                                <div class="product-card">
                                    <div class="product-image-container">
                                        <c:choose>
                                            <c:when test="${empty p.primaryImageUrl}">
                                                <img class="product-image" src="https://via.placeholder.com/600x800?text=No+Image" alt="${p.name}" />
                                            </c:when>
                                            <c:otherwise>
                                                <img class="product-image" src="${p.primaryImageUrl}" alt="${p.name}" />
                                            </c:otherwise>
                                        </c:choose>
                                        <button type="button" class="favorite-btn" onclick="event.preventDefault(); toggleWishlist('${p.productId}', this)">
                                            <c:choose>
                                                <c:when test="${wishlistProductIds.contains(p.productId)}">
                                                    <span class="material-symbols-outlined active">favorite</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="material-symbols-outlined">favorite</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </button>
                                        <a href="${pageContext.request.contextPath}/home/view-detail-product?productId=${p.productId}" class="quick-add-btn">
                                            <span class="material-symbols-outlined">shopping_bag</span>
                                        </a>
                                    </div>
                                    <div class="product-info">
                                        <div class="product-name">${p.name}</div>
                                        <div class="product-price-row">
                                            <span class="price"><fmt:formatNumber value="${productDAO.getDisplayPrice(p)}" type="number" groupingUsed="true"/> đ</span>
                                        </div>
                                    </div>
                                </div>
                            </a>
                        </c:forEach>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="empty-search">
                        <span class="material-symbols-outlined">search_off</span>
                        <h3>NO PRODUCTS FOUND</h3>
                        <p>Try changing filters or search keywords.</p>
                    </div>
                </c:otherwise>
            </c:choose>

            <c:if test="${totalPages > 1}">
                <div class="pagination">
                    <span class="pagination-info">SHOWING ${showing} OF ${totalProduct} PRODUCTS</span>
                    <div class="pagination-controls">
                        <c:if test="${currentPage > 1}">
                            <a class="page-btn" href="?${query}&page=${currentPage - 1}">
                                <span class="material-symbols-outlined">chevron_left</span>
                            </a>
                        </c:if>

                        <c:forEach var="i" begin="1" end="${totalPages}">
                            <c:choose>
                                <c:when test="${i == currentPage}">
                                    <a class="page-btn active" href="?${query}&page=${i}">${i}</a>
                                </c:when>
                                <c:otherwise>
                                    <a class="page-btn" href="?${query}&page=${i}">${i}</a>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>

                        <c:if test="${currentPage < totalPages}">
                            <a class="page-btn" href="?${query}&page=${currentPage + 1}">
                                <span class="material-symbols-outlined">chevron_right</span>
                            </a>
                        </c:if>
                    </div>
                </div>
            </c:if>
        </div>
    </main>
</form>

<script>
    function submitSearchFilterForm() {
        const form = document.getElementById('searchFilterForm');
        if (form) {
            form.submit();
        }
    }

    function toggleWishlist(productId, button) {
        fetch('${pageContext.request.contextPath}/home/customer/toggle-wishlist', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'productId=' + encodeURIComponent(productId)
        }).then(response => response.json()).then(data => {
            if (data && data.requiresLogin) {
                window.location.href = '${pageContext.request.contextPath}/auth/login';
                return;
            }
            if (data && data.favorite !== undefined) {
                const icon = button.querySelector('.material-symbols-outlined');
                if (icon) {
                    icon.classList.toggle('active', data.favorite === true);
                }
            }
        }).catch(error => {
            console.error('Wishlist toggle error:', error);
        });
    }
</script>
