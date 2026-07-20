<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
    Comments popup fragment.
    Included from ProductDetail.jsp via <jsp:include page="commentsModal.jsp" />.
    Relies on request-scoped attribute "product" (already set by the controller)
    and session attribute "USER" (Account) for permission checks.
--%>

<!-- ===== Comments Modal ===== -->
<div class="comments-modal-overlay" id="commentsModalOverlay">
    <div class="comments-modal">
        <div class="comments-modal-header">
            <h3>Customer comments</h3>
            <button type="button" class="comments-modal-close" id="closeCommentsBtn">&times;</button>
        </div>

        <div class="comments-modal-body">
            <div class="comments-list" id="commentsList">
                <div class="comments-loading">Loading comments...</div>
            </div>

            <div class="comments-empty" id="commentsEmpty" style="display:none;">
                No comments yet. Be the first to review this product.
            </div>
        </div>

        <div class="comments-add-panel" id="commentsAddPanel" style="display:none;">
            <div class="comments-rating-picker" id="ratingPicker">
                <c:forEach var="i" begin="1" end="5">
                    <button type="button" class="rating-star" data-value="${i}">★</button>
                </c:forEach>
            </div>
            <input type="hidden" id="ratingValue" value="" />

            <textarea id="commentContent" rows="3" maxlength="1000" placeholder="Share your experience with this product..."></textarea>

            <button type="button" class="comments-submit-btn" id="submitCommentBtn">Submit review</button>
        </div>

        <div class="comments-login-note" id="commentsLoginNote" style="display:none;">
            Only customers who purchased this product can leave a review.
        </div>
    </div>
</div>

