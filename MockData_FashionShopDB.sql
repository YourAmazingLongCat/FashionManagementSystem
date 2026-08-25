-- ====================================================================
-- SCRIPT TẠO DỮ LIỆU ẢO (MOCK DATA) CHO DATABASE FashionShopDB
-- Hướng dẫn: Mở SQL Server Management Studio (SSMS), mở file này và nhấn F5 (Execute)
-- Mật khẩu mặc định cho tất cả tài khoản là: 123456
-- ====================================================================

USE FashionShopDB;
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;
GO

-- 1. XÓA DỮ LIỆU CŨ THEO THỨ TỰ RÀNG BUỘC KHÓA NGOẠI
DELETE FROM dbo.WalletTransactions;
DELETE FROM dbo.Wallets;
DELETE FROM dbo.Comments;
DELETE FROM dbo.Wishlists;
DELETE FROM dbo.Cart;
DELETE FROM dbo.OrderItems;
DELETE FROM dbo.Orders;
DELETE FROM dbo.WarehouseImports;
DELETE FROM dbo.ProductVariants;
DELETE FROM dbo.ProductImages;
DELETE FROM dbo.Products;
DELETE FROM dbo.Sizes;
DELETE FROM dbo.Colors;
DELETE FROM dbo.Categories;
DELETE FROM dbo.Employees;
DELETE FROM dbo.Customers;
GO

-- ====================================================================
-- 2. TÀI KHOẢN ADMIN & NHÂN VIÊN (dbo.Employees)
-- Password hash cho '123456': $2a$12$LAP8w3oqvBEr9mCCZvzW0eKSKZIfSXVLLtFvlg1FuPlsrZ4/pRibO
-- ====================================================================
INSERT INTO dbo.Employees (employeeId, username, fullName, email, phone, address, passwordHash, role, salary, status, avatar)
VALUES
('EMP001', 'admin', N'Trần Minh Admin', 'admin@gmail.com', '0987654321', N'123 Cầu Giấy, Hà Nội', '$2a$12$LAP8w3oqvBEr9mCCZvzW0eKSKZIfSXVLLtFvlg1FuPlsrZ4/pRibO', 'Admin', 25000000, 'Active', '/Assets/Images/Customer/1774232153957_Eula_icon.png'),
('EMP002', 'staff01', N'Nguyễn Thu Staff', 'staff@gmail.com', '0912345678', N'456 Lê Duẩn, Đà Nẵng', '$2a$12$LAP8w3oqvBEr9mCCZvzW0eKSKZIfSXVLLtFvlg1FuPlsrZ4/pRibO', 'Staff', 12000000, 'Active', '/Assets/Images/Customer/1774254042762_Login.png'),
('EMP003', 'staff02', N'Lê Hoàng Kho', 'staff2@gmail.com', '0934567890', N'789 Nguyễn Huệ, TP.HCM', '$2a$12$LAP8w3oqvBEr9mCCZvzW0eKSKZIfSXVLLtFvlg1FuPlsrZ4/pRibO', 'Staff', 10000000, 'Active', NULL);
GO

-- ====================================================================
-- 3. TÀI KHOẢN KHÁCH HÀNG (dbo.Customers)
-- ====================================================================
INSERT INTO dbo.Customers (customerId, username, fullName, email, phone, address, passwordHash, status, avatar)
VALUES
('CUST001', 'nguyenvana', N'Nguyễn Văn A', 'customer@gmail.com', '0901112223', N'12 Hai Bà Trưng, Hoàn Kiếm, Hà Nội', '$2a$12$LAP8w3oqvBEr9mCCZvzW0eKSKZIfSXVLLtFvlg1FuPlsrZ4/pRibO', 'Active', '/Assets/Images/Customer/1774887828570_images.jpg'),
('CUST002', 'tranthib', N'Trần Thị Bích', 'customer2@gmail.com', '0903334445', N'34 Điện Biên Phủ, Quận 3, TP.HCM', '$2a$12$LAP8w3oqvBEr9mCCZvzW0eKSKZIfSXVLLtFvlg1FuPlsrZ4/pRibO', 'Active', '/Assets/Images/Customer/1773851691715_images.jpg'),
('CUST003', 'lequangc', N'Lê Quang Cường', 'customer3@gmail.com', '0905556667', N'56 Hùng Vương, Hải Châu, Đà Nẵng', '$2a$12$LAP8w3oqvBEr9mCCZvzW0eKSKZIfSXVLLtFvlg1FuPlsrZ4/pRibO', 'Active', NULL);
GO

