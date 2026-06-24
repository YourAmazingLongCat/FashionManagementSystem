<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Delete Color</title>
        <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/views/pages/productManagement/product-management.css?v=20260609-colors-delete">
        <style>
            body { margin: 0; font-family: 'Inter', sans-serif; color: #0f172a; background: linear-gradient(135deg, #f8fafc 0%, #eef2ff 100%); min-height: 100vh; }
            .form-shell { width: min(980px, calc(100% - 40px)); margin: 28px auto; }
            .form-panel { background: #ffffff; border: 1px solid rgba(226, 232, 240, 0.9); border-radius: 24px; box-shadow: 0 16px 40px rgba(15, 23, 42, 0.1); overflow: hidden; contain: content; }
            .form-hero { display: flex; align-items: flex-start; justify-content: space-between; gap: 20px; padding: 30px; background: linear-gradient(135deg, #fff7f7 0%, #ffe4e6 100%); border-bottom: 1px solid #fecdd3; }
            .eyebrow { margin: 0 0 10px; text-transform: uppercase; letter-spacing: 0.18em; font-size: 0.74rem; font-weight: 700; color: #dc2626; }
            .form-panel h1 { margin: 0; font-size: 2.15rem; line-height: 1.12; }
            .form-subtitle { margin: 14px 0 0; color: #64748b; line-height: 1.7; max-width: 620px; }
            .danger-chip { display: inline-flex; align-items: center; justify-content: center; padding: 12px 16px; border-radius: 18px; background: #ffffff; border: 1px solid #fecaca; box-shadow: 0 16px 30px rgba(220, 38, 38, 0.12); font-weight: 800; color: #b91c1c; }
            .form-body { padding: 30px; display: grid; gap: 24px; }
            .warning-banner { padding: 16px 18px; border-radius: 18px; background: rgba(220, 38, 38, 0.08); border: 1px solid rgba(220, 38, 38, 0.18); color: #991b1b; font-weight: 600; }
            .info-strip { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; }
            .info-card { background: #fffaf9; border: 1px solid #fee2e2; border-radius: 22px; padding: 18px; }
            .info-card span { display: block; margin-bottom: 8px; color: #64748b; font-size: 0.86rem; font-weight: 600; }
            .info-card strong { font-size: 1rem; color: #0f172a; }
            .delete-card { background: linear-gradient(180deg, #ffffff 0%, #fffafa 100%); border: 1px solid #fee2e2; border-radius: 26px; padding: 24px; display: grid; gap: 16px; }
            .delete-row { display: flex; align-items: center; justify-content: space-between; gap: 20px; padding-bottom: 14px; border-bottom: 1px solid #fee2e2; }
            .delete-row:last-child { border-bottom: none; padding-bottom: 0; }
            .delete-row span { color: #64748b; font-weight: 700; }
            .delete-row strong { text-align: right; }
            .color-meta { display: inline-flex; align-items: center; gap: 10px; }
            .swatch { width: 22px; height: 22px; border-radius: 999px; border: 1px solid rgba(15,23,42,0.12); background: ${empty color.hexCode ? '#000000' : color.hexCode}; }
            .form-actions { display: flex; justify-content: space-between; align-items: center; gap: 12px; }
            .action-note { color: #64748b; font-size: 0.9rem; }
            .action-buttons { display: flex; gap: 12px; flex-wrap: wrap; }
            .ghost-btn, .danger-btn { display: inline-flex; align-items: center; justify-content: center; padding: 12px 18px; border-radius: 14px; font-weight: 700; text-decoration: none; border: none; cursor: pointer; transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease; }
            .ghost-btn { background: #ffffff; color: #334155; border: 1px solid #dbe3f0; }
            .danger-btn { background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%); color: #ffffff; box-shadow: 0 18px 30px rgba(220, 38, 38, 0.22); }
            .ghost-btn:hover, .danger-btn:hover { transform: translateY(-2px); }
            @media (max-width: 900px) { .info-strip { grid-template-columns: 1fr; } }
            @media (max-width: 768px) { .form-shell { width: min(100% - 20px, 100%); margin: 10px auto; } .form-hero, .delete-row, .form-actions { flex-direction: column; align-items: stretch; } .action-buttons { width: 100%; } .form-body, .form-hero { padding: 20px; } }
        </style>
    </head>
    <body>
        <div class="form-shell">
            <section class="form-panel">
                <div class="form-hero">
                    <div>
                        <p class="eyebrow">Admin / Color</p>
                        <h1>Delete Color</h1>
                        <p class="form-subtitle">You are about to remove this color from the catalog. If it is already referenced by product variants, the delete may be blocked by the database.</p>
                    </div>
                    <div class="danger-chip">Permanent action</div>
                </div>

                <div class="form-body">
                    <div class="warning-banner">Please review the color details carefully before confirming this action.</div>

                    <div class="info-strip">
                        <div class="info-card">
                            <span>Color ID</span>
                            <strong>${color.colorId}</strong>
                        </div>
                        <div class="info-card">
                            <span>Color Name</span>
                            <strong>${color.colorName}</strong>
                        </div>
                        <div class="info-card">
                            <span>Hex</span>
                            <strong class="color-meta"><span class="swatch"></span>${color.hexCode}</strong>
                        </div>
                    </div>

                    <div class="delete-card">
                        <div class="delete-row">
                            <span>Color ID</span>
                            <strong>${color.colorId}</strong>
                        </div>
                        <div class="delete-row">
                            <span>Color Name</span>
                            <strong>${color.colorName}</strong>
                        </div>
                        <div class="delete-row">
                            <span>Hex Code</span>
                            <strong class="color-meta"><span class="swatch"></span>${color.hexCode}</strong>
                        </div>
                    </div>

                    <form method="post" action="${pageContext.request.contextPath}/staff/products" class="form-actions">
                        <input type="hidden" name="action" value="deleteColor">
                        <input type="hidden" name="colorId" value="${color.colorId}">
                        <span class="action-note">This action cannot be undone.</span>
                        <div class="action-buttons">
                            <a class="ghost-btn" href="${pageContext.request.contextPath}/staff/products?tab=colors">Cancel</a>
                            <button type="submit" class="danger-btn">Delete color</button>
                        </div>
                    </form>
                </div>
            </section>
        </div>
    </body>
</html>
