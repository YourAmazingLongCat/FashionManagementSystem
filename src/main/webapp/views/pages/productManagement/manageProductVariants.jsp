<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manage Product Variants</title>
    <style>
        * { box-sizing: border-box; }
        body { margin: 0; font-family: Arial, sans-serif; color: #111827; background: #f4f6f8; }
        .layout { display: grid; grid-template-columns: 240px minmax(0, 1fr); min-height: 100vh; }
        .sidebar { background: linear-gradient(180deg, #2c3e50, #1a252f); color: #ecf0f1; min-height: 100vh; position: sticky; top: 0; height: 100vh; }
        .brand { padding: 28px 15px; font-size: 1.5rem; font-weight: 700; border-bottom: 1px solid #34495e; text-align: center; }
        .nav { display: flex; flex-direction: column; min-height: calc(100vh - 100px); padding: 0; margin: 0; list-style: none; }
        .nav-item { list-style: none; }
        .nav-item.push { margin-top: auto; }
        .nav-link { color: #b0c4de; padding: 14px 28px; border-left: 3px solid transparent; display: block; text-decoration: none; font-weight: 600; }
        .nav-link:hover, .nav-link.active { background: #34495e; color: #fff; border-left-color: #1abc9c; }
        .main { min-width: 0; padding: 24px; }
        .page { width: 100%; margin: 0 auto; }
        .panel { background: #fff; border: 1px solid #d9dee5; border-radius: 8px; overflow: hidden; margin-bottom: 20px; }
        .panel-header { padding: 18px 22px; border-bottom: 1px solid #d9dee5; background: #f1f1f1; display: flex; align-items: center; justify-content: space-between; gap: 12px; }
        h1, h2 { margin: 0; font-size: 1.35rem; }
        h2 { font-size: 1.1rem; }
        .form { padding: 22px; display: grid; gap: 16px; }
        .form-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 14px; }
        .field { display: grid; gap: 7px; }
        .field-wide { grid-column: span 2; }
        label { font-weight: 700; font-size: 0.9rem; }
        input, select { width: 100%; min-height: 40px; padding: 9px 11px; border: 1px solid #cbd5e1; border-radius: 5px; font: inherit; background: #fff; }
        input[readonly] { background: #f8fafc; color: #475569; }
        .image-preview { width: 90px; height: 90px; object-fit: contain; border: 1px solid #d9dee5; background: #f8fafc; padding: 4px; }
        .actions { display: flex; justify-content: flex-end; gap: 10px; }
        .button, .back-link { display: inline-flex; align-items: center; justify-content: center; min-height: 40px; padding: 9px 16px; border-radius: 5px; border: 0; text-decoration: none; cursor: pointer; font-weight: 700; }
        .button { background: #16a34a; color: #fff; }
        .add-button { background: #14b8a6; }
        .close-button { background: #fff; color: #334155; border: 1px solid #cbd5e1; }
        .back-link { background: #fff; color: #334155; border: 1px solid #cbd5e1; }
        .alert { margin: 16px 22px 0; padding: 12px 14px; border-radius: 5px; font-weight: 600; }
        .alert-error { color: #991b1b; background: #fee2e2; }
        .alert-success { color: #166534; background: #dcfce7; }
        .table-wrap { overflow-x: auto; }
        table { width: 100%; border-collapse: collapse; table-layout: fixed; min-width: 1050px; }
        th, td { padding: 12px 10px; border: 1px solid #d9dee5; text-align: center; vertical-align: middle; }
        th { background: #f1f1f1; font-size: 0.82rem; letter-spacing: .05em; }
        td { height: 116px; font-size: .9rem; }
        th:nth-child(1), td:nth-child(1) { width: 11%; }
        th:nth-child(2), td:nth-child(2) { width: 10%; }
        th:nth-child(3), td:nth-child(3) { width: 15%; }
        th:nth-child(4), td:nth-child(4) { width: 12%; }
        th:nth-child(5), td:nth-child(5) { width: 24%; }
        th:nth-child(6), td:nth-child(6) { width: 12%; }
        th:nth-child(7), td:nth-child(7) { width: 8%; }
        th:nth-child(8), td:nth-child(8) { width: 8%; }
        .variant-image { width: 90px; height: 90px; object-fit: contain; }
        .no-image { color: #94a3b8; font-size: .8rem; }
        .description { color: #64748b; overflow-wrap: anywhere; overflow: hidden; }
        .description-text { display: -webkit-box; line-clamp: 3; -webkit-box-orient: vertical; -webkit-line-clamp: 3; overflow: hidden; }
        .muted { color: #64748b; }
        .pagination-bar { display: flex; align-items: center; justify-content: space-between; gap: 14px; padding: 16px 20px; flex-wrap: wrap; }
        .pagination-controls { display: flex; gap: 8px; }
        .page-link { min-width: 36px; height: 36px; padding: 0 10px; display: inline-flex; align-items: center; justify-content: center; border: 1px solid #cbd5e1; border-radius: 5px; color: #334155; text-decoration: none; font-weight: 700; }
        .page-link.active { background: #14b8a6; border-color: #14b8a6; color: #fff; }
        .modal-backdrop { position: fixed; inset: 0; z-index: 20; display: none; align-items: center; justify-content: center; padding: 24px; background: rgba(15, 23, 42, .58); }
        .modal-backdrop.open { display: flex; }
        .modal { width: min(980px, 100%); max-height: calc(100vh - 48px); overflow: auto; margin: 0; box-shadow: 0 24px 70px rgba(15, 23, 42, .28); }
        @media (max-width: 800px) { .layout { grid-template-columns: 1fr; } .sidebar { min-height: auto; height: auto; position: static; } .nav { min-height: auto; } .nav-item.push { margin-top: 0; } .main { padding: 12px; } .form-grid { grid-template-columns: 1fr; } .field-wide { grid-column: auto; } .modal-backdrop { padding: 10px; } }
    </style>
</head>
<body data-open-modal="${not empty formError}">
<div class="layout">
    <aside class="sidebar">
        <div class="brand">Staff</div>
        <ul class="nav">
            <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/staff/orders">Orders</a></li>
            <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/staff/payments">Payments</a></li>
            <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/staff/products?tab=products">Products</a></li>
            <li class="nav-item"><a class="nav-link active" href="${pageContext.request.contextPath}/staff/products?action=manageVariants">Manage Variants</a></li>
            <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/staff/warehouse/inventory">Warehouse</a></li>
            <li class="nav-item push"><a class="nav-link" href="${pageContext.request.contextPath}/profile">Profile</a></li>
            <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/auth/logout">Logout</a></li>
        </ul>
    </aside>
    <main class="main">
    <div class="page">
    <section class="panel">
        <div class="panel-header"><h1>Manage Product Variants</h1><button class="button add-button" type="button" id="openVariantModal">Add variant</button></div>
    </section>

    <div class="modal-backdrop" id="variantModalBackdrop">
    <section class="panel modal" id="variantModal">
        <div class="panel-header"><h2>Create product variant</h2><button class="button close-button" type="button" id="closeVariantModal">Close</button></div>
        <c:if test="${not empty param.message}"><div class="alert alert-${param.messageType eq 'error' ? 'error' : 'success'}">${param.message}</div></c:if>
        <c:if test="${not empty formError}"><div class="alert alert-error">${formError}</div></c:if>
        <form class="form" method="post" action="${pageContext.request.contextPath}/staff/products?action=createVariant" enctype="multipart/form-data">
            <div class="form-grid">
                <div class="field field-wide">
                    <label for="productId">Product</label>
                    <select id="productId" name="productId" required>
                        <option value="">-- Select product --</option>
                        <c:forEach var="product" items="${products}">
                            <option value="${product.productId}"
                                    data-name="${fn:escapeXml(product.name)}"
                                    data-price="${product.basePrice}"
                                    data-description="${fn:escapeXml(empty product.description ? 'No description available' : product.description)}"
                                    data-category="${fn:escapeXml(product.categoryName)}"
                                    data-category-id="${product.categoryId}"
                                    ${selectedProductId eq product.productId ? 'selected' : ''}>${product.productId} - ${product.name}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="field"><label for="productName">Product name</label><input id="productName" type="text" readonly></div>
                <div class="field"><label for="productPrice">Product price</label><input id="productPrice" type="text" readonly></div>
                <div class="field field-wide"><label for="productDescription">Description</label><input id="productDescription" type="text" readonly></div>
                <div class="field"><label for="productCategory">Category</label><input id="productCategory" type="text" readonly></div>
                <div class="field"><label for="sizeId">Size</label><select id="sizeId" name="sizeId" required><option value="">-- Select size --</option><c:forEach var="size" items="${sizes}"><option value="${size.sizeId}" data-category-id="${size.categoryId}">${size.sizeName}</option></c:forEach></select></div>
                <div class="field"><label for="colorId">Color</label><select id="colorId" name="colorId" required><option value="">-- Select color --</option><c:forEach var="color" items="${colors}"><option value="${color.colorId}">${color.colorName}</option></c:forEach></select></div>
                <div class="field"><label for="sku">SKU</label><input id="sku" name="sku" maxlength="100" placeholder="Optional SKU"></div>
                <div class="field"><label for="priceOverride">Variant price (optional)</label><input id="priceOverride" name="priceOverride" inputmode="numeric" placeholder="Uses product price"></div>
                <div class="field"><label for="variantImage">Variant image</label><input id="variantImage" name="variantImage" type="file" accept="image/*" required></div>
                <div class="field"><label>Image preview</label><div id="imagePreview" class="image-preview"><span class="no-image">No image</span></div></div>
            </div>
            <div class="actions"><a class="back-link" href="${pageContext.request.contextPath}/staff/products?tab=products">Back to products</a><button class="button" type="submit">Create variant</button></div>
        </form>
        <div id="allSizeOptions" hidden>
            <c:forEach var="size" items="${allSizes}"><span data-size-id="${size.sizeId}" data-size-name="${fn:escapeXml(size.sizeName)}" data-category-id="${size.categoryId}"></span></c:forEach>
        </div>
    </section>
    </div>

    <section class="panel">
        <div class="panel-header"><h2>Product variants</h2></div>
        <div class="table-wrap">
            <table>
                <thead><tr><th>ID PRODUCT</th><th>IMAGE</th><th>PRODUCT NAME</th><th>PRODUCT PRICE</th><th>DESCRIPTION</th><th>CATEGORY NAME</th><th>SIZE</th><th>COLOR</th></tr></thead>
                <tbody>
                <c:choose>
                    <c:when test="${empty variants}"><tr><td colspan="8" class="muted">No variants found.</td></tr></c:when>
                    <c:otherwise><c:forEach var="variant" items="${variants}"><tr>
                        <td>${variant.productId}</td>
                        <td><c:choose><c:when test="${not empty variant.imageUrl}"><img class="variant-image" src="${pageContext.request.contextPath}${variant.imageUrl}" alt="Variant image"></c:when><c:otherwise><span class="no-image">No image</span></c:otherwise></c:choose></td>
                        <td>${variant.productName}</td>
                        <td><fmt:formatNumber value="${empty variant.priceOverride ? variant.productBasePrice : variant.priceOverride}" type="number" groupingUsed="true" /> VND</td>
                        <td class="description"><span class="description-text">${empty variant.productDescription ? 'No description available' : variant.productDescription}</span></td>
                        <td>${variant.categoryName}</td><td>${variant.sizeName}</td><td>${variant.colorName}</td>
                    </tr></c:forEach></c:otherwise>
                </c:choose>
                </tbody>
            </table>
        </div>
        <c:if test="${totalPages > 1}">
            <div class="pagination-bar">
                <span class="muted">Showing ${variants.size()} of ${totalVariants} variants</span>
                <div class="pagination-controls">
                    <c:if test="${currentPage > 1}"><a class="page-link" href="?action=manageVariants&amp;productId=${selectedProductId}&amp;page=${currentPage - 1}">&#8249;</a></c:if>
                    <c:forEach var="page" begin="1" end="${totalPages}">
                        <a class="page-link ${page == currentPage ? 'active' : ''}" href="?action=manageVariants&amp;productId=${selectedProductId}&amp;page=${page}">${page}</a>
                    </c:forEach>
                    <c:if test="${currentPage < totalPages}"><a class="page-link" href="?action=manageVariants&amp;productId=${selectedProductId}&amp;page=${currentPage + 1}">&#8250;</a></c:if>
                </div>
            </div>
        </c:if>
    </section>
</div>
    </main>
</div>
<script>
(function () {
    const productSelect = document.getElementById('productId');
    const fields = { name: document.getElementById('productName'), price: document.getElementById('productPrice'), description: document.getElementById('productDescription'), category: document.getElementById('productCategory') };
    const imageInput = document.getElementById('variantImage');
    const imagePreview = document.getElementById('imagePreview');
    const modalBackdrop = document.getElementById('variantModalBackdrop');
    const openModalButton = document.getElementById('openVariantModal');
    const closeModalButton = document.getElementById('closeVariantModal');
    const sizeSelect = document.getElementById('sizeId');
    const allSizeOptions = Array.from(document.querySelectorAll('#allSizeOptions span')).map(node => ({
        id: node.dataset.sizeId,
        name: node.dataset.sizeName,
        categoryId: node.dataset.categoryId
    }));
    const formatPrice = value => value ? Number(value).toLocaleString('vi-VN') + ' VND' : '';
    const fillProduct = () => {
        const option = productSelect.options[productSelect.selectedIndex];
        const data = option && option.value ? option.dataset : {};
        fields.name.value = data.name || '';
        fields.price.value = formatPrice(data.price);
        fields.description.value = data.description || '';
        fields.category.value = data.category || '';
    };
    const refreshSizes = () => {
        const option = productSelect.options[productSelect.selectedIndex];
        const categoryId = option ? option.dataset.categoryId : '';
        sizeSelect.innerHTML = '<option value="">-- Select size --</option>';
        allSizeOptions.filter(size => size.categoryId === categoryId).forEach(size => {
            const sizeOption = document.createElement('option');
            sizeOption.value = size.id;
            sizeOption.textContent = size.name;
            sizeSelect.appendChild(sizeOption);
        });
    };
    productSelect.addEventListener('change', () => { fillProduct(); refreshSizes(); });
    imageInput.addEventListener('change', () => {
        const file = imageInput.files && imageInput.files[0];
        if (!file) { imagePreview.innerHTML = '<span class="no-image">No image</span>'; return; }
        imagePreview.innerHTML = '';
        const previewImage = document.createElement('img');
        previewImage.className = 'image-preview';
        previewImage.src = URL.createObjectURL(file);
        previewImage.alt = 'Variant preview';
        imagePreview.appendChild(previewImage);
    });
    const openModal = () => modalBackdrop.classList.add('open');
    const closeModal = () => modalBackdrop.classList.remove('open');
    openModalButton.addEventListener('click', openModal);
    closeModalButton.addEventListener('click', closeModal);
    modalBackdrop.addEventListener('click', event => { if (event.target === modalBackdrop) closeModal(); });
    document.addEventListener('keydown', event => { if (event.key === 'Escape') closeModal(); });
    fillProduct();
    refreshSizes();
    if (document.body.dataset.openModal === 'true') openModal();
})();
</script>
</body>
</html>