-- ====================================================================
-- 4. VÍ TIỀN KHÁCH HÀNG (dbo.Wallets)
-- ====================================================================
INSERT INTO dbo.Wallets (walletId, customerId, balance, walletStatus)
VALUES
('WAL001', 'CUST001', 5000000, 'Active'),
('WAL002', 'CUST002', 2500000, 'Active'),
('WAL003', 'CUST003', 1000000, 'Active');
GO

-- ====================================================================
-- 5. DANH MỤC SẢN PHẨM (dbo.Categories)
-- ====================================================================
INSERT INTO dbo.Categories (categoryId, name, description)
VALUES
('CAT001', 'Tops & Tees', N'Áo thun, áo phông streetwear, polo và áo kiểu thời trang'),
('CAT002', 'Outerwear', N'Áo khoác gió, áo jacket da, hoodie và sweater cao cấp'),
('CAT003', 'Accessories', N'Phụ kiện thời trang, nón, túi xách, balo, thắt lưng'),
('CAT004', 'Dresses & Skirts', N'Váy đầm, chân váy xếp ly, chân váy chữ A cá tính'),
('CAT005', 'Pants & Jeans', N'Quần tây âu, quần jean baggy, quần short năng động');
GO

-- ====================================================================
-- 6. BẢNG MÀU (dbo.Colors)
-- ====================================================================
INSERT INTO dbo.Colors (colorId, colorName, hexCode)
VALUES
('COL001', N'Đen (Black)', '#000000'),
('COL002', N'Trắng (White)', '#FFFFFF'),
('COL003', N'Be (Beige)', '#F5F5DC'),
('COL004', N'Xám (Grey)', '#808080'),
('COL005', N'Xanh Navy', '#000080'),
('COL006', N'Hồng (Pink)', '#FFC0CB'),
('COL007', N'Đỏ Nâu (Brown/Red)', '#8B0000');
GO

-- ====================================================================
-- 7. KÍCH THƯỚC (dbo.Sizes)
-- ====================================================================
INSERT INTO dbo.Sizes (sizeId, sizeName, categoryId)
VALUES
-- Sizes cho Tops & Tees
('SZ001', 'S', 'CAT001'),
('SZ002', 'M', 'CAT001'),
('SZ003', 'L', 'CAT001'),
('SZ004', 'XL', 'CAT001'),
-- Sizes cho Outerwear
('SZ005', 'M', 'CAT002'),
('SZ006', 'L', 'CAT002'),
('SZ007', 'XL', 'CAT002'),
-- Sizes cho Accessories
('SZ008', 'FreeSize', 'CAT003'),
-- Sizes cho Dresses & Skirts
('SZ009', 'S', 'CAT004'),
('SZ010', 'M', 'CAT004'),
('SZ011', 'L', 'CAT004'),
-- Sizes cho Pants & Jeans
('SZ012', '29', 'CAT005'),
('SZ013', '30', 'CAT005'),
('SZ014', '31', 'CAT005'),
('SZ015', '32', 'CAT005');
GO

