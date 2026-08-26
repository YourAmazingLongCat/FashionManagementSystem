<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:if test="${empty requestScope.contentPage}">
    <c:redirect url="${pageContext.request.contextPath}/customer/order-history" />
</c:if>

<section class="customer-order-page">
    <div class="co-container">
        <c:choose>
            <c:when test="${empty order}">
                <div class="co-empty-card">
                    <span class="material-symbols-outlined">error</span>
                    <h2>Order not found</h2>
                    <p><c:out value="${errorMessage}" /></p>
                </div>
            </c:when>

            <c:otherwise>
                <c:if test="${not orderPlaced and order.orderStatus eq 'Pending'}">
                    <div class="co-notice co-notice-warning">
                        <div class="co-notice-icon">
                            <span class="material-symbols-outlined">schedule</span>
                        </div>
                        <div class="co-notice-content">
                            <strong>Order confirmation expires in</strong>
                            <span id="order-confirmation-countdown"
                                  class="co-notice-countdown"
                                  data-order-expiry="${order.confirmationExpiresAt}">Calculating...</span>
                            <p>The order will be cancelled automatically when this time ends.</p>
                        </div>
                    </div>
                </c:if>

                <c:if test="${orderPlaced and order.orderStatus eq 'Pending'}">
                    <div class="co-notice co-notice-info">
                        <div class="co-notice-icon">
                            <span class="material-symbols-outlined">hourglass_top</span>
                        </div>
                        <div class="co-notice-content">
                            <c:choose>
                                <c:when test="${not empty payment and payment.paymentMethod eq 'VNPay' and payment.paymentStatus ne 'Paid'}">
                                    <strong>Complete your VNPay payment</strong>
                                    <p>Your order has been saved. Complete the payment so it can be confirmed.</p>
                                </c:when>
                                <c:otherwise>
                                    <strong>Your order is waiting for confirmation</strong>
                                    <p>We have received your order and will update its status after it is reviewed.</p>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </c:if>

                <div class="co-progress-card">
                    <c:choose>
                        <c:when test="${order.orderStatus eq 'Cancelled'}">
                            <div class="co-cancelled-message">
                                <span class="material-symbols-outlined">cancel</span>
                                This order has been cancelled.
                            </div>
                            <c:if test="${not empty order.cancellationReason}">
                                <div class="co-cancellation-audit">
                                    <div>
                                        <span>Reason</span>
                                        <strong><c:out value="${order.cancellationReason}" /></strong>
                                    </div>
                                    <div>
                                        <span>Cancelled by</span>
                                        <strong><c:out value="${order.cancelledBy}" /></strong>
                                    </div>
                                    <div>
                                        <span>Cancelled at</span>
                                        <strong><c:out value="${order.cancelledAt}" /></strong>
                                    </div>
                                </div>
                            </c:if>
                        </c:when>
                        <c:otherwise>
                            <div class="co-progress co-progress-${fn:toLowerCase(order.orderStatus)}">
                                <div class="co-progress-step">
                                    <span class="co-progress-icon"><span class="material-symbols-outlined">receipt_long</span></span>
                                    <span>Pending</span>
                                </div>
                                <div class="co-progress-line"></div>
                                <div class="co-progress-step">
                                    <span class="co-progress-icon"><span class="material-symbols-outlined">task_alt</span></span>
                                    <span>Confirmed</span>
                                </div>
                                <div class="co-progress-line"></div>
                                <div class="co-progress-step">
                                    <span class="co-progress-icon"><span class="material-symbols-outlined">inventory_2</span></span>
                                    <span>Processing</span>
                                </div>
                                <div class="co-progress-line"></div>
                                <div class="co-progress-step">
                                    <span class="co-progress-icon"><span class="material-symbols-outlined">local_shipping</span></span>
                                    <span>Shipping</span>
                                </div>
                                <div class="co-progress-line"></div>
                                <div class="co-progress-step">
                                    <span class="co-progress-icon"><span class="material-symbols-outlined">home</span></span>
                                    <span>Delivered</span>
                                </div>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="co-detail-grid">
                    <div class="co-detail-main">
                        <div class="co-card">
                            <div class="co-card-head">
                                <div>
                                    <h2>Order summary</h2>
                                </div>
                                <span class="co-date-chip">
                                    <span class="material-symbols-outlined">calendar_today</span>
                                    ${order.placedAt}
                                </span>
                            </div>

                            <div class="co-summary-grid">
                                <div class="co-summary-item">
                                    <span>Order ID</span>
                                    <strong><c:out value="${order.orderId}" /></strong>
                                </div>
                                <div class="co-summary-item">
                                    <span>Status</span>
                                    <strong><c:out value="${order.orderStatus}" /></strong>
                                </div>
                                <div class="co-summary-item">
                                    <span>Phone number</span>
                                    <strong>
                                        <c:choose>
                                            <c:when test="${not empty order.phone}"><c:out value="${order.phone}" /></c:when>
                                            <c:otherwise>Not provided</c:otherwise>
                                        </c:choose>
                                    </strong>
                                </div>
                                <div class="co-summary-item co-summary-address">
                                    <span>Shipping address</span>
                                    <strong>
                                        <c:choose>
                                            <c:when test="${not empty order.shippingAddress}"><c:out value="${order.shippingAddress}" /></c:when>
                                            <c:otherwise>Not provided</c:otherwise>
                                        </c:choose>
                                    </strong>
                                </div>
                                <div class="co-summary-item co-summary-total">
                                    <span>Order total</span>
                                    <strong><fmt:formatNumber value="${order.totalAmount}" type="number" groupingUsed="true" /> VND</strong>
                                </div>
                            </div>
                        </div>

                        <div class="co-card">
                            <div class="co-card-head">
                                <div>
                                    <h2>Items</h2>
                                    <p>${fn:length(orderItems)} product(s)</p>
                                </div>
                                <c:if test="${order.orderStatus eq 'Delivered'}">
                                    <div class="co-rating-header-actions">
                                        <c:forEach var="item" items="${orderItems}">
                                            <button type="button" class="co-rate-item-btn" data-rating-open-for="${item.variantId}">Rate this item</button>
                                        </c:forEach>
                                    </div>
                                </c:if>
                            </div>

                            <c:choose>
                                <c:when test="${empty orderItems}">
                                    <div class="co-inline-empty">
                                        <span class="material-symbols-outlined">inventory_2</span>
                                        <p>No products were found for this order.</p>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="co-table-wrap co-items-table-wrap">
                                        <table class="co-table co-items-table">
                                            <thead>
                                                <tr>
                                                    <th>Product variant</th>
                                                    <th>Quantity</th>
                                                    <th>Unit price</th>
                                                    <th>Subtotal</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach var="item" items="${orderItems}">
                                                    <tr>
                                                        <td>
                                                            <div class="co-product-cell">
                                                                <c:choose>
                                                                    <c:when test="${not empty item.imageUrl}">
                                                                        <img class="co-product-image"
                                                                             src="${pageContext.request.contextPath}${item.imageUrl}"
                                                                             alt="<c:out value='${item.productName}' />" />
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <span class="co-product-icon material-symbols-outlined">checkroom</span>
                                                                    </c:otherwise>
                                                                </c:choose>
                                                                <div class="co-variant-copy">
                                                                    <strong class="co-variant-name">
                                                                        <c:choose>
                                                                            <c:when test="${not empty item.productName}">
                                                                                <c:out value="${item.productName}" />
                                                                                <c:if test="${not empty item.colorName}"> - <c:out value="${item.colorName}" /></c:if>
                                                                                <c:if test="${not empty item.sizeName}"> / <c:out value="${item.sizeName}" /></c:if>
                                                                            </c:when>
                                                                            <c:otherwise>Variant <c:out value="${item.variantId}" /></c:otherwise>
                                                                        </c:choose>
                                                                    </strong>
                                                                    <c:if test="${not empty item.sku}">
                                                                        <span class="co-variant-sku">SKU: <c:out value="${item.sku}" /></span>
                                                                    </c:if>
                                                                </div>
                                                            </div>
                                                        </td>
                                                        <td>${item.quantity}</td>
                                                        <td><fmt:formatNumber value="${item.unitPrice}" type="number" groupingUsed="true" /> VND</td>
                                                        <td><strong class="co-money"><fmt:formatNumber value="${item.subTotal}" type="number" groupingUsed="true" /> VND</strong></td>
                                                    </tr>
                                                </c:forEach>
                                            </tbody>
                                        </table>
                                    </div>
                                    <c:if test="${order.orderStatus eq 'Delivered'}">
                                        <c:forEach var="item" items="${orderItems}">
                                            <form class="co-rating-form" data-rating-form="${item.variantId}" hidden>
                                                <button type="button" class="co-rating-close" data-rating-close aria-label="Close">&times;</button>
                                                <input type="hidden" name="orderItemId" value="${item.variantId}" />
                                                <div class="co-rating-stars" role="group" aria-label="Choose rating">
                                                    <button type="button" data-rating="1" aria-label="1 star">★</button>
                                                    <button type="button" data-rating="2" aria-label="2 stars">★</button>
                                                    <button type="button" data-rating="3" aria-label="3 stars">★</button>
                                                    <button type="button" data-rating="4" aria-label="4 stars">★</button>
                                                    <button type="button" data-rating="5" aria-label="5 stars">★</button>
                                                </div>
                                                <input type="hidden" name="rating" value="" required />
                                                <textarea name="content" class="co-input co-textarea" rows="2" maxlength="1000" placeholder="Your comment" required></textarea>
                                                <button type="submit" class="co-primary-btn co-rating-submit">Submit rating</button>
                                                <span class="co-rating-message" role="status"></span>
                                            </form>
                                        </c:forEach>
                                    </c:if>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <aside class="co-detail-side">
                        <c:choose>
                            <c:when test="${not orderPlaced and order.orderStatus eq 'Pending'}">
                                <div class="co-card co-sticky-card">
                                    <div class="co-card-head">
                                        <div>
                                            <h2>Place order</h2>
                                            <p>Complete your delivery and payment information.</p>
                                        </div>
                                        <span class="material-symbols-outlined co-card-head-icon">shopping_bag</span>
                                    </div>

                                    <form class="co-form" action="${pageContext.request.contextPath}/customer/order-detail" method="post">
                                        <input type="hidden" name="action" value="placeOrder" />
                                        <input type="hidden" name="orderId" value="${order.orderId}" />

                                        <div class="co-form-group">
                                            <label for="shippingAddress">Shipping address</label>
                                            <textarea id="shippingAddress"
                                                      name="shippingAddress"
                                                      class="co-input co-textarea"
                                                      placeholder="Enter your full delivery address"
                                                      required><c:out value="${order.shippingAddress}" /></textarea>
                                        </div>

                                        <div class="co-form-group">
                                            <label for="phone">Phone number</label>
                                            <input id="phone"
                                                   name="phone"
                                                   class="co-input"
                                                   type="tel"
                                                   value="<c:out value='${order.phone}' />"
                                                   placeholder="Example: 0912345678"
                                                   required />
                                        </div>

                                        <div class="co-form-group">
                                            <label for="paymentMethod">Payment method</label>
                                            <select id="paymentMethod" name="paymentMethod" class="co-input co-select">
                                                <option value="COD" selected>Cash On Delivery</option>
                                                <option value="VNPay">VNPay</option>
                                            </select>
                                        </div>

                                        <div class="co-payment-summary">
                                            <div>
                                                <span>Order amount</span>
                                                <strong><fmt:formatNumber value="${order.totalAmount}" type="number" groupingUsed="true" /> VND</strong>
                                            </div>
                                        </div>

                                        <button class="co-primary-btn co-full-btn" type="submit">
                                            Place order
                                            <span class="material-symbols-outlined">arrow_forward</span>
                                        </button>
                                    </form>
                                </div>
                            </c:when>

                            <c:otherwise>
                                <div class="co-card co-sticky-card">
                                    <div class="co-card-head">
                                        <div>
                                            <h2>Payment</h2>
                                            <p>Payment information for this order.</p>
                                        </div>
                                        <span class="material-symbols-outlined co-card-head-icon">payments</span>
                                    </div>

                                    <div class="co-info-list">
                                        <div>
                                            <span>Invoice ID</span>
                                            <strong><c:out value="${order.invoiceId}" /></strong>
                                        </div>
                                        <div>
                                            <span>Issued date</span>
                                            <strong><c:out value="${order.issuedDate}" /></strong>
                                        </div>
                                        <c:if test="${not empty payment}">
                                            <div>
                                                <span>Payment ID</span>
                                                <strong><c:out value="${payment.paymentId}" /></strong>
                                            </div>
                                        </c:if>
                                        <div>
                                            <span>Payment method</span>
                                            <strong>
                                                <c:choose>
                                                    <c:when test="${not empty payment and payment.paymentMethod eq 'COD'}">Cash On Delivery</c:when>
                                                    <c:when test="${not empty payment}">${payment.paymentMethod}</c:when>
                                                    <c:otherwise>Not selected</c:otherwise>
                                                </c:choose>
                                            </strong>
                                        </div>
                                        <div>
                                            <span>Payment status</span>
                                            <strong>
                                                <c:choose>
                                                    <c:when test="${not empty payment}">
                                                        <span class="co-payment-status co-payment-${fn:toLowerCase(payment.paymentStatus)}">${payment.paymentStatus}</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="co-payment-status co-payment-pending">Unpaid</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </strong>
                                        </div>
                                        <div>
                                            <span>Amount</span>
                                            <strong>
                                                <c:choose>
                                                    <c:when test="${not empty payment}"><fmt:formatNumber value="${payment.amount}" type="number" groupingUsed="true" /> VND</c:when>
                                                    <c:otherwise><fmt:formatNumber value="${order.totalAmount}" type="number" groupingUsed="true" /> VND</c:otherwise>
                                                </c:choose>
                                            </strong>
                                        </div>
                                    </div>

                                    <c:choose>
                                        <c:when test="${not empty payment and payment.paymentStatus eq 'Paid'}">
                                            <div class="co-mini-message co-mini-success">Payment completed successfully.</div>
                                        </c:when>
                                        <c:when test="${not empty payment and payment.paymentMethod eq 'COD'}">
                                            <div class="co-mini-message co-mini-info">Payment will be collected when your order is delivered.</div>
                                        </c:when>
                                        <c:when test="${not empty payment and payment.paymentMethod eq 'VNPay' and payment.paymentStatus eq 'Pending'}">
                                            <div class="co-mini-message co-mini-info">Continue to VNPay Sandbox to complete your payment.</div>
                                            <form action="${pageContext.request.contextPath}/customer/vnpay/start" method="post">
                                                <input type="hidden" name="paymentId" value="${payment.paymentId}" />
                                                <button class="co-primary-btn co-full-btn" type="submit">
                                                    Pay with VNPay
                                                    <span class="material-symbols-outlined">open_in_new</span>
                                                </button>
                                            </form>
                                        </c:when>
                                        <c:when test="${not empty payment and payment.paymentMethod eq 'VNPay' and (payment.paymentStatus eq 'Failed' or payment.paymentStatus eq 'Cancelled')}">
                                            <div class="co-mini-message co-mini-info">The previous VNPay payment was not completed.</div>
                                            <form action="${pageContext.request.contextPath}/customer/vnpay/start" method="post">
                                                <input type="hidden" name="orderId" value="${order.orderId}" />
                                                <button class="co-primary-btn co-full-btn" type="submit">
                                                    Try VNPay again
                                                    <span class="material-symbols-outlined">refresh</span>
                                                </button>
                                            </form>
                                        </c:when>
                                    </c:choose>
                                </div>

                                <div class="co-card">
                                    <div class="co-card-head">
                                        <div>
                                            <h2>Delivery information</h2>
                                            <p>
                                                <c:choose>
                                                    <c:when test="${canEditDelivery}">Confirm these details before placing the order.</c:when>
                                                    <c:otherwise>Delivery details are locked after the order is placed.</c:otherwise>
                                                </c:choose>
                                            </p>
                                        </div>
                                        <span class="material-symbols-outlined co-card-head-icon">local_shipping</span>
                                    </div>

                                    <c:choose>
                                        <c:when test="${canEditDelivery}">
                                            <form class="co-form" action="${pageContext.request.contextPath}/customer/order-detail" method="post">
                                                <input type="hidden" name="action" value="updateShipping" />
                                                <input type="hidden" name="orderId" value="${order.orderId}" />

                                                <div class="co-form-group">
                                                    <label for="updateShippingAddress">Shipping address</label>
                                                    <textarea id="updateShippingAddress"
                                                              name="shippingAddress"
                                                              class="co-input co-textarea"
                                                              required><c:out value="${order.shippingAddress}" /></textarea>
                                                </div>

                                                <div class="co-form-group">
                                                    <label for="updatePhone">Phone number</label>
                                                    <input id="updatePhone"
                                                           name="phone"
                                                           class="co-input"
                                                           type="tel"
                                                           value="<c:out value='${order.phone}' />"
                                                           required />
                                                </div>

                                                <button class="co-secondary-btn co-full-btn" type="submit">
                                                    <span class="material-symbols-outlined">save</span>
                                                    Save changes
                                                </button>
                                                <c:if test="${canCancel}">
                                                    <button type="button"
                                                            class="co-cancel-btn co-full-btn co-cancel-after-save"
                                                            id="openCustomerCancelModal">
                                                        <span class="material-symbols-outlined">cancel</span>
                                                        Cancel Order
                                                    </button>
                                                </c:if>
                                            </form>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="co-info-list">
                                                <div>
                                                    <span>Shipping address</span>
                                                    <strong><c:out value="${order.shippingAddress}" /></strong>
                                                </div>
                                                <div>
                                                    <span>Phone number</span>
                                                    <strong><c:out value="${order.phone}" /></strong>
                                                </div>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                        </c:otherwise>
                    </c:choose>
                    </aside>
                </div>
                <c:if test="${canCancel}">
                    <div class="co-cancel-modal" id="customerCancelModal" hidden>
                        <div class="co-cancel-modal-backdrop" data-close-cancel-modal></div>
                        <div class="co-cancel-dialog" role="dialog" aria-modal="true" aria-labelledby="customerCancelTitle">
                            <div class="co-cancel-dialog-head">
                                <div>
                                    <p class="co-eyebrow">Order action</p>
                                    <h2 id="customerCancelTitle">Cancel order</h2>
                                </div>
                                <button type="button" class="co-cancel-dialog-close" data-close-cancel-modal aria-label="Close cancel dialog">
                                    <span class="material-symbols-outlined">close</span>
                                </button>
                            </div>
                            <p class="co-cancel-warning">Please provide a reason for cancelling this order. This action cannot be undone.</p>
                            <form class="co-cancel-form" action="${pageContext.request.contextPath}/customer/cancel-order" method="post">
                                <input type="hidden" name="orderId" value="${order.orderId}" />
                                <label for="customerCancellationReason">Cancellation reason</label>
                                <textarea id="customerCancellationReason"
                                          name="reason"
                                          class="co-input co-textarea"
                                          maxlength="500"
                                          placeholder="Tell us why you need to cancel this order"
                                          required></textarea>
                                <div class="co-cancel-dialog-actions">
                                    <button type="button" class="co-secondary-btn" data-close-cancel-modal>Keep order</button>
                                    <button type="submit" class="co-cancel-btn">
                                        <span class="material-symbols-outlined">cancel</span>
                                        Cancel Order
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                </c:if>
        </c:otherwise>
    </c:choose>
