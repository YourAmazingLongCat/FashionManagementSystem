<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<form method="get" action="${pageContext.request.contextPath}/home/search">
    <main class="search-and-filter-page">
        <input type="hidden" name="search" value="${param.search}"> 
        
        <aside class="sidebar">
            <section class="filter-section">
                <h3 class="filter-section-title">CATEGORIES</h3>
                <div class="filter-group">
                    <c:forEach var="c" items="${categories}">
                        <label class="filter-item">
                            <input type="checkbox" name="category" value="${c.categoryId}"
                                   <c:if test="${selectedCategories != null && fn:contains(fn:join(selectedCategories, ','), c.categoryId)}">
                                       checked
                                   </c:if>>
                            <span class="custom-checkbox"></span>
                            <span class="filter-label">${c.name}</span>
                        </label>
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

            <section class="filter-section">
                <h3 class="filter-section-title">BRANDS</h3>
                <div class="filter-group">
                    <c:forEach var="b" items="${brands}">
                        <label class="filter-item">
                            <input type="checkbox" name="brand" value="${b.brandId}"
                                   <c:if test="${selectedBrands != null && fn:contains(fn:join(selectedBrands, ','), b.brandId)}">
                                       checked
                                   </c:if>>
                            <span class="custom-checkbox"></span>
                            <span class="filter-label">${b.name}</span>
                        </label>
                    </c:forEach>
                </div>
            </section>

            <section class="filter-section">
                <h3 class="filter-section-title">PRODUCT TYPE</h3>
                <div class="filter-group">
                    <label class="filter-item">
                        <input type="checkbox" name="type" value="Top"
                               <c:if test="${selectedTypes != null}"><c:forEach var="t" items="${selectedTypes}"><c:if test="${t eq 'Top'}">checked</c:if></c:forEach></c:if>>
                        <span class="custom-checkbox"></span>
                        <span class="filter-label">Tops & Tees</span>
                    </label>
                    <label class="filter-item">
                        <input type="checkbox" name="type" value="Bottom"
                               <c:if test="${selectedTypes != null}"><c:forEach var="t" items="${selectedTypes}"><c:if test="${t eq 'Bottom'}">checked</c:if></c:forEach></c:if>>
                        <span class="custom-checkbox"></span>
                        <span class="filter-label">Bottoms</span>
                    </label>
                    <label class="filter-item">
                        <input type="checkbox" name="type" value="Outerwear"
                               <c:if test="${selectedTypes != null}"><c:forEach var="t" items="${selectedTypes}"><c:if test="${t eq 'Outerwear'}">checked</c:if></c:forEach></c:if>>
                        <span class="custom-checkbox"></span>
                        <span class="filter-label">Outerwear</span>
                    </label>
                    <label class="filter-item">
                        <input type="checkbox" name="type" value="Accessory"
                               <c:if test="${selectedTypes != null}"><c:forEach var="t" items="${selectedTypes}"><c:if test="${t eq 'Accessory'}">checked</c:if></c:forEach></c:if>>
                        <span class="custom-checkbox"></span>
                        <span class="filter-label">Accessories</span>
                    </label>
                </div>
            </section>

            <button type="submit" class="btn btn-apply">APPLY FILTERS</button>
            <a href="${pageContext.request.contextPath}/home/search?reset=true" class="btn btn-reset">RESET ALL</a>
        </aside>

        <div class="content-area">
            <div class="results-header">
                <div>
                    <h2 class="results-title">SEARCH RESULTS FOR <span>"${param.search}"</span></h2>                               
                </div>
                <div class="sort-container">
                    <span class="results-count">SORT BY:</span>
                    <select class="sort-select" name="sort" onchange="this.form.submit()">
                        <option value="latest" ${selectedSort == 'latest' ? 'selected' : ''}>Latest Arrivals</option>
                        <option value="priceAsc" ${selectedSort == 'priceAsc' ? 'selected' : ''}>Price: Low to High</option>
                        <option value="priceDesc" ${selectedSort == 'priceDesc' ? 'selected' : ''}>Price: High to Low</option>
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
                                        <img class="product-image" src="${pageContext.request.contextPath}/${p.image}" />
                                        <button class="favorite-btn" onclick="event.preventDefault(); toggleWishlist(${p.productId})">
                                            <span class="material-symbols-outlined ${wishlistProductIds != null && wishlistProductIds.contains(p.productId) ? 'active' : ''}">
                                                favorite
                                            </span>
                                        </button>
                                    </div>
                                    <div class="product-info">
                                        <div class="product-name">${p.name}</div>
                                        <div class="product-price-row">
                                            <span class="price"><fmt:formatNumber value="${p.price}" type="number" groupingUsed="true"/> đ</span>
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

            <div class="pagination">
                <span class="pagination-info">
                    SHOWING ${showing} OF ${totalProduct} PRODUCTS
                </span>
                <div class="pagination-controls">
                    <c:if test="${currentPage > 1}">
                        <a class="page-btn" href="?${query}&page=${currentPage - 1}">
                            <span class="material-symbols-outlined">chevron_left</span>
                        </a>
                    </c:if>

                    <c:forEach var="i" begin="1" end="${totalPages}">
                        <a class="page-btn ${i == currentPage ? 'active' : ''}" href="?${query}&page=${i}">
                            ${i}
                        </a>
                    </c:forEach>

                    <c:if test="${currentPage < totalPages}">
                        <a class="page-btn" href="?${query}&page=${currentPage + 1}">
                            <span class="material-symbols-outlined">chevron_right</span>
                        </a>
                    </c:if>
                </div>
            </div>
        </div> 
    </main>
</form>

<script>
    function toggleWishlist(productId) {
        fetch('${pageContext.request.contextPath}/home/customer/toggle-wishlist', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'productId=' + productId
        }).then(() => {
            location.reload();
        });
    }
</script>