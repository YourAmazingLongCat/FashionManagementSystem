<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="content-page product-detail-page">
    <section class="product-detail-shell">
        <div class="detail-gallery-card">
            <div class="detail-image-wrap">
                <c:choose>
                    <c:when test="${not empty product.primaryImageUrl}">
                        <img class="detail-image" src="${product.primaryImageUrl}" alt="${product.name}" />
                    </c:when>
                    <c:otherwise>
                        <div class="detail-image detail-image-empty">No image</div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <div class="detail-content-card">
            <a class="detail-back-link" href="${pageContext.request.contextPath}/home">← Back to home</a>
            <p class="detail-category">${product.categoryName}</p>
            <h1 class="detail-title">${product.name}</h1>
            <div class="detail-price" id="detailPrice"><fmt:formatNumber value="${displayPrice}" type="number" groupingUsed="true"/> đ</div>
            <p class="detail-description">${empty product.description ? 'No description available for this product yet.' : product.description}</p>

            <c:if test="${not empty param.message}">
                <div class="detail-flash-message ${param.message eq 'added-to-cart' ? 'success' : 'error'}">
                    ${param.message eq 'added-to-cart' ? 'Added to cart successfully.' : 'Selected variant is unavailable.'}
                </div>
            </c:if>

            <form method="post" action="${pageContext.request.contextPath}/home/cart/add" class="detail-purchase-panel" id="addToCartForm">
                <input type="hidden" name="productId" value="${product.productId}" />
                <input type="hidden" name="variantId" id="selectedVariantId" value="" />

                <div class="detail-options-grid single-column-mobile">
                    <div class="detail-option-card">
                        <span class="detail-option-label">Choose color</span>
                        <div class="detail-chip-list selectable-list" id="colorOptions">
                            <c:forEach var="colorName" items="${product.colorNames}">
                                <button type="button" class="detail-chip detail-chip-button" data-color-name="${colorName}">${colorName}</button>
                            </c:forEach>
                        </div>
                    </div>
                    <div class="detail-option-card">
                        <span class="detail-option-label">Choose size</span>
                        <div class="detail-chip-list selectable-list" id="sizeOptions">
                            <c:forEach var="sizeName" items="${product.sizeNames}">
                                <button type="button" class="detail-chip detail-chip-button" data-size-name="${sizeName}">${sizeName}</button>
                            </c:forEach>
                        </div>
                    </div>
                </div>

                <div class="detail-selection-summary">
                    <div class="summary-info">
                        <strong id="selectedVariantLabel">Choose color and size</strong>
                        <span id="selectedVariantStock">Available stock will appear here.</span>
                    </div>
                    <div class="summary-actions">
                        <div class="detail-quantity-wrap">
                            <label for="quantity">Qty</label>
                            <input id="quantity" name="quantity" type="number" min="1" value="1" class="detail-quantity-input" />
                        </div>
                        <button type="submit" class="detail-add-cart-btn" id="addToCartButton" disabled>Add to cart</button>
                    </div>
                </div>
            </form>

            <div class="variant-table-card">
                <div class="variant-table-head">
                    <h3>Available variants</h3>
                    <span>${product.totalStockQty} items in stock</span>
                </div>
                <div class="variant-table-list" id="variantTableList">
                    <c:forEach var="variant" items="${product.variants}">
                        <div class="variant-table-row ${variant.stockQty <= 0 ? 'out-of-stock' : ''}"
                             data-variant-id="${variant.variantId}"
                             data-color-name="${variant.colorName}"
                             data-size-name="${variant.sizeName}"
                             data-stock-qty="${variant.stockQty}"
                             data-price="${variant.priceOverride != null ? variant.priceOverride : product.basePrice}">
                            <div>
                                <strong>${variant.colorName} / ${variant.sizeName}</strong>
                            </div>
                            <div>
                                <strong><fmt:formatNumber value="${variant.priceOverride != null ? variant.priceOverride : product.basePrice}" type="number" groupingUsed="true"/> đ</strong>
                                <span>${variant.stockQty > 0 ? variant.stockQty : 0} available</span>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </div>
        </div>
    </section>

    <c:if test="${not empty relatedProducts}">
        <section class="product-section related-section">
            <div class="section-header">
                <h2 class="section-title">MORE FROM ${product.categoryName}</h2>
            </div>
            <div class="products-grid">
                <c:forEach var="p" items="${relatedProducts}">
                    <c:if test="${p.productId ne product.productId}">
                        <a href="${pageContext.request.contextPath}/home/view-detail-product?productId=${p.productId}" class="product-link">
                            <div class="product-card">
                                <div class="product-image-container">
                                    <img class="product-image" src="${empty p.primaryImageUrl ? 'https://via.placeholder.com/600x800?text=No+Image' : p.primaryImageUrl}" alt="${p.name}" />
                                </div>
                                <div class="product-info">
                                    <div class="product-name">${p.name}</div>
                                    <div class="product-price-row">
                                        <span class="price"><fmt:formatNumber value="${productDAO.getDisplayPrice(p)}" type="number" groupingUsed="true"/> đ</span>
                                    </div>
                                </div>
                            </div>
                        </a>
                    </c:if>
                </c:forEach>
            </div>
        </section>
    </c:if>