</div>
</section>

<script>
(function () {
    var contextPath = '${pageContext.request.contextPath}';
    document.querySelectorAll('[data-rating-open-for]').forEach(function (button) {
        button.addEventListener('click', function () {
            var form = document.querySelector('[data-rating-form="' + button.dataset.ratingOpenFor + '"]');
            if (form) form.hidden = false;
        });
    });
    document.querySelectorAll('[data-rating-form]').forEach(function (form) {
        var closeButton = form.querySelector('[data-rating-close]');
        var stars = form.querySelectorAll('[data-rating]');
        var ratingInput = form.querySelector('input[name="rating"]');
        var message = form.querySelector('.co-rating-message');
        closeButton.addEventListener('click', function () { form.hidden = true; });
        stars.forEach(function (star) {
            star.addEventListener('click', function () {
                ratingInput.value = star.dataset.rating;
                stars.forEach(function (item) {
                    item.classList.toggle('selected', Number(item.dataset.rating) <= Number(ratingInput.value));
                });
            });
        });
        form.addEventListener('submit', function (event) {
            event.preventDefault();
            if (!ratingInput.value) {
                message.textContent = 'Please choose a rating.';
                return;
            }
            var submit = form.querySelector('.co-rating-submit');
            submit.disabled = true;
            message.textContent = 'Submitting...';
            var submitted = false;
            var data = new URLSearchParams(new FormData(form));
            data.set('action', 'addFromOrder');
            fetch(contextPath + '/comment', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: data
            }).then(function (response) { return response.json(); })
              .then(function (result) {
                  message.textContent = result.message || (result.success ? 'Rating submitted.' : 'Unable to submit rating.');
                  if (result.success) {
                      submitted = true;
                      form.querySelectorAll('button, textarea').forEach(function (field) { field.disabled = true; });
                  }
              }).catch(function () {
                  message.textContent = 'Unable to submit rating.';
              }).finally(function () {
                  if (!submitted) submit.disabled = false;
              });
        });
    });
})();
</script>

