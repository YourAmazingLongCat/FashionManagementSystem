<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Product Management</title>
        <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/views/pages/productManagement/product-management.css?v=20260609-friendly-admin-en">
        <style>
            body { margin: 0; font-family: 'Inter', sans-serif; color: #0f172a; background: linear-gradient(135deg, #f8fafc 0%, #eef2ff 100%); min-height: 100vh; }
            .product-shell { width: min(1440px, calc(100% - 40px)); margin: 28px auto; display: grid; grid-template-columns: 320px 1fr; gap: 24px; }
            .sidebar-panel, .content-panel { background: #ffffff; border: 1px solid rgba(226, 232, 240, 0.9); border-radius: 24px; box-shadow: 0 24px 60px rgba(15, 23, 42, 0.1); contain: content; }
            .sidebar-panel { padding: 28px; display: flex; flex-direction: column; gap: 24px; background: linear-gradient(180deg, #0f172a 0%, #312e81 100%); color: #ffffff; position: sticky; top: 32px; align-self: start; max-height: calc(100vh - 64px); overflow-y: auto; }
            .brand-label, .eyebrow, .sidebar-label, .meta-label { margin: 0 0 10px; text-transform: uppercase; letter-spacing: 0.18em; font-size: 0.74rem; font-weight: 700; }
            .sidebar-panel h1, .hero-panel h2 { margin: 0; font-size: 2rem; line-height: 1.12; }
            .sidebar-text, .section-header p, .product-description, .info-copy, .meta-copy { margin: 12px 0 0; color: #64748b; line-height: 1.7; }
            .sidebar-panel .sidebar-text { color: rgba(255, 255, 255, 0.78); }
            .sidebar-block { background: rgba(255, 255, 255, 0.08); border: 1px solid rgba(255, 255, 255, 0.12); border-radius: 24px; padding: 18px; }
            .sidebar-tabs { display: grid; gap: 12px; }
            .sidebar-tab, .home-link, .primary-btn, .table-btn { display: inline-flex; align-items: center; justify-content: center; gap: 10px; padding: 12px 16px; border-radius: 16px; font-weight: 700; text-decoration: none; transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease, border-color 0.2s ease; }
            .sidebar-tab { justify-content: flex-start; background: rgba(255, 255, 255, 0.06); color: rgba(255, 255, 255, 0.88); border: 1px solid transparent; }
            .sidebar-tab.active { background: #ffffff; color: #312e81; box-shadow: 0 18px 30px rgba(15, 23, 42, 0.18); }
            .tab-badge { margin-left: auto; min-width: 34px; height: 34px; border-radius: 999px; display: inline-flex; align-items: center; justify-content: center; font-size: 0.82rem; background: rgba(255, 255, 255, 0.12); color: inherit; }
            .sidebar-tab.active .tab-badge { background: rgba(79, 70, 229, 0.12); }
            .home-link { background: rgba(255, 255, 255, 0.12); color: #ffffff; border: 1px solid rgba(255, 255, 255, 0.18); }
            .sidebar-tab:hover, .home-link:hover, .primary-btn:hover, .table-btn:hover { transform: translateY(-2px); }
            .content-panel { padding: 24px; }
            .hero-panel { position: relative; display: flex; flex-direction: column; gap: 24px; padding: 30px; background: linear-gradient(135deg, #ffffff 0%, #ede9fe 100%); border: 1px solid #e2e8f0; border-radius: 26px; }
            .hero-panel h2 { font-size: 2.15rem; margin: 0; }
            .hero-actions { position: absolute; top: 24px; right: 24px; display: flex; gap: 12px; flex-wrap: wrap; }
            .profile-btn, .logout-btn { display: inline-flex; align-items: center; justify-content: center; gap: 10px; padding: 14px 24px; border-radius: 999px; background: #111827; color: #ffffff; text-transform: uppercase; font-weight: 800; font-size: 0.9rem; text-decoration: none; box-shadow: 0 20px 36px rgba(17, 24, 39, 0.18); transition: transform 0.2s ease, background-color 0.2s ease; }
            .profile-btn:hover, .logout-btn:hover { background: #1f2937; transform: translateY(-1px); }
            .logout-btn { background: #ef4444; }
            .logout-btn:hover { background: #dc2626; }
            .hero-stats { display: grid; grid-template-columns: repeat(4, minmax(150px, 1fr)); gap: 16px; margin-top: 16px; }
            @media (max-width: 900px) { .hero-panel { padding: 24px; } .hero-actions { position: static; width: 100%; } .profile-btn, .logout-btn { width: 100%; } .hero-stats { grid-template-columns: repeat(2, minmax(150px, 1fr)); } }
            @media (max-width: 600px) { .hero-stats { grid-template-columns: 1fr; } }
            .stat-card { background: #ffffff; border: 1px solid #e2e8f0; border-radius: 22px; padding: 18px; min-width: 150px; }
            .stat-card span { display: block; color: #64748b; font-size: 0.88rem; margin-bottom: 8px; }
            .stat-card strong { font-size: 1.8rem; }
            .alert { margin-top: 20px; padding: 16px 18px; border-radius: 18px; font-weight: 600; }
            .alert-success { background: rgba(22, 163, 74, 0.12); color: #166534; border: 1px solid rgba(22, 163, 74, 0.2); }
            .alert-error { background: rgba(220, 38, 38, 0.12); color: #991b1b; border: 1px solid rgba(220, 38, 38, 0.2); }
            .surface-panel { margin-top: 24px; background: #ffffff; border: 1px solid #e2e8f0; border-radius: 26px; overflow: hidden; }
            .filter-toolbar { display: grid; grid-template-columns: minmax(0, 1.6fr) repeat(2, minmax(180px, 0.7fr)) auto; gap: 12px; margin-bottom: 20px; }
            .filter-input, .filter-select { width: 100%; padding: 13px 15px; border-radius: 16px; border: 1px solid #dbe3f0; background: #fff; font: inherit; color: #0f172a; box-sizing: border-box; }
            .filter-actions { display: flex; gap: 10px; }
            .ghost-btn { display: inline-flex; align-items: center; justify-content: center; gap: 10px; padding: 12px 16px; border-radius: 16px; font-weight: 700; text-decoration: none; border: 1px solid #dbe3f0; background: #fff; color: #334155; }
            .section-header { padding: 24px; display: flex; justify-content: space-between; align-items: flex-start; gap: 16px; border-bottom: 1px solid #eef2f7; }
            .section-header h3 { margin: 0; font-size: 1.35rem; }
            .section-body { padding: 24px; }
            .product-list { display: grid; gap: 18px; }
            .product-card { display: grid; grid-template-columns: minmax(0, 1.65fr) minmax(240px, 1fr) auto; gap: 18px; align-items: stretch; padding: 20px; border: 1px solid #e2e8f0; border-radius: 20px; background: #ffffff; box-shadow: 0 8px 20px rgba(15, 23, 42, 0.04); contain: content; }
            .product-card:hover { box-shadow: 0 16px 30px rgba(15, 23, 42, 0.1); transform: translateY(-2px); transition: all 0.2s ease; }
            .product-main { display: flex; gap: 16px; min-width: 0; }
            .thumb, .thumb-empty { width: 88px; height: 88px; border-radius: 16px; border: 1px solid #e2e8f0; background: #f8fafc; flex-shrink: 0; }
            .thumb { object-fit: cover; display: block; }
            .thumb-empty { display: flex; align-items: center; justify-content: center; color: #94a3b8; font-size: 0.76rem; text-align: center; padding: 10px; }
            .product-copy { min-width: 0; display: grid; gap: 10px; align-content: start; }
            .product-head { display: flex; flex-wrap: wrap; align-items: center; gap: 10px; }
            .product-name { margin: 0; font-size: 1.08rem; line-height: 1.45; font-weight: 800; }
            .id-badge, .soft-badge, .summary-pill, .status-badge { display: inline-flex; align-items: center; justify-content: center; border-radius: 999px; font-weight: 700; }
            .id-badge { padding: 7px 12px; background: #eef2ff; color: #4338ca; font-size: 0.8rem; }
            .product-description { margin: 0; font-size: 0.94rem; white-space: normal; overflow-wrap: anywhere; word-break: break-word; max-width: 100%; }
            .product-meta { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px; }
            .meta-card { padding: 14px 16px; border-radius: 18px; background: #f8fafc; border: 1px solid #e2e8f0; min-width: 0; }
            .meta-label { margin-bottom: 8px; color: #94a3b8; }
            .meta-copy { margin: 0; font-size: 0.94rem; white-space: normal; overflow-wrap: anywhere; word-break: break-word; }
            .summary-pill-list { display: flex; flex-wrap: wrap; gap: 8px; }
            .summary-pill { padding: 6px 11px; background: #eef2ff; color: #334155; font-size: 0.8rem; }
            .product-side { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px; }
            .info-card { padding: 16px; border-radius: 20px; border: 1px solid #e2e8f0; background: #ffffff; box-shadow: inset 0 1px 0 rgba(255,255,255,0.6); text-align: center; min-width: 0; overflow: hidden; }
            .info-card strong { display: block; font-size: 1.18rem; margin-top: 6px; line-height: 1.45; white-space: normal; overflow-wrap: anywhere; word-break: break-word; max-width: 100%; }
            .info-copy { margin: 0; font-size: 0.82rem; }
            .status-badge { min-width: 112px; padding: 9px 14px; font-size: 0.82rem; }
            .status-Available { background: rgba(22, 163, 74, 0.12); color: #16a34a; }
            .status-OutOfStock { background: rgba(245, 158, 11, 0.14); color: #b45309; }
            .status-Inactive { background: rgba(100, 116, 139, 0.16); color: #475569; }
            .product-actions { display: flex; flex-direction: column; justify-content: space-between; gap: 14px; min-width: 128px; }
            .action-group { display: flex; flex-direction: column; gap: 10px; }
            .table-btn { min-width: 96px; padding: 11px 14px; font-size: 0.85rem; border: none; }
            .primary-btn { border: none; background: linear-gradient(135deg, #7c3aed 0%, #4f46e5 100%); color: #ffffff; box-shadow: 0 18px 30px rgba(124, 58, 237, 0.22); }
            .table-btn.edit { background: rgba(124, 58, 237, 0.12); color: #5b21b6; }
            .table-btn.delete { background: rgba(220, 38, 38, 0.12); color: #b91c1c; }
            .collection-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 18px; }
            .collection-card { padding: 20px; border: 1px solid #e2e8f0; border-radius: 24px; background: linear-gradient(180deg, #ffffff 0%, #fbfcff 100%); box-shadow: 0 14px 28px rgba(15, 23, 42, 0.05); display: grid; gap: 14px; }
            .collection-card-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 12px; }
            .collection-card h4 { margin: 0; font-size: 1.05rem; line-height: 1.4; }
            .collection-description { margin: 0; color: #64748b; line-height: 1.65; font-size: 0.93rem; }
            .collection-meta { display: flex; flex-wrap: wrap; gap: 8px; }
            .soft-badge { padding: 6px 12px; background: #f8fafc; color: #475569; font-size: 0.78rem; border: 1px solid #e2e8f0; }
            .color-preview { display: inline-flex; align-items: center; gap: 10px; }
            .color-dot { width: 22px; height: 22px; border-radius: 999px; border: 1px solid rgba(15,23,42,0.12); flex-shrink: 0; }

            /* Table styles */
            .data-table-wrapper { overflow-x: auto; }
            .data-table { width: 100%; border-collapse: collapse; font-size: 0.92rem; }
            .data-table th, .data-table td { padding: 14px 16px; text-align: left; border-bottom: 1px solid #e2e8f0; }
            .data-table th { background: #f8fafc; font-weight: 700; text-transform: uppercase; font-size: 0.78rem; letter-spacing: 0.06em; color: #64748b; white-space: nowrap; }
            .data-table tbody tr:hover { background: #f8fafc; }
            .data-table .cell-id { font-family: monospace; color: #4338ca; font-weight: 600; font-size: 0.85rem; }
            .data-table .cell-name { font-weight: 600; }
            .data-table .cell-description { color: #64748b; max-width: 300px; }
            .data-table .cell-actions { white-space: nowrap; }
            .data-table .action-group { display: flex; gap: 8px; }
            .data-table .table-btn { min-width: 70px; padding: 8px 12px; font-size: 0.8rem; }

            /* Category group styles for Sizes table */
            .size-group-list { display: flex; flex-direction: column; gap: 12px; }
            .size-group { background: #ffffff; border: 1px solid #e2e8f0; border-radius: 16px; overflow: hidden; }
            .size-group-header { display: flex; align-items: center; justify-content: space-between; padding: 16px 20px; background: linear-gradient(135deg, #7c3aed 0%, #4f46e5 100%); color: #ffffff; }
            .size-group-header-left { display: flex; align-items: center; gap: 12px; }
            .size-group-title { font-weight: 700; font-size: 0.95rem; }
            .size-group-count { font-size: 0.8rem; opacity: 0.85; }
            .add-size-btn { display: inline-flex; align-items: center; gap: 6px; padding: 6px 12px; background: rgba(255,255,255,0.15); border: 1px solid rgba(255,255,255,0.3); border-radius: 8px; color: #ffffff; font-size: 0.75rem; font-weight: 600; text-decoration: none; transition: all 0.2s ease; }
            .add-size-btn:hover { background: rgba(255,255,255,0.25); border-color: rgba(255,255,255,0.5); }
            .size-group-body .data-table-wrapper { border-radius: 0; border: none; border-top: 1px solid #e2e8f0; }
            .empty-state { padding: 42px 16px; text-align: center; }
            .empty-state h4 { margin: 0 0 10px; font-size: 1.15rem; }
            .empty-state p { margin: 0; color: #64748b; }
            .pagination-bar { margin-top: 22px; display: flex; justify-content: space-between; align-items: center; gap: 14px; flex-wrap: wrap; }
            .pagination-summary { color: #64748b; font-size: 0.92rem; font-weight: 600; }
            .pagination-controls { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
            .page-link { min-width: 42px; height: 42px; padding: 0 14px; border-radius: 14px; border: 1px solid #dbe3f0; background: #ffffff; color: #334155; display: inline-flex; align-items: center; justify-content: center; text-decoration: none; font-weight: 700; }
            .page-link.active { background: linear-gradient(135deg, #7c3aed 0%, #4f46e5 100%); color: #ffffff; border-color: transparent; box-shadow: 0 14px 24px rgba(124, 58, 237, 0.18); }
            .page-link:hover { transform: translateY(-2px); }
            .tab-panel { display: none; }
            .tab-panel.active { display: block; }
            @media (max-width: 1280px) { .product-card { grid-template-columns: minmax(0, 1fr); } .product-actions { flex-direction: row; align-items: center; min-width: 0; } .action-group { flex-direction: row; flex-wrap: wrap; } }
            @media (max-width: 1180px) { .product-shell { grid-template-columns: 1fr; } }
            @media (max-width: 900px) { .hero-panel { flex-direction: column; } .hero-stats { width: 100%; grid-template-columns: repeat(2, minmax(0, 1fr)); } .product-meta, .product-side { grid-template-columns: 1fr; } .section-header { flex-direction: column; } }
            @media (max-width: 768px) { .product-shell { width: min(100% - 20px, 100%); margin: 10px auto; } .content-panel, .sidebar-panel, .section-body, .section-header { padding-left: 16px; padding-right: 16px; } .hero-stats { grid-template-columns: 1fr; } .sidebar-tabs, .action-group { flex-direction: column; } .product-main { flex-direction: column; } .thumb, .thumb-empty { width: 100%; max-width: 96px; height: 96px; } .product-actions { flex-direction: column; align-items: stretch; } .table-btn, .primary-btn { width: 100%; } .collection-grid { grid-template-columns: 1fr; } }
        </style>
    </head>
    <body>
        <c:set var="activeTab" value="${empty param.tab ? 'products' : param.tab}" />
        <div class="product-shell">
            <aside class="sidebar-panel">
                <div>
                    <p class="brand-label">Fashion Shop</p>
                    <h1>Catalog Management</h1>
                    <p class="sidebar-text">Manage products, categories, colors, and sizes in one place.</p>
                </div>
                <div class="sidebar-block">
                    <p class="sidebar-label">Management</p>
                    <div class="sidebar-tabs">
                        <a class="sidebar-tab ${activeTab eq 'products' ? 'active' : ''}" href="${pageContext.request.contextPath}/staff/products?tab=products"><span>Products</span><span class="tab-badge">${totalProducts}</span></a>
                        <a class="sidebar-tab ${activeTab eq 'categories' ? 'active' : ''}" href="${pageContext.request.contextPath}/staff/products?tab=categories"><span>Categories</span><span class="tab-badge">${totalCategories}</span></a>
                        <a class="sidebar-tab ${activeTab eq 'colors' ? 'active' : ''}" href="${pageContext.request.contextPath}/staff/products?tab=colors"><span>Colors</span><span class="tab-badge">${totalColors}</span></a>
                        <a class="sidebar-tab ${activeTab eq 'sizes' ? 'active' : ''}" href="${pageContext.request.contextPath}/staff/products?tab=sizes"><span>Sizes</span><span class="tab-badge">${totalSizes}</span></a>
                        <a class="sidebar-tab ${activeTab eq 'warehouse' ? 'active' : ''}" href="${pageContext.request.contextPath}/staff/warehouse/inventory"><span>Warehouse</span><span class="tab-badge">&#128203;</span></a>
                    </div>
                </div>
                <!-- Profile & Logout removed per UI requirements -->
            </aside>

            <main class="content-panel">
                <section class="hero-panel">
                    <div>
                        <p class="eyebrow">Admin panel</p>
                        <h2>${activeTab eq 'categories' ? 'Category Management' : (activeTab eq 'colors' ? 'Color Management' : (activeTab eq 'sizes' ? 'Size Management' : 'Product Management'))}</h2>
                    </div>
                    <a class="profile-btn" href="${pageContext.request.contextPath}/profile">My Profile</a>
                    <div class="hero-stats">
                        <div class="stat-card"><span>Total products</span><strong>${totalProducts}</strong></div>
                        <div class="stat-card"><span>Total categories</span><strong>${totalCategories}</strong></div>
                        <div class="stat-card"><span>Total colors</span><strong>${totalColors}</strong></div>
                        <div class="stat-card"><span>Total sizes</span><strong>${totalSizes}</strong></div>
                    </div>
                </section>

                <c:if test="${not empty error}"><div class="alert alert-error">${error}</div></c:if>
                <c:if test="${not empty param.message}"><div class="alert ${param.messageType eq 'error' ? 'alert-error' : 'alert-success'}">${param.message}</div></c:if>

                <section class="tab-panel ${activeTab eq 'products' ? 'active' : ''}">
                    <section class="surface-panel">
                        <div class="section-header"><div><h3>Products</h3><p>Review images, colors, sizes, stock levels, and prices in a clean layout.</p></div><a class="primary-btn" href="${pageContext.request.contextPath}/staff/products?action=create&tab=products">Add product</a></div>
                        <div class="section-body">
                            <form id="productFilterForm" method="get" action="${pageContext.request.contextPath}/staff/products" class="filter-toolbar">
                                <input type="hidden" name="tab" value="products" />
                                <input id="productKeywordInput" class="filter-input" type="text" name="keyword" value="${param.keyword}" placeholder="Search product name, SKU, size, color..." autocomplete="off" />
                                <select class="filter-select" name="statusFilter" onchange="submitProductFilterForm()">
                                    <option value="">All status</option>
                                    <option value="Available" ${param.statusFilter eq 'Available' ? 'selected' : ''}>Available</option>
                                    <option value="OutOfStock" ${param.statusFilter eq 'OutOfStock' ? 'selected' : ''}>Out of stock</option>
                                    <option value="Inactive" ${param.statusFilter eq 'Inactive' ? 'selected' : ''}>Inactive</option>
                                </select>
                                <select class="filter-select" name="categoryFilter" onchange="submitProductFilterForm()">
                                    <option value="">All categories</option>
                                    <c:forEach var="category" items="${allCategoryItems}">
                                        <option value="${category.categoryId}" ${param.categoryFilter eq category.categoryId ? 'selected' : ''}>${category.name}</option>
                                    </c:forEach>
                                </select>
                                <div class="filter-actions">
                                    <button class="primary-btn" type="submit">Apply</button>
                                    <a class="ghost-btn" href="${pageContext.request.contextPath}/staff/products?tab=products">Reset</a>
                                </div>
                            </form>
                            <c:choose>
                                <c:when test="${empty products}">
                                    <div class="empty-state"><h4>No products found</h4><p>Add your first product to start building the catalog.</p></div>
                                </c:when>
                                <c:otherwise>
                                    <div class="product-list">
                                        <c:forEach var="product" items="${products}">
                                            <article class="product-card">
                                                <div class="product-main">
                                                    <c:choose>
                                                        <c:when test="${not empty product.primaryImageUrl}">
                                                            <img class="thumb" src="${pageContext.request.contextPath.concat(product.primaryImageUrl)}" alt="${product.name}" loading="lazy" onerror="this.style.display='none';this.nextElementSibling.style.display='flex';">
                                                            <div class="thumb-empty" style="display:none;">No image</div>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <div class="thumb-empty">No image</div>
                                                        </c:otherwise>
                                                    </c:choose>
                                                    <div class="product-copy">
                                                        <div class="product-head">
                                                            <span class="id-badge">${product.productId}</span>
                                                            <h4 class="product-name">${product.name}</h4>
                                                        </div>
                                                        <p class="product-description">${empty product.description ? 'No description available' : product.description}</p>
                                                        <div class="product-meta">
                                                            <div class="meta-card">
                                                                <p class="meta-label">Category</p>
                                                                <p class="meta-copy"><strong>${product.categoryName}</strong></p>
                                                            </div>
                                                            <div class="meta-card">
                                                                <p class="meta-label">Status</p>
                                                                <span class="status-badge status-${product.status}">${product.status}</span>
                                                            </div>
                                                            <div class="meta-card">
                                                                <p class="meta-label">Colors</p>
                                                                <div class="summary-pill-list">
                                                                    <c:forEach var="colorName" items="${product.colorNames}">
                                                                        <span class="summary-pill">${colorName}</span>
                                                                    </c:forEach>
                                                                </div>
                                                            </div>
                                                            <div class="meta-card">
                                                                <p class="meta-label">Sizes</p>
                                                                <div class="summary-pill-list">
                                                                    <c:forEach var="sizeName" items="${product.sizeNames}">
                                                                        <span class="summary-pill">${sizeName}</span>
                                                                    </c:forEach>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                                <div class="product-side">
                                                    <div class="info-card"><p class="info-copy">Stock</p><strong>${product.totalStockQty}</strong></div>
                                                    <div class="info-card"><p class="info-copy">Price</p><strong><fmt:formatNumber value="${product.basePrice}" type="number" groupingUsed="true" /> đ</strong></div>
                                                    <div class="info-card"><p class="info-copy">Catalog</p><strong>${product.categoryName}</strong></div>
                                                </div>
                                                <div class="product-actions">
                                                    <span class="status-badge status-${product.status}">${product.status}</span>
                                                    <div class="action-group">
                                                        <a class="table-btn edit" href="${pageContext.request.contextPath}/staff/products?action=edit&id=${product.productId}&tab=products">Edit</a>
                                                        <a class="table-btn delete" href="${pageContext.request.contextPath}/staff/products?action=delete&id=${product.productId}&tab=products">Delete</a>
                                                    </div>
                                                </div>
                                            </article>
                                        </c:forEach>
                                    </div>
                                    <c:if test="${totalPages > 1}">
                                        <div class="pagination-bar">
                                            <span class="pagination-summary">Showing ${products.size()} of ${totalProducts} products</span>
                                            <div class="pagination-controls">
                                                <c:if test="${currentPage > 1}">
                                                    <a class="page-link" href="?${productQuery}&page=${currentPage - 1}">‹</a>
                                                </c:if>
                                                <c:forEach var="i" begin="1" end="${totalPages}">
                                                    <c:choose>
                                                        <c:when test="${i == currentPage}">
                                                            <a class="page-link active" href="?${productQuery}&page=${i}">${i}</a>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <a class="page-link" href="?${productQuery}&page=${i}">${i}</a>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </c:forEach>
                                                <c:if test="${currentPage < totalPages}">
                                                    <a class="page-link" href="?${productQuery}&page=${currentPage + 1}">›</a>
                                                </c:if>
                                            </div>
                                        </div>
                                    </c:if>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </section>
                </section>

                <section class="tab-panel ${activeTab eq 'categories' ? 'active' : ''}">
                    <section class="surface-panel">
                        <div class="section-header"><div><h3>Categories</h3><p>Manage category names and descriptions in a structured table.</p></div><a class="primary-btn" href="${pageContext.request.contextPath}/staff/products?action=createCategory&tab=categories">Add category</a></div>
                        <div class="section-body">
                            <c:choose>
                                <c:when test="${empty categoryItems}">
                                    <div class="empty-state"><h4>No categories found</h4><p>Create categories to keep the catalog organized.</p></div>
                                </c:when>
                                <c:otherwise>
                                    <div class="data-table-wrapper">
                                        <table class="data-table">
                                            <thead>
                                                <tr>
                                                    <th>ID</th>
                                                    <th>Name</th>
                                                    <th>Description</th>
                                                    <th>Actions</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach var="category" items="${categoryItems}">
                                                    <tr>
                                                        <td class="cell-id">${category.categoryId}</td>
                                                        <td class="cell-name">${category.name}</td>
                                                        <td class="cell-description">${empty category.description ? '-' : category.description}</td>
                                                        <td class="cell-actions">
                                                            <div class="action-group">
                                                                <a class="table-btn edit" href="${pageContext.request.contextPath}/staff/products?action=editCategory&id=${category.categoryId}&tab=categories">Edit</a>
                                                                <a class="table-btn delete" href="${pageContext.request.contextPath}/staff/products?action=deleteCategory&id=${category.categoryId}&tab=categories">Delete</a>
                                                            </div>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                            </tbody>
                                        </table>
                                    </div>
                                    <c:if test="${totalPages > 1}">
                                        <div class="pagination-bar">
                                            <span class="pagination-summary">Showing ${categoryItems.size()} of ${totalCategories} categories</span>
                                            <div class="pagination-controls">
                                                <c:if test="${currentPage > 1}">
                                                    <a class="page-link" href="?${productQuery}&page=${currentPage - 1}">&#8249;</a>
                                                </c:if>
                                                <c:forEach var="i" begin="1" end="${totalPages}">
                                                    <c:choose>
                                                        <c:when test="${i == currentPage}">
                                                            <a class="page-link active" href="?${productQuery}&page=${i}">${i}</a>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <a class="page-link" href="?${productQuery}&page=${i}">${i}</a>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </c:forEach>
                                                <c:if test="${currentPage < totalPages}">
                                                    <a class="page-link" href="?${productQuery}&page=${currentPage + 1}">&#8250;</a>
                                                </c:if>
                                            </div>
                                        </div>
                                    </c:if>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </section>
                </section>

                <section class="tab-panel ${activeTab eq 'colors' ? 'active' : ''}">
                    <section class="surface-panel">
                        <div class="section-header"><div><h3>Colors</h3><p>Display color details in a structured table format.</p></div><a class="primary-btn" href="${pageContext.request.contextPath}/staff/products?action=createColor&tab=colors">Add color</a></div>
                        <div class="section-body">
                            <c:choose>
                                <c:when test="${empty colorItems}">
                                    <div class="empty-state"><h4>No colors found</h4><p>Add color options for your catalog.</p></div>
                                </c:when>
                                <c:otherwise>
                                    <div class="data-table-wrapper">
                                        <table class="data-table">
                                            <thead>
                                                <tr>
                                                    <th>ID</th>
                                                    <th>Color Name</th>
                                                    <th>Hex Code</th>
                                                    <th>Preview</th>
                                                    <th>Actions</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach var="color" items="${colorItems}">
                                                    <tr>
                                                        <td class="cell-id">${color.colorId}</td>
                                                        <td class="cell-name">${color.colorName}</td>
                                                        <td class="cell-id">${empty color.hexCode ? '-' : color.hexCode}</td>
                                                        <td>
                                                            <span class="color-preview" style="display: inline-flex; align-items: center; gap: 8px;">
                                                                <span class="color-dot" style="background: ${empty color.hexCode ? '#000000' : color.hexCode};"></span>
                                                            </span>
                                                        </td>
                                                        <td class="cell-actions">
                                                            <div class="action-group">
                                                                <a class="table-btn edit" href="${pageContext.request.contextPath}/staff/products?action=editColor&id=${color.colorId}&tab=colors">Edit</a>
                                                                <a class="table-btn delete" href="${pageContext.request.contextPath}/staff/products?action=deleteColor&id=${color.colorId}&tab=colors">Delete</a>
                                                            </div>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                            </tbody>
                                        </table>
                                    </div>
                                    <c:if test="${totalPages > 1}">
                                        <div class="pagination-bar">
                                            <span class="pagination-summary">Showing ${colorItems.size()} of ${totalColors} colors</span>
                                            <div class="pagination-controls">
                                                <c:if test="${currentPage > 1}">
                                                    <a class="page-link" href="?${productQuery}&page=${currentPage - 1}">&#8249;</a>
                                                </c:if>
                                                <c:forEach var="i" begin="1" end="${totalPages}">
                                                    <c:choose>
                                                        <c:when test="${i == currentPage}">
                                                            <a class="page-link active" href="?${productQuery}&page=${i}">${i}</a>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <a class="page-link" href="?${productQuery}&page=${i}">${i}</a>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </c:forEach>
                                                <c:if test="${currentPage < totalPages}">
                                                    <a class="page-link" href="?${productQuery}&page=${currentPage + 1}">&#8250;</a>
                                                </c:if>
                                            </div>
                                        </div>
                                    </c:if>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </section>
                </section>

                <section class="tab-panel ${activeTab eq 'sizes' ? 'active' : ''}">
                    <section class="surface-panel">
                        <div class="section-header"><div><h3>Sizes</h3><p>Manage sizes grouped by category. Click on a category to expand and view its sizes.</p></div><a class="primary-btn" href="${pageContext.request.contextPath}/staff/products?action=createSize&tab=sizes">Add size</a></div>
                        <div class="section-body">
                            <c:choose>
                                <c:when test="${empty sizesByCategory}">
                                    <div class="empty-state"><h4>No sizes found</h4><p>Add sizes that fit each category.</p></div>
                                </c:when>
                                <c:otherwise>
                                    <div class="size-group-list">
                                        <c:forEach var="catEntry" items="${sizesByCategory}">
                                            <c:set var="categoryId" value="${catEntry.key}" />
                                            <c:forEach var="cat" items="${allCategoryItems}">
                                                <c:if test="${cat.categoryId == categoryId}">
                                                    <c:set var="currentCategory" value="${cat}" />
                                                </c:if>
                                            </c:forEach>
                                            <c:set var="sizesInCategory" value="${catEntry.value}" />

                                            <div class="size-group">
                                                <div class="size-group-header" data-group="${currentCategory.categoryId}">
                                                    <div class="size-group-header-left">
                                                        <span class="size-group-title">${currentCategory.name}</span>
                                                        <span class="size-group-count">(${sizesInCategory.size()} sizes)</span>
                                                    </div>
                                                    <a class="add-size-btn" href="${pageContext.request.contextPath}/staff/products?action=createSize&categoryId=${currentCategory.categoryId}&tab=sizes" onclick="event.stopPropagation();">
                                                        + Add Size
                                                    </a>
                                                </div>
                                                <div class="size-group-body" data-group="${currentCategory.categoryId}">
                                                    <c:choose>
                                                        <c:when test="${empty sizesInCategory}">
                                                            <div style="padding: 20px; text-align: center; color: #94a3b8; font-style: italic;">No sizes in this category yet. Click "+ Add Size" to create one.</div>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <div class="data-table-wrapper">
                                                                <table class="data-table">
                                                                    <thead>
                                                                        <tr>
                                                                            <th>ID</th>
                                                                            <th>Size Name</th>
                                                                            <th>Actions</th>
                                                                        </tr>
                                                                    </thead>
                                                                    <tbody>
                                                                        <c:forEach var="size" items="${sizesInCategory}">
                                                                            <tr>
                                                                                <td class="cell-id">${size.sizeId}</td>
                                                                                <td class="cell-name">${size.sizeName}</td>
                                                                                <td class="cell-actions">
                                                                                    <div class="action-group">
                                                                                        <a class="table-btn edit" href="${pageContext.request.contextPath}/staff/products?action=editSize&id=${size.sizeId}&tab=sizes">Edit</a>
                                                                                        <a class="table-btn delete" href="${pageContext.request.contextPath}/staff/products?action=deleteSize&id=${size.sizeId}&tab=sizes">Delete</a>
                                                                                    </div>
                                                                                </td>
                                                                            </tr>
                                                                        </c:forEach>
                                                                    </tbody>
                                                                </table>
                                                            </div>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </div>
                                            </div>
                                        </c:forEach>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </section>
                </section>
            </main>
        </div>
    </body>
    <script>
        function submitProductFilterForm() {
            const form = document.getElementById('productFilterForm');
            if (form) {
                form.submit();
            }
        }

        const productKeywordInput = document.getElementById('productKeywordInput');

        if (productKeywordInput) {
            productKeywordInput.addEventListener('keydown', function (event) {
                if (event.key === 'Enter') {
                    event.preventDefault();
                    submitProductFilterForm();
                }
            });
        }

        // Placeholder for future enhancements
    </script>
</html>
