<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

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
            <h2 class="section-title">BROWSE CATEGORIES</h2>
        </div>
        <div class="categories-grid">
            <a href="${pageContext.request.contextPath}/home/search?type=Top" class="category-link">
                <div class="category-card">
                    <div class="product-image-container">
                        <img class="product-image" src="https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400&q=80" />                          
                    </div>
                    <span class="category-name">Tops & Tees</span>
                </div>
            </a>
            <a href="${pageContext.request.contextPath}/home/search?type=Bottom" class="category-link">
                <div class="category-card">
                    <div class="product-image-container">
                        <img class="product-image" src="https://images.unsplash.com/photo-1542272604-787c3835535d?w=400&q=80" />                          
                    </div>
                    <span class="category-name">Bottoms</span>
                </div>
            </a>
            <a href="${pageContext.request.contextPath}/home/search?type=Outerwear" class="category-link">
                <div class="category-card">
                    <div class="product-image-container">
                        <img class="product-image" src="https://images.unsplash.com/photo-1551028719-00167b16eac5?w=400&q=80" />                          
                    </div>
                    <span class="category-name">Outerwear</span>
                </div>
            </a>
            <a href="${pageContext.request.contextPath}/home/search?type=Accessory" class="category-link">
                <div class="category-card">
                    <div class="product-image-container">
                        <img class="product-image" src="https://images.unsplash.com/photo-1523206489230-c012c64b2b48?w=400&q=80" />                          
                    </div>
                    <span class="category-name">Accessories</span>
                </div>
            </a>
        </div>
    </section>

    <section class="product-section">
        <div class="section-header">
            <h2 class="section-title">NEW ARRIVALS</h2>
            <a class="view-all" href="${pageContext.request.contextPath}/home/search?sort=latest">
                VIEW ALL <span class="material-symbols-outlined">arrow_forward</span>
            </a>
        </div>
        <div class="products-grid">
            <c:forEach var="p" items="${newArrivals}">
                <a href="${pageContext.request.contextPath}/home/view-detail-product?productId=${p.productId}" class="product-link">
                    <div class="product-card">
                        <div class="product-image-container">
                            <img class="product-image" src="${p.image}" />
                            <form action="${pageContext.request.contextPath}/home/customer/toggle-wishlist" method="POST">
                                <input type="hidden" name="productId" value="${p.productId}" />
                                <button type="submit" class="favorite-btn">
                                    <span class="material-symbols-outlined ${wishlistProductIds != null && wishlistProductIds.contains(p.productId) ? 'active' : ''}">
                                        favorite
                                    </span>
                                </button>
                            </form>
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
    </section>

    <section class="product-section">
        <div class="section-header">
            <h2 class="section-title">TOPS & TEES</h2>
            <a class="view-all" href="${pageContext.request.contextPath}/home/search?type=Top">
                VIEW ALL <span class="material-symbols-outlined">arrow_forward</span>
            </a>
        </div>
        <div class="products-grid">
            <c:forEach var="p" items="${tops}">
                <a href="${pageContext.request.contextPath}/home/view-detail-product?productId=${p.productId}" class="product-link">
                    <div class="product-card">
                        <div class="product-image-container">
                            <img class="product-image" src="${p.image}" />
                            <form action="${pageContext.request.contextPath}/home/customer/toggle-wishlist" method="POST">
                                <input type="hidden" name="productId" value="${p.productId}" />
                                <button type="submit" class="favorite-btn">
                                    <span class="material-symbols-outlined ${wishlistProductIds != null && wishlistProductIds.contains(p.productId) ? 'active' : ''}">
                                        favorite
                                    </span>
                                </button>
                            </form>
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
    </section>

    <section class="product-section">
        <div class="section-header">
            <h2 class="section-title">PREMIUM OUTERWEAR</h2>
            <a class="view-all" href="${pageContext.request.contextPath}/home/search?type=Outerwear">
                VIEW ALL <span class="material-symbols-outlined">arrow_forward</span>
            </a>
        </div>
        <div class="products-grid">
            <c:forEach var="p" items="${outerwear}">
                <a href="${pageContext.request.contextPath}/home/view-detail-product?productId=${p.productId}" class="product-link">
                    <div class="product-card">
                        <div class="product-image-container">
                            <img class="product-image" src="${p.image}" />
                            <form action="${pageContext.request.contextPath}/home/customer/toggle-wishlist" method="POST">
                                <input type="hidden" name="productId" value="${p.productId}" />
                                <button type="submit" class="favorite-btn">
                                    <span class="material-symbols-outlined ${wishlistProductIds != null && wishlistProductIds.contains(p.productId) ? 'active' : ''}">
                                        favorite
                                    </span>
                                </button>
                            </form>
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
    </section>

    <section class="product-section">
        <div class="section-header">
            <h2 class="section-title">STREET ACCESSORIES</h2>
            <div class="container-button">
                <div class="filter-buttons">
                    <a class="btn-headphone" href="${pageContext.request.contextPath}/home/search?type=Hat">HATS</a>
                    <a class="btn-headphone" href="${pageContext.request.contextPath}/home/search?type=Bag">BAGS</a>
                </div>
                <a class="view-all" href="${pageContext.request.contextPath}/home/search?type=Hat&type=Bag">
                    VIEW ALL <span class="material-symbols-outlined">arrow_forward</span>
                </a>
            </div>
        </div>
        <div class="products-grid">
            <c:forEach var="p" items="${accessories}">
                <a href="${pageContext.request.contextPath}/home/view-detail-product?productId=${p.productId}" class="product-link">
                    <div class="product-card">
                        <div class="product-image-container">
                            <img class="product-image" src="${p.image}" />
                            <form action="${pageContext.request.contextPath}/home/customer/toggle-wishlist" method="POST">
                                <input type="hidden" name="productId" value="${p.productId}" />
                                <button type="submit" class="favorite-btn">
                                    <span class="material-symbols-outlined ${wishlistProductIds != null && wishlistProductIds.contains(p.productId) ? 'active' : ''}">
                                        favorite
                                    </span>
                                </button>
                            </form>
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
    </section>
</div>