<script>
(function () {
    var countdown = document.getElementById('order-confirmation-countdown');

    if (countdown) {
        function updateCountdown() {
            var expiryText = countdown.getAttribute('data-order-expiry');
            var expiryTime = new Date(expiryText).getTime();
            var remaining = expiryTime - Date.now();

            if (isNaN(expiryTime)) {
                countdown.textContent = 'Time unavailable';
                return;
            }

            if (remaining <= 0) {
                countdown.textContent = 'Expired';
                return;
            }

            var totalSeconds = Math.floor(remaining / 1000);
            var days = Math.floor(totalSeconds / 86400);
            var hours = Math.floor((totalSeconds % 86400) / 3600);
            var minutes = Math.floor((totalSeconds % 3600) / 60);
            var seconds = totalSeconds % 60;

            countdown.textContent = days + 'd ' + hours + 'h ' + minutes + 'm ' + seconds + 's';
        }

        updateCountdown();
        window.setInterval(updateCountdown, 1000);
    }

    var cancelModal = document.getElementById('customerCancelModal');
    var openCancelButton = document.getElementById('openCustomerCancelModal');

    if (cancelModal && openCancelButton) {
        var closeButtons = cancelModal.querySelectorAll('[data-close-cancel-modal]');
        var reasonInput = document.getElementById('customerCancellationReason');

        function openCancelModal() {
            cancelModal.hidden = false;
            document.body.classList.add('co-modal-open');
            if (reasonInput) {
                window.setTimeout(function () {
                    reasonInput.focus();
                }, 0);
            }
        }

        function closeCancelModal() {
            cancelModal.hidden = true;
            document.body.classList.remove('co-modal-open');
            openCancelButton.focus();
        }

        openCancelButton.addEventListener('click', openCancelModal);

        for (var i = 0; i < closeButtons.length; i++) {
            closeButtons[i].addEventListener('click', closeCancelModal);
        }

        document.addEventListener('keydown', function (event) {
            if (event.key === 'Escape' && !cancelModal.hidden) {
                closeCancelModal();
            }
        });
    }
})();
</script>
