<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manage Orders</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/Assets/css/order-management.css?v=20260824-1">
    <style id="order-management-inline-fallback">
/*
 * Critical Order popup styles are intentionally inlined.
 * Manage Product uses the same pattern for its modal CSS.
 * This keeps the modal functional even when Tomcat/browser serves a stale or missing static CSS asset.
 */
/* Staff Order Detail popup. Visual language mirrors Manage Product modal. */
body.order-modal-open {
    overflow: hidden;
}

.order-detail-modal {
    position: fixed;
    inset: 0;
    z-index: 9999;
    display: none;
    align-items: center;
    justify-content: center;
    padding: 24px;
}

.order-detail-modal.open {
    display: flex;
}

.order-detail-modal-overlay {
    position: absolute;
    inset: 0;
    background: rgba(15, 23, 42, 0.55);
    backdrop-filter: blur(4px);
}

.order-detail-modal-panel {
    position: relative;
    width: min(1120px, 100%);
    max-height: calc(100vh - 48px);
    overflow: hidden;
    display: flex;
    flex-direction: column;
    border-radius: 24px;
    background: #ffffff;
    box-shadow: 0 24px 60px rgba(15, 23, 42, 0.25);
}

.order-detail-modal-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    padding: 22px 28px;
    border-bottom: 1px solid #e2e8f0;
    background: #ffffff;
}

.order-detail-modal-eyebrow,
.order-modal-eyebrow {
    margin: 0 0 5px;
    color: #64748b;
    font-size: 0.72rem;
    font-weight: 800;
    letter-spacing: 0.12em;
    text-transform: uppercase;
}

.order-detail-modal-header h3 {
    margin: 0;
    color: #0f172a;
    font-size: 1.5rem;
    font-weight: 800;
}

.order-detail-modal-header p:not(.order-detail-modal-eyebrow) {
    margin: 5px 0 0;
    color: #64748b;
    font-size: 0.9rem;
}

.order-detail-modal-close {
    flex: 0 0 auto;
    width: 36px;
    height: 36px;
    border: 0;
    border-radius: 50%;
    background: #f1f5f9;
    color: #475569;
    font-size: 1.4rem;
    line-height: 1;
    cursor: pointer;
    transition: background 0.2s ease, color 0.2s ease;
}

.order-detail-modal-close:hover {
    background: #e2e8f0;
    color: #0f172a;
}

.order-detail-modal-close:focus-visible,
.order-modal-action-btn:focus-visible {
    outline: 3px solid rgba(124, 58, 237, 0.24);
    outline-offset: 2px;
}

.order-detail-modal-body {
    flex: 1;
    min-height: 240px;
    overflow-y: auto;
    padding: 24px 28px;
    background: #f8fafc;
}

.order-detail-modal-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    padding: 18px 28px;
    border-top: 1px solid #e2e8f0;
    background: #f8fafc;
}

.order-detail-modal-loading,
.order-detail-modal-error {
    min-height: 220px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
    border: 1px dashed #cbd5e1;
    border-radius: 18px;
    background: #ffffff;
    color: #64748b;
    font-weight: 700;
}

.order-detail-modal-error {
    border-color: #fecaca;
    background: #fef2f2;
    color: #b91c1c;
}

.order-modal-detail {
    color: #0f172a;
}

.order-modal-alert {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 18px;
    padding: 13px 15px;
    border: 1px solid transparent;
    border-radius: 14px;
    font-weight: 700;
}

.order-modal-alert-success {
    border-color: #bbf7d0;
    background: #f0fdf4;
    color: #166534;
}

.order-modal-alert-error {
    border-color: #fecaca;
    background: #fef2f2;
    color: #991b1b;
}

.order-modal-overview {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 20px;
    margin-bottom: 18px;
    padding: 20px 22px;
    border: 1px solid #e2e8f0;
    border-radius: 18px;
    background: #ffffff;
}

.order-modal-order-id {
    margin: 0;
    color: #0f172a;
    font-size: 1.35rem;
    font-weight: 800;
}

.order-modal-overview-meta {
    margin: 5px 0 0;
    color: #64748b;
    font-size: 0.88rem;
}