-- ====================================================================
-- 8. SẢN PHẨM (dbo.Products)
-- ====================================================================
INSERT INTO dbo.Products (productId, categoryId, name, description, basePrice, status)
VALUES
-- Tops & Tees
('PROD001', 'CAT001', N'Áo Thun In Hình Shark Streetwear', N'Chất liệu cotton 100% 2 chiều 250gsm dày dặn, form oversize chuẩn phong cách đường phố.', 290000, 'Available'),
('PROD002', 'CAT001', N'Áo Thun Basic Boxy Fit Form Rộng', N'Áo phông trơn chất vải cao cấp chống nhăn, thoáng mát, dễ phối đồ hàng ngày.', 220000, 'Available'),
('PROD003', 'CAT001', N'Áo Polo Phối Bo Cổ Cổ Điển', N'Thiết kế lịch lãm, chất vải cá sấu mè co giãn 4 chiều mềm mịn, thấm hút mồ hôi tốt.', 350000, 'Available'),

-- Outerwear
('PROD004', 'CAT002', N'Áo Hoodie Nỉ Bông Unisex Classic', N'Vải nỉ bông dày dặn, giữ ấm tốt, mũ 2 lớp đứng form với logo thêu sắc nét.', 450000, 'Available'),
('PROD005', 'CAT002', N'Áo Khoác Da Biker Jacket Form Chuẩn', N'Chất da PU cao cấp chống nổ, lót dù gió bên trong, khóa kéo kim loại mạ tĩnh điện bền bỉ.', 850000, 'Available'),
('PROD006', 'CAT002', N'Áo Khoác Dù 2 Lớp Chống Thấm', N'Vải dù miro 2 lớp cản gió và chống nước nhẹ, phối viền phản quang thể thao.', 390000, 'Available'),

-- Dresses & Skirts
('PROD007', 'CAT004', N'Chân Váy Tag Sắt EcoChic Nữ Tính', N'Chân váy dáng chữ A đính tag kim loại sang trọng, có quần bảo hộ bên trong tiện lợi.', 320000, 'Available'),
('PROD008', 'CAT004', N'Chân Váy Xếp Ly Tennis Skirt', N'Phong cách trẻ trung năng động, xếp ly sắc nét, chất tuyết mưa dày dặn tôn dáng.', 280000, 'Available'),

-- Pants & Jeans
('PROD009', 'CAT005', N'Quần Tây Âu Nam Nữ Ống Suông', N'Chất vải tuyết hàn cao cấp không nhăn xù, cạp chun ẩn thoải mái khi vận động.', 420000, 'Available'),
('PROD010', 'CAT005', N'Quần Jean Baggy Rách Gối Unisex', N'Vải denim 13oz bền chắc, wash màu xám khói vintage cực chất.', 490000, 'Available'),

-- Accessories
('PROD011', 'CAT003', N'Túi Canvas Đeo Chéo Streetwear', N'Túi vải bố bền đẹp, nhiều ngăn chứa đồ, thích hợp đi học và đi chơi.', 180000, 'Available'),
('PROD012', 'CAT003', N'Nón Lưỡi Trai Thêu Logo Minimalist', N'Chất kaki cao cấp, khóa kim loại điều chỉnh kích cỡ linh hoạt, form nón cứng cáp.', 150000, 'Available');
GO

-- ====================================================================
-- 9. HÌNH ẢNH SẢN PHẨM (dbo.ProductImages)
-- ====================================================================
INSERT INTO dbo.ProductImages (imageId, productId, imageUrl, isPrimary)
VALUES
-- PROD001
('IMG001', 'PROD001', '/Assets/Images/Product/TShirt/ao_thun_in_hinh_thiet_ke_ca_map_streetwear_mau_den.jpg.webp', 1),
('IMG002', 'PROD001', '/Assets/Images/Product/TShirt/3__1__cd0865ca50544899954b18ea36893b7f_master.webp', 0),

-- PROD002
('IMG003', 'PROD002', '/Assets/Images/Product/TShirt/6_copy_2e69ce1a05794dfbbb4fe8302be77ba8_master.jpg', 1),
('IMG004', 'PROD002', '/Assets/Images/Product/TShirt/images (12).jfif', 0),

-- PROD003
('IMG005', 'PROD003', '/Assets/Images/Product/TShirt/images (13).jfif', 1),

