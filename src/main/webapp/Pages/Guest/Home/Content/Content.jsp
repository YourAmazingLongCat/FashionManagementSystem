<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<div class="content-page">
    <section class="hero-section">
        <div id="heroCarousel" class="carousel slide" data-bs-ride="carousel">
            <div class="carousel-inner">
                <div class="carousel-item active">
                    <img class="carousel-image" src="https://images.unsplash.com/photo-1441984904996-e0b6ba687e04?w=1200&q=80" alt="Banner 1" />
                </div>
                <div class="carousel-item">
                    <img class="carousel-image" src="https://images.unsplash.com/photo-1445205170230-053b83016050?w=1200&q=80" alt="Banner 2" />
                </div>
                <div class="carousel-item">
                    <img class="carousel-image" src="https://images.unsplash.com/photo-1469334031218-e382a71b716b?w=1200&q=80" alt="Banner 3" />
                </div>
            </div>
            <button class="carousel-control-prev" type="button" data-bs-target="#heroCarousel" data-bs-slide="prev">
                <span class="carousel-control-prev-icon"></span>
            </button>
            <button class="carousel-control-next" type="button" data-bs-target="#heroCarousel" data-bs-slide="next">
                <span class="carousel-control-next-icon"></span>
            </button>
            <div class="carousel-indicators">
                <button type="button" data-bs-target="#heroCarousel" data-bs-slide-to="0" class="active"></button>
                <button type="button" data-bs-target="#heroCarousel" data-bs-slide-to="1"></button>
                <button type="button" data-bs-target="#heroCarousel" data-bs-slide-to="2"></button>
            </div>
        </div>
    </section>

    <div class="huge-marquee-container">
        <div class="huge-marquee-track">
            <span>FASHION STORE 2026 • NEW COLLECTION • </span>
            <span>FASHION STORE 2026 • NEW COLLECTION • </span>
            <span>FASHION STORE 2026 • NEW COLLECTION • </span>
            <span>FASHION STORE 2026 • NEW COLLECTION • </span>
        </div>
    </div>

    <section class="product-section">
        <div class="section-header">
            <h2 class="section-title">NEW ARRIVALS</h2>
            <a class="view-all" href="${pageContext.request.contextPath}/home/search?sort=latest">
                VIEW ALL <span class="material-symbols-outlined">arrow_forward</span>
            </a>
        </div>
        <div class="products-grid new-arrivals-grid" id="newArrivalsGrid">
            <c:forEach var="p" items="${newArrivals}" varStatus="arrivalStatus">
                <a href="${pageContext.request.contextPath}/home/view-detail-product?productId=${p.productId}" class="product-link">
                    <div class="product-card">
                        <div class="product-image-container">
                            <img class="product-image" src="${empty p.primaryImageUrl ? 'https://via.placeholder.com/600x800?text=No+Image' : pageContext.request.contextPath.concat(p.primaryImageUrl)}" alt="${p.name}" />
                            <button type="button" class="favorite-btn" onclick="event.preventDefault(); toggleWishlist('${p.productId}', this)">
                                <span class="material-symbols-outlined ${wishlistProductIds != null && wishlistProductIds.contains(p.productId) ? 'active' : ''}">
                                    favorite
                                </span>
                            </button>
                        </div>
                        <div class="product-info">
                            <div class="product-name">${p.name}</div>
                            <div class="product-price-row">
                                <span class="price"><fmt:formatNumber value="${productDAO.getDisplayPrice(p)}" type="number" groupingUsed="true"/> đ</span>
                            </div>
                            <c:set var="rs" value="${ratingMap[p.productId]}" />
                            <div class="product-rating-row">
                                <c:choose>
                                    <c:when test="${not empty rs && rs[1] > 0}">
                                        <span class="product-stars">
                                            <c:forEach begin="1" end="5" var="i">${i <= rs[0] + 0.5 ? '★' : '☆'}</c:forEach>
                                        </span>
                                        <span class="product-review-count">(${rs[1]})</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="product-no-rating">No reviews yet</span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </div>
                </a>
            </c:forEach>
        </div>
        <c:if test="${fn:length(newArrivals) > 4}">
            <div class="new-arrivals-pagination" aria-label="New arrivals pagination">
                <button type="button" class="arrival-page-btn" id="newArrivalsPrev" aria-label="Previous products">
                    <span class="material-symbols-outlined">chevron_left</span>
                </button>
                <span class="arrival-page-number" id="newArrivalsPage">1 / ${(fn:length(newArrivals) + 3) div 4}</span>
                <button type="button" class="arrival-page-btn" id="newArrivalsNext" aria-label="Next products">
                    <span class="material-symbols-outlined">chevron_right</span>
                </button>
            </div>
        </c:if>
    </section>
</div>

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
            if (data && data.inWishlist !== undefined) {
                const icon = button.querySelector('.material-symbols-outlined');
                if (icon) {
                    if (data.inWishlist === true) {
                        icon.textContent = 'favorite';
                        icon.classList.add('active');
                    } else {
                        icon.textContent = 'favorite_border';
                        icon.classList.remove('active');
                    }
                }
            }
        }).catch(error => {
            console.error('Wishlist toggle error:', error);
        });
    }
</script>

<script>
    (function () {
        const grid = document.getElementById('newArrivalsGrid');
        const prev = document.getElementById('newArrivalsPrev');
        const next = document.getElementById('newArrivalsNext');
        const pageLabel = document.getElementById('newArrivalsPage');
        if (!grid || !prev || !next || !pageLabel) return;

        const cards = Array.from(grid.querySelectorAll('.product-link'));
        const pageSize = 4;
        const totalPages = Math.ceil(cards.length / pageSize);
        let currentPage = 1;

        const renderPage = () => {
            cards.forEach((card, index) => {
                const first = (currentPage - 1) * pageSize;
                card.hidden = index < first || index >= first + pageSize;
            });
            pageLabel.textContent = currentPage + ' / ' + totalPages;
            prev.disabled = currentPage === 1;
            next.disabled = currentPage === totalPages;
        };

        prev.addEventListener('click', () => {
            if (currentPage > 1) { currentPage--; renderPage(); }
        });
        next.addEventListener('click', () => {
            if (currentPage < totalPages) { currentPage++; renderPage(); }
        });
        renderPage();
    })();
</script>
