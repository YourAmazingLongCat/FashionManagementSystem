<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Stock In - Warehouse</title>
        <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/views/pages/productManagement/product-management.css?v=20260617-warehouse">
        <style>
            body { margin: 0; font-family: 'Inter', sans-serif; color: #0f172a; background: linear-gradient(135deg, #f8fafc 0%, #eef2ff 100%); min-height: 100vh; }
            .warehouse-shell { width: min(1440px, calc(100% - 40px)); margin: 28px auto; display: grid; grid-template-columns: 280px 1fr; gap: 24px; }
            .sidebar-panel, .content-panel { background: #ffffff; border: 1px solid rgba(226, 232, 240, 0.9); border-radius: 24px; box-shadow: 0 16px 40px rgba(15, 23, 42, 0.08); contain: content; }
            .sidebar-panel { padding: 28px; display: flex; flex-direction: column; gap: 20px; background: linear-gradient(180deg, #0f172a 0%, #1e1b4b 100%); color: #ffffff; }
            .brand-label, .sidebar-label { margin: 0 0 8px; text-transform: uppercase; letter-spacing: 0.18em; font-size: 0.72rem; font-weight: 700; color: #94a3b8; }
            .sidebar-panel h1 { margin: 0; font-size: 1.6rem; line-height: 1.2; }
            .sidebar-text { margin: 10px 0 0; color: rgba(255, 255, 255, 0.7); font-size: 0.88rem; line-height: 1.6; }
            .sidebar-tabs { display: flex; flex-direction: column; gap: 8px; }
            .sidebar-tab { display: flex; align-items: center; gap: 12px; padding: 14px 16px; border-radius: 14px; color: rgba(255, 255, 255, 0.85); text-decoration: none; font-weight: 600; font-size: 0.92rem; transition: all 0.2s ease; border: 1px solid transparent; }
            .sidebar-tab:hover { background: rgba(255, 255, 255, 0.1); }
            .sidebar-tab.active { background: #ffffff; color: #1e1b4b; box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2); }
            .sidebar-tab .icon { font-size: 1.3rem; }
            .back-link { display: inline-flex; align-items: center; gap: 8px; padding: 12px 16px; border-radius: 14px; background: rgba(255, 255, 255, 0.1); color: #ffffff; text-decoration: none; font-weight: 600; font-size: 0.88rem; margin-top: auto; }
            .back-link:hover { background: rgba(255, 255, 255, 0.15); }
            .content-panel { padding: 28px; }
            .page-header { margin-bottom: 24px; }
            .page-header h2 { margin: 0 0 6px; font-size: 1.8rem; }
            .page-header p { margin: 0; color: #64748b; font-size: 0.95rem; }
            .table-panel { background: #ffffff; border: 1px solid #e2e8f0; border-radius: 20px; overflow: hidden; }
            .table-header { padding: 20px 24px; border-bottom: 1px solid #e2e8f0; }
            .table-header h3 { margin: 0; font-size: 1.1rem; }
            .table-wrapper { overflow-x: auto; }
            table { width: 100%; border-collapse: collapse; }
            th, td { padding: 12px 16px; text-align: left; border-bottom: 1px solid #f1f5f9; }
            th { background: #f8fafc; font-size: 0.78rem; text-transform: uppercase; letter-spacing: 0.08em; color: #64748b; font-weight: 700; white-space: nowrap; }
            tr:hover { background: #fafbff; }
            .stock-badge { display: inline-flex; align-items: center; justify-content: center; min-width: 60px; padding: 4px 10px; border-radius: 999px; font-weight: 700; font-size: 0.8rem; }
            .stock-high { background: rgba(22, 163, 74, 0.12); color: #16a34a; }
            .stock-low { background: rgba(220, 38, 38, 0.12); color: #dc2626; }
            .alert { padding: 14px 16px; border-radius: 12px; font-weight: 600; margin-bottom: 20px; }
            .alert-success { background: rgba(22, 163, 74, 0.12); color: #166534; border: 1px solid rgba(22, 163, 74, 0.2); }
            .alert-error { background: rgba(220, 38, 38, 0.12); color: #991b1b; border: 1px solid rgba(220, 38, 38, 0.2); }
            .btn { padding: 8px 16px; border-radius: 8px; border: none; font-weight: 700; font-size: 0.85rem; cursor: pointer; }
            .btn-primary { background: #16a34a; color: #ffffff; }
            .btn-primary:hover { background: #15803d; }
            @media (max-width: 1024px) { .warehouse-shell { grid-template-columns: 1fr; } }
            @media (max-width: 768px) { .warehouse-shell { width: min(100% - 20px, 100%); margin: 16px auto; } }
        </style>
    </head>
    <body>
        <div class="warehouse-shell">
            <aside class="sidebar-panel">
                <div>
                    <p class="brand-label">Fashion Shop</p>
                    <h1>Warehouse</h1>
                    <p class="sidebar-text">Inventory management, stock in/out</p>
                </div>
                <div class="sidebar-tabs">
                    <a class="sidebar-tab ${activeTab eq 'inventory' ? 'active' : ''}" href="${pageContext.request.contextPath}/staff/warehouse/inventory">
                        <span class="icon">&#128203;</span>
                        <span>Inventory</span>
                    </a>
                    <a class="sidebar-tab ${activeTab eq 'import' ? 'active' : ''}" href="${pageContext.request.contextPath}/staff/warehouse/import">
                        <span class="icon">&#10133;</span>
                        <span>Stock In</span>
                    </a>
                </div>
                <a class="back-link" href="${pageContext.request.contextPath}/staff/products">
                    &#8592; Back
                </a>
            </aside>

            <main class="content-panel">
                <div class="page-header">
                    <h2>Stock In</h2>
                    <p>Add stock quantity for each variant</p>
                </div>

                <c:if test="${not empty message}">
                    <div class="alert ${messageType eq 'error' ? 'alert-error' : 'alert-success'}">${message}</div>
                </c:if>

                <div class="table-panel">
                    <div class="table-header">
                        <h3>Variant List</h3>
                    </div>
                    <div class="table-wrapper">
                        <table>
                            <thead>
                                <tr>
                                    <th>SKU</th>
                                    <th>Product</th>
                                    <th>Size / Color</th>
                                    <th>Current Stock</th>
                                    <th>Add Quantity</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="item" items="${inventory}">
                                    <tr>
                                        <td><code>${item[7]}</code></td>
                                        <td><strong>${item[2]}</strong></td>
                                        <td>${item[4]} / ${item[6]}</td>
                                        <td>
                                            <span class="stock-badge ${item[8] <= 10 ? 'stock-low' : 'stock-high'}">${item[8]}</span>
                                        </td>
                                        <td>
                                            <form method="post" action="${pageContext.request.contextPath}/staff/warehouse/import">
                                                <input type="hidden" name="action" value="import">
                                                <input type="hidden" name="variantId" value="${item[0]}">
                                                <div style="display: flex; gap: 8px; align-items: center;">
                                                    <input type="number" name="quantity" min="1" value="10" required style="width: 80px; padding: 8px; border-radius: 8px; border: 1px solid #dbe3f0;">
                                                    <button type="submit" class="btn btn-primary">+ Add</button>
                                                </div>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </main>
        </div>
    </body>
</html>