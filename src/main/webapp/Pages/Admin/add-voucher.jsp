<%-- 
    Document   : add-voucher
    Created on : Jul 25, 2026, 7:45:52 AM
    Author     : ADMIN
--%>


<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>Thêm Voucher Mới</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    </head>
    <body class="bg-light">
        <div class="container mt-5 mb-5">
            <div class="card shadow-sm mx-auto" style="max-width: 700px;">
                <div class="card-header bg-success text-white py-3">
                    <h4 class="mb-0"><i class="fas fa-ticket-alt"></i> Thêm Mã Giảm Giá Mới</h4>
                </div>
                <div class="card-body p-4">
                    <!-- Form này sẽ gửi dữ liệu về ManageVoucherServlet để xử lý -->
                    <form action="${pageContext.request.contextPath}/ManageVoucherServlet" method="post" id="voucherForm">
                        <input type="hidden" name="action" value="addVoucher">

                        <div class="mb-3">
                            <label class="form-label fw-bold">Mã Code <span class="text-danger">*</span></label>
                            <input type="text" name="voucherCode" class="form-control" placeholder="VD: SUMMER2026, FREESHIP..." required>
                        </div>

                        <div class="row">
                            <div class="col-md-6 mb-3">
                                <label class="form-label fw-bold">Loại giảm giá</label>
                                <select name="discountType" id="discountType" class="form-select">
                                    <option value="Percentage">Theo phần trăm (%)</option>
                                    <option value="Fixed">Số tiền cố định (VNĐ)</option>
                                </select>
                            </div>
                            <div class="col-md-6 mb-3">
                                <label class="form-label fw-bold">Mức giảm <span class="text-danger">*</span></label>
                                <!-- Đã đổi type="number" thành type="text" và thêm id -->
                                <input type="text" name="discountValue" id="discountValue" class="form-control" placeholder="VD: 10 hoặc 50000" required>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-md-6 mb-3">
                                <label class="form-label fw-bold">Đơn hàng tối thiểu (VNĐ)</label>
                                <input type="text" name="minOrderValue" id="minOrderValue" class="form-control" value="0">
                            </div>
                            <div class="col-md-6 mb-3">
                                <label class="form-label fw-bold">Giảm tối đa (Nếu chọn loại %)</label>
                                <input type="text" name="maxDiscount" id="maxDiscount" class="form-control" placeholder="Để trống nếu không giới hạn">
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-md-6 mb-3">
                                <label class="form-label fw-bold">Giới hạn số lượt dùng <span class="text-danger">*</span></label>
                                <input type="number" name="usageLimit" class="form-control" value="100" required>
                            </div>
                            <div class="col-md-6 mb-3">
                                <label class="form-label fw-bold">Trạng thái ban đầu</label>
                                <select name="status" class="form-select">
                                    <option value="Active">Hoạt động (Active)</option>
                                    <option value="Inactive">Tạm khóa (Inactive)</option>
                                </select>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-md-6 mb-3">
                                <label class="form-label fw-bold">Ngày bắt đầu <span class="text-danger">*</span></label>
                                <input type="datetime-local" name="startDate" class="form-control" required>
                            </div>
                            <div class="col-md-6 mb-3">
                                <label class="form-label fw-bold">Ngày kết thúc <span class="text-danger">*</span></label>
                                <input type="datetime-local" name="endDate" class="form-control" required>
                            </div>
                        </div>

                        <hr class="mt-4">
                        <div class="d-flex justify-content-between mt-3">
                            <a href="${pageContext.request.contextPath}/manage-voucher" class="btn btn-secondary">
                                <i class="fas fa-arrow-left"></i> Hủy & Quay lại
                            </a>
                            <button type="submit" class="btn btn-success px-4">
                                <i class="fas fa-save"></i> Lưu Voucher
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

        <script>
            document.addEventListener("DOMContentLoaded", function () {
                const discountType = document.getElementById("discountType");
                const discountValue = document.getElementById("discountValue");
                const minOrderValue = document.getElementById("minOrderValue");
                const maxDiscount = document.getElementById("maxDiscount");
                const form = document.getElementById("voucherForm");

                function formatNumberWithCommas(numStr) {
                    return numStr.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
                }

                function cleanNumber(val) {
                    return val.replace(/[^\d]/g, "");
                }

                function applyFormatting(inputElement, isDiscountField) {
                    let rawValue = cleanNumber(inputElement.value);
                    if (!rawValue)
                        return;

                    let formattedValue = formatNumberWithCommas(rawValue);

                    if (isDiscountField) {
                        if (discountType.value === 'Fixed') {
                            inputElement.value = formattedValue + " VNĐ";
                        } else {
                            inputElement.value = formattedValue + " %";
                        }
                    } else {
                        inputElement.value = formattedValue + " VNĐ";
                    }
                }

                function removeFormatting(inputElement) {
                    inputElement.value = cleanNumber(inputElement.value);
                }

                discountValue.addEventListener("focus", function () {
                    removeFormatting(this);
                });
                discountValue.addEventListener("blur", function () {
                    applyFormatting(this, true);
                });

                minOrderValue.addEventListener("focus", function () {
                    removeFormatting(this);
                });
                minOrderValue.addEventListener("blur", function () {
                    applyFormatting(this, false);
                });

                maxDiscount.addEventListener("focus", function () {
                    removeFormatting(this);
                });
                maxDiscount.addEventListener("blur", function () {
                    applyFormatting(this, false);
                });

                discountType.addEventListener("change", function () {
                    if (discountValue.value) {
                        removeFormatting(discountValue);
                        applyFormatting(discountValue, true);
                    }
                });

                form.addEventListener("submit", function () {
                    removeFormatting(discountValue);
                    removeFormatting(minOrderValue);
                    removeFormatting(maxDiscount);
                });
            });
        </script>
    </body>
</html>