-- PROD004 (Hoodie)
('IMG006', 'PROD004', '/Assets/Images/Product/Hoodie/S0f3131039e454523a42a0360ccd21050Y.avif', 1),
('IMG007', 'PROD004', '/Assets/Images/Product/Hoodie/images (17).jfif', 0),
('IMG008', 'PROD004', '/Assets/Images/Product/Hoodie/images (18).jfif', 0),

-- PROD005 (Leather Jacket)
('IMG009', 'PROD005', '/Assets/Images/Product/Jacket/leather_5f0633f598304a8fab08e11adbda94ed.jpg', 1),
('IMG010', 'PROD005', '/Assets/Images/Product/Jacket/images (1).jfif', 0),

-- PROD006 (Wind Jacket)
('IMG011', 'PROD006', '/Assets/Images/Product/Jacket/images (5).jfif', 1),

-- PROD007 (Skirt EcoChic)
('IMG012', 'PROD007', '/Assets/Images/Product/chan-vay-tag-sat-ecochic-hong.png', 1),
('IMG013', 'PROD007', '/Assets/Images/Product/Skirt/z3439735412566183bbe512d9122cda0072c3c0e755f4d.jpg', 0),

-- PROD008 (Tennis Skirt)
('IMG014', 'PROD008', '/Assets/Images/Product/Skirt/images (1).jfif', 1),
('IMG015', 'PROD008', '/Assets/Images/Product/Skirt/images (2).jfif', 0),

-- PROD009 (Trousers)
('IMG016', 'PROD009', '/Assets/Images/Product/Trousers/GGTR02000302B_021_0.webp', 1),
('IMG017', 'PROD009', '/Assets/Images/Product/d9fe2fe6d91292f846277e7b0db117da.jpg', 0),

-- PROD010 (Jeans)
('IMG018', 'PROD010', '/Assets/Images/Product/images (10).jfif', 1),
('IMG019', 'PROD010', '/Assets/Images/Product/images (11).jfif', 0),

-- PROD011 (Canvas Bag)
('IMG020', 'PROD011', '/Assets/Images/Product/images (8).jfif', 1),

-- PROD012 (Cap)
('IMG021', 'PROD012', '/Assets/Images/Product/images (9).jfif', 1);
GO

-- ====================================================================
-- 10. BIẾN THỂ SẢN PHẨM (dbo.ProductVariants)
-- ====================================================================
INSERT INTO dbo.ProductVariants (variantId, productId, sizeId, colorId, sku, stockQty, reservedQty, priceOverride)
VALUES
-- PROD001 (Áo thun Shark)
('VAR001', 'PROD001', 'SZ002', 'COL001', 'SHARK-M-BLK', 50, 2, NULL),
('VAR002', 'PROD001', 'SZ003', 'COL001', 'SHARK-L-BLK', 45, 0, NULL),
('VAR003', 'PROD001', 'SZ002', 'COL002', 'SHARK-M-WHT', 30, 0, NULL),
('VAR004', 'PROD001', 'SZ003', 'COL002', 'SHARK-L-WHT', 25, 1, NULL),

-- PROD002 (Áo thun Basic)
('VAR005', 'PROD002', 'SZ002', 'COL001', 'BASIC-M-BLK', 60, 0, 200000),
('VAR006', 'PROD002', 'SZ003', 'COL003', 'BASIC-L-BGE', 40, 0, 200000),

-- PROD003 (Áo Polo)
('VAR007', 'PROD003', 'SZ002', 'COL005', 'POLO-M-NVY', 35, 0, NULL),
('VAR008', 'PROD003', 'SZ003', 'COL005', 'POLO-L-NVY', 30, 0, NULL),

-- PROD004 (Hoodie)
('VAR009', 'PROD004', 'SZ005', 'COL001', 'HOOD-M-BLK', 40, 1, NULL),
('VAR010', 'PROD004', 'SZ006', 'COL004', 'HOOD-L-GRY', 35, 0, NULL),

