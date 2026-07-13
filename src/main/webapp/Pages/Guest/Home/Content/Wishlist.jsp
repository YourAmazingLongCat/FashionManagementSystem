<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="content-page">
    <div class="wishlist-container">
        <div class="wishlist-header">
            <h1 class="section-title">MY WISHLIST</h1>
            <p class="wishlist-count">${wishlistProducts.size()} products</p>
        </div>

        <c:choose>
            <c:when test="${empty wishlistProducts}">
                <div class="wishlist-empty">
                    <span class="material-symbols-outlined empty-icon">favorite_border</span>
                    <h3>Your wishlist is empty</h3>
                    <p>Start adding products you love!</p>
                    <a href="${pageContext.request.contextPath}/home" class="continue-shopping-btn">CONTINUE SHOPPING</a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="products-grid">
                    <c:forEach var="p" items="${wishlistProducts}">
                        <a href="${pageContext.request.contextPath}/home/view-detail-product?productId=${p.productId}" class="product-link">
                            <div class="product-card">
                                <div class="product-image-container">
                                    <img class="product-image" src="${empty p.primaryImageUrl ? 'https://via.placeholder.com/600x800?text=No+Image' : p.primaryImageUrl}" alt="${p.name}" />
                                    <button type="button" class="favorite-btn active" onclick="event.preventDefault(); event.stopPropagation(); toggleWishlist('${p.productId}', this)">
                                        <span class="material-symbols-outlined active">favorite</span>
                                    </button>
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

                <c:if test="${totalPages > 1}">
                    <div class="pagination">
                        <span class="pagination-info">SHOWING ${showing} OF ${totalProduct} PRODUCTS</span>
                        <div class="pagination-controls">
                            <c:if test="${currentPage > 1}">
                                <a class="page-btn" href="?page=${currentPage - 1}">
                                    <span class="material-symbols-outlined">chevron_left</span>
                                </a>
                            </c:if>

                            <c:forEach var="i" begin="1" end="${totalPages}">
                                <c:choose>
                                    <c:when test="${i == currentPage}">
                                        <a class="page-btn active" href="?page=${i}">${i}</a>
                                    </c:when>
                                    <c:otherwise>
                                        <a class="page-btn" href="?page=${i}">${i}</a>
                                    </c:otherwise>
                                </c:choose>
                            </c:forEach>

                            <c:if test="${currentPage < totalPages}">
                                <a class="page-btn" href="?page=${currentPage + 1}">
                                    <span class="material-symbols-outlined">chevron_right</span>
                                </a>
                            </c:if>
                        </div>
                    </div>
                </c:if>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<style>
    .wishlist-container {
        max-width: 1300px;
        margin: 0 auto;
        padding: 0 20px;
    }

    .wishlist-header {
        padding: 40px 0 20px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        border-bottom: 2px solid var(--border-color);
        margin-bottom: 35px;
    }

    .wishlist-header .section-title {
        margin: 0;
    }

    .wishlist-count {
        margin: 0;
        font-size: 0.9rem;
        color: var(--gray-dark);
        font-weight: 500;
    }

    .wishlist-empty {
        text-align: center;
        padding: 80px 20px;
    }

    .wishlist-empty .empty-icon {
        font-size: 80px;
        color: #d1d5db;
    }

    .wishlist-empty h3 {
        font-size: 1.3rem;
        font-weight: 800;
        text-transform: uppercase;
        margin: 20px 0 10px 0;
        letter-spacing: -0.5px;
    }

    .wishlist-empty p {
        color: #6b7280;
        margin-bottom: 30px;
    }

    .continue-shopping-btn {
        display: inline-block;
        padding: 15px 40px;
        background-color: #000;
        color: #fff;
        text-decoration: none;
        font-family: 'Space Grotesk', sans-serif;
        font-weight: 700;
        font-size: 0.85rem;
        text-transform: uppercase;
        letter-spacing: 1px;
        transition: background-color 0.2s;
    }

    .continue-shopping-btn:hover {
        background-color: #333;
    }

    /* Grid layout */
    .products-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
        gap: 20px;
        margin-bottom: 40px;
    }

    /* Pagination */
    .pagination {
        display: flex;
        justify-content: space-between;
        align-items: center;
        border-top: 1px solid #e5e7eb;
        padding-top: 25px;
    }

    .pagination-info {
        font-weight: 600;
        font-size: 0.85rem;
        color: #6b7280;
    }

    .pagination-controls {
        display: flex;
        gap: 8px;
    }

    .page-btn {
        display: flex;
        justify-content: center;
        align-items: center;
        min-width: 40px;
        height: 40px;
        padding: 0 12px;
        border: 1.5px solid #000;
        background: transparent;
        color: #000;
        font-weight: 600;
        font-size: 0.9rem;
        text-decoration: none;
        transition: all 0.2s;
    }

    .page-btn:hover, .page-btn.active {
        background: #000;
        color: #fff;
    }

    @media (max-width: 768px) {
        .wishlist-header {
            flex-direction: column;
            align-items: flex-start;
            gap: 10px;
        }

        .pagination {
            flex-direction: column;
            gap: 15px;
        }
    }
</style>

<script>
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
            if (data && data.inWishlist !== undefined && data.inWishlist === false) {
                const card = button.closest('.product-card');
                const link = button.closest('.product-link');
                if (card) {
                    card.remove();
                } else if (link) {
                    link.remove();
                }
                const countEl = document.querySelector('.wishlist-count');
                if (countEl) {
                    const currentCount = parseInt(countEl.textContent) || 0;
                    const newCount = currentCount - 1;
                    countEl.textContent = newCount + ' products';
                    if (newCount <= 0) {
                        location.reload();
                    }
                }
            }
        }).catch(error => {
            console.error('Wishlist toggle error:', error);
        });
    }
</script>
