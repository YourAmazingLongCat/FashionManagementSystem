<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>${pageTitle}</title>
        <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/views/pages/productManagement/product-management.css?v=20260608-2243">
        <style>
            body {
                margin: 0;
                font-family: 'Inter', sans-serif;
                color: #0f172a;
                background:
                    radial-gradient(circle at top left, rgba(124, 58, 237, 0.18), transparent 28%),
                    radial-gradient(circle at top right, rgba(59, 130, 246, 0.14), transparent 24%),
                    linear-gradient(135deg, #f8fafc 0%, #eef2ff 100%);
                min-height: 100vh;
            }

            .form-shell {
                width: min(1080px, calc(100% - 40px));
                margin: 28px auto;
            }

            .form-panel {
                background: rgba(255, 255, 255, 0.94);
                border: 1px solid rgba(226, 232, 240, 0.9);
                border-radius: 30px;
                box-shadow: 0 24px 60px rgba(15, 23, 42, 0.16);
                overflow: hidden;
                backdrop-filter: blur(12px);
            }

            .form-hero {
                padding: 30px;
                background: linear-gradient(135deg, #ffffff 0%, #ede9fe 100%);
                border-bottom: 1px solid #e2e8f0;
            }

            .eyebrow {
                margin: 0 0 10px;
                text-transform: uppercase;
                letter-spacing: 0.18em;
                font-size: 0.74rem;
                font-weight: 700;
                color: #7c3aed;
            }

            .form-panel h1 {
                margin: 0;
                font-size: 2.15rem;
                line-height: 1.12;
            }

            .form-body {
                padding: 30px;
                display: grid;
                gap: 24px;
            }

            .alert {
                padding: 16px 18px;
                border-radius: 18px;
                font-weight: 600;
                background: rgba(220, 38, 38, 0.12);
                color: #991b1b;
                border: 1px solid rgba(220, 38, 38, 0.2);
            }

            .category-form {
                display: grid;
                gap: 24px;
            }

            .form-section {
                background: linear-gradient(180deg, #ffffff 0%, #fbfbff 100%);
                border: 1px solid #e2e8f0;
                border-radius: 26px;
                padding: 24px;
            }

            .section-heading {
                margin-bottom: 20px;
            }

            .section-heading h3 {
                margin: 0;
                font-size: 1.15rem;
            }

            .form-group {
                display: grid;
                gap: 10px;
            }

            label {
                font-weight: 700;
                color: #334155;
            }

            input,
            textarea {
                width: 100%;
                padding: 14px 16px;
                border-radius: 16px;
                border: 1px solid #dbe3f0;
                background: #ffffff;
                color: #0f172a;
                font: inherit;
                box-sizing: border-box;
                transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
            }

            input:focus,
            textarea:focus {
                outline: none;
                border-color: #7c3aed;
                box-shadow: 0 0 0 4px rgba(124, 58, 237, 0.12);
                transform: translateY(-1px);
            }

            textarea {
                resize: vertical;
                min-height: 200px;
            }

            .form-actions {
                display: flex;
                justify-content: flex-end;
                align-items: center;
                gap: 12px;
                padding-top: 6px;
            }

            .primary-btn,
            .ghost-btn {
                display: inline-flex;
                align-items: center;
                justify-content: center;
                padding: 12px 18px;
                border-radius: 14px;
                font-weight: 700;
                text-decoration: none;
                border: none;
                cursor: pointer;
                transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
            }

            .primary-btn {
                background: linear-gradient(135deg, #7c3aed 0%, #4f46e5 100%);
                color: #ffffff;
                box-shadow: 0 18px 30px rgba(124, 58, 237, 0.22);
            }

            .ghost-btn {
                background: #ffffff;
                color: #334155;
                border: 1px solid #dbe3f0;
            }

            .primary-btn:hover,
            .ghost-btn:hover {
                transform: translateY(-2px);
            }

            @media (max-width: 768px) {
                .form-shell {
                    width: min(100% - 20px, 100%);
                    margin: 10px auto;
                }

                .form-actions {
                    flex-direction: column;
                    align-items: stretch;
                }

                .form-body,
                .form-section,
                .form-hero {
                    padding: 20px;
                }
            }
        </style>
    </head>
    <body>
        <div class="form-shell">
            <section class="form-panel">
                <div class="form-hero">
                    <p class="eyebrow">Admin / Category</p>
                    <h1>${pageTitle}</h1>
                </div>

                <div class="form-body">
                    <c:if test="${not empty error}">
                        <div class="alert">${error}</div>
                    </c:if>

                    <form method="post" action="${pageContext.request.contextPath}/admin/products" class="category-form">
                        <input type="hidden" name="action" value="${formAction}">
                        <c:if test="${formAction eq 'editCategory'}">
                            <input type="hidden" name="categoryId" value="${category.categoryId}">
                        </c:if>

                        <section class="form-section">
                            <div class="section-heading">
                                <h3>Category details</h3>
                            </div>

                            <div class="form-group">
                                <label for="name">Category name</label>
                                <input id="name" name="name" type="text" maxlength="200" value="${category.name}" placeholder="Ex: T-Shirts" required>
                            </div>
                        </section>

                        <section class="form-section">
                            <div class="section-heading">
                                <h3>Description</h3>
                            </div>

                            <div class="form-group">
                                <label for="description">Category description</label>
                                <textarea id="description" name="description" rows="6" placeholder="Add a short description for this category...">${category.description}</textarea>
                            </div>
                        </section>

                        <div class="form-actions">
                            <a class="ghost-btn" href="${pageContext.request.contextPath}/admin/products?tab=categories">Back to categories</a>
                            <button type="submit" class="primary-btn">${formAction eq 'editCategory' ? 'Save changes' : 'Create category'}</button>
                        </div>
                    </form>
                </div>
            </section>
        </div>
    </body>
</html>
