<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>${pageTitle}</title>
        <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/views/pages/productManagement/product-management.css?v=20260609-product-manual-variants-4">
        <style>
            body { margin: 0; font-family: 'Inter', sans-serif; color: #0f172a; background: linear-gradient(135deg, #f8fafc 0%, #eef2ff 100%); min-height: 100vh; }
            .form-shell { width: min(1340px, calc(100% - 40px)); margin: 28px auto; }
            .form-panel { background: #ffffff; border: 1px solid rgba(226, 232, 240, 0.9); border-radius: 24px; box-shadow: 0 16px 40px rgba(15, 23, 42, 0.1); overflow: hidden; contain: content; }
            .form-hero { padding: 30px; background: #ffffff; border-bottom: 1px solid #e2e8f0; }
            .eyebrow { margin: 0 0 10px; text-transform: uppercase; letter-spacing: 0.18em; font-size: 0.74rem; font-weight: 700; color: #7c3aed; }
            .form-panel h1 { margin: 0; font-size: 2.15rem; line-height: 1.12; }
            .form-body { padding: 30px; display: grid; gap: 24px; }
            .alert { padding: 16px 18px; border-radius: 18px; font-weight: 600; }
            .alert-error { background: rgba(220, 38, 38, 0.12); color: #991b1b; border: 1px solid rgba(220, 38, 38, 0.2); }
            .alert-success { background: rgba(22, 163, 74, 0.12); color: #166534; border: 1px solid rgba(22, 163, 74, 0.2); }
            .product-form { display: grid; gap: 24px; }
            .form-section { background: linear-gradient(180deg, #ffffff 0%, #fbfbff 100%); border: 1px solid #e2e8f0; border-radius: 26px; padding: 24px; }
            .section-heading { margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center; gap: 12px; }
            .section-heading h3 { margin: 0; font-size: 1.15rem; }
            .section-heading p { margin: 4px 0 0; color: #64748b; font-size: 0.88rem; }
            .form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 20px; }
            .form-group { display: grid; gap: 10px; }
            .full-width { width: 100%; }
            label { font-weight: 700; color: #334155; }
            input, select, textarea, button { font: inherit; box-sizing: border-box; }
            input, select, textarea { width: 100%; padding: 14px 16px; border-radius: 16px; border: 1px solid #dbe3f0; background: #ffffff; color: #0f172a; transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease; }
            input:focus, select:focus, textarea:focus { outline: none; border-color: #7c3aed; box-shadow: 0 0 0 4px rgba(124, 58, 237, 0.12); transform: translateY(-1px); }
            textarea { resize: vertical; min-height: 180px; }
            .price-input-wrap { position: relative; }
            .price-suffix { position: absolute; right: 16px; top: 50%; transform: translateY(-50%); color: #64748b; font-weight: 700; pointer-events: none; }
            .image-preview { width: 140px; height: 140px; border-radius: 20px; object-fit: cover; border: 1px solid #dbe3f0; background: #f8fafc; display: flex; align-items: center; justify-content: center; color: #94a3b8; overflow: hidden; flex-shrink: 0; }
            .image-preview img { width: 100%; height: 100%; object-fit: cover; display: block; }
            .image-section-grid { align-items: start; }
            .image-preview-group { align-self: start; }
            .variants-list { display: grid; gap: 16px; }
            .variant-row { border: 1px solid #e2e8f0; border-radius: 22px; padding: 18px; background: #ffffff; display: grid; gap: 16px; }
            .variant-row-header { display: flex; justify-content: space-between; align-items: center; gap: 12px; }
            .variant-row-title-wrap { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
            .variant-row-title { font-weight: 800; color: #334155; }
            .variant-stock-badge { display: inline-flex; align-items: center; padding: 5px 12px; border-radius: 999px; font-size: 0.78rem; font-weight: 700; }
            .variant-stock-badge.in-stock { background: rgba(22, 163, 74, 0.12); color: #15803d; border: 1px solid rgba(22, 163, 74, 0.25); }
            .variant-stock-badge.out-of-stock { background: rgba(220, 38, 38, 0.12); color: #b91c1c; border: 1px solid rgba(220, 38, 38, 0.25); }
            .variant-row-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 16px; align-items: end; }
            .variant-remove-btn, .variant-add-btn { display: inline-flex; align-items: center; justify-content: center; gap: 8px; padding: 10px 16px; border-radius: 14px; font-weight: 700; border: none; cursor: pointer; transition: transform 0.18s ease, box-shadow 0.18s ease, background 0.18s ease; }
            .variant-add-btn { background: linear-gradient(135deg, #7c3aed 0%, #4f46e5 100%); color: #ffffff; box-shadow: 0 12px 24px rgba(124, 58, 237, 0.2); }
            .variant-remove-btn { background: rgba(220, 38, 38, 0.12); color: #b91c1c; }
            .variant-add-btn:hover, .variant-remove-btn:hover { transform: translateY(-2px); }
            .variant-empty { padding: 24px; border-radius: 20px; border: 2px dashed #dbe3f0; color: #94a3b8; text-align: center; background: #ffffff; }
            .inline-note { color: #64748b; font-size: 0.9rem; }
            .form-actions { display: flex; justify-content: flex-end; align-items: center; gap: 12px; padding-top: 6px; }
            .primary-btn, .ghost-btn { display: inline-flex; align-items: center; justify-content: center; gap: 8px; padding: 12px 18px; border-radius: 14px; font-weight: 700; text-decoration: none; border: none; cursor: pointer; transition: transform 0.18s ease, box-shadow 0.18s ease, background 0.18s ease; }
            .primary-btn { background: linear-gradient(135deg, #7c3aed 0%, #4f46e5 100%); color: #ffffff; box-shadow: 0 18px 30px rgba(124, 58, 237, 0.22); }
            .ghost-btn { background: #ffffff; color: #334155; border: 1px solid #dbe3f0; }
            .primary-btn:hover, .ghost-btn:hover { transform: translateY(-2px); }
            @media (max-width: 1100px) { .variant-row-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
            @media (max-width: 768px) { .form-shell { width: min(100% - 20px, 100%); margin: 10px auto; } .form-actions, .section-heading, .variant-row-header { flex-direction: column; align-items: stretch; } .variant-row-grid { grid-template-columns: 1fr 1fr; } .form-body, .form-section, .form-hero { padding: 20px; } }
        </style>
    </head>
    <body>
        <div class="form-shell">
            <section class="form-panel wide">
                <div class="form-hero">
                    <p class="eyebrow">Admin / Product</p>
                    <h1>${pageTitle}</h1>
                </div>

                <div class="form-body">
                    <c:if test="${not empty success}">
                        <div class="alert alert-success">${success}</div>
                    </c:if>
                    <c:if test="${not empty error}">
                        <div class="alert alert-error">${error}</div>
                    </c:if>

                    <form id="productForm" method="post" action="${pageContext.request.contextPath}/staff/products" class="product-form" enctype="multipart/form-data">
                        <input type="hidden" name="action" value="${formAction}">
                        <input type="hidden" name="existingImageUrl" value="${product.primaryImageUrl}">
                        <c:if test="${formAction eq 'edit'}">
                            <input type="hidden" name="productId" value="${product.productId}">
                        </c:if>

                        <section class="form-section">
                            <div class="section-heading">
                                <h3>Product details</h3>
                            </div>
                            <div class="form-grid">
                                <div class="form-group">
                                    <label for="name">Product name</label>
                                    <input id="name" name="name" type="text" maxlength="200" value="${product.name}" placeholder="Ex: Oversized Graphic Tee" required>
                                </div>
                                <div class="form-group">
                                    <label for="categoryId">Category</label>
                                    <select id="categoryId" name="categoryId" required>
                                        <option value="">-- Select category --</option>
                                        <c:forEach var="category" items="${categories}">
                                            <option value="${category.categoryId}" ${product.categoryId eq category.categoryId ? 'selected' : ''}>${category.name}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                        </section>

                        <section class="form-section">
                            <div class="section-heading">
                                <h3>Pricing and availability</h3>
                            </div>
                            <div class="form-grid">
                                <div class="form-group">
                                    <label for="basePrice">Base price</label>
                                    <div class="price-input-wrap">
                                        <input id="basePrice" name="basePrice" type="text" inputmode="numeric" value="${empty product.basePrice ? '' : product.basePrice.setScale(0, 0)}" placeholder="650.000" required class="base-price-input">
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="status">Status</label>
                                    <select id="status" name="status" required>
                                        <c:forEach var="item" items="${statuses}">
                                            <option value="${item}" ${product.status eq item ? 'selected' : ''}>${item}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                        </section>

                        <c:if test="${formAction eq 'edit'}">
                            <section class="form-section">
                                <div class="section-heading"><h3>Inventory summary</h3></div>
                                <div style="display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px;">
                                    <div class="meta-card" style="padding: 16px; border-radius: 18px; background: #f8fafc; border: 1px solid #e2e8f0;">
                                        <p style="margin: 0; color: #94a3b8; font-size: 0.78rem; text-transform: uppercase; letter-spacing: 0.12em; font-weight: 700;">Total variants</p>
                                        <strong style="display: block; margin-top: 8px; font-size: 1.6rem;">${fn:length(product.variants)}</strong>
                                    </div>
                                    <div class="meta-card" style="padding: 16px; border-radius: 18px; background: #f8fafc; border: 1px solid #e2e8f0;">
                                        <p style="margin: 0; color: #94a3b8; font-size: 0.78rem; text-transform: uppercase; letter-spacing: 0.12em; font-weight: 700;">In stock</p>
                                        <strong style="display: block; margin-top: 8px; font-size: 1.6rem; color: #15803d;">${product.totalStockQty}</strong>
                                    </div>
                                    <div class="meta-card" style="padding: 16px; border-radius: 18px; background: #f8fafc; border: 1px solid #e2e8f0;">
                                        <p style="margin: 0; color: #94a3b8; font-size: 0.78rem; text-transform: uppercase; letter-spacing: 0.12em; font-weight: 700;">Status</p>
                                        <strong style="display: block; margin-top: 8px; font-size: 1.1rem;">${product.status}</strong>
                                    </div>
                                </div>
                                <p class="inline-note" style="margin-top: 14px;">Stock quantity is the total physical units in warehouse. Use <strong>Manage stock</strong> above to import new inventory.</p>
                            </section>
                        </c:if>

                        <section class="form-section">
                            <div class="section-heading"><h3>Image</h3></div>
                            <div class="form-grid image-section-grid">
                                <div class="form-group">
                                    <label for="productImage">Upload image</label>
                                    <input id="productImage" name="productImage" type="file" accept="image/*" ${formAction eq 'create' ? 'required' : ''}>
                                </div>
                                <div class="form-group image-preview-group">
                                    <label>Current image</label>
                                    <div id="imagePreview" class="image-preview" data-existing-src="${product.primaryImageUrl}">
                                        <c:choose>
                                            <c:when test="${not empty product.primaryImageUrl}">
                                                <img src="${pageContext.request.contextPath}${product.primaryImageUrl}" alt="Product image preview">
                                            </c:when>
                                            <c:otherwise>
                                                <span>No image</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </div>
                        </section>

                        <section class="form-section">
                            <div class="section-heading"><h3>Description</h3></div>
                            <div class="form-group full-width">
                                <label for="description">Product description</label>
                                <textarea id="description" name="description" rows="6" placeholder="Add a short description about material, style, or target audience...">${product.description}</textarea>
                            </div>
                        </section>

                        <div class="form-actions">
                            <a class="ghost-btn" href="${pageContext.request.contextPath}/staff/products?tab=products">Back to products</a>
                            <c:if test="${formAction eq 'edit'}">
                                <a class="ghost-btn" href="${pageContext.request.contextPath}/staff/products?action=manageVariants&productId=${product.productId}">Manage Product Variants</a>
                            </c:if>
                            <button type="submit" class="primary-btn">${formAction eq 'edit' ? 'Save changes' : 'Create product'}</button>
                        </div>
                    </form>

                </div>
            </section>
        </div>

        <script>
            (function () {
                const formatPrice = (value) => {
                    const digits = (value || '').replace(/\D/g, '');
                    if (!digits) return '';
                    return Number(digits).toLocaleString('vi-VN');
                };

                const bindCurrencyInput = (input) => {
                    if (!input) return;
                    input.value = formatPrice(input.value);
                    input.addEventListener('input', function () {
                        this.value = formatPrice(this.value);
                    });
                };

                const createField = (labelText, inputElement) => {
                    const wrapper = document.createElement('div');
                    wrapper.className = 'form-group';
                    const label = document.createElement('label');
                    label.textContent = labelText;
                    wrapper.appendChild(label);
                    wrapper.appendChild(inputElement);
                    return wrapper;
                };

                const createSelect = (name, items, selectedValue, placeholder) => {
                    const select = document.createElement('select');
                    select.name = name;
                    select.required = true;

                    const placeholderOption = document.createElement('option');
                    placeholderOption.value = '';
                    placeholderOption.textContent = placeholder;
                    select.appendChild(placeholderOption);

                    items.forEach(item => {
                        const option = document.createElement('option');
                        option.value = item.id;
                        option.textContent = item.name;
                        if ((selectedValue || '') === item.id) {
                            option.selected = true;
                        }
                        select.appendChild(option);
                    });

                    return select;
                };

                const replaceSelectOptions = (select, items, selectedValue, placeholder) => {
                    if (!select) return;
                    const currentValue = selectedValue == null ? select.value : selectedValue;
                    select.innerHTML = '';

                    const placeholderOption = document.createElement('option');
                    placeholderOption.value = '';
                    placeholderOption.textContent = placeholder;
                    select.appendChild(placeholderOption);

                    let hasSelectedValue = false;
                    items.forEach(item => {
                        const option = document.createElement('option');
                        option.value = item.id;
                        option.textContent = item.name;
                        if ((currentValue || '') === item.id) {
                            option.selected = true;
                            hasSelectedValue = true;
                        }
                        select.appendChild(option);
                    });

                    if (!hasSelectedValue) {
                        select.value = '';
                    }
                };

                const createInput = (config) => {
                    const input = document.createElement('input');
                    input.type = config.type || 'text';
                    input.name = config.name;
                    if (config.placeholder) input.placeholder = config.placeholder;
                    if (config.value != null) input.value = config.value;
                    if (config.min != null) input.min = config.min;
                    if (config.inputMode) input.inputMode = config.inputMode;
                    if (config.required) input.required = true;
                    return input;
                };

                const setImagePreview = (src) => {
                    const imagePreview = document.getElementById('imagePreview');
                    if (!imagePreview) return;
                    imagePreview.innerHTML = '';

                    if (src) {
                        const img = document.createElement('img');
                        img.src = src;
                        img.alt = 'Product image preview';
                        imagePreview.appendChild(img);
                        return;
                    }

                    const text = document.createElement('span');
                    text.textContent = 'No image';
                    imagePreview.appendChild(text);
                };

                const ctx = '${pageContext.request.contextPath}';

                bindCurrencyInput(document.getElementById('basePrice'));

                const categorySelect = document.getElementById('categoryId');
                const productImageInput = document.getElementById('productImage');
                const imagePreview = document.getElementById('imagePreview');
                const getSizeOptionsByCategory = (categoryId) => {
                    if (!categoryId) {
                        return [];
                    }
                    return allSizeOptions.filter(item => item.categoryId === categoryId);
                };

                const updateEmptyState = () => {
                    variantsEmpty.style.display = variantsList.children.length === 0 ? 'block' : 'none';
                };

                const renumberVariantRows = () => {
                    Array.from(variantsList.children).forEach((row, index) => {
                        const title = row.querySelector('.variant-row-title');
                        if (title) title.textContent = 'Variant';
                    });
                };

                const refreshAllSizeSelects = () => {
                    Array.from(variantsList.querySelectorAll('select[name="variantSizeId"]')).forEach(select => {
                        if (sizeOptions.length > 0) {
                            const currentVal = select.value;
                            select.innerHTML = '';
                            const placeholderOption = document.createElement('option');
                            placeholderOption.value = '';
                            placeholderOption.textContent = '-- Select size --';
                            select.appendChild(placeholderOption);

                            let hasSelectedValue = false;
                            sizeOptions.forEach(item => {
                                const option = document.createElement('option');
                                option.value = item.id;
                                option.textContent = item.name;
                                if ((currentVal || '') === item.id) {
                                    option.selected = true;
                                    hasSelectedValue = true;
                                }
                                select.appendChild(option);
                            });

                            if (!hasSelectedValue) {
                                select.value = '';
                            }
                        }
                        select.disabled = sizeOptions.length === 0;
                    });
                };

                const attachVariantRowEvents = (row) => {
                    const removeBtn = row.querySelector('.variant-remove-btn');
                    const priceInput = row.querySelector('input[name="variantPriceOverride"]');
                    const sizeSelect = row.querySelector('select[name="variantSizeId"]');
                    const colorSelect = row.querySelector('select[name="variantColorId"]');

                    if (removeBtn) {
                        removeBtn.addEventListener('click', () => {
                            row.remove();
                            renumberVariantRows();
                            updateEmptyState();
                        });
                    }

                    if (sizeSelect) {
                        sizeSelect.disabled = sizeOptions.length === 0;
                    }
                    bindCurrencyInput(priceInput);
                };

                const addVariantRow = (variant = {}) => {
                    const row = document.createElement('div');
                    row.className = 'variant-row';
                    if (variant.variantId) row.dataset.variantId = variant.variantId;

                    const header = document.createElement('div');
                    header.className = 'variant-row-header';

                    const titleWrap = document.createElement('div');
                    titleWrap.className = 'variant-row-title-wrap';
                    const title = document.createElement('div');
                    title.className = 'variant-row-title';
                    title.textContent = 'Variant';
                    titleWrap.appendChild(title);

                    // Hiển thị stockQty hiện tại (chỉ khi edit - có variantId)
                    if (variant.variantId) {
                        const stockBadge = document.createElement('span');
                        const stockNum = Number(variant.availableQty || 0);
                        stockBadge.className = 'variant-stock-badge ' + (stockNum > 0 ? 'in-stock' : 'out-of-stock');
                        stockBadge.textContent = 'Stock: ' + stockNum;
                        stockBadge.title = 'Current available stock (managed in Warehouse module)';
                        titleWrap.appendChild(stockBadge);
                    }

                    const removeBtn = document.createElement('button');
                    removeBtn.type = 'button';
                    removeBtn.className = 'variant-remove-btn';
                    removeBtn.textContent = 'Remove';

                    header.appendChild(titleWrap);
                    header.appendChild(removeBtn);

                    const grid = document.createElement('div');
                    grid.className = 'variant-row-grid';

                    const sizeSelect = createSelect('variantSizeId', sizeOptions, variant.sizeId || '', '-- Select size --');
                    const colorSelect = createSelect('variantColorId', colorOptions, variant.colorId || '', '-- Select color --');
                    sizeSelect.disabled = sizeOptions.length === 0;

                    grid.appendChild(createField('Size', sizeSelect));
                    grid.appendChild(createField('Color', colorSelect));
                    grid.appendChild(createField('SKU', createInput({ name: 'variantSku', value: variant.sku || '', placeholder: 'Required SKU' })));
                    grid.appendChild(createField('Price override', createInput({ name: 'variantPriceOverride', value: variant.priceOverride || '', placeholder: 'Optional', inputMode: 'numeric' })));

                    const enabledInput = document.createElement('input');
                    enabledInput.type = 'hidden';
                    enabledInput.name = 'variantEnabled';
                    enabledInput.value = 'true';

                    // Preserve existing variantId so the server can UPDATE the
                    // row instead of inserting a brand new one. Without this
                    // hidden input the server would treat every save as a
                    // fresh variant and wipe out any warehouse import history
                    // (variantId changes -> FK references are lost).
                    let variantIdInput = null;
                    if (variant.variantId) {
                        variantIdInput = document.createElement('input');
                        variantIdInput.type = 'hidden';
                        variantIdInput.name = 'variantId';
                        variantIdInput.value = variant.variantId;
                    }

                    row.appendChild(header);
                    row.appendChild(grid);
                    row.appendChild(enabledInput);
                    if (variantIdInput) row.appendChild(variantIdInput);

                    variantsList.appendChild(row);
                    attachVariantRowEvents(row);
                    renumberVariantRows();
                    updateEmptyState();
                };

                const loadSizesByCategory = (categoryId) => {
                    sizeOptions = getSizeOptionsByCategory(categoryId);
                    refreshAllSizeSelects();
                };

                if (productImageInput && imagePreview) {
                    productImageInput.addEventListener('change', () => {
                        const file = productImageInput.files && productImageInput.files[0];
                        if (!file) {
                            const existingSrc = imagePreview.dataset.existingSrc || '';
                            setImagePreview(existingSrc ? ctx + existingSrc : '');
                            return;
                        }

                        const fileReader = new FileReader();
                        fileReader.onload = (event) => {
                            setImagePreview(event.target && event.target.result ? String(event.target.result) : '');
                        };
                        fileReader.readAsDataURL(file);
                    });
                }

                setImagePreview(imagePreview && imagePreview.dataset.existingSrc ? ctx + imagePreview.dataset.existingSrc : '');
            })();
        </script>
    </body>
</html>
