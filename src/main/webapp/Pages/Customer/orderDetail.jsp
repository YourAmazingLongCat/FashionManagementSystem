<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:if test="${empty requestScope.contentPage}">
    <c:redirect url="${pageContext.request.contextPath}/customer/order-history" />
</c:if>

<style>
    .review-modal-overlay {
        display: none;
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0,0,0,0.5);
        z-index: 1000;
        justify-content: center;
        align-items: center;
    }
    .review-modal-overlay.active {
        display: flex;
    }
    .review-modal {
        background: white;
        border-radius: 12px;
        width: 90%;
        max-width: 450px;
        max-height: 90vh;
        overflow-y: auto;
        padding: 24px;
        position: relative;
    }
    .review-modal-close {
        position: absolute;
        top: 12px;
        right: 12px;
        background: none;
        border: none;
        font-size: 24px;
        cursor: pointer;
        color: #666;
    }
    .review-modal h3 {
        margin: 0 0 16px 0;
        color: #333;
    }
    .review-product-info {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 12px;
        background: #f5f5f5;
        border-radius: 8px;
        margin-bottom: 16px;
    }
    .review-product-info img {
        width: 50px;
        height: 50px;
        object-fit: cover;
        border-radius: 4px;
    }
    .review-product-info .product-name {
        font-weight: 500;
        color: #333;
    }
    .review-product-info .variant-info {
        font-size: 12px;
        color: #666;
    }
    .review-stars {
        display: flex;
        gap: 4px;
        margin-bottom: 16px;
    }
    .review-stars .star {
        font-size: 32px;
        color: #ddd;
        cursor: pointer;
        transition: color 0.2s;
    }
    .review-stars .star.active,
    .review-stars .star:hover {
        color: #ffc107;
    }
    .review-form-group {
        margin-bottom: 16px;
    }
    .review-form-group label {
        display: block;
        margin-bottom: 4px;
        font-weight: 500;
        color: #333;
    }
    .review-form-group textarea {
        width: 100%;
        padding: 12px;
        border: 1px solid #ddd;
        border-radius: 8px;
        resize: vertical;
        min-height: 80px;
        font-family: inherit;
    }
    .review-form-group textarea:focus {
        outline: none;
        border-color: #1976d2;
    }
    .review-btn {
        background: #1976d2;
        color: white;
        border: none;
        padding: 12px 24px;
        border-radius: 8px;
        cursor: pointer;
        font-weight: 500;
        transition: background 0.2s;
        width: 100%;
    }
    .review-btn:hover {
        background: #1565c0;
    }
    .review-btn:disabled {
        background: #ccc;
        cursor: not-allowed;
    }
    .review-status-badge {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        padding: 4px 10px;
        border-radius: 20px;
        font-size: 12px;
        font-weight: 500;
    }
    .review-status-badge.can-review {
        background: #e3f2fd;
        color: #1976d2;
        cursor: pointer;
    }
    .review-status-badge.can-review:hover {
        background: #bbdefb;
    }
    .review-status-badge.reviewed {
        background: #e8f5e9;
        color: #2e7d32;
    }
    .review-status-badge.not-eligible {
        background: #fafafa;
        color: #9e9e9e;
    }
    .review-eligibility-msg {
        padding: 12px;
        border-radius: 8px;
        margin-bottom: 16px;
        text-align: center;
    }
    .review-eligibility-msg.error {
        background: #ffebee;
        color: #c62828;
    }
    .review-eligibility-msg.success {
        background: #e8f5e9;
        color: #2e7d32;
    }
    .review-success {
        text-align: center;
        padding: 20px;
    }
    .review-success .material-symbols-outlined {
        font-size: 48px;
        color: #4caf50;
    }
    .review-success h4 {
        margin: 12px 0 8px 0;
        color: #333;
    }
    .review-success p {
        color: #666;
        margin: 0;
    }
</style>

