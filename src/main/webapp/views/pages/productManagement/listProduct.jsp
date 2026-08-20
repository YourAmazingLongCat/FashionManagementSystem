<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Product Management</title>
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
        <style>
            body { margin: 0; font-family: 'Inter', 'Segoe UI', sans-serif; color: #2c3e50; background: #f8f9fa; }
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
                text-decoration: none;
            }
            .sidebar .nav-link:hover, .sidebar .nav-link.active {
                background: #34495e; color: #fff; border-left-color: #1abc9c;
            }
            .sidebar .nav-link i { width: 24px; margin-right: 10px; }
            .sidebar .nav { display: flex; flex-direction: column; min-height: calc(100vh - 130px); padding: 0; margin: 0; list-style: none; }
            .sidebar .nav-item { list-style: none; }
            .sidebar .nav-item.mt-auto { margin-top: auto; }
            .main-content { padding: 20px 30px; }
            .stat-card {
                background: #fff; border-radius: 12px; padding: 20px;
                box-shadow: 0 4px 12px rgba(0,0,0,0.05); border-left: 4px solid #1abc9c;
                transition: 0.2s;
            }
            .stat-card:hover { transform: translateY(-4px); }
            .stat-card .stat-number { font-size: 2rem; font-weight: 700; }
            .stat-card .stat-label { color: #6c757d; text-transform: uppercase; font-size: 0.9rem; }
            .stat-card span { display: block; color: #6c757d; font-size: 0.9rem; margin-bottom: 8px; text-transform: uppercase; font-weight: 500; }
            .stat-card strong { font-size: 2rem; font-weight: 700; color: #2c3e50; }

            /* Content panel that holds the catalog */
            .product-shell { width: 100%; margin: 0; display: block; }
            .content-panel { background: transparent; border: none; box-shadow: none; padding: 0; }

            /* Hero panel */
            .hero-panel {
                position: relative; display: flex; flex-direction: column; gap: 24px;
                padding: 30px; background: #fff; border: 1px solid #e2e8f0;
                border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); margin-bottom: 20px;
            }
            .hero-panel h2 { font-size: 2rem; margin: 0; color: #2c3e50; }
            .eyebrow { margin: 0 0 10px; text-transform: uppercase; letter-spacing: 0.18em; font-size: 0.74rem; font-weight: 700; color: #1abc9c; }
            .hero-stats { display: grid; grid-template-columns: repeat(4, minmax(150px, 1fr)); gap: 16px; margin-top: 16px; }

            .alert { margin-top: 20px; padding: 16px 18px; border-radius: 8px; font-weight: 600; }
            .alert-success { background: #dcfce7; color: #166534; border: 1px solid rgba(22, 163, 74, 0.2); }
            .alert-error { background: #fee2e2; color: #991b1b; border: 1px solid rgba(220, 38, 38, 0.2); }

            /* Surface panel = card */
            .surface-panel { margin-top: 0; background: #fff; border: 1px solid #e2e8f0;
                border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); overflow: hidden; }
            .filter-toolbar { display: grid; grid-template-columns: minmax(0, 1.6fr) repeat(2, minmax(180px, 0.7fr)) auto; gap: 12px; margin-bottom: 20px; }
            .filter-input, .filter-select { width: 100%; padding: 10px 13px; border-radius: 8px; border: 1px solid #dbe3f0; background: #fff; font: inherit; color: #0f172a; box-sizing: border-box; }
            .filter-actions { display: flex; gap: 10px; }
            .ghost-btn { display: inline-flex; align-items: center; justify-content: center; gap: 10px; padding: 10px 16px; border-radius: 8px; font-weight: 600; text-decoration: none; border: 1px solid #dbe3f0; background: #fff; color: #334155; }
            .section-header { padding: 20px 24px; display: flex; justify-content: space-between; align-items: flex-start; gap: 16px; border-bottom: 1px solid #eef2f7; background: #f8f9fa; }
            .section-header h3 { margin: 0; font-size: 1.35rem; color: #2c3e50; }
            .section-header p { margin: 6px 0 0; color: #64748b; }
            .section-body { padding: 24px; }
            .product-list { display: grid; gap: 18px; }
            .product-card { display: grid; grid-template-columns: minmax(0, 1.65fr) minmax(180px, 0.8fr) auto; gap: 14px; align-items: start; padding: 16px; border: 1px solid #e2e8f0; border-radius: 12px; background: #ffffff; box-shadow: 0 2px 6px rgba(15, 23, 42, 0.04); }
            .product-card:hover { box-shadow: 0 4px 12px rgba(15, 23, 42, 0.08); transition: all 0.2s ease; }
            .product-main { display: flex; gap: 16px; min-width: 0; }
            .thumb, .thumb-empty { width: 76px; height: 76px; border-radius: 12px; border: 1px solid #e2e8f0; background: #f8fafc; flex-shrink: 0; }
            .thumb { object-fit: cover; display: block; }
            .thumb-empty { display: flex; align-items: center; justify-content: center; color: #94a3b8; font-size: 0.76rem; text-align: center; padding: 10px; }
            .product-copy { min-width: 0; display: grid; gap: 10px; align-content: start; }
            .product-head { display: flex; flex-wrap: wrap; align-items: center; gap: 10px; }
            .product-name { margin: 0; font-size: 1.08rem; line-height: 1.45; font-weight: 700; color: #2c3e50; }
            .product-description { margin: 0; font-size: 0.94rem; color: #64748b; }
            .product-meta { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 10px; }
            .meta-card { padding: 10px 12px; border-radius: 10px; background: #f8fafc; border: 1px solid #e2e8f0; min-width: 0; }
            .meta-label { margin: 0 0 6px; color: #94a3b8; font-size: 0.72rem; text-transform: uppercase; letter-spacing: 0.08em; font-weight: 700; }
            .meta-copy { margin: 0; font-size: 0.9rem; color: #2c3e50; }
            .summary-pill-list { display: flex; flex-wrap: wrap; gap: 6px; }
            .id-badge, .soft-badge, .summary-pill, .status-badge { display: inline-flex; align-items: center; justify-content: center; border-radius: 999px; font-weight: 700; }
            .id-badge { padding: 5px 10px; background: #e0f2fe; color: #0369a1; font-size: 0.8rem; }
            .summary-pill { padding: 5px 10px; background: #e0f2fe; color: #0369a1; font-size: 0.78rem; }
            .product-side { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
            .info-card { padding: 10px 12px; border-radius: 10px; border: 1px solid #e2e8f0; background: #f8fafc; text-align: center; min-width: 0; overflow: hidden; }
            .info-card strong { display: block; font-size: 1rem; margin-top: 2px; line-height: 1.2; }
            .info-copy { margin: 0; font-size: 0.7rem; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.08em; font-weight: 700; }
            .status-badge { min-width: 112px; padding: 9px 14px; font-size: 0.82rem; }
            .status-Available { background: #dcfce7; color: #166534; }
            .status-OutOfStock { background: #fef3c7; color: #92400e; }
            .status-Inactive { background: #e2e8f0; color: #475569; }
            .product-actions { display: flex; flex-direction: column; justify-content: space-between; gap: 12px; min-width: 128px; }
            .action-group { display: flex; flex-direction: column; gap: 10px; }
            .table-btn { min-width: 96px; padding: 10px 14px; font-size: 0.85rem; border: none; cursor: pointer; }
            .primary-btn { display: inline-flex; align-items: center; justify-content: center; gap: 10px; padding: 10px 16px; border-radius: 8px; font-weight: 600; text-decoration: none; cursor: pointer; transition: all 0.2s ease; border: none; background: #1abc9c; color: #fff; }
            .primary-btn:hover { background: #16a085; transform: translateY(-1px); }
            .table-btn.edit { background: rgba(124, 58, 237, 0.12); color: #5b21b6; border-radius: 8px; }
            .table-btn.delete { background: rgba(220, 38, 38, 0.12); color: #b91c1c; border-radius: 8px; }
            .collection-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 18px; }
            .collection-card { padding: 20px; border: 1px solid #e2e8f0; border-radius: 12px; background: #fff; box-shadow: 0 2px 6px rgba(15, 23, 42, 0.04); display: grid; gap: 14px; }
            .collection-card h4 { margin: 0; font-size: 1.05rem; color: #2c3e50; }
            .collection-description { margin: 0; color: #64748b; }
            .soft-badge { padding: 6px 12px; background: #f8fafc; color: #475569; font-size: 0.78rem; border: 1px solid #e2e8f0; }
            .color-preview { display: inline-flex; align-items: center; gap: 10px; }
            .color-dot { width: 22px; height: 22px; border-radius: 999px; border: 1px solid rgba(15,23,42,0.12); flex-shrink: 0; }

            /* Table styles */
            .data-table-wrapper { overflow-x: auto; }
            .data-table { width: 100%; border-collapse: collapse; font-size: 0.92rem; }
            .data-table th, .data-table td { padding: 14px 16px; text-align: left; border-bottom: 1px solid #e2e8f0; }
            .data-table th { background: #f1f3f5; font-weight: 600; text-transform: uppercase; font-size: 0.78rem; letter-spacing: 0.06em; color: #6c757d; white-space: nowrap; }
            .data-table tbody tr:hover { background: #f8f9fa; }
            .data-table .cell-id { font-family: monospace; color: #0369a1; font-weight: 600; font-size: 0.85rem; }
            .data-table .cell-name { font-weight: 600; }
            .data-table .cell-description { color: #64748b; max-width: 300px; }
            .data-table .cell-actions { white-space: nowrap; }
            .data-table .action-group { display: flex; gap: 8px; }
            .data-table .table-btn { min-width: 70px; padding: 8px 12px; font-size: 0.8rem; }

            /* Keep product rows readable while allowing the table to scroll on small screens. */
            .product-table-wrapper { overflow-x: hidden; border: 1px solid #d9dee5; }
            .product-table { width: 100%; table-layout: fixed; color: #111827; }
            .product-table th, .product-table td { text-align: center; vertical-align: middle; border: 1px solid #d9dee5; }
            .product-table th { height: 66px; padding: 12px 10px; background: #f1f1f1; color: #111827; font-size: 0.86rem; line-height: 1.35; }
            .product-table td { height: 136px; padding: 10px 14px; font-size: 1rem; }
            .product-table tbody tr:nth-child(even) { background: #fafbfc; }
            .product-table tbody tr:hover { background: #f1f5f9; }
            .product-table th:nth-child(1), .product-table td:nth-child(1) { width: 9%; }
            .product-table th:nth-child(2), .product-table td:nth-child(2) { width: 11%; }
            .product-table th:nth-child(3), .product-table td:nth-child(3) { width: 11%; }
            .product-table th:nth-child(4), .product-table td:nth-child(4) { width: 11%; }
            .product-table th:nth-child(5), .product-table td:nth-child(5) { width: 27%; }
            .product-table th:nth-child(6), .product-table td:nth-child(6) { width: 11%; }
            .product-table th:nth-child(7), .product-table td:nth-child(7) { width: 12%; }
            .product-table th:nth-child(8), .product-table td:nth-child(8) { width: 8%; }
            .product-image-cell { padding: 10px !important; }
            .product-image { display: block; width: min(125px, 100%); height: auto; aspect-ratio: 1; margin: 0 auto; object-fit: contain; }
            .product-image-empty { width: min(125px, 100%); aspect-ratio: 1; margin: 0 auto; display: flex; align-items: center; justify-content: center; background: #f3f4f6; color: #94a3b8; font-size: 0.8rem; }
            .product-name-cell { line-height: 1.45; font-weight: 500 !important; }
            .product-id-cell { overflow-wrap: anywhere; word-break: break-word; line-height: 1.3; }
            .product-price-cell { white-space: nowrap; }
            .product-description-cell { overflow: hidden; line-height: 1.45; }
            .product-description-text { display: -webkit-box; overflow: hidden; overflow-wrap: anywhere; word-break: break-word; -webkit-box-orient: vertical; -webkit-line-clamp: 3; }
            .product-category-cell { line-height: 1.45; text-transform: uppercase; }
            .product-status { display: inline-flex; align-items: center; justify-content: center; min-width: 92px; padding: 8px 10px; border-radius: 4px; font-size: 0.82rem; font-weight: 700; }
            .product-status.visible { background: #dcfce7; color: #166534; }
            .product-status.hidden { background: #fee2e2; color: #991b1b; }
            .product-actions-cell .action-group { justify-content: center; flex-direction: column; }

            @media (max-width: 1200px) {
                .product-table th, .product-table td { padding: 8px 6px; font-size: 0.86rem; }
                .product-table th { font-size: 0.72rem; }
                .product-table td { height: 112px; }
                .product-table .table-btn { min-width: 0; width: 100%; padding: 7px 4px; font-size: 0.72rem; }
                .product-status { min-width: 0; width: 100%; padding: 7px 3px; font-size: 0.7rem; }
            }

            /* Category group styles for Sizes table */
            .size-group-list { display: flex; flex-direction: column; gap: 12px; }
            .size-group { background: #ffffff; border: 1px solid #e2e8f0; border-radius: 12px; overflow: hidden; }
            .size-group-header { display: flex; align-items: center; justify-content: space-between; padding: 16px 20px; background: #f1f3f5; color: #2c3e50; border-bottom: 1px solid #e2e8f0; }
            .size-group-title { font-weight: 700; font-size: 1rem; }
            .size-group-count { font-size: 0.85rem; color: #6c757d; margin-left: 10px; }
            .add-size-btn { display: inline-flex; align-items: center; gap: 6px; padding: 6px 12px; background: #1abc9c; border: none; border-radius: 8px; color: #ffffff; font-size: 0.8rem; font-weight: 600; text-decoration: none; cursor: pointer; transition: background 0.2s ease; }
            .add-size-btn:hover { background: #16a085; }
            .empty-state { padding: 42px 16px; text-align: center; color: #64748b; }
            .empty-state h4 { margin: 0 0 10px; font-size: 1.15rem; color: #2c3e50; }
            .empty-state p { margin: 0; color: #64748b; }
            .pagination-bar { margin-top: 22px; display: flex; justify-content: space-between; align-items: center; gap: 14px; flex-wrap: wrap; }
            .pagination-summary { color: #64748b; font-size: 0.92rem; font-weight: 600; }
            .pagination-controls { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
            .page-link { min-width: 42px; height: 42px; padding: 0 14px; border-radius: 8px; border: 1px solid #dbe3f0; background: #ffffff; color: #334155; display: inline-flex; align-items: center; justify-content: center; text-decoration: none; font-weight: 700; transition: all 0.2s ease; }
            .page-link.active { background: #1abc9c; color: #ffffff; border-color: transparent; }
            .page-link:hover { transform: translateY(-1px); }
            .tab-panel { display: none; }
            .tab-panel.active { display: block; }

            @media (max-width: 1280px) { .product-card { grid-template-columns: minmax(0, 1fr); } .product-actions { flex-direction: row; align-items: center; min-width: 0; } .action-group { flex-direction: row; flex-wrap: wrap; } }
            @media (max-width: 1180px) { .product-shell { grid-template-columns: 1fr; } }
            @media (max-width: 900px) { .hero-panel { flex-direction: column; } .hero-stats { width: 100%; grid-template-columns: repeat(2, minmax(0, 1fr)); } .product-meta, .product-side { grid-template-columns: 1fr; } .section-header { flex-direction: column; } }
            @media (max-width: 768px) { .sidebar { min-height: auto; height: auto; } .main-content { padding: 15px; } .stat-card .stat-number { font-size: 1.5rem; } .hero-stats { grid-template-columns: 1fr; } .product-main { flex-direction: column; } .thumb, .thumb-empty { width: 100%; max-width: 96px; height: 96px; } .product-actions { flex-direction: column; align-items: stretch; } .table-btn, .primary-btn { width: 100%; } .collection-grid { grid-template-columns: 1fr; } }
        </style>
    </head>
    <body>
        <c:set var="activeTab" value="${empty param.tab ? 'products' : param.tab}" />
        <div class="container-fluid p-0">
            <div class="row g-0">
                <!-- Sidebar -->
                <div class="col-md-3 col-lg-2 sidebar">
                    <div class="brand">Staff</div>
                    <ul class="nav flex-column">
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/staff/orders">Orders</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/staff/payments">Payments</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link active" href="${pageContext.request.contextPath}/staff/products">Products</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/staff/products?action=manageVariants">Manage Variants</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/staff/warehouse/inventory">Warehouse</a>
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
                    <div class="product-shell">
                        <div class="content-panel">
                            <section class="hero-panel">
                                <div>
                                    <h2>${activeTab eq 'categories' ? 'Category Management' : (activeTab eq 'colors' ? 'Color Management' : (activeTab eq 'sizes' ? 'Size Management' : 'Product Management'))}</h2>
                                </div>
                                <ul class="nav nav-pills mt-2" style="gap: 8px;">
                                    <li class="nav-item"><a class="nav-link ${activeTab eq 'products' ? 'active' : ''}" style="background: ${activeTab eq 'products' ? '#1abc9c' : '#f1f3f5'}; color: ${activeTab eq 'products' ? '#fff' : '#2c3e50'}; font-weight: 600; border-radius: 8px;" href="${pageContext.request.contextPath}/staff/products?tab=products">Products</a></li>
                                    <li class="nav-item"><a class="nav-link ${activeTab eq 'categories' ? 'active' : ''}" style="background: ${activeTab eq 'categories' ? '#1abc9c' : '#f1f3f5'}; color: ${activeTab eq 'categories' ? '#fff' : '#2c3e50'}; font-weight: 600; border-radius: 8px;" href="${pageContext.request.contextPath}/staff/products?tab=categories">Categories</a></li>
                                    <li class="nav-item"><a class="nav-link ${activeTab eq 'colors' ? 'active' : ''}" style="background: ${activeTab eq 'colors' ? '#1abc9c' : '#f1f3f5'}; color: ${activeTab eq 'colors' ? '#fff' : '#2c3e50'}; font-weight: 600; border-radius: 8px;" href="${pageContext.request.contextPath}/staff/products?tab=colors">Colors</a></li>
                                    <li class="nav-item"><a class="nav-link ${activeTab eq 'sizes' ? 'active' : ''}" style="background: ${activeTab eq 'sizes' ? '#1abc9c' : '#f1f3f5'}; color: ${activeTab eq 'sizes' ? '#fff' : '#2c3e50'}; font-weight: 600; border-radius: 8px;" href="${pageContext.request.contextPath}/staff/products?tab=sizes">Sizes</a></li>
                                </ul>
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
                        <div class="section-header"><div><h3>Products</h3><p>Review images, colors, sizes, stock levels, and prices in a clean layout.</p></div><button type="button" class="primary-btn" data-open-create>Add product</button></div>
                        <div class="section-body">
                            <form id="productFilterForm" method="get" action="${pageContext.request.contextPath}/staff/products" class="filter-toolbar">
                                <input type="hidden" name="tab" value="products" />
                                <input id="productKeywordInput" class="filter-input" type="text" name="keyword" value="${param.keyword}" placeholder="Search product name, SKU, size, color..." autocomplete="off" />
                                <select class="filter-select" name="statusFilter" onchange="submitProductFilterForm()">
                                    <option value="">All status</option>
                                    <option value="Available" ${param.statusFilter eq 'Available' ? 'selected' : ''}>Available</option>
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
                                    <div class="product-table-wrapper">
                                        <table class="data-table product-table">
                                            <thead>
                                                <tr>
                                                    <th class="product-id-column">ID<br>PRODUCT</th>
                                                    <th class="product-image-column">IMAGE</th>
                                                    <th>PRODUCT<br>NAME</th>
                                                    <th>PRODUCT<br>PRICE</th>
                                                    <th class="product-description-column">DESCRIPTION</th>
                                                    <th>CATEGORY<br>NAME</th>
                                                    <th>STATUS<br>(VISIBLE/HIDDEN)</th>
                                                    <th class="product-actions-column">ACTIONS</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach var="product" items="${products}">
                                                    <tr>
                                                        <td class="cell-id product-id-cell">${product.productId}</td>
                                                        <td class="product-image-cell">
                                                            <c:choose>
                                                                <c:when test="${not empty product.primaryImageUrl}">
                                                                    <img class="product-image" src="${pageContext.request.contextPath.concat(product.primaryImageUrl)}" alt="${empty product.name ? 'Product image' : product.name}" loading="lazy" onerror="this.style.display='none';this.nextElementSibling.style.display='flex';">
                                                                    <span class="product-image-empty" style="display:none;">No image</span>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <span class="product-image-empty">No image</span>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </td>
                                                        <td class="cell-name product-name-cell">${empty product.name ? '-' : product.name}</td>
                                                        <td class="product-price-cell"><fmt:formatNumber value="${product.basePrice}" type="number" groupingUsed="true" /> VND</td>
                                                        <td class="cell-description product-description-cell" title="${product.description}"><span class="product-description-text">${empty product.description ? 'No description available' : product.description}</span></td>
                                                        <td class="product-category-cell">${empty product.categoryName ? '-' : product.categoryName}</td>
                                                        <td class="product-status-cell">
                                                            <span class="product-status ${product.status eq 'Available' ? 'visible' : 'hidden'}">${product.status eq 'Available' ? 'VISIBLE' : 'HIDDEN'}</span>
                                                        </td>
                                                        <td class="cell-actions product-actions-cell">
                                                            <div class="action-group">
                                                                <a class="table-btn edit" href="javascript:void(0)" data-open-edit data-product-id="${product.productId}">Edit</a>
                                                                <a class="table-btn delete" href="javascript:void(0)" onclick="openDeleteModal('product','${product.productId}','${fn:escapeXml(product.name)}')">Delete</a>
                                                            </div>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                            </tbody>
                                        </table>
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
                        <div class="section-header"><div><h3>Categories</h3><p>Manage category names and descriptions in a structured table.</p></div><button type="button" class="primary-btn" onclick="openCategoryModal()">Add category</button></div>
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
                                                                <button type="button" class="table-btn edit" onclick="openCategoryModal('${category.categoryId}')">Edit</button>
                                                                <button type="button" class="table-btn delete" onclick="openDeleteModal('category','${category.categoryId}','${fn:escapeXml(category.name)}')">Delete</button>
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
                        <div class="section-header"><div><h3>Colors</h3><p>Display color details in a structured table format.</p></div><button type="button" class="primary-btn" onclick="openColorModal()">Add color</button></div>
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
                                                                <button type="button" class="table-btn edit" onclick="openColorModal('${color.colorId}')">Edit</button>
                                                                <button type="button" class="table-btn delete" onclick="openDeleteModal('color','${color.colorId}','${fn:escapeXml(color.colorName)}')">Delete</button>
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
                        <div class="section-header"><div><h3>Sizes</h3><p>Manage sizes grouped by category. Click on a category to expand and view its sizes.</p></div><button type="button" class="primary-btn" onclick="openSizeModal()">Add size</button></div>
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
                                                    <button type="button" class="add-size-btn" onclick="event.stopPropagation(); openSizeModal('${currentCategory.categoryId}')">
                                                        + Add Size
                                                    </button>
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
                                                                                        <button type="button" class="table-btn edit" onclick="openSizeModal(null,'${size.sizeId}')">Edit</button>
                                                                                        <button type="button" class="table-btn delete" onclick="openDeleteModal('size','${size.sizeId}','${fn:escapeXml(size.sizeName)}')">Delete</button>
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
                        </div><!-- content-panel -->
                    </div><!-- product-shell -->
                </div><!-- main-content -->
            </div><!-- row g-0 -->
        </div><!-- container-fluid p-0 -->
    </body>

    <!-- Modal: Add / Edit category -->
    <div id="categoryModal" class="product-modal" aria-hidden="true">
        <div class="product-modal-overlay" data-close-modal></div>
        <div class="product-modal-panel" role="dialog" aria-modal="true">
            <header class="product-modal-header">
                <h3 id="categoryModalTitle">Add category</h3>
                <button type="button" class="product-modal-close" data-close-modal aria-label="Close">×</button>
            </header>
            <form id="categoryForm">
                <input type="hidden" name="categoryId" id="categoryIdInput">
                <div class="product-modal-body">
                    <div id="categoryModalAlert" class="alert" style="display:none;"></div>
                    <div class="product-modal-grid">
                        <div class="form-group full-width">
                            <label for="categoryNameInput">Category name *</label>
                            <input id="categoryNameInput" name="name" type="text" maxlength="200" required placeholder="Ex: T-Shirts">
                        </div>
                        <div class="form-group full-width">
                            <label for="categoryDescInput">Description</label>
                            <textarea id="categoryDescInput" name="description" rows="4" placeholder="Add a short description for this category..."></textarea>
                        </div>
                    </div>
                </div>
                <footer class="product-modal-footer">
                    <button type="button" class="ghost-btn" data-close-modal>Cancel</button>
                    <button type="submit" class="primary-btn">Save</button>
                </footer>
            </form>
        </div>
    </div>

    <!-- Modal: Add / Edit color -->
    <div id="colorModal" class="product-modal" aria-hidden="true">
        <div class="product-modal-overlay" data-close-modal></div>
        <div class="product-modal-panel" role="dialog" aria-modal="true">
            <header class="product-modal-header">
                <h3 id="colorModalTitle">Add color</h3>
                <button type="button" class="product-modal-close" data-close-modal aria-label="Close">×</button>
            </header>
            <form id="colorForm">
                <input type="hidden" name="colorId" id="colorIdInput">
                <div class="product-modal-body">
                    <div id="colorModalAlert" class="alert" style="display:none;"></div>
                    <div class="product-modal-grid">
                        <div class="form-group">
                            <label for="colorNameInput">Color name *</label>
                            <input id="colorNameInput" name="colorName" type="text" maxlength="100" required placeholder="Ex: Red">
                        </div>
                        <div class="form-group">
                            <label for="colorHexInput">Hex code *</label>
                            <input id="colorHexInput" name="hexCode" type="color" value="#000000" required style="height: 48px; padding: 6px;">
                        </div>
                    </div>
                </div>
                <footer class="product-modal-footer">
                    <button type="button" class="ghost-btn" data-close-modal>Cancel</button>
                    <button type="submit" class="primary-btn">Save</button>
                </footer>
            </form>
        </div>
    </div>

    <!-- Modal: Add / Edit size -->
    <div id="sizeModal" class="product-modal" aria-hidden="true">
        <div class="product-modal-overlay" data-close-modal></div>
        <div class="product-modal-panel" role="dialog" aria-modal="true">
            <header class="product-modal-header">
                <h3 id="sizeModalTitle">Add size</h3>
                <button type="button" class="product-modal-close" data-close-modal aria-label="Close">×</button>
            </header>
            <form id="sizeForm">
                <input type="hidden" name="sizeId" id="sizeIdInput">
                <div class="product-modal-body">
                    <div id="sizeModalAlert" class="alert" style="display:none;"></div>
                    <div class="product-modal-grid">
                        <div class="form-group">
                            <label for="sizeCategorySelect">Category *</label>
                            <select id="sizeCategorySelect" name="categoryId" required>
                                <option value="">-- Select category --</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label for="sizeNameInput">Size name *</label>
                            <input id="sizeNameInput" name="sizeName" type="text" maxlength="50" required placeholder="Ex: S, M, L, 38, 40...">
                        </div>
                    </div>
                </div>
                <footer class="product-modal-footer">
                    <button type="button" class="ghost-btn" data-close-modal>Cancel</button>
                    <button type="submit" class="primary-btn">Save</button>
                </footer>
            </form>
        </div>
    </div>

    <!-- Modal: Delete chung cho category / color / size -->
    <div id="deleteModal" class="product-modal" aria-hidden="true">
        <div class="product-modal-overlay" data-close-modal></div>
        <div class="product-modal-panel" role="dialog" aria-modal="true" style="width: min(480px, 100%);">
            <header class="product-modal-header">
                <h3 id="deleteModalTitle">Delete</h3>
                <button type="button" class="product-modal-close" data-close-modal aria-label="Close">×</button>
            </header>
            <div class="product-modal-body">
                <div id="deleteModalAlert" class="alert" style="display:none;"></div>
                <p id="deleteModalBody" style="margin: 0; color: #334155; line-height: 1.6;">Are you sure you want to delete this item?</p>
            </div>
            <footer class="product-modal-footer">
                <button type="button" class="ghost-btn" data-close-modal>Cancel</button>
                <button type="button" id="deleteModalConfirm" class="primary-btn" style="background: linear-gradient(135deg, #ef4444 0%, #b91c1c 100%); box-shadow: 0 18px 30px rgba(239, 68, 68, 0.22);">Delete</button>
            </footer>
        </div>
    </div>

    <!-- Modal: Add / Edit product -->
    <div id="productModal" class="product-modal" aria-hidden="true">
        <div class="product-modal-overlay" data-close-modal></div>
        <div class="product-modal-panel" role="dialog" aria-modal="true" aria-labelledby="productModalTitle">
            <header class="product-modal-header">
                <div>
                    <h3 id="productModalTitle">Add Product</h3>
                </div>
                <button type="button" class="product-modal-close" data-close-modal aria-label="Close">×</button>
            </header>
            <div class="product-modal-body">
                <div id="productModalAlert" class="alert" style="display:none;"></div>
                <form id="productModalForm" method="post" action="${pageContext.request.contextPath}/staff/products" enctype="multipart/form-data">
                    <input type="hidden" name="action" id="productModalAction" value="create">
                    <input type="hidden" name="productId" id="productModalProductId" value="">
                    <input type="hidden" name="existingImageUrl" id="productModalExistingImage" value="">

                    <div class="product-modal-grid">
                        <div class="form-group">
                            <label for="modalName">Product name *</label>
                            <input id="modalName" name="name" type="text" required maxlength="200" placeholder="e.g. Classic denim jacket">
                        </div>
                        <div class="form-group">
                            <label for="modalCategory">Category *</label>
                            <select id="modalCategory" name="categoryId" required>
                                <option value="">-- Select category --</option>
                                <c:forEach var="category" items="${categories}">
                                    <option value="${category.categoryId}">${category.name}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="form-group">
                            <label for="modalBasePrice">Base price (đ) *</label>
                            <input id="modalBasePrice" name="basePrice" type="text" inputmode="numeric" required placeholder="e.g. 500000">
                        </div>
                        <div class="form-group">
                            <label for="modalStatus">Status *</label>
                            <select id="modalStatus" name="status" required>
                                <c:forEach var="status" items="${statuses}">
                                    <c:if test="${status != 'OutOfStock'}">
                                        <option value="${status}">${status}</option>
                                    </c:if>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="form-group full-width">
                            <label for="modalDescription">Description</label>
                            <textarea id="modalDescription" name="description" rows="3" placeholder="Add a short description about material, style, or target audience..."></textarea>
                        </div>
                        <div class="form-group full-width">
                            <label for="modalImage">Product image</label>
                            <input id="modalImage" name="productImage" type="file" accept="image/*">
                            <div id="modalImagePreview" class="modal-image-preview">No image</div>
                        </div>
                    </div>

                </form>
            </div>
            <footer class="product-modal-footer">
                <button type="button" class="ghost-btn" data-close-modal>Close</button>
                <button type="submit" form="productModalForm" class="primary-btn" id="productModalSubmit">Save product</button>
            </footer>
        </div>
    </div>

    <style>
        .product-modal { position: fixed; inset: 0; z-index: 9999; display: none; align-items: center; justify-content: center; padding: 24px; }
        .product-modal.open { display: flex; }
        .product-modal-overlay { position: absolute; inset: 0; background: rgba(15, 23, 42, 0.55); backdrop-filter: blur(4px); }
        .product-modal-panel { position: relative; background: #ffffff; border-radius: 24px; width: min(960px, 100%); max-height: calc(100vh - 48px); display: flex; flex-direction: column; box-shadow: 0 24px 60px rgba(15, 23, 42, 0.25); overflow: hidden; }
        .product-modal-header { padding: 22px 28px; border-bottom: 1px solid #e2e8f0; display: flex; justify-content: space-between; align-items: center; gap: 16px; }
        .product-modal-header h3 { margin: 0; font-size: 1.5rem; }
        .product-modal-close { background: #f1f5f9; border: none; width: 36px; height: 36px; border-radius: 50%; font-size: 1.4rem; cursor: pointer; color: #475569; }
        .product-modal-close:hover { background: #e2e8f0; }
        .product-modal-body { padding: 24px 28px; overflow-y: auto; flex: 1; }
        .product-modal-footer { padding: 18px 28px; border-top: 1px solid #e2e8f0; display: flex; justify-content: flex-end; gap: 12px; background: #f8fafc; }
        .product-modal-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 18px; }
        .product-modal-grid .full-width { grid-column: 1 / -1; }
        .product-modal-grid label { display: block; font-weight: 700; margin-bottom: 8px; color: #334155; font-size: 0.9rem; }
        .product-modal-grid input, .product-modal-grid select, .product-modal-grid textarea { width: 100%; padding: 12px 14px; border-radius: 14px; border: 1px solid #dbe3f0; background: #ffffff; font: inherit; box-sizing: border-box; }
        .product-modal-grid input:focus, .product-modal-grid select:focus, .product-modal-grid textarea:focus { outline: none; border-color: #7c3aed; box-shadow: 0 0 0 4px rgba(124, 58, 237, 0.12); }
        .modal-image-preview { width: 120px; height: 120px; border-radius: 16px; border: 1px solid #dbe3f0; background: #f8fafc; display: flex; align-items: center; justify-content: center; color: #94a3b8; margin-top: 10px; overflow: hidden; }
        .modal-image-preview img { width: 100%; height: 100%; object-fit: cover; }
        .modal-section-heading { margin: 24px 0 12px; }
        .modal-section-heading h4 { margin: 0; font-size: 1.05rem; }
        .modal-section-heading p { margin: 4px 0 0; color: #64748b; font-size: 0.85rem; }
        .variant-row { border: 1px solid #e2e8f0; border-radius: 18px; padding: 16px; background: #ffffff; display: grid; gap: 14px; margin-bottom: 12px; }
        .variant-row-header { display: flex; justify-content: space-between; align-items: center; gap: 12px; }
        .variant-row-title-wrap { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
        .variant-row-title { font-weight: 700; color: #334155; }
        .variant-stock-badge { display: inline-flex; padding: 4px 10px; border-radius: 999px; font-size: 0.75rem; font-weight: 700; }
        .variant-stock-badge.in-stock { background: rgba(22, 163, 74, 0.12); color: #15803d; }
        .variant-stock-badge.out-of-stock { background: rgba(220, 38, 38, 0.12); color: #b91c1c; }
        .variant-row-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 12px; }
        .variant-row-grid label { display: block; font-size: 0.78rem; font-weight: 600; margin-bottom: 6px; color: #475569; }
        .variant-row-grid select, .variant-row-grid input { padding: 10px 12px; border-radius: 12px; border: 1px solid #dbe3f0; background: #ffffff; width: 100%; box-sizing: border-box; }
        .variant-remove-btn { background: rgba(220, 38, 38, 0.12); color: #b91c1c; border: none; padding: 8px 14px; border-radius: 12px; font-weight: 700; cursor: pointer; }
        @media (max-width: 768px) { .product-modal-grid { grid-template-columns: 1fr; } .variant-row-grid { grid-template-columns: 1fr 1fr; } }
    </style>

    <script type="application/json" id="allCategoriesData">
        <c:forEach var="cat" items="${allCategoryItems}" varStatus="status">
            <c:if test="${not status.first}">,</c:if>{"id":"${fn:escapeXml(cat.categoryId)}","name":"${fn:escapeXml(cat.name)}"}
        </c:forEach>
    </script>

    <script>
        (function () {
            const ctx = '${pageContext.request.contextPath}';
            const modal = document.getElementById('productModal');
            const form = document.getElementById('productModalForm');
            const titleEl = document.getElementById('productModalTitle');
            const actionInput = document.getElementById('productModalAction');
            const productIdInput = document.getElementById('productModalProductId');
            const existingImageInput = document.getElementById('productModalExistingImage');
            const nameInput = document.getElementById('modalName');
            const categorySelect = document.getElementById('modalCategory');
            const basePriceInput = document.getElementById('modalBasePrice');
            const statusSelect = document.getElementById('modalStatus');
            const descriptionInput = document.getElementById('modalDescription');
            const imageInput = document.getElementById('modalImage');
            const imagePreview = document.getElementById('modalImagePreview');
            const alertBox = document.getElementById('productModalAlert');
            const submitBtn = document.getElementById('productModalSubmit');

            const formatPrice = (value) => {
                // Accept both string and number (priceOverride from JSON can be a number)
                const str = value == null ? '' : String(value);
                const digits = str.replace(/\D/g, '');
                if (!digits) return '';
                return Number(digits).toLocaleString('vi-VN');
            };

            const showAlert = (msg, type) => {
                if (!msg) { alertBox.style.display = 'none'; return; }
                alertBox.className = 'alert alert-' + (type || 'error');
                alertBox.textContent = msg;
                alertBox.style.display = 'block';
            };

            const setImagePreview = (src) => {
                imagePreview.innerHTML = '';
                if (src) {
                    // Normalize URL: keep http(s)/data:, prepend context for relative paths
                    let finalSrc = src;
                    if (!/^(https?:|data:)/i.test(src)) {
                        if (!src.startsWith('/')) {
                            finalSrc = ctx + '/' + src;
                        } else {
                            finalSrc = ctx + src;
                        }
                    }
                    const img = document.createElement('img');
                    img.src = finalSrc;
                    img.onerror = function () {
                        imagePreview.innerHTML = '';
                        const span = document.createElement('span');
                        span.textContent = 'Image not found';
                        imagePreview.appendChild(span);
                    };
                    imagePreview.appendChild(img);
                } else {
                    const span = document.createElement('span');
                    span.textContent = 'No image';
                    imagePreview.appendChild(span);
                }
            };

            const openModal = (mode, productId, productData) => {
                form.reset();
                showAlert('', 'error');
                actionInput.value = mode;
                productIdInput.value = productId || '';
                titleEl.textContent = mode === 'edit' ? 'Update Product' : 'Add Product';
                submitBtn.textContent = mode === 'edit' ? 'Save changes' : 'Create product';
                imageInput.required = mode === 'create';

                if (mode === 'edit' && productData) {
                    nameInput.value = productData.name || '';
                    categorySelect.value = productData.categoryId || '';
                    basePriceInput.value = formatPrice(productData.basePrice);
                    statusSelect.value = productData.status || 'Available';
                    // Fallback to Available if status not in dropdown (e.g. OutOfStock)
                    if (!statusSelect.value) statusSelect.value = 'Available';
                    descriptionInput.value = productData.description || '';
                    existingImageInput.value = productData.primaryImageUrl || '';
                    setImagePreview(productData.primaryImageUrl || '');
                } else {
                    existingImageInput.value = '';
                    setImagePreview('');
                }
                modal.classList.add('open');
                modal.setAttribute('aria-hidden', 'false');
            };

            const closeModal = () => {
                modal.classList.remove('open');
                modal.setAttribute('aria-hidden', 'true');
                // Strip ?openModal query for clean URL
                if (window.history && window.history.replaceState) {
                    const url = new URL(window.location.href);
                    url.searchParams.delete('openModal');
                    url.searchParams.delete('productId');
                    window.history.replaceState({}, '', url);
                }
            };

            // Close buttons
            modal.querySelectorAll('[data-close-modal]').forEach(el => {
                el.addEventListener('click', closeModal);
            });
            document.addEventListener('keydown', (e) => { if (e.key === 'Escape' && modal.classList.contains('open')) closeModal(); });

            // Preview image when file is selected
            imageInput.addEventListener('change', () => {
                const file = imageInput.files && imageInput.files[0];
                if (!file) {
                    setImagePreview(existingImageInput.value);
                    return;
                }
                const reader = new FileReader();
                reader.onload = (e) => setImagePreview(e.target.result);
                reader.readAsDataURL(file);
            });

            // Currency formatter cho base price
            basePriceInput.addEventListener('input', function () { this.value = formatPrice(this.value); });

            // NÚT "Add product" trong trang
            document.querySelectorAll('[data-open-create]').forEach(btn => {
                btn.addEventListener('click', (e) => { e.preventDefault(); openModal('create', null, null); });
            });

            // "Edit" button in each product card
            document.querySelectorAll('[data-open-edit]').forEach(btn => {
                btn.addEventListener('click', (e) => {
                    e.preventDefault();
                    const productId = btn.dataset.productId;
                    titleEl.textContent = 'Loading...';
                    // Clear form first to avoid showing stale data
                    form.reset();
                    showAlert('', 'error');
                    modal.classList.add('open');
                    modal.setAttribute('aria-hidden', 'false');
                    fetch(ctx + '/staff/products?action=getProductJson&id=' + encodeURIComponent(productId), {
                        credentials: 'same-origin',
                        headers: { 'X-Requested-With': 'XMLHttpRequest' }
                    })
                        .then(async r => {
                            const text = await r.text();
                            let data;
                            try { data = JSON.parse(text); }
                            catch (parseErr) {
                                throw new Error('Server returned non-JSON (status ' + r.status + '): ' + text.substring(0, 120));
                            }
                            return data;
                        })
                        .then(data => {
                            if (!data) { showAlert('Empty response from server.', 'error'); return; }
                            if (data.error) { showAlert(data.error, 'error'); return; }
                            openModal('edit', productId, data);
                        })
                        .catch(err => {
                            // Don't close modal - keep error visible for debugging
                            titleEl.textContent = 'Update Product';
                            showAlert('Cannot load product: ' + err.message, 'error');
                            console.error('Edit product load failed:', err);
                        });
                });
            });

            // Validate the product fields before submitting.
            form.addEventListener('submit', function (e) {
                showAlert('', 'error');
                if (!nameInput.value.trim()) { e.preventDefault(); showAlert('Product name is required.', 'error'); return; }
                if (!categorySelect.value) { e.preventDefault(); showAlert('Please select a category.', 'error'); return; }
                if (!basePriceInput.value.trim()) { e.preventDefault(); showAlert('Base price is required.', 'error'); return; }
                if (actionInput.value === 'create' && (!imageInput.files || imageInput.files.length === 0)) {
                    e.preventDefault();
                    showAlert('Please choose a product image.', 'error');
                }
            });

            // Auto-open modal if URL has ?openModal=create
            const urlParams = new URLSearchParams(window.location.search);
            const openMode = urlParams.get('openModal');
            if (openMode === 'create') {
                openModal('create', null, null);
            }

            // ============ Modal Category ============
            const categoryModal = document.getElementById('categoryModal');
            const categoryForm = document.getElementById('categoryForm');
            const categoryTitleEl = document.getElementById('categoryModalTitle');
            const categoryIdInput = document.getElementById('categoryIdInput');
            const categoryNameInput = document.getElementById('categoryNameInput');
            const categoryDescInput = document.getElementById('categoryDescInput');

            function attachModalClose(modalEl) {
                if (!modalEl) return;
                modalEl.querySelectorAll('[data-close-modal]').forEach(el => {
                    el.addEventListener('click', () => {
                        modalEl.classList.remove('open');
                        modalEl.setAttribute('aria-hidden', 'true');
                    });
                });
            }
            // Show alert (error/success) inside the open modal instead of productModal alert box
            function localShowAlert(modalEl, msg, type) {
                if (!modalEl) { alert(msg); return; }
                const box = modalEl.querySelector('.alert');
                if (!box) { alert(msg); return; }
                box.className = 'alert alert-' + (type || 'error');
                box.textContent = msg;
                box.style.display = 'block';
            }
            function localClearAlert(modalEl) {
                if (!modalEl) return;
                const box = modalEl.querySelector('.alert');
                if (box) { box.style.display = 'none'; box.textContent = ''; }
            }
            attachModalClose(categoryModal);
            document.addEventListener('keydown', (e) => {
                if (e.key === 'Escape') {
                    document.querySelectorAll('.product-modal.open').forEach(m => {
                        m.classList.remove('open');
                        m.setAttribute('aria-hidden', 'true');
                    });
                }
            });

            window.openCategoryModal = function (categoryId) {
                if (!categoryModal) return;
                localClearAlert(categoryModal);
                if (categoryId) {
                    categoryTitleEl.textContent = 'Edit category';
                    categoryIdInput.value = categoryId;
                    categoryNameInput.value = '';
                    categoryDescInput.value = '';
                    fetch(ctx + '/staff/products?action=getCategoryJson&id=' + encodeURIComponent(categoryId))
                        .then(r => r.json())
                        .then(data => {
                            if (data.error) { localShowAlert(categoryModal, data.error, 'error'); return; }
                            categoryNameInput.value = data.name || '';
                            categoryDescInput.value = data.description || '';
                        }).catch(err => localShowAlert(categoryModal, 'Failed to load category: ' + err.message, 'error'));
                } else {
                    categoryTitleEl.textContent = 'Add category';
                    categoryIdInput.value = '';
                    categoryForm.reset();
                }
                categoryModal.classList.add('open');
                categoryModal.setAttribute('aria-hidden', 'false');
            };

            categoryForm.addEventListener('submit', function (e) {
                e.preventDefault();
                localClearAlert(categoryModal);
                const isEdit = categoryIdInput.value !== '';
                const params = new URLSearchParams();
                params.set('action', isEdit ? 'editCategory' : 'createCategory');
                params.set('name', categoryNameInput.value.trim());
                params.set('description', categoryDescInput.value.trim());
                if (isEdit) params.set('categoryId', categoryIdInput.value);
                fetch(ctx + '/staff/products', { method: 'POST', body: params })
                    .then(r => r.json())
                    .then(data => {
                        if (data.success) {
                            localShowAlert(categoryModal, data.message, 'success');
                            setTimeout(() => { categoryModal.classList.remove('open'); window.location.reload(); }, 600);
                        } else {
                            localShowAlert(categoryModal, data.message || 'Failed', 'error');
                        }
                    }).catch(err => localShowAlert(categoryModal, 'Request failed: ' + err.message, 'error'));
            });

            // ============ Modal Color ============
            const colorModal = document.getElementById('colorModal');
            const colorForm = document.getElementById('colorForm');
            const colorTitleEl = document.getElementById('colorModalTitle');
            const colorIdInput = document.getElementById('colorIdInput');
            const colorNameInput = document.getElementById('colorNameInput');
            const colorHexInput = document.getElementById('colorHexInput');
            attachModalClose(colorModal);

            window.openColorModal = function (colorId) {
                if (!colorModal) return;
                localClearAlert(colorModal);
                if (colorId) {
                    colorTitleEl.textContent = 'Edit color';
                    colorIdInput.value = colorId;
                    fetch(ctx + '/staff/products?action=getColorJson&id=' + encodeURIComponent(colorId))
                        .then(r => r.json())
                        .then(data => {
                            if (data.error) { localShowAlert(colorModal, data.error, 'error'); return; }
                            colorNameInput.value = data.colorName || '';
                            colorHexInput.value = data.hexCode || '#000000';
                        }).catch(err => localShowAlert(colorModal, 'Failed to load color: ' + err.message, 'error'));
                } else {
                    colorTitleEl.textContent = 'Add color';
                    colorIdInput.value = '';
                    colorForm.reset();
                    colorHexInput.value = '#000000';
                }
                colorModal.classList.add('open');
                colorModal.setAttribute('aria-hidden', 'false');
            };

            colorForm.addEventListener('submit', function (e) {
                e.preventDefault();
                localClearAlert(colorModal);
                const isEdit = colorIdInput.value !== '';
                const params = new URLSearchParams();
                params.set('action', isEdit ? 'editColor' : 'createColor');
                params.set('colorName', colorNameInput.value.trim());
                params.set('hexCode', colorHexInput.value);
                if (isEdit) params.set('colorId', colorIdInput.value);
                fetch(ctx + '/staff/products', { method: 'POST', body: params })
                    .then(r => r.json())
                    .then(data => {
                        if (data.success) {
                            localShowAlert(colorModal, data.message, 'success');
                            setTimeout(() => { colorModal.classList.remove('open'); window.location.reload(); }, 600);
                        } else {
                            localShowAlert(colorModal, data.message || 'Failed', 'error');
                        }
                    }).catch(err => localShowAlert(colorModal, 'Request failed: ' + err.message, 'error'));
            });

            // ============ Modal Size ============
            const sizeModal = document.getElementById('sizeModal');
            const sizeForm = document.getElementById('sizeForm');
            const sizeTitleEl = document.getElementById('sizeModalTitle');
            const sizeIdInput = document.getElementById('sizeIdInput');
            const sizeCategorySelect = document.getElementById('sizeCategorySelect');
            const sizeNameInput = document.getElementById('sizeNameInput');
            attachModalClose(sizeModal);

            function populateSizeCategoryOptions() {
                sizeCategorySelect.innerHTML = '<option value="">-- Select category --</option>';
                for (const cat of allCategoriesForSize) {
                    const opt = document.createElement('option');
                    opt.value = cat.id;
                    opt.textContent = cat.name;
                    sizeCategorySelect.appendChild(opt);
                }
            }
            const allCategoriesForSize = (() => {
                // Read from JSON block rendered by servlet - contains ALL categories, regardless of active tab
                try {
                    const raw = document.getElementById('allCategoriesData').textContent.trim() || '[]';
                    return JSON.parse('[' + raw + ']');
                } catch (err) {
                    return [];
                }
            })();

            window.openSizeModal = function (preselectCategoryId, sizeId) {
                if (!sizeModal) return;
                localClearAlert(sizeModal);
                populateSizeCategoryOptions();
                if (sizeId) {
                    sizeTitleEl.textContent = 'Edit size';
                    sizeIdInput.value = sizeId;
                    sizeNameInput.value = '';
                    fetch(ctx + '/staff/products?action=getSizeJson&id=' + encodeURIComponent(sizeId))
                        .then(r => r.json())
                        .then(data => {
                            if (data.error) { localShowAlert(sizeModal, data.error, 'error'); return; }
                            sizeNameInput.value = data.sizeName || '';
                            sizeCategorySelect.value = data.categoryId || '';
                        }).catch(err => localShowAlert(sizeModal, 'Failed to load size: ' + err.message, 'error'));
                } else {
                    sizeTitleEl.textContent = 'Add size';
                    sizeIdInput.value = '';
                    sizeForm.reset();
                    if (preselectCategoryId) sizeCategorySelect.value = preselectCategoryId;
                }
                sizeModal.classList.add('open');
                sizeModal.setAttribute('aria-hidden', 'false');
            };

            sizeForm.addEventListener('submit', function (e) {
                e.preventDefault();
                localClearAlert(sizeModal);
                const isEdit = sizeIdInput.value !== '';
                const params = new URLSearchParams();
                params.set('action', isEdit ? 'editSize' : 'createSize');
                params.set('sizeName', sizeNameInput.value.trim());
                params.set('categoryId', sizeCategorySelect.value);
                if (isEdit) params.set('sizeId', sizeIdInput.value);
                fetch(ctx + '/staff/products', { method: 'POST', body: params })
                    .then(r => r.json())
                    .then(data => {
                        if (data.success) {
                            localShowAlert(sizeModal, data.message, 'success');
                            setTimeout(() => { sizeModal.classList.remove('open'); window.location.reload(); }, 600);
                        } else {
                            localShowAlert(sizeModal, data.message || 'Failed', 'error');
                        }
                    }).catch(err => localShowAlert(sizeModal, 'Request failed: ' + err.message, 'error'));
            });

            // ============ Modal Delete (chung cho category/color/size) ============
            const deleteModalEl = document.getElementById('deleteModal');
            const deleteModalTitle = document.getElementById('deleteModalTitle');
            const deleteModalBody = document.getElementById('deleteModalBody');
            const deleteModalConfirm = document.getElementById('deleteModalConfirm');
            attachModalClose(deleteModalEl);
            let deleteTarget = null;

            window.openDeleteModal = function (type, id, label) {
                if (!deleteModalEl) return;
                localClearAlert(deleteModalEl);
                deleteTarget = { type: type, id: id };
                const typeName = (type === 'category') ? 'Category' : (type === 'color') ? 'Color' : (type === 'size') ? 'Size' : 'Product';
                deleteModalTitle.textContent = 'Delete ' + typeName;
                deleteModalBody.textContent = 'Are you sure you want to delete ' + typeName.toLowerCase() + ' "' + label + '"? This action cannot be undone.';
                deleteModalEl.classList.add('open');
                deleteModalEl.setAttribute('aria-hidden', 'false');
            };

            deleteModalConfirm.addEventListener('click', function () {
                if (!deleteTarget) return;
                localClearAlert(deleteModalEl);
                const params = new URLSearchParams();
                const actionMap = {
                    category: 'deleteCategory', color: 'deleteColor', size: 'deleteSize', product: 'deleteProduct'
                };
                const idMap = {
                    category: 'categoryId', color: 'colorId', size: 'sizeId', product: 'productId'
                };
                const action = actionMap[deleteTarget.type];
                const idParam = idMap[deleteTarget.type];
                if (!action || !idParam) {
                    localShowAlert(deleteModalEl, 'Unsupported delete target', 'error');
                    return;
                }
                params.set('action', action);
                params.set(idParam, deleteTarget.id);
                fetch(ctx + '/staff/products', {
                    method: 'POST',
                    body: params,
                    headers: { 'X-Requested-With': 'XMLHttpRequest' }
                })
                    .then(r => r.json())
                    .then(data => {
                        if (data.success) {
                            localShowAlert(deleteModalEl, data.message, 'success');
                            setTimeout(() => { deleteModalEl.classList.remove('open'); window.location.reload(); }, 600);
                        } else {
                            localShowAlert(deleteModalEl, data.message || 'Failed', 'error');
                        }
                    }).catch(err => localShowAlert(deleteModalEl, 'Request failed: ' + err.message, 'error'));
            });
        })();
    </script>
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
