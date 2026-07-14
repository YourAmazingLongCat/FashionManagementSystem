<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    
    <style>
        body { background: #f0f2f5; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }
        .sidebar {
            background: linear-gradient(180deg, #2c3e50, #1a252f);
            min-height: 100vh; padding: 0; color: #ecf0f1;
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
        .sidebar .nav-link.back-home {
            margin-top: 30px; border-top: 1px solid #34495e; padding-top: 20px;
            color: #f1c40f;
        }
        .sidebar .nav-link.back-home:hover { background: #2c3e50; color: #f39c12; }
        .main-content { padding: 20px 30px; }
        
        /* Chỉnh lại kiểu Card cho giống mẫu */
        .stat-card, .chart-card {
            background: #fff; 
            border-radius: 4px; /* Bo góc nhẹ theo hình mẫu */
            padding: 20px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.03); 
            border: none;
            height: 100%;
        }
        .chart-card-title {
            color: #1d3161;
            font-weight: 600;
            text-align: center;
            margin-bottom: 20px;
            font-size: 1.1rem;
        }
        .custom-legend {
            font-size: 0.9rem;
            color: #4a5568;
        }
        .custom-legend .legend-val {
            color: #1d3161;
            font-weight: 600;
        }
        
        .hidden-section { display: none; }
    </style>
</head>
<body>
<div class="container-fluid p-0">
    <div class="row g-0">
        <!-- Sidebar -->
        <div class="col-md-3 col-lg-2 sidebar">
            <div class="brand"><i class="fas fa-chart-pie"></i> Admin</div>
            <ul class="nav flex-column">
                <li class="nav-item">
                    <a class="nav-link ${param.section == null || param.section == 'dashboard' ? 'active' : ''}" 
                       href="?section=dashboard">
                        <i class="fas fa-tachometer-alt"></i> Dashboard Overview
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${param.section == 'accountManage' ? 'active' : ''}" 
                       href="?section=accountManage">
                        <i class="fas fa-user-cog"></i> Account Management
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${param.section == 'customerStats' ? 'active' : ''}" 
                       href="?section=customerStats">
                        <i class="fas fa-users"></i> Customer Statistics
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${param.section == 'revenueStats' ? 'active' : ''}" 
                       href="?section=revenueStats">
                        <i class="fas fa-dollar-sign"></i> Revenue Statistics
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${param.section == 'profitStats' ? 'active' : ''}" 
                       href="?section=profitStats">
                        <i class="fas fa-chart-line"></i> Profit Statistics
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${param.section == 'orderStats' ? 'active' : ''}" 
                       href="?section=orderStats">
                        <i class="fas fa-shopping-cart"></i> Order Statistics
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link back-home" href="${pageContext.request.contextPath}/home">
                        <i class="fas fa-home"></i> Back to Home
                    </a>
                </li>
            </ul>
        </div>

        <!-- Main Content -->
        <div class="col-md-9 col-lg-10 main-content">
            
            <c:if test="${not empty toastMsg}">
                <div class="alert alert-success alert-dismissible fade show" role="alert">
                    <i class="fas fa-check-circle me-2"></i> ${toastMsg}
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </c:if>

            <c:set var="currentSection" value="${param.section}" />
            <c:if test="${empty currentSection}">
                <c:set var="currentSection" value="dashboard" />
            </c:if>

            <!-- ================= DASHBOARD HIỆN ĐẠI (GRID LAYOUT) ================= -->
            <div id="dashboard" class="${currentSection != 'dashboard' ? 'hidden-section' : ''}">
                <div class="row">
                    
                    <!-- CỘT 1: BIỂU ĐỒ TRÒN (Lý do trả hàng) -->
                    <div class="col-lg-4 col-md-12 mb-4">
                        <div class="chart-card d-flex flex-column align-items-center">
                            <h5 class="chart-card-title">Lý do trả hàng</h5>
                            <div style="width: 220px; height: 220px;">
                                <canvas id="reasonPieChart"></canvas>
                            </div>
                            
                            <!-- Custom HTML Legend -->
                            <div class="w-100 mt-4 px-3 custom-legend">
                                <div class="d-flex justify-content-between mb-2">
                                    <span><i class="fas fa-square me-2" style="color: #1d3161;"></i> Không vừa size</span>
                                    <span class="legend-val">47%</span>
                                </div>
                                <div class="d-flex justify-content-between mb-2">
                                    <span><i class="fas fa-square me-2" style="color: #304b8a;"></i> Hàng lỗi</span>
                                    <span class="legend-val">32%</span>
                                </div>
                                <div class="d-flex justify-content-between mb-2">
                                    <span><i class="fas fa-square me-2" style="color: #4c68a8;"></i> Hàng hư hại</span>
                                    <span class="legend-val">10%</span>
                                </div>
                                <div class="d-flex justify-content-between mb-2">
                                    <span><i class="fas fa-square me-2" style="color: #6f88c2;"></i> Hàng giao chậm</span>
                                    <span class="legend-val">8%</span>
                                </div>
                                <div class="d-flex justify-content-between">
                                    <span><i class="fas fa-square me-2" style="color: #98aee0;"></i> Giao hàng sai</span>
                                    <span class="legend-val">3%</span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- CỘT 2: ĐƯỜNG (Tỉ lệ) + BÁN NGUYỆT (Gauge) -->
                    <div class="col-lg-4 col-md-12 mb-4 d-flex flex-column">
                        <div class="chart-card mb-4" style="flex: 1;">
                            <h5 class="chart-card-title">Tỉ lệ đơn hàng</h5>
                            <div style="height: 180px;">
                                <canvas id="rateLineChart"></canvas>
                            </div>
                        </div>
                        
                        <div class="chart-card text-center d-flex flex-column justify-content-center align-items-center" style="flex: 1;">
                            <div style="width: 200px; height: 100px; position: relative;">
                                <canvas id="gaugeChart"></canvas>
                                <!-- Cột mốc nhỏ bên phải Gauge -->
                                <div style="position: absolute; top: 40px; right: -25px; border: 1px solid #1d3161; padding: 2px 6px; font-size: 0.8rem; color: #1d3161;">
                                    92.1%
                                </div>
                            </div>
                            <h2 class="fw-bold mb-0 mt-3" style="color: #1d3161;">94.1%</h2>
                            <p class="text-muted small">Năm nay</p>
                        </div>
                    </div>

                    <!-- CỘT 3: CỘT (Tổng đơn) + THỐNG KÊ SỐ LỚN -->
                    <div class="col-lg-4 col-md-12 mb-4 d-flex flex-column">
                        <div class="chart-card mb-4" style="flex: 1;">
                            <h5 class="chart-card-title">Tổng đơn hàng</h5>
                            <div style="height: 180px;">
                                <canvas id="totalOrderBarChart"></canvas>
                            </div>
                        </div>
                        
                        <div class="chart-card text-center d-flex flex-column justify-content-center" style="flex: 1;">
                            <h1 class="fw-normal" style="color: #1d3161; font-size: 2.8rem; letter-spacing: 1px;">14.34M</h1>
                            <p class="mb-4" style="color: #1d3161; font-weight: 500;">Tổng đơn hàng</p>
                            
                            <h2 class="fw-normal mb-1" style="color: #1d3161; font-size: 2rem;">2.56</h2>
                            <p class="text-muted small mb-0">Đơn đặt hàng trung bình cho mỗi khách hàng</p>
                        </div>
                    </div>

                </div>
            </div>

            <!-- ================= ACCOUNT MANAGEMENT SECTION ================= -->
            <div id="accountManage" class="section-card ${currentSection != 'accountManage' ? 'hidden-section' : ''}">
                <div class="card shadow-sm border-0">
                    <div class="card-header bg-white py-3"><i class="fas fa-user-cog me-2"></i> Account Management</div>
                    <div class="card-body p-0">
                        <div class="table-responsive">
                            <table class="table table-hover mb-0 text-center align-middle">
                                <thead class="table-light">
                                    <tr>
                                        <th>ID</th>
                                        <th>Full Name</th>
                                        <th>Email</th>
                                        <th>Role</th>
                                        <th>Status</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${not empty accountList}">
                                            <c:forEach var="acc" items="${accountList}">
                                                <tr>
                                                    <td>${acc.accountId}</td>
                                                    <td>${acc.fullName}</td>
                                                    <td>${acc.email}</td>
                                                    <td>
                                                        <form action="${pageContext.request.contextPath}/admin" method="POST" class="m-0">
                                                            <input type="hidden" name="action" value="updateRole">
                                                            <input type="hidden" name="accountId" value="${acc.accountId}">
                                                            <input type="hidden" name="section" value="accountManage">
                                                            <select name="role" class="form-select form-select-sm w-auto mx-auto" onchange="this.form.submit()" ${acc.role == 'Admin' ? 'disabled' : ''}>
                                                                <option value="Admin" ${acc.role == 'Admin' ? 'selected' : ''}>Admin</option>
                                                                <option value="Staff" ${acc.role == 'Staff' ? 'selected' : ''}>Staff</option>
                                                                <option value="Customer" ${acc.role == 'Customer' ? 'selected' : ''}>Customer</option>
                                                            </select>
                                                        </form>
                                                    </td>
                                                    <td>
                                                        <form action="${pageContext.request.contextPath}/admin" method="POST" class="m-0">
                                                            <input type="hidden" name="action" value="updateStatus">
                                                            <input type="hidden" name="accountId" value="${acc.accountId}">
                                                            <input type="hidden" name="section" value="accountManage">
                                                            <select name="status" class="form-select form-select-sm w-auto mx-auto fw-bold ${acc.status == 'Active' ? 'text-success' : 'text-danger'}" onchange="this.form.submit()" ${acc.role == 'Admin' ? 'disabled' : ''}>
                                                                <option value="Active" ${acc.status == 'Active' ? 'selected' : ''}>Active</option>
                                                                <option value="Locked" ${acc.status == 'Locked' ? 'selected' : ''}>Locked</option>
                                                            </select>
                                                        </form>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <tr><td colspan="5" class="text-muted py-4">No accounts found</td></tr>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
            
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

<!-- Script vẽ các biểu đồ theo phong cách Navy Minimalist -->
<script>
    document.addEventListener("DOMContentLoaded", function() {
        
        // Bảng màu Navy từ đậm đến nhạt giống mẫu
        const navyColors = ['#1d3161', '#304b8a', '#4c68a8', '#6f88c2', '#98aee0'];
        const labelsShort = ['Ja T7', 'Fe T7', 'Ma T7', 'Ap T7', 'Ma T7', 'Ju T7', 'Ju T7', 'Au T7', 'Se T7', 'Oc T7', 'No T7', 'De T7'];

        // 1. BIỂU ĐỒ TRÒN (Pie Chart) - Lý do trả hàng
        const pieCtx = document.getElementById('reasonPieChart');
        if (pieCtx) {
            new Chart(pieCtx.getContext('2d'), {
                type: 'pie',
                data: {
                    labels: ['Không vừa size', 'Hàng lỗi', 'Hàng hư hại', 'Hàng giao chậm', 'Giao hàng sai'],
                    datasets: [{
                        data: [47, 32, 10, 8, 3],
                        backgroundColor: navyColors,
                        borderWidth: 0 // Không viền trắng chia cắt
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { display: false }, // Ẩn legend mặc định vì đã tự thiết kế HTML ở dưới
                        tooltip: { callbacks: { label: function(context) { return context.label + ': ' + context.raw + '%'; } } }
                    }
                }
            });
        }

        // 2. BIỂU ĐỒ ĐƯỜNG (Line Chart) - Tỉ lệ đơn hàng
        const lineCtx = document.getElementById('rateLineChart');
        if (lineCtx) {
            new Chart(lineCtx.getContext('2d'), {
                type: 'line',
                data: {
                    labels: labelsShort,
                    datasets: [{
                        data: [93.5, 92.8, 94.5, 94.2, 95.8, 97.2, 94.0, 92.5, 91.8, 90.8, 95.5, 96.8],
                        borderColor: '#1d3161',
                        borderWidth: 1.5,
                        pointRadius: 0, // Ẩn điểm chấm
                        pointHoverRadius: 4,
                        fill: false,
                        tension: 0 // Đường thẳng gấp khúc, không cong (như mẫu)
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: { legend: { display: false } },
                    scales: {
                        x: {
                            grid: { display: false, drawBorder: false },
                            ticks: { font: { size: 9 }, color: '#718096', maxRotation: 45, minRotation: 45 }
                        },
                        y: {
                            min: 90,
                            max: 97.5,
                            grid: { color: 'rgba(0,0,0,0.05)', drawBorder: false },
                            ticks: { 
                                stepSize: 2.5, 
                                font: { size: 10 }, color: '#718096',
                                callback: function(value) { return value + '%'; }
                            }
                        }
                    }
                }
            });
        }

        // 3. BIỂU ĐỒ BÁN NGUYỆT (Gauge Chart)
        const gaugeCtx = document.getElementById('gaugeChart');
        if (gaugeCtx) {
            new Chart(gaugeCtx.getContext('2d'), {
                type: 'doughnut',
                data: {
                    datasets: [{
                        data: [94.1, 5.9], // Giá trị đạt được và phần còn lại
                        backgroundColor: ['#1d3161', '#cbd5e1'], // Xanh navy đậm và xám nhạt
                        borderWidth: 0
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    circumference: 180, // Cắt nửa hình tròn
                    rotation: 270,      // Xoay lên trên
                    cutout: '80%',      // Độ mỏng của viền
                    plugins: { tooltip: { enabled: false } }, // Tắt hiển thị khi rê chuột
                    hover: { mode: null }
                }
            });
        }

        // 4. BIỂU ĐỒ CỘT (Bar Chart) - Tổng đơn hàng
        const barCtx = document.getElementById('totalOrderBarChart');
        if (barCtx) {
            new Chart(barCtx.getContext('2d'), {
                type: 'bar',
                data: {
                    labels: labelsShort,
                    datasets: [{
                        data: [1.3, 1.1, 0.95, 1.0, 1.05, 1.15, 1.1, 1.12, 1.2, 1.3, 1.4, 1.5],
                        backgroundColor: '#1d3161',
                        barPercentage: 0.6 // Cột thanh mảnh giống ảnh
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: { legend: { display: false } },
                    scales: {
                        x: {
                            grid: { display: false, drawBorder: false },
                            ticks: { font: { size: 9 }, color: '#718096', maxRotation: 45, minRotation: 45 }
                        },
                        y: {
                            beginAtZero: true,
                            max: 2.0,
                            grid: { color: 'rgba(0,0,0,0.05)', drawBorder: false },
                            ticks: { 
                                stepSize: 0.5, 
                                font: { size: 10 }, color: '#718096',
                                callback: function(value) { return value + 'M'; }
                            }
                        }
                    }
                }
            });
        }

    });
</script>
</body>
</html>