<section class="wallet-page">
    <div class="wallet-hero">
        <div>
            <p class="wallet-breadcrumb">Customer / Order Detail</p>
            <h1 class="wallet-title">Order Detail</h1>
            <p class="wallet-subtitle">Review your order and shipping progress.</p>
        </div>
        <a class="wallet-outline-btn" href="${pageContext.request.contextPath}/customer/order-history">
            <span class="material-symbols-outlined">arrow_back</span>
            Back to orders
        </a>
    </div>

    <c:choose>
        <c:when test="${empty order}">
            <div class="wallet-empty">
                <span class="material-symbols-outlined">error</span>
                <h3>Order not found</h3>
                <p>${errorMessage}</p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="wallet-order-grid">
                <div class="wallet-history-card">
                    <div class="wallet-section-head">
                        <h2>${order.orderId}</h2>
                        <span class="payment-status payment-status-${fn:toLowerCase(order.orderStatus)}">${order.orderStatus}</span>
                    </div>

                    <div class="wallet-info-list">
                        <div><span>Shipping Address</span><strong>${order.shippingAddress}</strong></div>
                        <div><span>Phone</span><strong>${order.phone}</strong></div>
                        <div><span>Placed At</span><strong>${order.placedAt}</strong></div>
                        <div>
                            <span>Total Amount</span>
                            <strong><fmt:formatNumber value="${order.totalAmount}" type="number" groupingUsed="true" /> VND</strong>
                        </div>
                    </div>

                    <div class="wallet-table-wrap wallet-table-space">
                        <table class="wallet-table">
                            <thead>
                                <tr>
                                    <th>Variant</th>
                                    <th>Quantity</th>
                                    <th>Unit Price</th>
                                    <th>Discount</th>
                                    <th>Subtotal</th>
                                    <th>Review</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="item" items="${orderItems}">
                                    <tr data-order-item-id="${item.orderItemId}">
                                        <td><strong>${item.variantId}</strong></td>
                                        <td>${item.quantity}</td>
                                        <td><fmt:formatNumber value="${item.unitPrice}" type="number" groupingUsed="true" /> VND</td>
                                        <td><fmt:formatNumber value="${item.discountAmount}" type="number" groupingUsed="true" /> VND</td>
                                        <td class="wallet-money"><fmt:formatNumber value="${item.subTotal}" type="number" groupingUsed="true" /> VND</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${order.orderStatus eq 'Delivered'}">
                                                    <span class="review-status-badge can-review"
                                                          onclick="openReviewModal('${item.orderItemId}')">
                                                        <span class="material-symbols-outlined" style="font-size: 14px;">star</span>
                                                        Review
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="review-status-badge not-eligible">
                                                        <span class="material-symbols-outlined" style="font-size: 14px;">lock</span>
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>

                <aside class="wallet-payment-panel">
                    <div class="wallet-form-head">
                        <span class="material-symbols-outlined">payments</span>
                        <div>
                            <h2>Bill</h2>
                            <p>Your order bill information.</p>
                        </div>
                    </div>

                    <div class="wallet-payment-row">
                        <span>Bill ID</span>
                        <strong>
                            <c:choose>
                                <c:when test="${not empty bill}">${bill.billId}</c:when>
                                <c:otherwise>-</c:otherwise>
                            </c:choose>
                        </strong>
                    </div>

                    <div class="wallet-payment-row">
                        <span>Payment Method</span>
                        <strong>
                            <c:choose>
                                <c:when test="${not empty bill}">${bill.paymentMethod}</c:when>
                                <c:otherwise>COD (Cash on Delivery)</c:otherwise>
                            </c:choose>
                        </strong>
                    </div>

                    <div class="wallet-payment-row">
                        <span>Payment Status</span>
                        <c:choose>
                            <c:when test="${not empty bill}">
                                <strong class="payment-status payment-status-${fn:toLowerCase(bill.paymentStatus)}">${bill.paymentStatus}</strong>
                            </c:when>
                            <c:otherwise>
                                <strong class="payment-status payment-status-pending">Pending</strong>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <div class="wallet-payment-row">
                        <span>Amount</span>
                        <strong><fmt:formatNumber value="${order.totalAmount}" type="number" groupingUsed="true" /> VND</strong>
                    </div>

                    <c:if test="${not empty bill && bill.paymentStatus eq 'Pending' && bill.paymentMethod eq 'COD'}">
                        <div class="wallet-alert wallet-alert-success" style="margin-top: 12px;">
                            COD selected. Please pay when the order is delivered.
                        </div>
                    </c:if>

                    <c:if test="${order.orderStatus eq 'Cancelled'}">
                        <div class="wallet-alert wallet-alert-error" style="margin-top: 12px;">
                            This order has been cancelled.
                        </div>
                    </c:if>

                    <c:if test="${order.orderStatus eq 'Delivered'}">
                        <div class="wallet-alert wallet-alert-success" style="margin-top: 12px;">
                            Order has been delivered successfully!
                        </div>
                    </c:if>
                </aside>
            </div>
        </c:otherwise>
    </c:choose>
</section>

<!-- Review Modal -->
<div class="review-modal-overlay" id="reviewModal">
    <div class="review-modal">
        <button class="review-modal-close" onclick="closeReviewModal()">&times;</button>
        <div id="reviewModalContent">
            <!-- Dynamic content -->
        </div>
    </div>
</div>

