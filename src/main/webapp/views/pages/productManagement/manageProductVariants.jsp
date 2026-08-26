<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manage Product Variants</title>
    <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <style>
        body { margin: 0; font-family: system-ui, -apple-system, "Segoe UI", Roboto, sans-serif; color: #2c3e50; background: #f8f9fa; }
        .container-fluid { padding: 0; }
        .row { margin: 0; }
        .row > * { padding: 0; }
        .sidebar {
            background: linear-gradient(180deg, #2c3e50, #1a252f);
            position: sticky; top: 0; height: 100vh; overflow-y: auto;
            align-self: flex-start; padding: 0; color: #ecf0f1;
        }
        .sidebar .brand {
            padding: 20px 15px; font-size: 1.5rem; font-weight: 600;
            border-bottom: 1px solid #34495e; text-align: center;
        }
        .sidebar .nav-link {
            color: #b0c4de; padding: 12px 20px; border-left: 3px solid transparent;
            transition: 0.3s; font-weight: 500; display: flex; align-items: center;
            text-decoration: none; font-size: 1rem; line-height: 1.5;
        }
        .sidebar .nav-link:hover, .sidebar .nav-link.active {
            background: #34495e; color: #fff; border-left-color: #1abc9c;
        }
        .sidebar .nav-link i { width: 24px; margin-right: 10px; }
        .sidebar .nav { display: flex; flex-direction: column; min-height: calc(100vh - 130px); padding: 0; margin: 0; list-style: none; }
        .sidebar .nav-item { list-style: none; }
        .sidebar .nav-item.mt-auto { margin-top: auto; }
        .main-content { padding: 20px 30px; }
        .card { border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); border: 1px solid #e2e8f0; background: #fff; }
        .card-header { background: #f8f9fa; font-weight: 600; border-bottom: 1px solid #e2e8f0; }
        .table th { background: #f1f3f5; border-top: none; font-size: 0.82rem; letter-spacing: 0.05em; }
        .alert { padding: 14px 16px; border-radius: 12px; font-weight: 600; margin-bottom: 18px; }
        .alert-error { background: rgba(220, 38, 38, 0.12); color: #991b1b; border: 1px solid rgba(220, 38, 38, 0.2); }
        .alert-success { background: rgba(22, 163, 74, 0.12); color: #166534; border: 1px solid rgba(22, 163, 74, 0.2); }
        .form-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 14px; }
        .field { display: grid; gap: 7px; }
        .field-wide { grid-column: span 2; }
        .form-card { padding: 22px; display: grid; gap: 16px; }
        label { font-weight: 700; font-size: 0.9rem; color: #2c3e50; }
        .form-control, input, select { width: 100%; min-height: 40px; padding: 9px 11px; border: 1px solid #cbd5e1; border-radius: 8px; font: inherit; background: #fff; }
        input[readonly] { background: #f8fafc; color: #475569; }
        .image-preview { width: 90px; height: 90px; object-fit: contain; border: 1px solid #d9dee5; background: #f8fafc; padding: 4px; border-radius: 8px; }
        .variant-image { width: 90px; height: 90px; object-fit: contain; border-radius: 8px; }
        .no-image { color: #94a3b8; font-size: .8rem; }
        .description { color: #64748b; overflow-wrap: anywhere; overflow: hidden; }
        .description-text { display: -webkit-box; line-clamp: 3; -webkit-box-orient: vertical; -webkit-line-clamp: 3; overflow: hidden; }
        .muted { color: #64748b; }
        .actions { display: flex; justify-content: flex-end; gap: 10px; }
        .btn-add-variant {
            display: inline-flex; align-items: center; gap: 8px;
            padding: 10px 18px; border-radius: 8px; border: none;
            background: linear-gradient(135deg, #1abc9c, #16a085);
            color: #fff; font-weight: 600; cursor: pointer; transition: 0.2s;
        }
        .btn-add-variant:hover { transform: translateY(-1px); box-shadow: 0 6px 14px rgba(26, 188, 156, 0.3); }
        .btn-primary {
            display: inline-flex; align-items: center; justify-content: center; gap: 10px;
            padding: 10px 16px; border-radius: 8px; font-weight: 600; text-decoration: none;
            cursor: pointer; transition: all 0.2s ease; border: none; background: #1abc9c; color: #fff;
        }
        .btn-primary:hover { background: #16a085; color: #fff; }
        .btn-secondary {
            display: inline-flex; align-items: center; justify-content: center; gap: 10px;
            padding: 10px 16px; border-radius: 8px; font-weight: 600; text-decoration: none;
            border: 1px solid #dbe3f0; background: #fff; color: #334155;
        }
        .btn-secondary:hover { background: #f8fafc; color: #334155; }
        .modal-backdrop { position: fixed; inset: 0; z-index: 1050; display: none; align-items: center; justify-content: center; padding: 24px; background: rgba(15, 23, 42, .58); }
        .modal-backdrop.open { display: flex; }
        .modal-card { width: min(980px, 100%); max-height: calc(100vh - 48px); overflow: auto; margin: 0; box-shadow: 0 24px 70px rgba(15, 23, 42, 0.28); }
        .page-title-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; flex-wrap: wrap; gap: 12px; }
        .page-title-row h1 { margin: 0; font-size: 1.5rem; font-weight: 700; color: #2c3e50; }
        .table-wrap { overflow-x: auto; }
        table { width: 100%; border-collapse: collapse; table-layout: fixed; min-width: 1050px; }
        th, td { padding: 12px 10px; border: 1px solid #d9dee5; text-align: center; vertical-align: middle; }
        td { height: 116px; font-size: .9rem; }
        th:nth-child(1), td:nth-child(1) { width: 11%; }
        th:nth-child(2), td:nth-child(2) { width: 10%; }
        th:nth-child(3), td:nth-child(3) { width: 15%; }
        th:nth-child(4), td:nth-child(4) { width: 12%; }
        th:nth-child(5), td:nth-child(5) { width: 24%; }
        th:nth-child(6), td:nth-child(6) { width: 12%; }
        th:nth-child(7), td:nth-child(7) { width: 8%; }
        th:nth-child(8), td:nth-child(8) { width: 8%; }
        .pagination-bar { display: flex; align-items: center; justify-content: space-between; gap: 14px; padding: 16px 20px; flex-wrap: wrap; }
        .pagination-controls { display: flex; gap: 8px; }
        .page-link { min-width: 36px; height: 36px; padding: 0 10px; display: inline-flex; align-items: center; justify-content: center; border: 1px solid #cbd5e1; border-radius: 5px; color: #334155; text-decoration: none; font-weight: 700; }
        .page-link.active { background: #14b8a6; border-color: #14b8a6; color: #fff; }
        .variant-filter-bar { display: flex; flex-wrap: wrap; gap: 10px; padding: 16px 20px; align-items: center; background: #fafbfc; border-bottom: 1px solid #e2e8f0; }
        .variant-filter-bar .filter-field { position: relative; flex: 1 1 240px; min-width: 200px; }
        .variant-filter-bar .filter-field i { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: #94a3b8; pointer-events: none; }
        .variant-filter-bar .filter-field input,
        .variant-filter-bar .filter-field select { padding-left: 36px; min-height: 42px; }
        .variant-filter-bar .btn-primary,
        .variant-filter-bar .btn-secondary { flex: 0 0 auto; min-width: 110px; padding: 10px 18px; }
        @media (max-width: 800px) { .form-grid { grid-template-columns: 1fr; } .field-wide { grid-column: auto; } .modal-backdrop { padding: 10px; } }
        @media (max-width: 768px) { .sidebar { min-height: auto; height: auto; position: static; } .main-content { padding: 15px; } }
    </style>
</head>
<body data-open-modal="${not empty formError}">
<div class="container-fluid p-0">
    <div class="row g-0">
        <!-- Sidebar -->
        <div class="col-md-3 col-lg-2 sidebar">
            <div class="brand">Management</div>
            <ul class="nav flex-column">
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/staff/orders">Manage Orders</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/staff/products">Manage Products</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link active" href="${pageContext.request.contextPath}/staff/products?action=manageVariants">Manage Variants</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/staff/warehouse/inventory">Manage Warehouse</a>
                </li>
                <li class="nav-item mt-auto">
                    <a class="nav-link" href="${pageContext.request.contextPath}/profile">Profile</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/auth/logout">Logout</a>
                </li>
            </ul>
        </div>

        <!-- Main Content -->
        <div class="col-md-9 col-lg-10 main-content">
            <div class="page-title-row">
                <h1>Manage Product Variants</h1>
                <button class="btn-add-variant" type="button" id="openVariantModal">
                    <i class="fas fa-plus"></i> Add variant
                </button>
            </div>

            <div class="modal-backdrop" id="variantModalBackdrop">
                <div class="card modal-card" id="variantModal">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <h2 class="m-0" style="font-size: 1.1rem;">Create product variant</h2>
                        <button class="btn-secondary" type="button" id="closeVariantModal">Close</button>
                    </div>
                    <c:if test="${not empty param.message}"><div class="alert ${param.messageType eq 'error' ? 'alert-error' : 'alert-success'}">${param.message}</div></c:if>
                    <c:if test="${not empty formError}"><div class="alert alert-error">${formError}</div></c:if>
                    <form class="form-card" method="post" action="${pageContext.request.contextPath}/staff/products?action=createVariant" enctype="multipart/form-data">
                        <div class="form-grid">
                            <div class="field field-wide">
                                <label for="productId">Product</label>
                                <select id="productId" name="productId" class="form-select" required>
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
                            <div class="field"><label for="productName">Product name</label><input id="productName" type="text" class="form-control" readonly></div>
                            <div class="field"><label for="productPrice">Product price</label><input id="productPrice" type="text" class="form-control" readonly></div>
                            <div class="field field-wide"><label for="productDescription">Description</label><input id="productDescription" type="text" class="form-control" readonly></div>
                            <div class="field"><label for="productCategory">Category</label><input id="productCategory" type="text" class="form-control" readonly></div>
                            <div class="field"><label for="sizeId">Size</label><select id="sizeId" name="sizeId" class="form-select" required><option value="">-- Select size --</option><c:forEach var="size" items="${sizes}"><option value="${size.sizeId}">${size.sizeName}</option></c:forEach></select></div>
                            <div class="field"><label for="colorId">Color</label><select id="colorId" name="colorId" class="form-select" required><option value="">-- Select color --</option><c:forEach var="color" items="${colors}"><option value="${color.colorId}">${color.colorName}</option></c:forEach></select></div>
                            <div class="field"><label for="sku">SKU</label><input id="sku" name="sku" maxlength="100" placeholder="Optional SKU" class="form-control"></div>
                            <div class="field"><label for="priceOverride">Variant price (optional)</label><input id="priceOverride" name="priceOverride" inputmode="numeric" placeholder="Uses product price" class="form-control"></div>
                            <div class="field"><label for="variantImage">Variant image</label><input id="variantImage" name="variantImage" type="file" accept="image/*" required class="form-control"></div>
                            <div class="field"><label>Image preview</label><div id="imagePreview" class="image-preview"><span class="no-image">No image</span></div></div>
                        </div>
                        <div class="actions">
                            <a class="btn-secondary" href="${pageContext.request.contextPath}/staff/products?tab=products">Back to products</a>
                            <button class="btn-primary" type="submit">Create variant</button>
                        </div>
                    </form>
                    <div id="allSizeOptions" hidden>
                        <c:forEach var="size" items="${allSizes}"><span data-size-id="${size.sizeId}" data-size-name="${fn:escapeXml(size.sizeName)}"></span></c:forEach>
                    </div>
                </div>
            </div>

            <div class="card">
                <div class="card-header"><h2 class="m-0" style="font-size: 1.1rem;">Product variants</h2></div>
                <form class="variant-filter-bar" method="get" action="${pageContext.request.contextPath}/staff/products">
                    <input type="hidden" name="action" value="manageVariants">
                    <input type="hidden" name="productId" value="${fn:escapeXml(selectedProductId)}">
                    <div class="filter-field">
                        <i class="fas fa-search"></i>
                        <input type="text" name="keyword" value="${fn:escapeXml(keyword)}" placeholder="Search by product name, size or color..." autocomplete="off">
                    </div>
                    <div class="filter-field">
                        <i class="fas fa-tag"></i>
                        <select name="categoryId">
                            <option value="">All categories</option>
                            <c:forEach var="cat" items="${categories}">
                                <option value="${cat.categoryId}" ${selectedCategoryId eq cat.categoryId ? 'selected' : ''}>${cat.name}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <button class="btn-primary" type="submit">Search</button>
                    <a class="btn-secondary" href="${pageContext.request.contextPath}/staff/products?action=manageVariants">Reset</a>
                </form>
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
                    <div class="pagination-bar" style="justify-content: center;">
                        <div class="pagination-controls">
                            <c:if test="${currentPage > 1}"><a class="page-link" href="?action=manageVariants&amp;productId=${selectedProductId}&amp;keyword=${fn:escapeXml(keyword)}&amp;categoryId=${fn:escapeXml(selectedCategoryId)}&amp;page=${currentPage - 1}">&#8249;</a></c:if>
                            <c:forEach var="page" begin="1" end="${totalPages}">
                                <a class="page-link ${page == currentPage ? 'active' : ''}" href="?action=manageVariants&amp;productId=${selectedProductId}&amp;keyword=${fn:escapeXml(keyword)}&amp;categoryId=${fn:escapeXml(selectedCategoryId)}&amp;page=${page}">${page}</a>
                            </c:forEach>
                            <c:if test="${currentPage < totalPages}"><a class="page-link" href="?action=manageVariants&amp;productId=${selectedProductId}&amp;keyword=${fn:escapeXml(keyword)}&amp;categoryId=${fn:escapeXml(selectedCategoryId)}&amp;page=${currentPage + 1}">&#8250;</a></c:if>
                        </div>
                    </div>
                </c:if>
            </div>
        </div>
    </div>
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
        name: node.dataset.sizeName
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
        sizeSelect.innerHTML = '<option value="">-- Select size --</option>';
        allSizeOptions.forEach(size => {
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