.order-modal-status {
    display: inline-flex;
    align-items: center;
    width: fit-content;
    padding: 6px 11px;
    border-radius: 999px;
    background: #e2e8f0;
    color: #334155;
    font-size: 0.78rem;
    font-weight: 800;
    white-space: nowrap;
}

.order-modal-status.status-pending { background: #fef3c7; color: #92400e; }
.order-modal-status.status-confirmed { background: #ede9fe; color: #5b21b6; }
.order-modal-status.status-processing { background: #dbeafe; color: #1e40af; }
.order-modal-status.status-shipping { background: #cffafe; color: #155e75; }
.order-modal-status.status-delivered,
.order-modal-status.status-paid { background: #dcfce7; color: #166534; }
.order-modal-status.status-cancelled,
.order-modal-status.status-failed,
.order-modal-status.status-refunded { background: #fee2e2; color: #991b1b; }

.order-modal-layout {
    display: grid;
    grid-template-columns: minmax(0, 1.55fr) minmax(300px, 0.85fr);
    gap: 18px;
}

.order-modal-column {
    min-width: 0;
    display: grid;
    align-content: start;
    gap: 18px;
}

.order-modal-card {
    overflow: hidden;
    border: 1px solid #e2e8f0;
    border-radius: 18px;
    background: #ffffff;
}

.order-modal-card-header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 14px;
    padding: 18px 20px;
    border-bottom: 1px solid #e2e8f0;
}

.order-modal-card-title {
    margin: 0;
    color: #0f172a;
    font-size: 1rem;
    font-weight: 800;
}

.order-modal-card-subtitle {
    margin: 4px 0 0;
    color: #64748b;
    font-size: 0.82rem;
}

.order-modal-card-icon {
    display: grid;
    place-items: center;
    flex: 0 0 auto;
    width: 38px;
    height: 38px;
    border-radius: 12px;
    background: #f1f5f9;
    color: #7c3aed;
}

.order-modal-meta-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 14px;
    padding: 18px 20px 20px;
}

.order-modal-meta-item {
    min-width: 0;
    padding: 12px 14px;
    border: 1px solid #e2e8f0;
    border-radius: 14px;
    background: #f8fafc;
}

.order-modal-meta-item.full-width {
    grid-column: 1 / -1;
}

.order-modal-meta-label {
    display: block;
    margin-bottom: 5px;
    color: #64748b;
    font-size: 0.74rem;
    font-weight: 800;
    text-transform: uppercase;
}

.order-modal-meta-value {
    overflow-wrap: anywhere;
    color: #0f172a;
    font-weight: 700;
}

.order-modal-items-wrap {
    overflow-x: auto;
}

.order-modal-items-table {
    width: 100%;
    min-width: 620px;
    border-collapse: collapse;
}

.order-modal-items-table th,
.order-modal-items-table td {
    padding: 13px 16px;
    border-bottom: 1px solid #eef2f7;
    text-align: left;
    vertical-align: middle;
}

.order-modal-items-table th {
    background: #f8fafc;
    color: #475569;
    font-size: 0.75rem;
    font-weight: 800;
    text-transform: uppercase;
}

.order-modal-items-table tbody tr:last-child td {
    border-bottom: 0;
}

.order-modal-variant {
    font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
    font-weight: 700;
}

.order-modal-price {
    font-weight: 800;
}

.order-modal-detail-list {
    padding: 10px 20px 18px;
}

.order-modal-detail-row {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 18px;
    padding: 10px 0;
    border-bottom: 1px solid #eef2f7;
}

.order-modal-detail-row:last-child {
    border-bottom: 0;
}

.order-modal-detail-row span:first-child {
    color: #64748b;
}

.order-modal-detail-row strong {
    min-width: 0;
    text-align: right;
    overflow-wrap: anywhere;
}

.order-modal-empty {
    padding: 22px;
    text-align: center;
    color: #64748b;
}

.order-modal-actions {
    display: grid;
    gap: 10px;
    padding: 18px 20px 20px;
}

.order-modal-action-form {
    margin: 0;
}

.order-modal-action-btn {
    width: 100%;
    min-height: 44px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 10px 14px;
    border: 1px solid #7c3aed;
    border-radius: 12px;
    background: #7c3aed;
    color: #ffffff;
    font: inherit;
    font-weight: 800;
    cursor: pointer;
    transition: transform 0.15s ease, box-shadow 0.15s ease, background 0.15s ease;
}

.order-modal-action-btn:hover {
    background: #6d28d9;
    box-shadow: 0 8px 20px rgba(124, 58, 237, 0.18);
}

.order-modal-action-btn.secondary {
    border-color: #cbd5e1;
    background: #ffffff;
    color: #334155;
}

.order-modal-action-btn.secondary:hover {
    background: #f8fafc;
    box-shadow: none;
}

.order-modal-action-btn.danger {
    border-color: #ef4444;
    background: #ffffff;
    color: #b91c1c;
}

.order-modal-action-btn.danger:hover {
    background: #fef2f2;
    box-shadow: none;
}

.order-modal-action-btn:disabled {
    opacity: 0.58;
    cursor: wait;
}

.order-modal-info {
    display: flex;
    gap: 10px;
    padding: 13px 14px;
    border: 1px solid #dbeafe;
    border-radius: 13px;
    background: #eff6ff;
    color: #1e40af;
    font-size: 0.85rem;
    line-height: 1.5;
}

.order-modal-info.warning {
    border-color: #fde68a;
    background: #fffbeb;
    color: #92400e;
}

.order-modal-info.success {
    border-color: #bbf7d0;
    background: #f0fdf4;
    color: #166534;
}

.order-modal-cancelled-banner {
    margin: 18px 20px 0;
    padding: 10px 12px;
    border-radius: 12px;
    background: #fee2e2;
    color: #991b1b;
    text-align: center;
    font-size: 0.8rem;
    font-weight: 900;
    letter-spacing: 0.08em;
}

.order-modal-progress {
    display: grid;
    grid-template-columns: repeat(5, minmax(0, 1fr));
    gap: 8px;
    padding: 20px;
}

.order-modal-progress-step {
    min-width: 0;
    display: grid;
    justify-items: center;
    gap: 7px;
    color: #94a3b8;
    text-align: center;
    font-size: 0.72rem;
    font-weight: 700;
}

.order-modal-progress-dot {
    display: grid;
    place-items: center;
    width: 28px;
    height: 28px;
    border: 2px solid #cbd5e1;
    border-radius: 50%;
    background: #ffffff;
    color: #64748b;
    font-weight: 800;
}

.order-modal-progress-step.is-complete {
    color: #166534;
}

.order-modal-progress-step.is-complete .order-modal-progress-dot {
    border-color: #22c55e;
    background: #dcfce7;
    color: #166534;
}

@media (max-width: 900px) {
    .order-detail-modal {
        padding: 14px;
    }

    .order-detail-modal-panel {
        max-height: calc(100vh - 28px);
    }

    .order-modal-layout {
        grid-template-columns: 1fr;
    }
}

@media (max-width: 640px) {
    .order-detail-modal {
        padding: 0;
        align-items: stretch;
    }

    .order-detail-modal-panel {
        width: 100%;
        max-height: 100vh;
        border-radius: 0;
    }

    .order-detail-modal-header,
    .order-detail-modal-body,
    .order-detail-modal-footer {
        padding-left: 18px;
        padding-right: 18px;
    }

    .order-modal-overview {
        flex-direction: column;
    }

    .order-modal-meta-grid {
        grid-template-columns: 1fr;
    }

    .order-modal-meta-item.full-width {
        grid-column: auto;
    }

    .order-modal-detail-row {
        display: grid;
        gap: 4px;
    }

    .order-modal-detail-row strong {
        text-align: left;
    }

    .order-modal-progress {
        grid-template-columns: 1fr;
    }

    .order-modal-progress-step {
        grid-template-columns: 30px 1fr;
        align-items: center;
        justify-items: start;
        text-align: left;
    }
}

    </style>
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
            transition: 0.3s; font-weight: 500;
        }
        .sidebar .nav-link:hover, .sidebar .nav-link.active {
            background: #34495e; color: #fff; border-left-color: #1abc9c;
        }
        .sidebar .nav-link i { width: 24px; margin-right: 10px; }
        .sidebar .nav { display: flex; flex-direction: column; min-height: calc(100vh - 130px); }
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
        .card { border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
        .card-header { background: #f8f9fa; font-weight: 600; }
        .table th { background: #f1f3f5; border-top: none; }
        .hidden-section { display: none; }
        .search-form { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
        .search-form .form-control { width: auto; min-width: 220px; }
        .status-badge {
            display: inline-block; padding: 6px 12px; border-radius: 999px;
            font-weight: 600; font-size: 0.85rem;
        }
        .status-pending { background: #fef3c7; color: #92400e; }
        .status-processing { background: #dbeafe; color: #1e40af; }
        .status-confirmed { background: #e0e7ff; color: #3730a3; }
        .status-shipping { background: #cffafe; color: #155e75; }
        .status-delivered { background: #dcfce7; color: #166534; }
        .status-cancelled { background: #fee2e2; color: #991b1b; }
        @media (max-width: 768px) {
            .sidebar { min-height: auto; height: auto; }
            .main-content { padding: 15px; }
            .stat-card .stat-number { font-size: 1.5rem; }
        }
    </style>
</head>
<body>
<div class="container-fluid p-0">
    <div class="row g-0">
        <!-- Sidebar -->
        <jsp:include page="/views/common/staffSidebar.jsp">
            <jsp:param name="activeMenu" value="orders" />
        </jsp:include>

        <!-- Main Content -->
        <div class="col-md-9 col-lg-10 main-content">

            <c:set var="pagePendingOrders" value="${0}" />
            <c:set var="pageShippingOrders" value="${0}" />
            <c:set var="pageDeliveredOrders" value="${0}" />
            <c:set var="pageProcessingOrders" value="${0}" />
            <c:forEach var="order" items="${listOrders}">
                <c:if test="${order.orderStatus eq 'Pending'}">
                    <c:set var="pagePendingOrders" value="${pagePendingOrders + 1}" />
                </c:if>
                <c:if test="${order.orderStatus eq 'Processing'}">
                    <c:set var="pageProcessingOrders" value="${pageProcessingOrders + 1}" />
                </c:if>
                <c:if test="${order.orderStatus eq 'Shipping'}">
                    <c:set var="pageShippingOrders" value="${pageShippingOrders + 1}" />
                </c:if>
                <c:if test="${order.orderStatus eq 'Delivered'}">
                    <c:set var="pageDeliveredOrders" value="${pageDeliveredOrders + 1}" />
                </c:if>
            </c:forEach>

            <!-- Top stat cards -->
            <div class="row mb-4 g-3">
                <div class="col-md-3 col-6">
                    <div class="stat-card">
                        <div class="stat-label">Total Records</div>
                        <div class="stat-number">${empty totalOrders ? fn:length(listOrders) : totalOrders}</div>
                    </div>
                </div>
                <div class="col-md-3 col-6">
                    <div class="stat-card" style="border-left-color: #f59e0b;">
                        <div class="stat-label">Pending</div>
                        <div class="stat-number">${pagePendingOrders}</div>
                    </div>
                </div>
                <div class="col-md-3 col-6">
                    <div class="stat-card" style="border-left-color: #3498db;">
                        <div class="stat-label">Shipping</div>
                        <div class="stat-number">${pageShippingOrders}</div>
                    </div>
                </div>
                <div class="col-md-3 col-6">
                    <div class="stat-card" style="border-left-color: #27ae60;">
                        <div class="stat-label">Delivered</div>
                        <div class="stat-number">${pageDeliveredOrders}</div>
                    </div>
                </div>
            </div>

            <!-- Order Records -->
            <div class="card">
                <div class="card-header d-flex justify-content-between align-items-center flex-wrap">
                    <span>Order Records</span>
                </div>
                <div class="card-body">
                    <form class="search-form mb-3" method="get" action="${pageContext.request.contextPath}/staff/orders">
                        <input type="text" name="keyword" value="${fn:escapeXml(keyword)}"
                               class="form-control" placeholder="Order ID, customer ID, phone, address, or status"/>
                        <select name="status" class="form-select" style="width: auto;">
                            <option value="">All Statuses</option>
                            <option value="Pending" ${status eq 'Pending' ? 'selected' : ''}>Pending</option>
                            <option value="Confirmed" ${status eq 'Confirmed' ? 'selected' : ''}>Confirmed</option>
                            <option value="Processing" ${status eq 'Processing' ? 'selected' : ''}>Processing</option>
                            <option value="Shipping" ${status eq 'Shipping' ? 'selected' : ''}>Shipping</option>
                            <option value="Delivered" ${status eq 'Delivered' ? 'selected' : ''}>Delivered</option>
                            <option value="Cancelled" ${status eq 'Cancelled' ? 'selected' : ''}>Cancelled</option>
                        </select>
                        <input type="date" name="dateFrom" value="${fn:escapeXml(dateFrom)}" class="form-control" style="width: auto;" title="From date"/>
                        <input type="date" name="dateTo" value="${fn:escapeXml(dateTo)}" class="form-control" style="width: auto;" title="To date"/>
                        <button type="submit" class="btn btn-primary">Apply</button>
                        <c:if test="${not empty keyword or not empty status or not empty dateFrom or not empty dateTo}">
                            <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/staff/orders">
                                Clear
                            </a>
                        </c:if>
                    </form>

                    <c:choose>
                        <c:when test="${empty listOrders}">
                            <div class="text-center text-muted py-5">
                                <p class="mb-0">No orders found. Try another search term.</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="table-responsive">
                                <table class="table table-hover table-striped align-middle">
                                    <thead>
                                        <tr>
                                            <th>Order</th>
                                            <th>Customer</th>
                                            <th>Placed At</th>
                                            <th>Contact</th>
                                            <th>Status</th>
                                            <th class="text-end">Total</th>
                                            <th class="text-end">Action</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="order" items="${listOrders}">
                                            <tr>
                                                <td>
                                                    <div class="fw-semibold"><code>${order.orderId}</code></div>
                                                    <small class="text-muted">${order.shippingAddress}</small>
                                                </td>
                                                <td>${order.customerId}</td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${not empty order.placedAt}">
                                                            ${fn:replace(order.placedAt, 'T', ' ')}
                                                        </c:when>
                                                        <c:otherwise>-</c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${not empty order.phone}">${order.phone}</c:when>
                                                        <c:otherwise>-</c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <span class="status-badge status-${fn:toLowerCase(order.orderStatus)}">
                                                        ${order.orderStatus}
                                                    </span>
                                                </td>
                                                <td class="text-end fw-semibold">
                                                    <fmt:formatNumber value="${order.totalAmount}" type="number" groupingUsed="true" /> đ
                                                </td>
                                                <td class="text-end">
                                                    <button type="button" class="btn btn-sm btn-primary js-open-order-modal"
                                                            data-order-id="<c:out value='${order.orderId}' />">
                                                        Manage
                                                    </button>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <c:if test="${not empty totalPages and totalPages > 1}">
                        <nav class="mt-3">
                            <ul class="pagination justify-content-center flex-wrap pagination-sm">
                                <c:url var="previousPageUrl" value="/staff/orders">
                                    <c:param name="page" value="${currentPage - 1}" />
                                    <c:param name="pageSize" value="${pageSize}" />
                                    <c:param name="keyword" value="${keyword}" />
                                    <c:param name="status" value="${status}" />
                                    <c:param name="dateFrom" value="${dateFrom}" />
                                    <c:param name="dateTo" value="${dateTo}" />
                                </c:url>
                                <c:url var="nextPageUrl" value="/staff/orders">
                                    <c:param name="page" value="${currentPage + 1}" />
                                    <c:param name="pageSize" value="${pageSize}" />
                                    <c:param name="keyword" value="${keyword}" />
                                    <c:param name="status" value="${status}" />
                                    <c:param name="dateFrom" value="${dateFrom}" />
                                    <c:param name="dateTo" value="${dateTo}" />
                                </c:url>
                                <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                                    <a class="page-link" href="${previousPageUrl}">Prev</a>
                                </li>
                                <c:forEach var="i" begin="1" end="${totalPages}">
                                    <li class="page-item ${i == currentPage ? 'active' : ''}">
                                        <a class="page-link" href="?page=${i}&pageSize=${pageSize}&keyword=${fn:escapeXml(keyword)}&status=${status}&dateFrom=${dateFrom}&dateTo=${dateTo}">${i}</a>
                                    </li>
                                </c:forEach>
                                <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                                    <a class="page-link" href="${nextPageUrl}">Next</a>
                                </li>
                            </ul>
                            <div class="text-center text-muted">Page ${currentPage} of ${totalPages}</div>
                        </nav>
                    </c:if>
                </div>
            </div>

        </div><!-- end main-content -->
    </div><!-- end row -->
</div><!-- end container-fluid -->

<div id="orderDetailModal" class="order-detail-modal" aria-hidden="true"
     data-open-order="<c:out value='${param.openOrder}' />">
    <div class="order-detail-modal-overlay" data-close-order-modal></div>
    <section class="order-detail-modal-panel" role="dialog" aria-modal="true"
             aria-labelledby="orderDetailModalTitle">
        <header class="order-detail-modal-header">
            <div>
                <p class="order-detail-modal-eyebrow">Order Management</p>
                <h3 id="orderDetailModalTitle">Order Detail</h3>
            </div>
            <button type="button" class="order-detail-modal-close" data-close-order-modal
                    aria-label="Close order detail">&times;</button>
        </header>
        <div id="orderDetailModalBody" class="order-detail-modal-body" aria-live="polite">
            <div class="order-detail-modal-loading">
                <i class="fas fa-spinner fa-spin" aria-hidden="true"></i>
                <span>Loading order details...</span>
            </div>
        </div>
        <footer class="order-detail-modal-footer">
            <button type="button" class="btn btn-outline-secondary" data-close-order-modal>Close</button>
        </footer>
    </section>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
(function () {
    'use strict';

    var modal = document.getElementById('orderDetailModal');
    var modalBody = document.getElementById('orderDetailModalBody');
    var modalTitle = document.getElementById('orderDetailModalTitle');
    var contextPath = '${pageContext.request.contextPath}';
    var activeOrderId = '';
    var returnFocus = null;
    var orderChanged = false;

    if (!modal || !modalBody) {
        return;
    }

    function loadingMarkup() {
        return '<div class="order-detail-modal-loading">'
            + '<i class="fas fa-spinner fa-spin" aria-hidden="true"></i>'
            + '<span>Loading order details...</span></div>';
    }

    function errorMarkup(message) {
        var wrapper = document.createElement('div');
        wrapper.className = 'order-detail-modal-error';
        var icon = document.createElement('i');
        icon.className = 'fas fa-triangle-exclamation';
        icon.setAttribute('aria-hidden', 'true');
        var text = document.createElement('span');
        text.textContent = message || 'Unable to load this order.';
        wrapper.appendChild(icon);
        wrapper.appendChild(text);
        modalBody.textContent = '';
        modalBody.appendChild(wrapper);
    }

    function cleanOpenOrderFromUrl() {
        try {
            var url = new URL(window.location.href);
            if (url.searchParams.has('openOrder')) {
                url.searchParams.delete('openOrder');
                window.history.replaceState({}, '', url.pathname + (url.search ? url.search : '') + url.hash);
            }
        } catch (ignore) {
        }
    }

    function loadOrder(orderId) {
        activeOrderId = orderId;
        modalTitle.textContent = 'Order #' + orderId;
        modalBody.innerHTML = loadingMarkup();

        return fetch(contextPath + '/staff/order-detail?modal=1&orderId=' + encodeURIComponent(orderId), {
            method: 'GET',
            headers: {
                'X-Requested-With': 'XMLHttpRequest',
                'Accept': 'text/html'
            },
            cache: 'no-store'
        }).then(function (response) {
            if (!response.ok && response.status !== 404) {
                throw new Error('Unable to load this order.');
            }
            return response.text();
        }).then(function (html) {
            modalBody.innerHTML = html;
            var detail = modalBody.querySelector('.order-modal-detail');
            if (detail && detail.getAttribute('data-order-id')) {
                modalTitle.textContent = 'Order #' + detail.getAttribute('data-order-id');
            }
        }).catch(function (error) {
            errorMarkup(error.message);
        });
    }

    function openOrderModal(orderId, trigger) {
        if (!orderId) {
            return;
        }
        returnFocus = trigger || document.activeElement;
        modal.classList.add('open');
        modal.setAttribute('aria-hidden', 'false');
        document.body.classList.add('order-modal-open');
        loadOrder(orderId);
        window.setTimeout(function () {
            var closeButton = modal.querySelector('.order-detail-modal-close');
            if (closeButton) {
                closeButton.focus();
            }
        }, 0);
    }

    function closeOrderModal() {
        modal.classList.remove('open');
        modal.setAttribute('aria-hidden', 'true');
        document.body.classList.remove('order-modal-open');
        if (returnFocus && typeof returnFocus.focus === 'function') {
            returnFocus.focus();
        }
        if (orderChanged) {
            window.location.reload();
        }
    }

    document.querySelectorAll('.js-open-order-modal').forEach(function (button) {
        button.addEventListener('click', function () {
            openOrderModal(button.getAttribute('data-order-id'), button);
        });
    });

    modal.querySelectorAll('[data-close-order-modal]').forEach(function (element) {
        element.addEventListener('click', closeOrderModal);
    });

    modalBody.addEventListener('submit', function (event) {
        var form = event.target.closest('.order-modal-action-form');
        if (!form) {
            return;
        }

        event.preventDefault();
        var confirmMessage = form.getAttribute('data-confirm');
        if (confirmMessage && !window.confirm(confirmMessage)) {
            return;
        }

        var submitButton = form.querySelector('button[type="submit"]');
        if (submitButton) {
            submitButton.disabled = true;
        }

        // Send staff order actions as application/x-www-form-urlencoded.
        // The servlets read values through request.getParameter(...), so using
        // multipart FormData here can make orderId/newStatus unavailable unless
        // the servlet is configured with @MultipartConfig.
        var formData = new FormData(form);
        var submittedOrderId = formData.get('orderId');
        if ((!submittedOrderId || String(submittedOrderId).trim() === '') && activeOrderId) {
            formData.set('orderId', activeOrderId);
            submittedOrderId = activeOrderId;
        }
        var submittedNewStatus = formData.get('newStatus');
        var requestBody = new URLSearchParams();
        formData.forEach(function (value, key) {
            requestBody.append(key, value);
        });

        fetch(form.action, {
            method: 'POST',
            body: requestBody,
            headers: {
                'X-Requested-With': 'XMLHttpRequest',
                'X-Order-Id': submittedOrderId || activeOrderId || '',
                'X-Order-Status': submittedNewStatus || '',
                'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
                'Accept': 'application/json'
            }
        }).then(function (response) {
            return response.json().catch(function () {
                throw new Error('The server returned an invalid response.');
            }).then(function (payload) {
                if (!response.ok) {
                    throw new Error(payload.message || 'Unable to update this order.');
                }

                // Business-rule failures are returned as JSON with
                // success=false. Do not mark the order as changed; reload the
                // popup so the server-side flash message explains the reason.
                if (!payload.success) {
                    return loadOrder(activeOrderId);
                }

                orderChanged = true;
                return loadOrder(activeOrderId);
            });
        }).catch(function (error) {
            errorMarkup(error.message);
        }).finally(function () {
            if (submitButton && document.body.contains(submitButton)) {
                submitButton.disabled = false;
            }
        });
    });

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && modal.classList.contains('open')) {
            closeOrderModal();
        }
    });

    var initialOrderId = modal.getAttribute('data-open-order');
    if (initialOrderId) {
        cleanOpenOrderFromUrl();
        openOrderModal(initialOrderId, null);
    }
})();
</script>
</body>
</html>
