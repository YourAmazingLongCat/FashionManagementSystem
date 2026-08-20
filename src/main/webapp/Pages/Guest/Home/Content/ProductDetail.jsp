<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="content-page product-detail-page">
    <section class="product-detail-shell">
        <div class="detail-gallery-card">
            <div class="detail-gallery">
                <div class="detail-thumbnails" id="detailThumbnails">
                    <c:if test="${not empty product.primaryImageUrl}">
                        <button type="button" class="detail-thumbnail active" data-image-url="${pageContext.request.contextPath.concat(product.primaryImageUrl)}">
                            <img src="${pageContext.request.contextPath.concat(product.primaryImageUrl)}" alt="${product.name}" />
                        </button>
                    </c:if>
                    <c:forEach var="variant" items="${product.variants}">
                        <c:if test="${not empty variant.imageUrl}">
                            <button type="button" class="detail-thumbnail" data-image-url="${pageContext.request.contextPath.concat(variant.imageUrl)}">
                                <img src="${pageContext.request.contextPath.concat(variant.imageUrl)}" alt="${product.name} ${variant.colorName} ${variant.sizeName}" />
                            </button>
                        </c:if>
                    </c:forEach>
                </div>
                <div class="detail-image-wrap">
                <c:choose>
                    <c:when test="${not empty product.primaryImageUrl}">
                        <img id="detailMainImage" class="detail-image" src="${pageContext.request.contextPath.concat(product.primaryImageUrl)}" alt="${product.name}" />
                    </c:when>
                    <c:otherwise>
                        <div id="detailMainImage" class="detail-image detail-image-empty">No image</div>
                    </c:otherwise>
                </c:choose>
                </div>
            </div>
        </div>

        <div class="detail-content-card">
            <p class="detail-category">${product.categoryName}</p>
            <h1 class="detail-title">${product.name}</h1>
            <span id="commentCountBadge" style="display:none;">0</span>
            <div class="detail-rating-row">
                <c:choose>
                    <c:when test="${not empty ratingSummary && ratingSummary[1] > 0}">
                        <span class="detail-stars">
                            <c:forEach begin="1" end="5" var="i">
                                <c:choose>
                                    <c:when test="${i <= ratingSummary[0] + 0.5}">★</c:when>
                                    <c:otherwise>☆</c:otherwise>
                                </c:choose>
                            </c:forEach>
                        </span>
                        <span class="detail-rating-value"><fmt:formatNumber value="${ratingSummary[0]}" maxFractionDigits="1"/></span>
                        <span class="detail-review-count">(${ratingSummary[1]} reviews)</span>
                    </c:when>
                    <c:otherwise>
                        <span class="detail-no-rating">No reviews yet</span>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="detail-price" id="detailPrice"><fmt:formatNumber value="${displayPrice}" type="number" groupingUsed="true"/> đ</div>
            <p class="detail-description">${empty product.description ? 'No description available for this product yet.' : product.description}</p>

            <c:if test="${not empty param.message}">
                <div class="detail-flash-message ${param.message eq 'added-to-cart' ? 'success' : 'error'}">
                    ${param.message eq 'added-to-cart' ? 'Added to cart successfully.' : 'Selected variant is unavailable.'}
                </div>
            </c:if>

            <div id="loginRequiredMsg" class="detail-flash-message error" style="display: none;">
                Please login to add items to cart.
            </div>

            <form method="post" action="${pageContext.request.contextPath}/home/cart/add" class="detail-purchase-panel" id="addToCartForm" data-logged-in="${not empty sessionScope.USER}">
                <input type="hidden" name="productId" value="${product.productId}" />
                <input type="hidden" name="variantId" id="selectedVariantId" value="" />

                <div class="detail-options-grid single-column-mobile">
                    <div class="detail-option-card">
                        <span class="detail-option-label">Color: <strong id="selectedColorText">Choose color</strong></span>
                        <div class="detail-chip-list selectable-list" id="colorOptions">
                            <c:forEach var="colorName" items="${product.colorNames}">
                                <button type="button" class="detail-chip detail-chip-button" data-color-name="${colorName}">${colorName}</button>
                            </c:forEach>
                        </div>
                    </div>
                    <div class="detail-option-card">
                        <span class="detail-option-label">Size: <strong id="selectedSizeText">Choose size</strong></span>
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
                        <span id="selectedVariantStock">Select color and size</span>
                    </div>
                    <div class="summary-actions">
                        <div class="detail-quantity-wrap">
                            <div class="qty-control">
                                <button type="button" class="qty-btn qty-minus" onclick="adjustQty(-1)">−</button>
                                <input id="quantity" name="quantity" type="number" min="1" value="1" class="detail-quantity-input" onchange="validateQty(this)" />
                                <button type="button" class="qty-btn qty-plus" onclick="adjustQty(1)">+</button>
                            </div>
                        </div>
                        <button type="submit" class="detail-add-cart-btn" id="addToCartButton" disabled>Add to cart</button>
                    </div>
                </div>
            </form>

            <c:if test="${not empty product.variants}">
            <script>
                window.__VARIANT_DATA__ = [
                    <c:forEach var="variant" items="${product.variants}" varStatus="vs">
                        {variantId: "${variant.variantId}", colorName: "${variant.colorName}", sizeName: "${variant.sizeName}", colorHex: "${variant.colorHexCode}", imageUrl: "${empty variant.imageUrl ? '' : pageContext.request.contextPath.concat(variant.imageUrl)}", stockQty: ${variant.availableQty}, price: ${variant.priceOverride != null ? variant.priceOverride : product.basePrice}}${vs.last ? '' : ','}
                    </c:forEach>
                ];
            </script>
            </c:if>
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
                                    <img class="product-image" src="${empty p.primaryImageUrl ? 'https://via.placeholder.com/600x800?text=No+Image' : pageContext.request.contextPath.concat(p.primaryImageUrl)}" alt="${p.name}" />
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
                    </c:if>
                </c:forEach>
            </div>
        </section>
    </c:if>

    <jsp:include page="commentsModal.jsp" />
