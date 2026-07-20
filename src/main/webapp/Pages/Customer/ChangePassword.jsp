<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<style>
    header.header-guest-page {
        display: none !important;
    }
    .password-container {
        max-width: 560px;
        margin: 100px auto 60px;
        background: #ffffff;
        padding: 40px;
        border-radius: 24px;
        box-shadow: 0 22px 70px rgba(15, 23, 42, 0.12);
        text-align: center;
    }

    .password-header h2 {
        margin: 0 0 30px;
        text-transform: uppercase;
        letter-spacing: 0.08em;
        font-weight: 800;
        color: #111827;
    }

    .info-group {
        margin-bottom: 20px;
        text-align: left;
    }

    .info-group label {
        display: block;
        font-size: 0.75rem;
        color: #6b7280;
        text-transform: uppercase;
        margin-bottom: 8px;
        font-weight: 700;
        letter-spacing: 0.08em;
    }

    .info-group input {
        width: 100%;
        padding: 14px 16px;
        background: #f9fafb;
        border: 1px solid #d1d5db;
        font-size: 0.95rem;
        color: #111827;
        border-radius: 14px;
        box-sizing: border-box;
        outline: none;
        transition: border-color 0.2s ease, box-shadow 0.2s ease;
    }

    .info-group input:focus {
        border-color: #fb923c;
        box-shadow: 0 0 0 4px rgba(251, 146, 60, 0.12);
    }

    .btn-save {
        background-color: #111827;
        color: #ffffff;
        padding: 16px 0;
        border: none;
        border-radius: 14px;
        text-transform: uppercase;
        font-weight: 800;
        letter-spacing: 0.14em;
        cursor: pointer;
        width: 100%;
        margin-top: 20px;
        transition: transform 0.2s ease, background-color 0.2s ease;
    }

    .btn-save:hover {
        background-color: #1f2937;
        transform: translateY(-1px);
    }

    .error-msg {
        color: #dc2626;
        font-size: 0.95rem;
        font-weight: 700;
        margin-bottom: 16px;
        text-align: left;
    }

    .success-msg {
        color: #16a34a;
        font-size: 0.95rem;
        font-weight: 700;
        margin-bottom: 16px;
        text-align: left;
    }
    .page-actions {
        width: 100%;
        margin: 24px 0 0;
        display: flex;
        justify-content: center;
        align-items: center;
        gap: 12px;
    }
    .page-actions .btn-save {
        padding: 14px 26px;
        border-radius: 999px;
        white-space: nowrap;
        display: block;
        width: 90%;
        max-width: 560px;
        margin: 0 auto !important;
    }
    .btn-logout {
        background: #ef4444;
    }
    .btn-logout:hover {
        background: #dc2626;
    }
</style>

<c:set var="backUrl" value="${pageContext.request.contextPath}/home" />
<c:if test="${sessionScope.USER.role eq 'Staff'}">
    <c:set var="backUrl" value="${pageContext.request.contextPath}/staff/products" />
</c:if>
<c:if test="${sessionScope.USER.role eq 'Admin'}">
    <c:set var="backUrl" value="${pageContext.request.contextPath}/Admin" />
</c:if>
<div class="page-actions">
    <a class="btn-save" href="${backUrl}" style="background:#111827; margin-top:0;">Back to Home</a>
</div>
<div class="password-container">
    <div class="password-header" style="display:flex; justify-content:flex-start; align-items:center; gap:12px; margin-bottom: 24px;">
        <h2>CHANGE PASSWORD</h2>
    </div>

    <c:if test="${not empty requestScope.error}">
        <div class="error-msg">${requestScope.error}</div>
    </c:if>
    <c:if test="${not empty requestScope.success}">
        <div class="success-msg">${requestScope.success}</div>
    </c:if>

    <form action="${pageContext.request.contextPath}/change-password" method="post">
        <div class="info-group">
            <label>CURRENT PASSWORD</label>
            <input type="password" name="oldPassword" placeholder="Enter current password" required>
        </div>

        <div class="info-group">
            <label>NEW PASSWORD</label>
            <input type="password" name="newPassword" placeholder="Enter new password" required>
        </div>

        <div class="info-group">
            <label>CONFIRM NEW PASSWORD</label>
            <input type="password" name="confirmPassword" placeholder="Confirm new password" required>
        </div>

        <button type="submit" class="btn-save">UPDATE PASSWORD</button>
    </form>
</div>