</div>

<script>
    (function () {
        const colorButtons = Array.from(document.querySelectorAll('#colorOptions .detail-chip-button'));
        const sizeButtons = Array.from(document.querySelectorAll('#sizeOptions .detail-chip-button'));
        const variantRows = Array.from(document.querySelectorAll('#variantTableList .variant-table-row'));
        const selectedVariantId = document.getElementById('selectedVariantId');
        const selectedVariantLabel = document.getElementById('selectedVariantLabel');
        const selectedVariantStock = document.getElementById('selectedVariantStock');
        const addToCartButton = document.getElementById('addToCartButton');
        const quantityInput = document.getElementById('quantity');
        const detailPrice = document.getElementById('detailPrice');

        let selectedColor = '';
        let selectedSize = '';

        const formatPrice = (value) => {
            const amount = Number(String(value || '0').replace(/,/g, ''));
            return amount.toLocaleString('vi-VN') + ' đ';
        };

        const setActiveButton = (buttons, value, attr) => {
            buttons.forEach(button => {
                button.classList.toggle('active', button.dataset[attr] === value);
            });
        };

        const refreshVariantSelection = () => {
            const matched = variantRows.find(row => row.dataset.colorName === selectedColor && row.dataset.sizeName === selectedSize);
            variantRows.forEach(row => row.classList.toggle('selected', row === matched));

            if (!matched) {
                selectedVariantId.value = '';
                addToCartButton.disabled = true;
                selectedVariantLabel.textContent = selectedColor || selectedSize ? 'This combination is unavailable' : 'Choose color and size';
                selectedVariantStock.textContent = 'Available stock will appear here.';
                return;
            }

            const stockQty = Number(matched.dataset.stockQty || '0');
            selectedVariantId.value = matched.dataset.variantId;
            detailPrice.textContent = formatPrice(matched.dataset.price);
            selectedVariantLabel.textContent = matched.dataset.colorName + ' / ' + matched.dataset.sizeName;
            selectedVariantStock.textContent = stockQty > 0 ? stockQty + ' items available' : 'Out of stock';
            quantityInput.max = String(Math.max(stockQty, 1));
            if (Number(quantityInput.value) > stockQty && stockQty > 0) {
                quantityInput.value = String(stockQty);
            }
            addToCartButton.disabled = stockQty <= 0;
        };

        colorButtons.forEach(button => button.addEventListener('click', () => {
            selectedColor = button.dataset.colorName;
            setActiveButton(colorButtons, selectedColor, 'colorName');
            refreshVariantSelection();
        }));

        sizeButtons.forEach(button => button.addEventListener('click', () => {
            selectedSize = button.dataset.sizeName;
            setActiveButton(sizeButtons, selectedSize, 'sizeName');
            refreshVariantSelection();
        }));
    })();
</script>
