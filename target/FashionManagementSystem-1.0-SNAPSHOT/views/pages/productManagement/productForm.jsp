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
            body { margin: 0; font-family: 'Inter', sans-serif; color: #0f172a; background: radial-gradient(circle at top left, rgba(124, 58, 237, 0.18), transparent 28%), radial-gradient(circle at top right, rgba(59, 130, 246, 0.14), transparent 24%), linear-gradient(135deg, #f8fafc 0%, #eef2ff 100%); min-height: 100vh; }
            .form-shell { width: min(1340px, calc(100% - 40px)); margin: 28px auto; }
            .form-panel { background: rgba(255, 255, 255, 0.94); border: 1px solid rgba(226, 232, 240, 0.9); border-radius: 30px; box-shadow: 0 24px 60px rgba(15, 23, 42, 0.16); overflow: hidden; backdrop-filter: blur(12px); }
            .form-hero { padding: 30px; background: linear-gradient(135deg, #ffffff 0%, #ede9fe 100%); border-bottom: 1px solid #e2e8f0; }
            .eyebrow { margin: 0 0 10px; text-transform: uppercase; letter-spacing: 0.18em; font-size: 0.74rem; font-weight: 700; color: #7c3aed; }
            .form-panel h1 { margin: 0; font-size: 2.15rem; line-height: 1.12; }
            .form-body { padding: 30px; display: grid; gap: 24px; }
            .alert { padding: 16px 18px; border-radius: 18px; font-weight: 600; }
            .alert-error { background: rgba(220, 38, 38, 0.12); color: #991b1b; border: 1px solid rgba(220, 38, 38, 0.2); }
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
            .image-preview { width: 140px; height: 140px; border-radius: 20px; object-fit: cover; border: 1px solid #dbe3f0; background: #f8fafc; display: flex; align-items: center; justify-content: center; color: #94a3b8; overflow: hidden; }
            .image-preview img { width: 100%; height: 100%; object-fit: cover; display: block; }
            .variants-list { display: grid; gap: 16px; }
            .variant-row { border: 1px solid #e2e8f0; border-radius: 22px; padding: 18px; background: #ffffff; display: grid; gap: 16px; }
            .variant-row-header { display: flex; justify-content: space-between; align-items: center; gap: 12px; }
            .variant-row-title { font-weight: 800; color: #334155; }
            .variant-row-grid { display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)); gap: 16px; align-items: end; }
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
            @media (max-width: 900px) { .form-grid { grid-template-columns: 1fr; } }
            @media (max-width: 768px) { .form-shell { width: min(100% - 20px, 100%); margin: 10px auto; } .form-actions, .section-heading, .variant-row-header { flex-direction: column; align-items: stretch; } .variant-row-grid { grid-template-columns: 1fr; } .form-body, .form-section, .form-hero { padding: 20px; } }
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
                    <c:if test="${not empty error}">
                        <div class="alert alert-error">${error}</div>
                    </c:if>

                    <form method="post" action="${pageContext.request.contextPath}/admin/products" class="product-form" enctype="multipart/form-data">
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
                                        <input id="basePrice" name="basePrice" type="text" inputmode="numeric" value="${empty product.basePrice ? '' : product.basePrice.setScale(0, 0)}" placeholder="650.000" required>
                                        <span class="price-suffix">đ</span>
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

                        <section class="form-section">
                            <div class="section-heading">
                                <div>
                                    <h3>Product variants</h3>
                                    <p>Add each size/color option as a separate row.</p>
                                </div>
                                <button type="button" class="variant-add-btn" id="addVariantBtn">Add variant</button>
                            </div>

                            <div id="variantsList" class="variants-list"></div>
                            <div id="variantsEmpty" class="variant-empty">No variants added yet. Click <strong>Add variant</strong> to create one.</div>
                            <p class="inline-note">Each row represents one exact product option, for example: Size M + Black.</p>
                        </section>

                        <section class="form-section">
                            <div class="section-heading"><h3>Image</h3></div>
                            <div class="form-grid">
                                <div class="form-group">
                                    <label for="productImage">Upload image</label>
                                    <input id="productImage" name="productImage" type="file" accept="image/*">
                                </div>
                                <div class="form-group">
                                    <label>Current image</label>
                                    <div id="imagePreview" class="image-preview" data-existing-src="${product.primaryImageUrl}">
                                        <c:choose>
                                            <c:when test="${not empty product.primaryImageUrl}">
                                                <img src="${product.primaryImageUrl}" alt="Product image preview">
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
                            <a class="ghost-btn" href="${pageContext.request.contextPath}/admin/products?tab=products">Back to products</a>
                            <button type="submit" class="primary-btn">${formAction eq 'edit' ? 'Save changes' : 'Create product'}</button>
                        </div>
                    </form>

                    <div id="variantOptionsData" style="display:none;">
                        <div id="sizeOptionsData" data-selected-category-id="${product.categoryId}">
                            <c:forEach var="size" items="${allSizes}">
                                <div data-size-id="${size.sizeId}" data-size-name="${size.sizeName}" data-category-id="${size.categoryId}"></div>
                            </c:forEach>
                        </div>
                        <div id="colorOptionsData">
                            <c:forEach var="color" items="${colors}">
                                <div data-color-id="${color.colorId}" data-color-name="${fn:escapeXml(color.colorName)}"></div>
                            </c:forEach>
                        </div>
                        <div id="existingVariantsData">
                            <c:forEach var="variant" items="${product.variants}">
                                <div class="variant-data"
                                     data-size-id="${variant.sizeId}"
                                     data-color-id="${variant.colorId}"
                                     data-sku="${variant.sku}"
                                     data-stock-qty="${variant.stockQty}"
                                     data-price-override="${variant.priceOverride == null ? '' : variant.priceOverride.setScale(0, 0)}"></div>
                            </c:forEach>
                        </div>
                    </div>
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

                bindCurrencyInput(document.getElementById('basePrice'));

                const categorySelect = document.getElementById('categoryId');
                const productImageInput = document.getElementById('productImage');
                const imagePreview = document.getElementById('imagePreview');
                const variantsList = document.getElementById('variantsList');
                const variantsEmpty = document.getElementById('variantsEmpty');
                const addVariantBtn = document.getElementById('addVariantBtn');
                const allSizeOptions = Array.from(document.querySelectorAll('#sizeOptionsData div')).map(node => ({
                    id: node.dataset.sizeId,
                    name: node.dataset.sizeName,
                    categoryId: node.dataset.categoryId
                }));
                const colorOptions = Array.from(document.querySelectorAll('#colorOptionsData div')).map(node => ({
                    id: node.dataset.colorId,
                    name: node.dataset.colorName
                }));
                const existingVariants = Array.from(document.querySelectorAll('#existingVariantsData .variant-data')).map(node => ({
                    sizeId: node.dataset.sizeId || '',
                    colorId: node.dataset.colorId || '',
                    sku: node.dataset.sku || '',
                    stockQty: node.dataset.stockQty || '0',
                    priceOverride: node.dataset.priceOverride || ''
                }));
                let sizeOptions = [];

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
                        if (title) title.textContent = `Variant ${index + 1}`;
                    });
                };

                const refreshAllSizeSelects = () => {
                    Array.from(variantsList.querySelectorAll('select[name="variantSizeId"]')).forEach(select => {
                        replaceSelectOptions(select, sizeOptions, select.value, '-- Select size --');
                        select.disabled = sizeOptions.length === 0;
                    });
                };

                const attachVariantRowEvents = (row) => {
                    const removeBtn = row.querySelector('.variant-remove-btn');
                    const priceInput = row.querySelector('input[name="variantPriceOverride"]');
                    const sizeSelect = row.querySelector('select[name="variantSizeId"]');
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

                    const header = document.createElement('div');
                    header.className = 'variant-row-header';

                    const title = document.createElement('div');
                    title.className = 'variant-row-title';
                    title.textContent = `Variant ${variantsList.children.length + 1}`;

                    const removeBtn = document.createElement('button');
                    removeBtn.type = 'button';
                    removeBtn.className = 'variant-remove-btn';
                    removeBtn.textContent = 'Remove';

                    header.appendChild(title);
                    header.appendChild(removeBtn);

                    const grid = document.createElement('div');
                    grid.className = 'variant-row-grid';

                    const sizeSelect = createSelect('variantSizeId', sizeOptions, variant.sizeId || '', '-- Select size --');
                    sizeSelect.disabled = sizeOptions.length === 0;

                    grid.appendChild(createField('Size', sizeSelect));
                    grid.appendChild(createField('Color', createSelect('variantColorId', colorOptions, variant.colorId || '', '-- Select color --')));
                    grid.appendChild(createField('SKU', createInput({ name: 'variantSku', value: variant.sku || '', placeholder: 'Required SKU' })));
                    grid.appendChild(createField('Stock', createInput({ type: 'number', name: 'variantStockQty', value: variant.stockQty || '0', placeholder: '0', min: '0' })));
                    grid.appendChild(createField('Price override', createInput({ name: 'variantPriceOverride', value: variant.priceOverride || '', placeholder: 'Optional', inputMode: 'numeric' })));

                    const enabledInput = document.createElement('input');
                    enabledInput.type = 'hidden';
                    enabledInput.name = 'variantEnabled';
                    enabledInput.value = 'true';

                    row.appendChild(header);
                    row.appendChild(grid);
                    row.appendChild(enabledInput);

                    variantsList.appendChild(row);
                    attachVariantRowEvents(row);
                    updateEmptyState();
                };

                const loadSizesByCategory = (categoryId) => {
                    sizeOptions = getSizeOptionsByCategory(categoryId);
                    refreshAllSizeSelects();
                };

                if (categorySelect) {
                    categorySelect.addEventListener('change', () => {
                        loadSizesByCategory(categorySelect.value);
                    });
                }

                if (productImageInput && imagePreview) {
                    productImageInput.addEventListener('change', () => {
                        const file = productImageInput.files && productImageInput.files[0];
                        if (!file) {
                            setImagePreview(imagePreview.dataset.existingSrc || '');
                            return;
                        }

                        const fileReader = new FileReader();
                        fileReader.onload = (event) => {
                            setImagePreview(event.target && event.target.result ? String(event.target.result) : '');
                        };
                        fileReader.readAsDataURL(file);
                    });
                }

                if (addVariantBtn) {
                    addVariantBtn.disabled = false;
                    addVariantBtn.addEventListener('click', () => addVariantRow());
                }

                loadSizesByCategory(categorySelect ? categorySelect.value : '');

                if (existingVariants.length > 0) {
                    existingVariants.forEach(addVariantRow);
                } else {
                    addVariantRow();
                }

                setImagePreview(imagePreview ? imagePreview.dataset.existingSrc || '' : '');
                renumberVariantRows();
                updateEmptyState();
            })();
        </script>
    </body>
</html>