<script>
    let currentRating = 0;
    let currentOrderItemId = '';

    function openReviewModal(orderItemId) {
        currentOrderItemId = orderItemId;
        currentRating = 0;
        document.getElementById('reviewModal').classList.add('active');

        // Check eligibility via AJAX
        fetch('${pageContext.request.contextPath}/comment-data?action=checkOrderItem&orderItemId=' + orderItemId)
            .then(response => response.json())
            .then(data => {
                if (data.error) {
                    displayMessage(data.error, 'error');
                } else {
                    displayReviewForm(data);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                displayMessage('An error occurred. Please try again.', 'error');
            });
    }

    function displayReviewForm(data) {
        const modal = document.getElementById('reviewModalContent');

        if (!data.eligible) {
            let message = data.reason || 'You cannot review this product.';
            if (data.alreadyReviewed) {
                message = 'You have already reviewed this product.';
            } else if (data.windowExpired) {
                message = 'Review window has expired (7 days after order placement).';
            }
            displayMessage(message, 'error');
            return;
        }

        modal.innerHTML = `
            <h3>Rate this product</h3>
            <p style="color: #666; font-size: 14px; margin-bottom: 16px;">
                You have ${data.remainingDays} day(s) left to review
            </p>
            <div class="review-stars" id="reviewStars">
                <span class="star" data-value="1" onclick="setRating(1)">&#9733;</span>
                <span class="star" data-value="2" onclick="setRating(2)">&#9733;</span>
                <span class="star" data-value="3" onclick="setRating(3)">&#9733;</span>
                <span class="star" data-value="4" onclick="setRating(4)">&#9733;</span>
                <span class="star" data-value="5" onclick="setRating(5)">&#9733;</span>
            </div>
            <div class="review-form-group">
                <label>Your review (optional)</label>
                <textarea id="reviewComment" placeholder="Share your experience..." maxlength="1000"></textarea>
            </div>
            <button class="review-btn" onclick="submitReview()" id="submitBtn">Submit Review</button>
        `;
    }

    function displayMessage(message, type) {
        const modal = document.getElementById('reviewModalContent');
        modal.innerHTML = `
            <h3>Review Product</h3>
            <div class="review-eligibility-msg ${type}">
                ${message}
            </div>
            <button class="review-btn" onclick="closeReviewModal()">Close</button>
        `;
    }

    function setRating(rating) {
        currentRating = rating;
        const stars = document.querySelectorAll('.review-stars .star');
        stars.forEach((star, index) => {
            if (index < rating) {
                star.classList.add('active');
            } else {
                star.classList.remove('active');
            }
        });
    }

    function submitReview() {
        if (currentRating === 0) {
            alert('Please select a star rating.');
            return;
        }

        const btn = document.getElementById('submitBtn');
        const comment = document.getElementById('reviewComment').value.trim();

        btn.disabled = true;
        btn.textContent = 'Submitting...';

        const formData = new URLSearchParams();
        formData.append('action', 'addFromOrder');
        formData.append('orderItemId', currentOrderItemId);
        formData.append('rating', currentRating);
        formData.append('content', comment);

        fetch('${pageContext.request.contextPath}/comment', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: formData
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                displayReviewSuccess();
                updateReviewStatus(currentOrderItemId, 'reviewed');
            } else {
                alert(data.message || 'Failed to submit review');
                btn.disabled = false;
                btn.textContent = 'Submit Review';
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('An error occurred. Please try again.');
            btn.disabled = false;
            btn.textContent = 'Submit Review';
        });
    }

    function displayReviewSuccess() {
        const modal = document.getElementById('reviewModalContent');
        modal.innerHTML = `
            <div class="review-success">
                <span class="material-symbols-outlined">check_circle</span>
                <h4>Thank you!</h4>
                <p>Your review has been submitted.</p>
                <button class="review-btn" onclick="closeReviewModal()" style="margin-top: 16px;">Close</button>
            </div>
        `;
    }

    function updateReviewStatus(orderItemId, status) {
        const row = document.querySelector('tr[data-order-item-id="${item.orderItemId}"]');
        if (row) {
            const badge = row.querySelector('.review-status-badge');
            if (badge && status === 'reviewed') {
                badge.className = 'review-status-badge reviewed';
                badge.innerHTML = '<span class="material-symbols-outlined" style="font-size: 14px;">check</span> Reviewed';
                badge.onclick = null;
                badge.style.cursor = 'default';
            }
        }
    }

    function closeReviewModal() {
        document.getElementById('reviewModal').classList.remove('active');
    }

    // Close modal on overlay click
    document.getElementById('reviewModal').addEventListener('click', function(e) {
        if (e.target === this) {
            closeReviewModal();
        }
    });

    // Close modal on ESC key
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            closeReviewModal();
        }
    });
</script>
