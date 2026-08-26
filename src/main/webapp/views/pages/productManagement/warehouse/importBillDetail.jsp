<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Import Bill Detail - Management</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <style>
        body { margin: 0; font-family: system-ui, -apple-system, "Segoe UI", Roboto, sans-serif; font-size: 0.9rem; color: #2c3e50; background: #f8f9fa; }
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
            border-bottom: 1px solid #34495e; text-align: center; color: #fff;
        }
        .sidebar .nav-link {
            color: #b0c4de; padding: 12px 20px; border-left: 3px solid transparent;
            transition: 0.3s; font-weight: 500; display: block;
            text-decoration: none; font-size: 1rem; line-height: 1.5;
        }
        .sidebar .nav-link:hover, .sidebar .nav-link.active {
            background: #34495e; color: #fff; border-left-color: #1abc9c;
        }
        .sidebar .nav { display: flex; flex-direction: column; min-height: calc(100vh - 130px); padding: 0; margin: 0; list-style: none; }
        .sidebar .nav-item { list-style: none; }
        .sidebar .nav-item.mt-auto { margin-top: auto; }
        .main-content { padding: 20px 30px; }

        /* Subtabs */
        .warehouse-subtabs {
            display: inline-flex; gap: 8px; margin-bottom: 20px; flex-wrap: wrap;
        }
        .warehouse-subtabs a {
            padding: 8px 18px; border-radius: 8px; text-decoration: none;
            color: #334155; font-weight: 600; font-size: 0.88rem;
            display: inline-flex; align-items: center; gap: 8px;
            background: #fff; border: 1px solid #dbe3f0; transition: all 0.2s ease;
        }
        .warehouse-subtabs a:hover {
            background: #f8fafc; color: #1abc9c; border-color: #1abc9c;
        }
        .warehouse-subtabs a.active {
            background: #1abc9c; color: #fff; border-color: #1abc9c;
            box-shadow: 0 4px 12px rgba(26, 188, 156, 0.25);
        }

        /* Bill Header Card */
        .bill-header-card {
            background: #ffffff; border: 1px solid #e2e8f0;
            border-radius: 12px; padding: 20px 24px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.06); margin-bottom: 20px;
        }
        .bill-title-row {
            display: flex; justify-content: space-between; align-items: center;
            flex-wrap: wrap; gap: 14px; margin-bottom: 18px;
        }
        .bill-title {
            font-size: 1.4rem; font-weight: 700; margin: 0; color: #2c3e50;
        }
        .bill-meta-grid {
            display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 14px;
        }
        .bill-meta-item {
            background: #f8fafc; border: 1px solid #e2e8f0;
            border-radius: 10px; padding: 12px 16px;
        }
        .bill-meta-item .lbl {
            font-size: 0.76rem; font-weight: 700; text-transform: uppercase;
            letter-spacing: 0.05em; color: #64748b; margin-bottom: 4px;
        }
        .bill-meta-item .val {
            font-size: 1.05rem; font-weight: 700; color: #2c3e50;
        }

        /* Surface Card */
        .surface-card {
            background: #ffffff; border: 1px solid #e2e8f0;
            border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.06);
            overflow: hidden; margin-bottom: 20px;
        }
        .surface-header {
            padding: 16px 20px; border-bottom: 1px solid #e2e8f0; background: #f8f9fa;
        }
        .surface-header-title { font-size: 1.15rem; font-weight: 700; margin: 0; color: #2c3e50; }

        /* Modern Table */
        .modern-table { width: 100%; border-collapse: collapse; font-size: 0.9rem; }
        .modern-table th {
            background: #f1f3f5; color: #2c3e50; font-size: 0.8rem; font-weight: 700;
            text-transform: uppercase; letter-spacing: 0.05em; padding: 12px 14px;
            border: 1px solid #d9dee5; text-align: center; vertical-align: middle;
        }
        .modern-table td {
            padding: 12px 14px; border: 1px solid #d9dee5; vertical-align: middle; text-align: center;
        }
        .modern-table tbody tr:hover { background: #fafbfc; }

        .sku-tag {
            font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
            font-size: 0.82rem; font-weight: 700; background: #f1f5f9; color: #2c3e50;
            padding: 4px 8px; border-radius: 6px; border: 1px solid #e2e8f0; display: inline-block;
        }

        .qty-pill-modern {
            background: #e0f2fe; color: #0369a1; padding: 2px 8px; border-radius: 6px; font-weight: 700; font-size: 0.85rem;
        }

        /* Summary Banner */
        .summary-banner {
            margin-top: 20px; padding: 18px 24px;
            background: linear-gradient(135deg, #2c3e50, #1a252f);
            border-radius: 12px; color: #ffffff;
            display: flex; justify-content: space-between; align-items: center;
            flex-wrap: wrap; gap: 16px; box-shadow: 0 4px 16px rgba(15, 23, 42, 0.1);
        }
        .summary-banner .lbl {
            font-size: 0.78rem; font-weight: 700; text-transform: uppercase;
            letter-spacing: 0.06em; color: #94a3b8; margin-bottom: 4px;
        }
        .summary-banner .val { font-size: 1.5rem; font-weight: 800; color: #ffffff; }

        .btn-back-modern {
            padding: 8px 16px; border-radius: 8px; border: 1px solid #cbd5e1;
            background: #ffffff; color: #334155; font-weight: 600; font-size: 0.9rem;
            text-decoration: none; display: inline-flex; align-items: center; gap: 8px; transition: 0.2s;
        }
        .btn-back-modern:hover { background: #f8fafc; color: #2c3e50; }

        @media (max-width: 768px) {
            .sidebar { min-height: auto; height: auto; position: static; }
            .main-content { padding: 15px; }
        }
    </style>
</head>
<body>
<div class="container-fluid p-0">
    <div class="row g-0">
        <!-- Sidebar -->
        <jsp:include page="/views/common/staffSidebar.jsp">
            <jsp:param name="activeMenu" value="warehouse" />
        </jsp:include>

        <!-- Main Content -->
        <div class="col-md-9 col-lg-10 main-content">

            <!-- Subtabs -->
            <div class="warehouse-subtabs">
                <a class="${activeTab eq 'inventory' ? 'active' : ''}" href="${pageContext.request.contextPath}/staff/warehouse/inventory">
                    <i class="fas fa-boxes"></i> Inventory
                </a>
                <a class="${activeTab eq 'import' ? 'active' : ''}" href="${pageContext.request.contextPath}/staff/warehouse/import">
                    <i class="fas fa-dolly"></i> Stock In
                </a>
                <a class="${activeTab eq 'import-bills' ? 'active' : ''}" href="${pageContext.request.contextPath}/staff/warehouse/import-bills">
                    <i class="fas fa-file-invoice-dollar"></i> Import Bills
                </a>
            </div>

            <div class="bill-header-card">
                <div class="bill-title-row">
                    <h1 class="bill-title">Import Bill Details</h1>
                    <a class="btn-back-modern" href="${pageContext.request.contextPath}/staff/warehouse/import-bills">
                        <i class="fas fa-arrow-left"></i> Back to Bills
                    </a>
                </div>
                <div class="bill-meta-grid">
                    <div class="bill-meta-item">
                        <div class="lbl">Imported By Staff</div>
                        <div class="val">${billEmployeeName}</div>
                    </div>
                    <div class="bill-meta-item">
                        <div class="lbl">Timestamp</div>
                        <div class="val"><fmt:formatDate value="${billImportedAt}" pattern="dd/MM/yyyy HH:mm:ss" /></div>
                    </div>
                    <div class="bill-meta-item">
                        <div class="lbl">Distinct Variants</div>
                        <div class="val">${billRows.size()}</div>
                    </div>
                </div>
            </div>

            <div class="surface-card">
                <div class="surface-header">
                    <h2 class="surface-header-title">Imported Items Breakdown</h2>
                </div>
                <div class="table-responsive">
                    <table class="modern-table">
                        <thead>
                            <tr>
                                <th style="width: 50px;">#</th>
                                <th style="text-align: left;">Product Name</th>
                                <th>SKU</th>
                                <th class="text-end">Imported Quantity</th>
                                <th class="text-end">Unit Cost</th>
                                <th class="text-end">Line Total</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty billRows}">
                                    <tr>
                                        <td colspan="6" style="padding: 48px 20px; text-align: center; color: #64748b;">
                                            Bill details not found or deleted.
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
                                            <td style="color: #64748b; font-weight: 600;">${loop.count}</td>
                                            <td style="text-align: left;"><strong style="color: #2c3e50;">${row[2]}</strong></td>
                                            <td><span class="sku-tag">${row[3]}</span></td>
                                            <td class="text-end"><span class="qty-pill-modern">${row[4]}</span></td>
                                            <td class="text-end font-monospace fw-bold"><fmt:formatNumber value="${row[5]}" pattern="#,##0"/> đ</td>
                                            <td class="text-end font-monospace fw-bold" style="color: #16a34a; font-size: 0.98rem;">
                                                <fmt:formatNumber value="${row[6]}" pattern="#,##0"/> đ
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>

            <c:if test="${not empty billRows}">
                <div class="summary-banner">
                    <div>
                        <div class="lbl">Total Units Added</div>
                        <div class="val">${grandQty} units</div>
                    </div>
                    <div class="text-end">
                        <div class="lbl">Total Inbound Cost</div>
                        <div class="val"><fmt:formatNumber value="${grandTotal}" pattern="#,##0"/> đ</div>
                    </div>
                </div>
            </c:if>

        </div><!-- end main-content -->
    </div><!-- end row -->
</div><!-- end container-fluid -->

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>