-- PROD005 (Leather Jacket)
('VAR011', 'PROD005', 'SZ005', 'COL001', 'LEATH-M-BLK', 20, 0, NULL),
('VAR012', 'PROD005', 'SZ006', 'COL001', 'LEATH-L-BLK', 15, 0, NULL),

-- PROD006 (Wind Jacket)
('VAR013', 'PROD006', 'SZ005', 'COL005', 'WIND-M-NVY', 50, 0, NULL),

-- PROD007 (Chân váy EcoChic)
('VAR014', 'PROD007', 'SZ009', 'COL006', 'SKIRT-S-PNK', 25, 0, NULL),
('VAR015', 'PROD007', 'SZ010', 'COL006', 'SKIRT-M-PNK', 20, 0, NULL),

-- PROD008 (Tennis Skirt)
('VAR016', 'PROD008', 'SZ009', 'COL002', 'TENNIS-S-WHT', 30, 0, NULL),
('VAR017', 'PROD008', 'SZ010', 'COL001', 'TENNIS-M-BLK', 30, 0, NULL),

-- PROD009 (Quần tây âu)
('VAR018', 'PROD009', 'SZ013', 'COL001', 'PANTS-30-BLK', 40, 0, NULL),
('VAR019', 'PROD009', 'SZ014', 'COL004', 'PANTS-31-GRY', 35, 0, NULL),

-- PROD010 (Quần Jean)
('VAR020', 'PROD010', 'SZ013', 'COL005', 'JEAN-30-NVY', 45, 0, NULL),

-- PROD011 (Túi Canvas)
('VAR021', 'PROD011', 'SZ008', 'COL003', 'BAG-FREE-BGE', 80, 0, NULL),

-- PROD012 (Nón Lưỡi Trai)
('VAR022', 'PROD012', 'SZ008', 'COL001', 'CAP-FREE-BLK', 100, 0, NULL);
GO

-- ====================================================================
-- 11. NHẬP KHO BAN ĐẦU (dbo.WarehouseImports)
-- ====================================================================
INSERT INTO dbo.WarehouseImports (importId, variantId, quantity, importPrice, employeeId, importedAt)
VALUES
('IMP001', 'VAR001', 50, 150000, 'EMP002', DATEADD(DAY, -10, GETDATE())),
('IMP002', 'VAR002', 45, 150000, 'EMP002', DATEADD(DAY, -10, GETDATE())),
('IMP003', 'VAR009', 40, 250000, 'EMP002', DATEADD(DAY, -8, GETDATE())),
('IMP004', 'VAR011', 20, 500000, 'EMP002', DATEADD(DAY, -5, GETDATE())),
('IMP005', 'VAR014', 25, 180000, 'EMP002', DATEADD(DAY, -3, GETDATE())),
('IMP006', 'VAR021', 80, 90000, 'EMP002', DATEADD(DAY, -2, GETDATE()));
GO

-- ====================================================================
-- 12. GIỎ HÀNG (dbo.Cart)
-- ====================================================================
INSERT INTO dbo.Cart (cartId, customerId, variantId, quantity)
VALUES
('CART001', 'CUST001', 'VAR001', 1),
('CART002', 'CUST001', 'VAR021', 2),
('CART003', 'CUST002', 'VAR014', 1);
GO

-- ====================================================================
-- 13. DANH SÁCH YÊU THÍCH (dbo.Wishlists)
-- ====================================================================
INSERT INTO dbo.Wishlists (wishlistId, customerId, productId, createdAt)
VALUES
('WISH001', 'CUST001', 'PROD001', DATEADD(DAY, -5, GETDATE())),
('WISH002', 'CUST001', 'PROD004', DATEADD(DAY, -4, GETDATE())),
('WISH003', 'CUST001', 'PROD007', DATEADD(DAY, -2, GETDATE())),
('WISH004', 'CUST002', 'PROD005', DATEADD(DAY, -1, GETDATE()));
GO

