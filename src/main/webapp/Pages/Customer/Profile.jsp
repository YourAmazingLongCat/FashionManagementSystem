<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<style>
    body { background: #f3f4f6; }
    header.header-guest-page {
        display: none !important;
    }
    .account-page {
        width: min(960px, calc(100% - 40px));
        margin: 60px auto 40px;
    }
    .account-title {
        text-align: center;
        font-size: 2.6rem;
        font-weight: 800;
        color: #111827;
        margin-bottom: 28px;
    }
    .account-card {
        display: grid;
        grid-template-columns: 1fr;
        gap: 24px;
        max-width: 700px;
        margin: 0 auto;
        background: #ffffff;
        border: 1px solid #e5e7eb;
        border-radius: 24px;
        box-shadow: 0 24px 80px rgba(15, 23, 42, 0.08);
        overflow: hidden;
    }
    .account-panel {
        padding: 32px;
    }
    .panel-heading {
        display: block;
        margin-bottom: 8px;
        text-align: center;
    }
    .panel-heading h3 {
        margin: 0 0 6px 0;
        font-size: 1.05rem;
        text-transform: uppercase;
        letter-spacing: 0.16em;
        color: #111827;
    }
    .panel-heading .status-pill { display: none; }

    .actions-wrapper {
        display: flex;
        flex-direction: column;
        gap: 12px;
        align-items: center;
        margin: 12px 0;
    }
    .actions-wrapper .action-pill {
        width: 90%;
        max-width: 640px;
        padding: 14px 20px;
        border-radius: 999px;
        text-align: center;
        text-decoration: none;
        font-weight: 700;
    }
    .actions-wrapper .action-pill.primary { background: #eef2ff; color: #1d4ed8; }
    .actions-wrapper .action-pill.secondary { background: #111827; color: #fff; }
    .history-list {
        list-style: none;
        padding: 0;
        margin: 0;
        display: grid;
        gap: 14px;
    }
    .history-item {
        padding: 16px 18px;
        border-radius: 18px;
        background: #f8fafc;
        border: 1px solid #e5e7eb;
        color: #475569;
        min-height: 68px;
    }
    .history-item span {
        display: block;
        color: #0f172a;
        font-weight: 700;
        margin-bottom: 6px;
    }
    .account-detail {
        display: grid;
        gap: 18px;
    }
    .detail-row {
        display: grid;
        grid-template-columns: 1fr;
        gap: 6px;
    }
    .detail-label {
        font-size: 0.75rem;
        font-weight: 700;
        text-transform: uppercase;
        color: #6b7280;
        letter-spacing: 0.12em;
    }
    .detail-value,
    .detail-input {
        width: 100%;
        border-radius: 16px;
        border: 1px solid #d1d5db;
        background: #f8fafc;
        color: #111827;
        padding: 14px 16px;
        font-size: 0.95rem;
    }
    .detail-input { background: #ffffff; }
    .detail-value { cursor: default; }
    .account-actions {
        display: flex;
        flex-wrap: wrap;
        gap: 12px;
        margin-top: 16px;
    }
    .btn-primary,
    .btn-secondary,
    .btn-logout {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        gap: 8px;
        width: 100%;
        padding: 14px 20px;
        border-radius: 999px;
        font-weight: 700;
        text-decoration: none;
        border: none;
        cursor: pointer;
        transition: transform 0.2s ease, background-color 0.2s ease;
    }
    .btn-primary { background: #111827; color: #ffffff; }
    .btn-primary:hover { background: #1f2937; transform: translateY(-1px); }
    .btn-secondary { background: #eef2ff; color: #1d4ed8; }
    .btn-secondary:hover { background: #dbeafe; transform: translateY(-1px); }
    .btn-logout { background: #ef4444; color: #ffffff; }
    .btn-logout:hover { background: #dc2626; transform: translateY(-1px); }
    @media (max-width: 880px) {
        .account-card { grid-template-columns: 1fr; }
    }
</style>

<div class="account-page">
    <h1 class="account-title">Account</h1>
    <div class="account-card">
        <section class="account-panel">
            <c:set var="homeUrl" value="${pageContext.request.contextPath}/home" />
            <c:if test="${sessionScope.USER.role eq 'Staff'}">
                <c:set var="homeUrl" value="${pageContext.request.contextPath}/staff/products" />
            </c:if>
            <c:if test="${sessionScope.USER.role eq 'Admin'}">
                <c:set var="homeUrl" value="${pageContext.request.contextPath}/Admin" />
            </c:if>
            <div class="panel-heading">
                <h3>Account details</h3>
            </div>
            <div class="actions-wrapper">
                <a class="action-pill action-pill-primary action-pill primary" href="${pageContext.request.contextPath}/change-password">Change Password</a>
                <a class="action-pill action-pill-secondary action-pill secondary" href="${homeUrl}">Back to Home</a>
            </div>
            <form action="${pageContext.request.contextPath}/profile/update" method="post" class="account-detail">
                <div class="detail-row">
                    <label class="detail-label">Full name</label>
                    <input class="detail-input" type="text" name="fullName" value="${sessionScope.USER.fullName}" required>
                </div>
                <div class="detail-row">
                    <label class="detail-label">Email</label>
                    <div class="detail-value">${sessionScope.USER.email}</div>
                </div>
                <div class="detail-row">
                    <label class="detail-label">Phone</label>
                    <input class="detail-input" type="text" name="phone" value="${sessionScope.USER.phone}">
                </div>
                <c:if test="${sessionScope.USER.role ne 'Staff' && sessionScope.USER.role ne 'Admin'}">
                    <div class="detail-row">
                        <label class="detail-label">Shipping address</label>
                        <input class="detail-input" type="text" name="address" value="${sessionScope.USER.address}">
                    </div>
                </c:if>
                <div class="account-actions">
                    <button type="submit" class="btn-primary">Save Change</button>
                    <a class="btn-logout" href="${pageContext.request.contextPath}/auth/logout">Log Out</a>
                </div>
            </form>
        </section>
    </div>
</div>