<script>
    (function () {
        const productId = '${product.productId}';
        const contextPath = '${pageContext.request.contextPath}';
        const currentAccountId = '${sessionScope.USER.accountId}';
        const currentRole = ('${sessionScope.USER.role}' || '').toLowerCase();
        const isStaffOrAdmin = currentRole === 'admin' || currentRole === 'staff';

        const openBtn = document.getElementById('openCommentsBtn');
        const closeBtn = document.getElementById('closeCommentsBtn');
        const overlay = document.getElementById('commentsModalOverlay');
        const listEl = document.getElementById('commentsList');
        const emptyEl = document.getElementById('commentsEmpty');
        const addPanel = document.getElementById('commentsAddPanel');
        const loginNote = document.getElementById('commentsLoginNote');
        const badge = document.getElementById('commentCountBadge');
        const ratingPicker = document.getElementById('ratingPicker');
        const ratingValueInput = document.getElementById('ratingValue');
        const commentContentInput = document.getElementById('commentContent');
        const submitCommentBtn = document.getElementById('submitCommentBtn');

        let dataLoaded = false;
        let editingCommentId = null;

        const escapeHtml = (s) => {
            if (s == null) return '';
            return String(s)
                .replace(/&/g, '&amp;').replace(/</g, '&lt;')
                .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
        };

        const renderStarsStatic = (rating) => {
            let html = '';
            for (let i = 1; i <= 5; i++) {
                html += i <= rating ? '★' : '☆';
            }
            return html;
        };

        const renderStarPicker = (name, selected) => {
            let html = '<div class="comments-rating-picker inline-rating-picker" data-current="' + selected + '">';
            for (let i = 1; i <= 5; i++) {
                html += '<button type="button" class="rating-star' + (i <= selected ? ' selected' : '') + '" data-value="' + i + '">★</button>';
            }
            html += '</div>';
            return html;
        };

        const post = (params) => {
            const body = new URLSearchParams(params);
            return fetch(contextPath + '/comment', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: body
            });
        };

        const renderComments = (data) => {
            const comments = data.comments || [];
            badge.style.display = comments.length > 0 ? 'inline-flex' : 'none';
            badge.textContent = comments.length;

            if (comments.length === 0) {
                listEl.innerHTML = '';
                emptyEl.style.display = 'block';
            } else {
                emptyEl.style.display = 'none';

                listEl.innerHTML = comments.map(c => {
                    const isOwner = currentAccountId && c.accountId === currentAccountId;
                    const isHidden = c.status === 'Hidden';
                    const isEditing = editingCommentId === c.commentId;

                    if (isEditing) {
                        return '' +
                            '<div class="comment-item comment-item-editing" data-comment-id="' + c.commentId + '">' +
                                '<div class="comment-item-head">' +
                                    '<strong>' + escapeHtml(c.accountFullName || c.accountUsername) + '</strong>' +
                                '</div>' +
                                renderStarPicker('edit', c.rating) +
                                '<textarea class="edit-content-input" rows="3" maxlength="1000">' + escapeHtml(c.content) + '</textarea>' +
                                '<div class="comment-edit-actions">' +
                                    '<button type="button" class="comment-action-btn comment-save-btn" data-action="save-edit" data-comment-id="' + c.commentId + '">Save</button>' +
                                    '<button type="button" class="comment-action-btn comment-cancel-btn" data-action="cancel-edit">Cancel</button>' +
                                '</div>' +
                            '</div>';
                    }

                    let actionsHtml = '';
                    if (isOwner && c.canEdit) {
                        actionsHtml += '<button type="button" class="comment-action-btn" data-action="edit" data-comment-id="' + c.commentId + '">Edit</button>';
                    }
                    if (isOwner) {
                        actionsHtml += '<button type="button" class="comment-action-btn comment-danger-btn" data-action="delete" data-comment-id="' + c.commentId + '">Delete</button>';
                    }
                    if (isStaffOrAdmin) {
                        actionsHtml += '<button type="button" class="comment-action-btn" data-action="toggle" data-comment-id="' + c.commentId + '">' + (isHidden ? 'Unhide' : 'Hide') + '</button>';
                    }

                    return '' +
                        '<div class="comment-item' + (isHidden ? ' comment-hidden' : '') + '" data-comment-id="' + c.commentId + '">' +
                            '<div class="comment-item-head">' +
                                '<strong>' + escapeHtml(c.accountFullName || c.accountUsername) + '</strong>' +
                                '<span class="comment-stars">' + renderStarsStatic(c.rating) + '</span>' +
                            '</div>' +
                            '<p class="comment-content">' + escapeHtml(c.content) + '</p>' +
                            '<div class="comment-meta">' +
                                '<span>' + escapeHtml(c.createdAt) + '</span>' +
                                (c.variantInfo ? '<span> &middot; ' + escapeHtml(c.variantInfo) + '</span>' : '') +
                                (isHidden ? '<span class="comment-hidden-tag"> &middot; Hidden</span>' : '') +
                            '</div>' +
                            (actionsHtml ? '<div class="comment-item-actions">' + actionsHtml + '</div>' : '') +
                        '</div>';
                }).join('');
            }

            if (data.eligibleOrderItemId) {
                addPanel.style.display = 'block';
                loginNote.style.display = 'none';
            } else {
                addPanel.style.display = 'none';
                loginNote.style.display = 'block';
            }
        };

        const loadComments = () => {
            listEl.innerHTML = '<div class="comments-loading">Loading comments...</div>';
            fetch(contextPath + '/comment-data?productId=' + encodeURIComponent(productId))
                .then(res => res.json())
                .then(data => {
                    dataLoaded = true;
                    renderComments(data);
                })
                .catch(() => {
                    listEl.innerHTML = '<div class="comments-loading">Failed to load comments.</div>';
                });
        };

        const refreshBadgeOnly = () => {
            fetch(contextPath + '/comment-data?productId=' + encodeURIComponent(productId))
                .then(res => res.json())
                .then(data => {
                    const count = (data.comments || []).length;
                    if (count > 0) {
                        badge.style.display = 'inline-flex';
                        badge.textContent = count;
                    }
                })
                .catch(() => {});
        };

        const openModal = () => {
            overlay.classList.add('active');
            document.body.style.overflow = 'hidden';
            loadComments();
        };

        const closeModal = () => {
            overlay.classList.remove('active');
            document.body.style.overflow = '';
            editingCommentId = null;
        };

        if (openBtn) openBtn.addEventListener('click', openModal);
        if (closeBtn) closeBtn.addEventListener('click', closeModal);
        if (overlay) {
            overlay.addEventListener('click', (e) => {
                if (e.target === overlay) closeModal();
            });
        }

        // ----- Add new review -----
        if (ratingPicker) {
            const stars = Array.from(ratingPicker.querySelectorAll('.rating-star'));
            stars.forEach(star => {
                star.addEventListener('click', () => {
                    const value = star.dataset.value;
                    ratingValueInput.value = value;
                    stars.forEach(s => s.classList.toggle('selected', Number(s.dataset.value) <= Number(value)));
                });
            });
        }

        if (submitCommentBtn) {
            submitCommentBtn.addEventListener('click', () => {
                const rating = ratingValueInput.value;
                const content = (commentContentInput.value || '').trim();
                if (!rating) {
                    alert('Please choose a star rating.');
                    return;
                }
                if (!content) {
                    alert('Please write a comment.');
                    return;
                }

                submitCommentBtn.disabled = true;
                post({ action: 'add', productId: productId, rating: rating, content: content })
                    .then(() => {
                        commentContentInput.value = '';
                        ratingValueInput.value = '';
                        ratingPicker.querySelectorAll('.rating-star').forEach(s => s.classList.remove('selected'));
                        loadComments();
                    })
                    .catch(() => alert('Could not submit your review. Please try again.'))
                    .finally(() => { submitCommentBtn.disabled = false; });
            });
        }

        // ----- Edit / Delete / Hide (event delegation) -----
        listEl.addEventListener('click', (e) => {
            const button = e.target.closest('[data-action]');
            if (!button) return;
            const action = button.dataset.action;
            const commentId = button.dataset.commentId;

            if (action === 'edit') {
                editingCommentId = commentId;
                loadComments();
            }

            if (action === 'cancel-edit') {
                editingCommentId = null;
                loadComments();
            }

            if (action === 'save-edit') {
                const item = listEl.querySelector('.comment-item-editing[data-comment-id="' + commentId + '"]');
                if (!item) return;
                const pickerEl = item.querySelector('.inline-rating-picker');
                const ratingButtons = Array.from(pickerEl ? pickerEl.querySelectorAll('.rating-star') : []);
                const activeRating = ratingButtons.filter(b => b.classList.contains('selected')).length;
                const content = (item.querySelector('.edit-content-input').value || '').trim();

                if (!activeRating) {
                    alert('Please choose a star rating.');
                    return;
                }
                if (!content) {
                    alert('Comment cannot be empty.');
                    return;
                }

                button.disabled = true;
                post({ action: 'update', productId: productId, commentId: commentId, rating: activeRating, content: content })
                    .then(() => {
                        editingCommentId = null;
                        loadComments();
                    })
                    .catch(() => alert('Could not save changes. Please try again.'))
                    .finally(() => { button.disabled = false; });
            }

            if (action === 'delete') {
                if (!confirm('Delete this comment? This cannot be undone.')) return;
                button.disabled = true;
                post({ action: 'delete', productId: productId, commentId: commentId })
                    .then(() => loadComments())
                    .catch(() => alert('Could not delete the comment. Please try again.'))
                    .finally(() => { button.disabled = false; });
            }

            if (action === 'toggle') {
                button.disabled = true;
                post({ action: 'toggle', productId: productId, commentId: commentId })
                    .then(() => loadComments())
                    .catch(() => alert('Could not update visibility. Please try again.'))
                    .finally(() => { button.disabled = false; });
            }
        });

        // Inline rating picker clicks (inside edit mode)
        listEl.addEventListener('click', (e) => {
            const star = e.target.closest('.inline-rating-picker .rating-star');
            if (!star) return;
            const picker = star.closest('.inline-rating-picker');
            const value = Number(star.dataset.value);
            Array.from(picker.querySelectorAll('.rating-star')).forEach((s, idx) => {
                s.classList.toggle('selected', (idx + 1) <= value);
            });
        });

        // Load comment count quietly on page load so badge shows without opening modal
        refreshBadgeOnly();
    })();
</script>