-- ====================================================================
-- 14. ĐƠN HÀNG MẪU (dbo.Orders & dbo.OrderItems)
-- ====================================================================
-- Đơn hàng 1: Đã giao thành công (Thanh toán qua VNPay)
INSERT INTO dbo.Orders (orderId, customerId, orderStatus, shippingAddress, shippingPhone, placedAt, totalAmount, paymentMethod, paymentStatus, paidAmount, issuedDate)
VALUES ('ORD001', 'CUST001', 'Delivered', N'12 Hai Bà Trưng, Hoàn Kiếm, Hà Nội', '0901112223', DATEADD(DAY, -7, GETDATE()), 740000, 'VNPay', 'Paid', 740000, DATEADD(DAY, -7, GETDATE()));

INSERT INTO dbo.OrderItems (orderItemId, orderId, variantId, quantity, unitPrice, discountAmount)
VALUES
('ITEM001', 'ORD001', 'VAR001', 1, 290000, 0),
('ITEM002', 'ORD001', 'VAR009', 1, 450000, 0);

-- Đơn hàng 2: Đang giao hàng (Thanh toán COD)
INSERT INTO dbo.Orders (orderId, customerId, orderStatus, shippingAddress, shippingPhone, placedAt, totalAmount, paymentMethod, paymentStatus, paidAmount, issuedDate)
VALUES ('ORD002', 'CUST002', 'Shipping', N'34 Điện Biên Phủ, Quận 3, TP.HCM', '0903334445', DATEADD(DAY, -2, GETDATE()), 320000, 'COD', 'Pending', 0, DATEADD(DAY, -2, GETDATE()));

INSERT INTO dbo.OrderItems (orderItemId, orderId, variantId, quantity, unitPrice, discountAmount)
VALUES
('ITEM003', 'ORD002', 'VAR014', 1, 320000, 0);

-- Đơn hàng 3: Chờ xác nhận (Pending)
INSERT INTO dbo.Orders (orderId, customerId, orderStatus, shippingAddress, shippingPhone, placedAt, totalAmount, paymentMethod, paymentStatus, paidAmount, issuedDate)
VALUES ('ORD003', 'CUST001', 'Pending', N'12 Hai Bà Trưng, Hoàn Kiếm, Hà Nội', '0901112223', GETDATE(), 290000, 'Wallet', 'Paid', 290000, GETDATE());

INSERT INTO dbo.OrderItems (orderItemId, orderId, variantId, quantity, unitPrice, discountAmount)
VALUES
('ITEM004', 'ORD003', 'VAR001', 1, 290000, 0);
GO

-- ====================================================================
-- 15. BÌNH LUẬN & ĐÁNH GIÁ (dbo.Comments)
-- ====================================================================
INSERT INTO dbo.Comments (commentId, variantId, customerId, rating, content, createdAt, status)
VALUES
('CMT001', 'VAR001', 'CUST001', 5, N'Áo chất vải rất dày dặn và mát mẻ, form oversize mặc cực kì ưng ý!', DATEADD(DAY, -5, GETDATE()), 'Active'),
('CMT002', 'VAR009', 'CUST001', 5, N'Mũ áo hoodie dày dặn đứng form, lót nỉ mềm mịn giữ ấm siêu tốt.', DATEADD(DAY, -4, GETDATE()), 'Active'),
('CMT003', 'VAR014', 'CUST002', 4, N'Chân váy xinh, form chuẩn, tag sắt sáng bóng xịn xò.', DATEADD(DAY, -1, GETDATE()), 'Active'),
('CMT004', 'VAR005', 'CUST003', 5, N'Áo thun basic giá hợp lý, chất lượng vượt mong đợi.', DATEADD(DAY, -2, GETDATE()), 'Active');
GO

PRINT N'✅ ĐÃ IMPORT THÀNH CÔNG DỮ LIỆU MẪU CHO FASHION SHOP DB!';
GO
