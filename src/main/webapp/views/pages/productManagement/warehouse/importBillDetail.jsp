<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Import Bill Detail - Warehouse</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <style>
        body { background: #f8f9fa; }
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
        .warehouse-subtabs {
            display: flex; gap: 6px; padding: 6px;
            background: #fff; border-radius: 12px; border: 1px solid #e2e8f0;
            box-shadow: 0 2px 8px rgba(0,0,0,0.04); margin-bottom: 20px; flex-wrap: wrap;
        }
        .warehouse-subtabs a {
            padding: 10px 18px; border-radius: 8px; text-decoration: none;
            color: #475569; font-weight: 600; font-size: 0.9rem;
            display: inline-flex; align-items: center; gap: 8px;
            transition: 0.2s;
        }
        .warehouse-subtabs a:hover { background: #f1f5f9; color: #1e293b; }
        .warehouse-subtabs a.active {
            background: linear-gradient(135deg, #1abc9c, #16a085);
            color: #fff;
        }
        .bill-header-card {
            background: #ffffff;
            color: #1e293b; padding: 20px 24px; border-radius: 12px;
            border: 1px solid #e2e8f0;
            margin-bottom: 20px;
        }
        .bill-header-card h2 { margin: 0 0 6px; font-weight: 700; font-size: 1.4rem; }
        .bill-meta { display: flex; gap: 24px; flex-wrap: wrap; margin-top: 14px; }
        .bill-meta-item { background: #f8fafc; padding: 10px 16px; border-radius: 8px; min-width: 140px; border: 1px solid #e2e8f0; }
        .bill-meta-item .lbl { font-size: 0.75rem; color: #64748b; text-transform: uppercase; letter-spacing: 0.08em; font-weight: 700; }
        .bill-meta-item .val { font-size: 1.05rem; font-weight: 700; margin-top: 4px; color: #1e293b; }
        .card { border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
        .table-panel { background: #ffffff; border: 1px solid #e2e8f0; border-radius: 12px; overflow: hidden; }
        .table-header { padding: 16px 20px; border-bottom: 1px solid #e2e8f0; }
        .table-header h3 { margin: 0; font-size: 1.05rem; }
        .table-wrapper { overflow-x: auto; }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 14px 18px; text-align: left; border-bottom: 1px solid #f1f5f9; vertical-align: middle; }
        th { background: #f8fafc; font-size: 0.78rem; text-transform: uppercase; letter-spacing: 0.08em; color: #64748b; font-weight: 700; white-space: nowrap; }
        tr:hover { background: #fafbff; }
        .product-name { color: #0f172a; font-weight: 600; }
        .sku-tag { font-family: monospace; color: #475569; font-size: 0.85rem; background: #f1f5f9; padding: 3px 8px; border-radius: 6px; }
        .qty-pill { display: inline-flex; align-items: center; justify-content: center; min-width: 70px; padding: 6px 14px; border-radius: 999px; font-weight: 700; font-size: 0.9rem; background: rgba(26, 188, 156, 0.14); color: #0f766e; }
        .price-cell { color: #0f172a; font-weight: 600; }
        .total-cell { color: #0f766e; font-weight: 800; }
        .summary-card {
            margin-top: 20px; padding: 18px 22px;
            background: linear-gradient(135deg, #f0fdfa 0%, #ccfbf1 100%);
            border: 1px solid #5eead4; border-radius: 14px;
            display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 14px;
        }
        .summary-card .lbl { font-size: 0.85rem; color: #115e59; font-weight: 600; text-transform: uppercase; letter-spacing: 0.06em; }
        .summary-card .val { font-size: 1.5rem; font-weight: 800; color: #0f766e; }
        .empty-state { padding: 40px 20px; text-align: center; color: #64748b; }
        @media (max-width: 768px) { .sidebar { min-height: auto; height: auto; } .main-content { padding: 15px; } .bill-meta { gap: 10px; } }
    </style>
</head>
<body>
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
                    <a class="nav-link" href="${pageContext.request.contextPath}/staff/products">Products</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link active" href="${pageContext.request.contextPath}/staff/warehouse/inventory">Warehouse</a>
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

            <div class="warehouse-subtabs">
                <a class="${activeTab eq 'inventory' ? 'active' : ''}" href="${pageContext.request.contextPath}/staff/warehouse/inventory">Inventory</a>
                <a class="${activeTab eq 'import' ? 'active' : ''}" href="${pageContext.request.contextPath}/staff/warehouse/import">Stock In</a>
                <a class="${activeTab eq 'import-bills' ? 'active' : ''}" href="${pageContext.request.contextPath}/staff/warehouse/import-bills">Import Bills</a>
            </div>

            <div class="bill-header-card">
                <h2>Import Bill Detail</h2>
                <div class="bill-meta">
                    <div class="bill-meta-item">
                        <div class="lbl">Imported By</div>
                        <div class="val">${billEmployeeName}</div>
                    </div>
                    <div class="bill-meta-item">
                        <div class="lbl">Date</div>
                        <div class="val"><fmt:formatDate value="${billImportedAt}" pattern="dd/MM/yyyy HH:mm:ss" /></div>
                    </div>
                    <div class="bill-meta-item">
                        <div class="lbl">Items</div>
                        <div class="val">${billRows.size()}</div>
                    </div>
                </div>
            </div>

            <div class="table-panel">
                <div class="table-header">
                    <h3>Imported Items</h3>
                </div>
                <div class="table-wrapper">
                    <table>
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>Product</th>
                                <th>SKU</th>
                                <th class="text-end">Quantity</th>
                                <th class="text-end">Import Price</th>
                                <th class="text-end">Line Total</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty billRows}">
                                    <tr>
                                        <td colspan="6">
                                            <div class="empty-state">
                                                <h4>Bill not found</h4>
                                                <p>This bill may have been deleted or the link is invalid</p>
                                            </div>
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:set var="grandQty" value="0" />
                                    <c:set var="grandTotal" value="0" />
                                    <c:forEach var="row" items="${billRows}" varStatus="loop">
                                        <c:set var="grandQty" value="${grandQty + row[4]}" />
                                        <c:set var="grandTotal" value="${grandTotal + row[6]}" />
                                        <tr>
                                            <td>${loop.count}</td>
                                            <td><span class="product-name">${row[2]}</span></td>
                                            <td><span class="sku-tag">${row[3]}</span></td>
                                            <td class="text-end"><span class="qty-pill">${row[4]}</span></td>
                                            <td class="text-end price-cell"><fmt:formatNumber value="${row[5]}" pattern="#,##0"/> VND</td>
                                            <td class="text-end total-cell"><fmt:formatNumber value="${row[6]}" pattern="#,##0"/> VND</td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>

            <c:if test="${not empty billRows}">
                <div class="summary-card">
                    <div>
                        <div class="lbl">Total Quantity Imported</div>
                        <div class="val">${grandQty}</div>
                    </div>
                    <div class="text-end">
                        <div class="lbl">Total Import Cost</div>
                        <div class="val"><fmt:formatNumber value="${grandTotal}" pattern="#,##0"/> VND</div>
                    </div>
                </div>
            </c:if>

        </div><!-- end main-content -->
    </div><!-- end row -->
</div><!-- end container-fluid -->

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>