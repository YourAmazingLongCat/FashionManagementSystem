<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<form id="searchFilterForm" method="get" action="${pageContext.request.contextPath}/home/search">
    <main class="sf-page">
        <input type="hidden" name="search" value="${param.search}">

        <!-- ========== SIDEBAR: FILTERS ========== -->
        <aside class="sf-sidebar">
            <section class="sf-section">
                <h3 class="sf-title">PRICE RANGE</h3>
                <div class="sf-price-row">
                    <div class="sf-price-wrap">
                        <span class="sf-currency">đ</span>
                        <input type="number" name="minPrice" placeholder="Min" value="${selectedMinPrice}"
                               class="sf-price-input" min="0" step="1000">
                    </div>
                    <span class="sf-dash">-</span>
                    <div class="sf-price-wrap">
                        <span class="sf-currency">đ</span>
                        <input type="number" name="maxPrice" placeholder="Max" value="${selectedMaxPrice}"
                               class="sf-price-input" min="0" step="1000">
                    </div>
                </div>
                <div class="sf-price-error" id="priceError"></div>
                <button type="button" class="sf-btn-apply" onclick="sfApplyPrice()">Apply</button>
            </section>

            <section class="sf-section">
                <h3 class="sf-title">CATEGORIES</h3>
                <div class="sf-group">
                    <c:forEach var="c" items="${categories}">
                        <label class="sf-item">
                            <input type="checkbox" name="category" value="${c.categoryId}"
                                   <c:if test="${selectedCategoryIds.contains(c.categoryId)}">checked="checked"</c:if>
                                   onchange="sfSubmit()">
                            <span class="sf-box"></span>
                            <span class="sf-label">${c.name}</span>
                        </label>
                    </c:forEach>
                </div>
            </section>

            <a href="${pageContext.request.contextPath}/home/search" class="sf-btn-reset">RESET ALL</a>
        </aside>

        <!-- ========== MAIN: RESULTS ========== -->
        <div class="sf-main">
            <div class="sf-header">
                <h2 class="sf-results-title">
                    <c:choose>
                        <c:when test="${not empty param.search}">SEARCH RESULTS FOR <span>"${param.search}"</span></c:when>
                        <c:otherwise>ALL PRODUCTS</c:otherwise>
                    </c:choose>
                </h2>
                <div class="sf-sort">
                    <span class="sf-sort-label">SORT BY:</span>
                    <select class="sf-sort-select" name="sort" onchange="sfSubmit()">
                        <option value="latest" <c:if test="${isLatestSort}">selected="selected"</c:if>>Latest Arrivals</option>
                        <option value="priceAsc" <c:if test="${isPriceAscSort}">selected="selected"</c:if>>Price: Low to High</option>
                        <option value="priceDesc" <c:if test="${isPriceDescSort}">selected="selected"</c:if>>Price: High to Low</option>
                    </select>
                </div>
            </div>

            <c:choose>
                <c:when test="${not empty products}">
                    <div class="sf-grid">
                        <c:forEach var="p" items="${products}">
                            <a href="${pageContext.request.contextPath}/home/view-detail-product?productId=${p.productId}" class="sf-card-link">
                                <div class="sf-card">
                                    <div class="sf-img-box">
                                        <c:choose>
                                            <c:when test="${empty p.primaryImageUrl}">
                                                <img class="sf-img" src="https://via.placeholder.com/600x800?text=No+Image" alt="${p.name}" />
                                            </c:when>
                                            <c:otherwise>
                                                <img class="sf-img" src="${pageContext.request.contextPath}${p.primaryImageUrl}" alt="${p.name}" />
                                            </c:otherwise>
                                        </c:choose>
                                        <button type="button" class="sf-fav" onclick="event.preventDefault(); sfToggleFav('${p.productId}', this)">
                                            <c:choose>
                                                <c:when test="${wishlistProductIds.contains(p.productId)}">
                                                    <span class="material-symbols-outlined sf-fav-on">favorite</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="material-symbols-outlined">favorite_border</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </button>
                                    </div>
                                    <div class="sf-info">
                                        <div class="sf-name">${p.name}</div>
                                        <div class="sf-price">
                                            <fmt:formatNumber value="${productDAO.getDisplayPrice(p)}" type="number" groupingUsed="true"/> đ
                                        </div>
                                    </div>
                                </div>
                            </a>
                        </c:forEach>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="sf-empty">
                        <span class="material-symbols-outlined">search_off</span>
                        <h3>NO PRODUCTS FOUND</h3>
                        <p>Try changing filters or search keywords.</p>
                    </div>
                </c:otherwise>
            </c:choose>

            <c:if test="${totalPages > 1}">
                <div class="sf-pagination">
                    <span class="sf-page-info">SHOWING ${showing} OF ${totalProduct} PRODUCTS</span>
                    <div class="sf-page-controls">
                        <c:if test="${currentPage > 1}">
                            <a class="sf-page-btn" href="?${query}&page=${currentPage - 1}">
                                <span class="material-symbols-outlined">chevron_left</span>
                            </a>
                        </c:if>
                        <c:forEach var="i" begin="1" end="${totalPages}">
                            <a class="sf-page-btn <c:if test='${i == currentPage}'>active</c:if>"
                               href="?${query}&page=${i}">${i}</a>
                        </c:forEach>
                        <c:if test="${currentPage < totalPages}">
                            <a class="sf-page-btn" href="?${query}&page=${currentPage + 1}">
                                <span class="material-symbols-outlined">chevron_right</span>
                            </a>
                        </c:if>
                    </div>
                </div>
            </c:if>
        </div>
    </main>

    <style>
        /* ===== ALL CSS ISOLATED WITH sf- PREFIX ===== */
        .sf-page {
            display: flex;
            flex-direction: row;
            max-width: 1400px;
            margin: 0 auto;
            padding: 30px 20px;
            font-family: 'Space Grotesk', sans-serif;
            gap: 30px;
            min-height: 80vh;
            box-sizing: border-box;
        }

        .sf-sidebar {
            width: 240px;
            min-width: 240px;
            display: flex;
            flex-direction: column;
            gap: 0;
            position: sticky;
            top: 100px;
            align-self: flex-start;
            height: fit-content;
        }

        .sf-section {
            padding: 18px 0;
            border-bottom: 1px solid #e5e7eb;
        }

        .sf-section:first-child { padding-top: 0; }
        .sf-section:last-of-type { border-bottom: none; }

        .sf-title {
            font-weight: 700;
            font-size: 0.85rem;
            text-transform: uppercase;
            margin: 0 0 16px 0;
            color: #000;
            letter-spacing: 0.8px;
        }

        .sf-price-row {
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .sf-price-wrap {
            position: relative;
            flex: 1;
        }

        .sf-currency {
            position: absolute;
            left: 10px;
            top: 50%;
            transform: translateY(-50%);
            color: #9ca3af;
            font-size: 0.85rem;
            pointer-events: none;
        }

        .sf-price-input {
            width: 100%;
            padding: 8px 8px 8px 24px;
            border: 1px solid #e5e7eb;
            border-radius: 4px;
            font-size: 0.85rem;
            font-family: 'Space Grotesk', sans-serif;
            background: #fff;
            box-sizing: border-box;
        }

        .sf-price-input:focus {
            outline: none;
            border-color: #000;
        }

        .sf-dash {
            color: #9ca3af;
            font-size: 0.85rem;
        }

        .sf-price-error {
            color: #dc2626;
            font-size: 0.75rem;
            margin-top: 6px;
            display: none;
        }

        .sf-btn-apply {
            width: 100%;
            padding: 8px 16px;
            margin-top: 10px;
            background: #000;
            color: #fff;
            border: none;
            border-radius: 4px;
            font-size: 0.8rem;
            font-weight: 600;
            font-family: 'Space Grotesk', sans-serif;
            cursor: pointer;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .sf-btn-apply:hover { background: #333; }

        .sf-group {
            display: flex;
            flex-direction: column;
            gap: 10px;
        }

        .sf-item {
            display: flex;
            align-items: center;
            cursor: pointer;
            font-size: 0.9rem;
            font-weight: 500;
            color: #374151;
            padding: 4px 0;
        }

        .sf-item:hover { color: #000; }

        .sf-item input { display: none; }

        .sf-box {
            width: 18px;
            height: 18px;
            border: 1.5px solid #d1d5db;
            margin-right: 12px;
            display: inline-block;
            position: relative;
            flex-shrink: 0;
        }

        .sf-item input:checked + .sf-box {
            background: #000;
            border-color: #000;
        }

        .sf-item input:checked + .sf-box::after {
            content: '';
            position: absolute;
            left: 5px;
            top: 1px;
            width: 5px;
            height: 10px;
            border: solid #fff;
            border-width: 0 2px 2px 0;
            transform: rotate(45deg);
        }

        .sf-btn-reset {
            display: block;
            width: 100%;
            padding: 12px;
            text-align: center;
            background: transparent;
            color: #000;
            border: 2px solid #000;
            font-weight: 700;
            font-size: 0.8rem;
            text-transform: uppercase;
            letter-spacing: 1px;
            text-decoration: none;
            margin-top: 20px;
            box-sizing: border-box;
        }

        .sf-btn-reset:hover { background: #f5f5f5; }

        /* ===== MAIN AREA ===== */
        .sf-main {
            flex: 1 1 0;
            min-width: 0;
        }

        .sf-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            border-bottom: 2px solid #000;
            padding-bottom: 15px;
            margin-bottom: 25px;
            gap: 16px;
            flex-wrap: wrap;
        }

        .sf-results-title {
            font-weight: 800;
            font-size: 1.4rem;
            text-transform: uppercase;
            margin: 0;
            letter-spacing: -0.5px;
        }

        .sf-results-title span {
            color: #6b7280;
            font-weight: 600;
        }

        .sf-sort {
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .sf-sort-label {
            font-weight: 600;
            font-size: 0.85rem;
            color: #6b7280;
        }

        .sf-sort-select {
            padding: 8px 32px 8px 12px;
            border: 1.5px solid #000;
            border-radius: 0;
            font-family: 'Inter', sans-serif;
            font-weight: 500;
            font-size: 0.9rem;
            cursor: pointer;
            background: #fff;
            appearance: none;
            -webkit-appearance: none;
            -moz-appearance: none;
            background-image: url("data:image/svg+xml;charset=UTF-8,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3e%3cpolyline points='6 9 12 15 18 9'%3e%3c/polyline%3e%3c/svg%3e");
            background-repeat: no-repeat;
            background-position: right 10px center;
            background-size: 14px;
        }

        /* ===== GRID: FIXED 4 COLUMNS ===== */
        .sf-grid {
            display: grid !important;
            grid-template-columns: repeat(4, 1fr) !important;
            gap: 20px !important;
            width: 100%;
            box-sizing: border-box;
        }

        .sf-card-link {
            text-decoration: none;
            color: inherit;
            display: block;
        }

        .sf-card {
            background: transparent;
            border: none;
            overflow: hidden;
            transition: transform 0.3s ease;
            display: flex;
            flex-direction: column;
        }

        .sf-card:hover { transform: translateY(-4px); }

        .sf-img-box {
            position: relative;
            width: 100%;
            aspect-ratio: 3 / 4;
            background: #f5f5f5;
            overflow: hidden;
        }

        .sf-img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            transition: transform 0.5s ease;
        }

        .sf-card:hover .sf-img { transform: scale(1.05); }

        .sf-fav {
            position: absolute;
            top: 10px;
            right: 10px;
            background: #fff;
            border: 1px solid #000;
            border-radius: 50%;
            width: 34px;
            height: 34px;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            z-index: 5;
        }

        .sf-fav:hover { background: #000; }
        .sf-fav:hover .material-symbols-outlined { color: #fff; }

        .sf-fav .material-symbols-outlined {
            font-size: 18px;
            color: #000;
        }

        .sf-fav .sf-fav-on { color: #ef4444; font-variation-settings: 'FILL' 1; }
        .sf-fav:hover .sf-fav-on { color: #fff; }

        .sf-info { padding-top: 12px; }

        .sf-name {
            font-weight: 700;
            font-size: 0.85rem;
            text-transform: uppercase;
            color: #000;
            margin-bottom: 4px;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            letter-spacing: -0.3px;
        }

        .sf-price {
            font-family: 'Inter', sans-serif;
            font-weight: 600;
            font-size: 0.95rem;
            color: #000;
        }

        /* ===== EMPTY ===== */
        .sf-empty {
            text-align: center;
            padding: 80px 20px;
            color: #6b7280;
        }

        .sf-empty .material-symbols-outlined {
            font-size: 4rem;
            color: #d1d5db;
        }

        .sf-empty h3 {
            font-weight: 800;
            font-size: 1.3rem;
            text-transform: uppercase;
            margin: 20px 0 10px;
        }

        .sf-empty p { color: #9ca3af; }

        /* ===== PAGINATION ===== */
        .sf-pagination {
            display: flex;
            justify-content: space-between;
            align-items: center;
            border-top: 1px solid #e5e7eb;
            padding-top: 20px;
            margin-top: 30px;
        }

        .sf-page-info {
            font-weight: 600;
            font-size: 0.85rem;
            color: #6b7280;
        }

        .sf-page-controls {
            display: flex;
            gap: 6px;
        }

        .sf-page-btn {
            display: flex;
            justify-content: center;
            align-items: center;
            min-width: 36px;
            height: 36px;
            padding: 0 10px;
            border: 1.5px solid #000;
            background: transparent;
            color: #000;
            font-weight: 600;
            font-size: 0.85rem;
            text-decoration: none;
        }

        .sf-page-btn:hover, .sf-page-btn.active {
            background: #000;
            color: #fff;
        }

        .sf-page-btn .material-symbols-outlined { font-size: 18px; }

        /* ===== RESPONSIVE ===== */
        @media (max-width: 1100px) {
            .sf-grid { grid-template-columns: repeat(3, 1fr) !important; }
        }

        @media (max-width: 768px) {
            .sf-page { flex-direction: column; }
            .sf-sidebar { width: 100%; min-width: unset; position: static; }
            .sf-grid { grid-template-columns: repeat(2, 1fr) !important; }
            .sf-header { flex-direction: column; align-items: flex-start; }
        }
    </style>
</form>

<script>
    function sfSubmit() {
        document.getElementById('searchFilterForm').submit();
    }

    function sfApplyPrice() {
        const minInput = document.querySelector('input[name="minPrice"]');
        const maxInput = document.querySelector('input[name="maxPrice"]');
        const errorDiv = document.getElementById('priceError');

        const min = minInput.value ? parseFloat(minInput.value) : null;
        const max = maxInput.value ? parseFloat(maxInput.value) : null;

        if (min !== null && max !== null && min > max) {
            errorDiv.textContent = 'Min price cannot exceed max price';
            errorDiv.style.display = 'block';
            return;
        }

        errorDiv.style.display = 'none';
        sfSubmit();
    }

    function sfToggleFav(productId, button) {
        fetch('${pageContext.request.contextPath}/home/customer/toggle-wishlist', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'productId=' + encodeURIComponent(productId)
        }).then(response => response.json()).then(data => {
            if (data && data.requiresLogin) {
                window.location.href = '${pageContext.request.contextPath}/auth/login';
                return;
            }
            if (data && data.inWishlist !== undefined) {
                const icon = button.querySelector('.material-symbols-outlined');
                if (icon) {
                    if (data.inWishlist === true) {
                        icon.textContent = 'favorite';
                        icon.classList.add('sf-fav-on');
                    } else {
                        icon.textContent = 'favorite_border';
                        icon.classList.remove('sf-fav-on');
                    }
                }
            }
        }).catch(error => console.error('Wishlist toggle error:', error));
    }
</script>
