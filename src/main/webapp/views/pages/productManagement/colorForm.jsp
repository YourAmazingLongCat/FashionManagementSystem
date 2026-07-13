<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>${pageTitle}</title>
        <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/views/pages/productManagement/product-management.css?v=20260609-friendly-colors-en">
        <style>
            body { margin: 0; font-family: 'Inter', sans-serif; color: #0f172a; background: linear-gradient(135deg, #f8fafc 0%, #eef2ff 100%); min-height: 100vh; }
            .form-shell { width: min(1080px, calc(100% - 40px)); margin: 28px auto; }
            .form-panel { background: #ffffff; border: 1px solid rgba(226, 232, 240, 0.9); border-radius: 24px; box-shadow: 0 16px 40px rgba(15, 23, 42, 0.1); overflow: hidden; contain: content; }
            .form-hero { padding: 30px; background: #ffffff; border-bottom: 1px solid #e2e8f0; }
            .eyebrow { margin: 0 0 10px; text-transform: uppercase; letter-spacing: 0.18em; font-size: 0.74rem; font-weight: 700; color: #7c3aed; }
            .form-panel h1 { margin: 0; font-size: 2.15rem; line-height: 1.12; }
            .form-body { padding: 30px; display: grid; gap: 24px; }
            .alert { padding: 16px 18px; border-radius: 18px; font-weight: 600; background: rgba(220, 38, 38, 0.12); color: #991b1b; border: 1px solid rgba(220, 38, 38, 0.2); }
            .color-form { display: grid; gap: 24px; }
            .form-section { background: linear-gradient(180deg, #ffffff 0%, #fbfbff 100%); border: 1px solid #e2e8f0; border-radius: 26px; padding: 24px; }
            .section-heading { margin-bottom: 20px; }
            .section-heading h3 { margin: 0; font-size: 1.15rem; }
            .section-heading p { margin: 6px 0 0; color: #64748b; }
            .form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 20px; }
            .form-group { display: grid; gap: 10px; }
            label { font-weight: 700; color: #334155; }
            input { width: 100%; padding: 14px 16px; border-radius: 16px; border: 1px solid #dbe3f0; background: #ffffff; color: #0f172a; font: inherit; box-sizing: border-box; transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease; }
            input:focus { outline: none; border-color: #7c3aed; box-shadow: 0 0 0 4px rgba(124, 58, 237, 0.12); transform: translateY(-1px); }
            .color-picker-wrap { display: flex; gap: 16px; align-items: center; }
            .color-picker { width: 88px; height: 64px; padding: 0; border-radius: 18px; overflow: hidden; cursor: pointer; }
            .color-preview { display: flex; align-items: center; gap: 14px; padding: 16px; border-radius: 18px; border: 1px solid #dbe3f0; background: #ffffff; }
            .swatch { width: 56px; height: 56px; border-radius: 18px; border: 1px solid rgba(15,23,42,0.12); background: #000000; }
            .swatch-note strong { display: block; font-size: 1rem; }
            .swatch-note span { color: #64748b; font-size: 0.9rem; }
            .form-actions { display: flex; justify-content: flex-end; align-items: center; gap: 12px; padding-top: 6px; }
            .primary-btn, .ghost-btn { display: inline-flex; align-items: center; justify-content: center; padding: 12px 18px; border-radius: 14px; font-weight: 700; text-decoration: none; border: none; cursor: pointer; transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease; }
            .primary-btn { background: linear-gradient(135deg, #7c3aed 0%, #4f46e5 100%); color: #ffffff; box-shadow: 0 18px 30px rgba(124, 58, 237, 0.22); }
            .ghost-btn { background: #ffffff; color: #334155; border: 1px solid #dbe3f0; }
            .primary-btn:hover, .ghost-btn:hover { transform: translateY(-2px); }
            @media (max-width: 768px) { .form-shell { width: min(100% - 20px, 100%); margin: 10px auto; } .form-grid { grid-template-columns: 1fr; } .color-picker-wrap, .form-actions { flex-direction: column; align-items: stretch; } .form-body, .form-section, .form-hero { padding: 20px; } }
        </style>
    </head>
    <body>
        <div class="form-shell">
            <section class="form-panel">
                <div class="form-hero">
                    <p class="eyebrow">Admin / Colors</p>
                    <h1>${pageTitle}</h1>
                </div>

                <div class="form-body">
                    <c:if test="${not empty error}">
                        <div class="alert">${error}</div>
                    </c:if>

                    <form method="post" action="${pageContext.request.contextPath}/admin/products" class="color-form">
                        <input type="hidden" name="action" value="${formAction}">
                        <c:if test="${formAction eq 'editColor'}">
                            <input type="hidden" name="colorId" value="${color.colorId}">
                        </c:if>
                        <input type="hidden" id="hexCode" name="hexCode" value="${empty color.hexCode ? '#000000' : color.hexCode}">

                        <section class="form-section">
                            <div class="section-heading">
                                <h3>Color details</h3>
                                <p>Enter a color name and pick a display color. No technical color code knowledge is needed.</p>
                            </div>

                            <div class="form-grid">
                                <div class="form-group">
                                    <label for="colorName">Color name</label>
                                    <input id="colorName" name="colorName" type="text" maxlength="100" value="${color.colorName}" placeholder="Example: Navy Blue, Beige, Pastel Pink" required>
                                </div>
                                <div class="form-group">
                                    <label>Display color</label>
                                    <div class="color-picker-wrap">
                                        <input id="colorPicker" class="color-picker" type="color" value="${empty color.hexCode ? '#000000' : color.hexCode}">
                                        <div class="color-preview">
                                            <div class="swatch" id="swatchPreview"></div>
                                            <div class="swatch-note">
                                                <strong id="swatchTitle">Selected color</strong>
                                                <span id="swatchCode">${empty color.hexCode ? '#000000' : color.hexCode}</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </section>

                        <div class="form-actions">
                            <a class="ghost-btn" href="${pageContext.request.contextPath}/admin/products?tab=colors">Back</a>
                            <button type="submit" class="primary-btn">${formAction eq 'editColor' ? 'Save changes' : 'Add color'}</button>
                        </div>
                    </form>
                </div>
            </section>
        </div>

        <script>
            (function () {
                const colorPicker = document.getElementById('colorPicker');
                const hexCode = document.getElementById('hexCode');
                const swatchPreview = document.getElementById('swatchPreview');
                const swatchCode = document.getElementById('swatchCode');
                const swatchTitle = document.getElementById('swatchTitle');
                const colorName = document.getElementById('colorName');

                function updatePreview() {
                    const value = colorPicker.value || '#000000';
                    hexCode.value = value.toUpperCase();
                    swatchPreview.style.backgroundColor = value;
                    swatchCode.textContent = value.toUpperCase();
                    swatchTitle.textContent = colorName.value && colorName.value.trim() ? colorName.value.trim() : 'Selected color';
                }

                colorPicker.addEventListener('input', updatePreview);
                colorName.addEventListener('input', updatePreview);
                updatePreview();
            })();
        </script>
    </body>
</html>
