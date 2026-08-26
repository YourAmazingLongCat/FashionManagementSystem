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
                <div class="detail-image-column">
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
                    <h2 class="detail-description-heading">PRODUCT DESCRIPTION</h2>
                    <p class="detail-description">${empty product.description ? 'No description available for this product yet.' : product.description}</p>
                </div>
            </div>
        </div>

        <div class="detail-content-card">
            
            <h1 class="detail-title">${product.name}</h1>
            <span id="commentCountBadge" style="display:none;">0</span>
            <div class="detail-rating-row">
                <button type="button" id="openCommentsBtn" style="background:none;border:none;padding:0;cursor:pointer;display:inline-flex;align-items:center;gap:6px;font:inherit;">
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
                            <span class="detail-review-count" style="text-decoration:underline;color:#666;">(${ratingSummary[1]} reviews)</span>
                        </c:when>
                        <c:otherwise>
                            <span class="detail-no-rating" style="text-decoration:underline;color:#666;">No reviews yet (Click to review)</span>
                        </c:otherwise>
                    </c:choose>
                </button>
            </div>

            <div class="detail-price" id="detailPrice"><fmt:formatNumber value="${displayPrice}" type="number" groupingUsed="true"/> đ</div>

            <c:if test="${not empty param.message}">
                <div class="detail-flash-message ${param.message eq 'added-to-cart' ? 'success' : 'error'}">
                    ${param.message eq 'added-to-cart' ? 'Added to cart successfully.' : 'The product has reached the maximum limit in the cart.'}
                </div>
            </c:if>

            <div id="loginRequiredMsg" class="detail-flash-message error" style="display: none;">
                Please login to add items to cart.
            </div>

            <form method="post" action="${pageContext.request.contextPath}/cart/add" class="detail-purchase-panel" id="addToCartForm" data-logged-in="${not empty sessionScope.USER}">
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

            <section class="product-service-highlights" aria-label="Product service highlights">
                <div class="service-highlight-item">
                    <span class="service-highlight-icon" aria-hidden="true">✓</span>
                    <span>Quality Product</span>
                </div>
                <div class="service-highlight-item">
                    <span class="service-highlight-icon" aria-hidden="true">▣</span>
                    <span>Free Shipping</span>
                </div>
                <div class="service-highlight-item">
                    <span class="service-highlight-icon" aria-hidden="true">⇆</span>
                    <span>14-Day Return</span>
                </div>
                <div class="service-highlight-item">
                    <span class="service-highlight-icon" aria-hidden="true">☎</span>
                    <span>24/7 Support</span>
                </div>
            </section>

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

    <!-- ===== INLINE CUSTOMER REVIEWS & COMMENTS SECTION ===== -->
    <section class="product-section reviews-section" id="reviewsSection" style="margin-top: 40px; padding: 30px; background: #fafafa; border-radius: 12px; border: 1px solid #eaeaea;">
        <div class="section-header" style="display: flex; justify-content: space-between; align-items: center; border-bottom: 2px solid #222; padding-bottom: 12px; margin-bottom: 24px;">
            <h2 class="section-title" style="margin: 0; font-size: 1.4rem; font-weight: 700; text-transform: uppercase;">
                Customer Reviews & Ratings
            </h2>
            <div style="display: flex; align-items: center; gap: 8px;">
                <c:choose>
                    <c:when test="${not empty ratingSummary && ratingSummary[1] > 0}">
                        <span style="font-size: 1.5rem; font-weight: bold; color: #f59e0b;"><fmt:formatNumber value="${ratingSummary[0]}" maxFractionDigits="1"/> / 5.0</span>
                        <span style="color: #f59e0b; font-size: 1.2rem;">
                            <c:forEach begin="1" end="5" var="i">${i <= ratingSummary[0] + 0.5 ? '★' : '☆'}</c:forEach>
                        </span>
                        <span style="color: #666; font-size: 0.95rem;">(${ratingSummary[1]} reviews)</span>
                    </c:when>
                    <c:otherwise>
                        <span style="color: #888; font-size: 0.95rem;">No reviews yet</span>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <!-- Add Review Box -->
        <div class="review-add-card" style="background: #fff; padding: 20px; border-radius: 8px; border: 1px solid #e0e0e0; margin-bottom: 30px;">
            <c:choose>
                <c:when test="${empty sessionScope.USER}">
                    <div style="text-align: center; padding: 15px; color: #555;">
                        <p style="margin: 0 0 10px 0;">Please <strong>log in</strong> to write a review for this product.</p>
                        <a href="${pageContext.request.contextPath}/auth/login" class="detail-chip-button" style="display: inline-block; padding: 8px 20px; background: #222; color: #fff; text-decoration: none; border-radius: 4px; font-weight: 600;">Log in</a>
                    </div>
                </c:when>
                <c:when test="${not empty eligibleVariantId}">
                    <h3 style="margin: 0 0 12px 0; font-size: 1.1rem; font-weight: 600;">Write a Review</h3>
                    <form method="post" action="${pageContext.request.contextPath}/comment" style="display: flex; flex-direction: column; gap: 12px;">
                        <input type="hidden" name="action" value="add" />
                        <input type="hidden" name="productId" value="${product.productId}" />

                        <div style="display: flex; align-items: center; gap: 10px;">
                            <label style="font-weight: 600; font-size: 0.95rem;">Rating:</label>
                            <div class="star-rating-select" style="display: inline-flex; gap: 4px; font-size: 1.5rem; cursor: pointer; color: #f59e0b;">
                                <select name="rating" required style="padding: 6px 12px; border-radius: 4px; border: 1px solid #ccc; font-size: 0.95rem; font-weight: 600; color: #f59e0b;">
                                    <option value="5" selected>★★★★★ - 5 Stars (Excellent)</option>
                                    <option value="4">★★★★☆ - 4 Stars (Good)</option>
                                    <option value="3">★★★☆☆ - 3 Stars (Average)</option>
                                    <option value="2">★★☆☆☆ - 2 Stars (Poor)</option>
                                    <option value="1">★☆☆☆☆ - 1 Star (Terrible)</option>
                                </select>
                            </div>
                        </div>

                        <div>
                            <label style="display: block; font-weight: 600; font-size: 0.95rem; margin-bottom: 6px;">Review Content:</label>
                            <textarea name="content" rows="3" required placeholder="Share your thoughts about product quality, size, and fit..." style="width: 100%; box-sizing: border-box; padding: 12px; border-radius: 6px; border: 1px solid #ccc; font-family: inherit; font-size: 0.95rem; resize: vertical;"></textarea>
                        </div>

                        <div>
                            <button type="submit" style="background: #222; color: #fff; border: none; padding: 10px 24px; border-radius: 6px; font-weight: 600; font-size: 0.95rem; cursor: pointer; transition: background 0.2s;">Submit Review</button>
                        </div>
                    </form>
                </c:when>
                <c:otherwise>
                    <div style="padding: 12px 16px; background: #f8fafc; border-left: 4px solid #3b82f6; color: #475569; font-size: 0.95rem;">
                        ℹ️ Only customers who have purchased and received this product can write a review.
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <!-- Comments List -->
        <div class="comments-stream" style="display: flex; flex-direction: column; gap: 16px;">
            <c:choose>
                <c:when test="${not empty productComments}">
                    <c:forEach var="c" items="${productComments}">
                        <div class="comment-card" style="background: #fff; padding: 18px 20px; border-radius: 8px; border: 1px solid #e5e7eb; display: flex; flex-direction: column; gap: 8px;">
                            <div style="display: flex; justify-content: space-between; align-items: center;">
                                <div style="display: flex; align-items: center; gap: 10px;">
                                    <div style="width: 38px; height: 38px; border-radius: 50%; background: #1e293b; color: #fff; display: flex; align-items: center; justify-content: center; font-weight: bold; font-size: 1rem; text-transform: uppercase;">
                                        ${empty c.accountFullName ? 'U' : c.accountFullName.substring(0, 1)}
                                    </div>
                                    <div>
                                        <div style="font-weight: 600; font-size: 1rem; color: #111;">
                                            ${empty c.accountFullName ? c.accountUsername : c.accountFullName}
                                        </div>
                                        <c:if test="${not empty c.variantInfo}">
                                            <div style="font-size: 0.82rem; color: #64748b;">Purchased: ${c.variantInfo}</div>
                                        </c:if>
                                    </div>
                                </div>
                                <div style="text-align: right;">
                                    <div style="color: #f59e0b; font-size: 1.1rem;">
                                        <c:forEach begin="1" end="5" var="i">${i <= c.rating ? '★' : '☆'}</c:forEach>
                                    </div>
                                    <div style="font-size: 0.8rem; color: #94a3b8;">
                                        <fmt:formatDate value="${c.createdAt}" pattern="dd/MM/yyyy HH:mm"/>
                                    </div>
                                </div>
                            </div>
                            <p style="margin: 6px 0 0 0; color: #334155; font-size: 0.95rem; line-height: 1.5; white-space: pre-wrap;">${c.content}</p>
                        </div>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <div style="text-align: center; padding: 40px 20px; background: #fff; border-radius: 8px; border: 1px dashed #cbd5e1; color: #64748b;">
                        <span style="font-size: 2rem; display: block; margin-bottom: 8px;">💬</span>
                        <p style="margin: 0; font-size: 1rem; font-weight: 500;">No reviews yet for this product.</p>
                        <p style="margin: 4px 0 0 0; font-size: 0.88rem; color: #94a3b8;">Be the first to share your experience!</p>
                    </div>
                </c:otherwise>
            </c:choose>
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
        const min = 1;
        const cur = parseInt(input.value, 10);
        let val = isNaN(cur) ? 1 : cur + delta;

        clearQtyValidity(input);

        if (delta > 0) {
            // User pressed '+'. If we are already at the max
            if (!isNaN(max) && max > 0 && val > max) {
                input.value = String(max);
                input.setCustomValidity(
                    'You have reached the maximum quantity (' + max + ') for this variant.'
                );
                input.reportValidity();
                return;
            }
        } else if (delta < 0) {
            // User pressed '-'. If we are already at the min
            if (val < min) {
                input.value = String(min);
                input.setCustomValidity(
                    'You have reached the minimum quantity (' + min + ').'
                );
                input.reportValidity();
                return;
            }
        }

        if (val < min) val = min;
        if (!isNaN(max) && max > 0 && val > max) val = max;
        input.value = String(val);
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