</div>

<script>
    function clearQtyValidity(input) {
        input.setCustomValidity('');
    }

    function adjustQty(delta) {
        const input = document.getElementById('quantity');
        const max = parseInt(input.max, 10);
        const cur = parseInt(input.value, 10);
        let val = isNaN(cur) ? 1 : cur + delta;
        if (val < 1) val = 1;
        if (!isNaN(max) && max > 0 && val > max) val = max;
        input.value = val;
        clearQtyValidity(input);
    }

    function validateQty(input) {
        clearQtyValidity(input);

        const raw = String(input.value || '').trim();
        if (raw === '' || isNaN(Number(raw))) {
            input.setCustomValidity('Please enter a valid number.');
            input.reportValidity();
            return;
        }

        const val = parseInt(raw, 10);
        const max = parseInt(input.max, 10);

        if (val < 1) {
            input.setCustomValidity('Quantity must be greater than or equal to 1.');
            input.reportValidity();
            return;
        }

        if (!isNaN(max) && max > 0 && val > max) {
            input.setCustomValidity('Quantity exceeds ' + max + ' in stock. Please enter ' + max + ' or less.');
            input.reportValidity();
            return;
        }
    }

    (function () {
        const colorButtons = Array.from(document.querySelectorAll('#colorOptions .detail-chip-button'));
        const sizeButtons = Array.from(document.querySelectorAll('#sizeOptions .detail-chip-button'));
        const selectedVariantId = document.getElementById('selectedVariantId');
        const selectedVariantLabel = document.getElementById('selectedVariantLabel');
        const selectedVariantStock = document.getElementById('selectedVariantStock');
        const addToCartButton = document.getElementById('addToCartButton');
        const quantityInput = document.getElementById('quantity');
        const detailPrice = document.getElementById('detailPrice');
        const addToCartForm = document.getElementById('addToCartForm');
        const selectedColorText = document.getElementById('selectedColorText');
        const selectedSizeText = document.getElementById('selectedSizeText');
        const mainImage = document.getElementById('detailMainImage');
        const thumbnailNodes = Array.from(document.querySelectorAll('.detail-thumbnail'));
        const seenImageUrls = new Set();
        thumbnailNodes.forEach(thumbnail => {
            const imageUrl = thumbnail.dataset.imageUrl || '';
            if (!imageUrl || seenImageUrls.has(imageUrl)) {
                thumbnail.remove();
                return;
            }
            seenImageUrls.add(imageUrl);
        });
        const thumbnails = Array.from(document.querySelectorAll('.detail-thumbnail'));

        let selectedColor = '';
        let selectedSize = '';

        const isLoggedIn = addToCartForm.dataset.loggedIn === 'true';

        const variantData = window.__VARIANT_DATA__ || [];

        const formatPrice = (value) => {
            const amount = Number(String(value || '0').replace(/,/g, ''));
            return amount.toLocaleString('vi-VN') + ' đ';
        };

        const setActiveButton = (buttons, value, attr) => {
            buttons.forEach(button => {
                button.classList.toggle('active', button.dataset[attr] === value);
            });
        };

        const setMainImage = (imageUrl) => {
            if (!mainImage || !imageUrl || mainImage.tagName !== 'IMG') return;
            mainImage.src = imageUrl;
            thumbnails.forEach(thumbnail => thumbnail.classList.toggle('active', thumbnail.dataset.imageUrl === imageUrl));
        };

        thumbnails.forEach(thumbnail => {
            const showThumbnailImage = () => setMainImage(thumbnail.dataset.imageUrl);
            thumbnail.addEventListener('click', showThumbnailImage);
            thumbnail.addEventListener('mouseenter', showThumbnailImage);
        });

        const colorHexByName = {};
        variantData.forEach(variant => {
            if (variant.colorHex && !colorHexByName[variant.colorName]) colorHexByName[variant.colorName] = variant.colorHex;
        });
        colorButtons.forEach(button => {
            const hex = colorHexByName[button.dataset.colorName];
            if (hex) {
                button.style.background = hex;
                button.title = button.dataset.colorName;
                button.textContent = '';
                button.classList.add('color-swatch');
            }
        });

        const variantMap = {};
        variantData.forEach(v => {
            if (!variantMap[v.colorName]) variantMap[v.colorName] = {};
            variantMap[v.colorName][v.sizeName] = v;
        });

        const refreshAvailability = () => {
            colorButtons.forEach(btn => {
                const c = btn.dataset.colorName || '';
                const rows = Object.values(variantMap[c] || {});
                const hasAny = rows.some(r => r.stockQty > 0);
                btn.disabled = !hasAny;
                btn.classList.toggle('unavailable', !hasAny);
            });
            sizeButtons.forEach(btn => {
                const s = btn.dataset.sizeName || '';
                let hasAny = false;
                Object.values(variantMap).forEach(bySize => {
                    const row = bySize[s];
                    if (row && row.stockQty > 0) hasAny = true;
                });
                btn.disabled = !hasAny;
                btn.classList.toggle('unavailable', !hasAny);
            });
        };

        const refreshVariantSelection = () => {
            const matched = (variantMap[selectedColor] || {})[selectedSize];

            if (selectedColorText) selectedColorText.textContent = selectedColor || 'Choose color';
            if (selectedSizeText) selectedSizeText.textContent = selectedSize || 'Choose size';

            if (!matched) {
                selectedVariantId.value = '';
                addToCartButton.disabled = true;
                if (!selectedColor && !selectedSize) {
                    selectedVariantLabel.textContent = 'Choose color and size';
                    selectedVariantStock.textContent = 'Available stock will appear here.';
                } else {
                    selectedVariantLabel.textContent = 'This combination is unavailable';
                    selectedVariantStock.textContent = 'Please pick another option.';
                }
                return;
            }

            const stockQty = matched.stockQty || 0;
            selectedVariantId.value = matched.variantId;
            detailPrice.textContent = formatPrice(matched.price);
            if (matched.imageUrl) setMainImage(matched.imageUrl);
            selectedVariantLabel.textContent = matched.colorName + ' / ' + matched.sizeName;
            if (stockQty > 0) {
                selectedVariantStock.textContent = stockQty + ' items available';
                quantityInput.max = String(Math.max(stockQty, 1));
                if (Number(quantityInput.value) > stockQty) {
                    quantityInput.value = String(stockQty);
                }
                addToCartButton.disabled = false;
            } else {
                selectedVariantStock.textContent = 'Out of stock';
                quantityInput.max = '0';
                addToCartButton.disabled = true;
            }
        };

        colorButtons.forEach(button => button.addEventListener('click', () => {
                if (button.disabled) return;
                selectedColor = button.dataset.colorName;
                setActiveButton(colorButtons, selectedColor, 'colorName');
                refreshVariantSelection();
            }));

        sizeButtons.forEach(button => button.addEventListener('click', () => {
                if (button.disabled) return;
                selectedSize = button.dataset.sizeName;
                setActiveButton(sizeButtons, selectedSize, 'sizeName');
                refreshVariantSelection();
            }));

        if (addToCartForm) {
            addToCartForm.addEventListener('submit', function(e) {
                if (!isLoggedIn) {
                    e.preventDefault();
                    const loginMsg = document.getElementById('loginRequiredMsg');
                    if (loginMsg) {
                        loginMsg.style.display = 'block';
                        setTimeout(() => loginMsg.style.display = 'none', 3000);
                    }
                }
            });
        }

        refreshAvailability();
    })();
</script>
