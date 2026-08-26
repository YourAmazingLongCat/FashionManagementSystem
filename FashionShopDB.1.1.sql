SET NOCOUNT ON;
SET XACT_ABORT ON;
GO

IF DB_ID(N'FashionShopDB') IS NULL
BEGIN
    CREATE DATABASE FashionShopDB;
END;
GO

USE FashionShopDB;
GO

IF OBJECT_ID(N'dbo.Customers', N'U') IS NOT NULL
   OR OBJECT_ID(N'dbo.Employees', N'U') IS NOT NULL
   OR OBJECT_ID(N'dbo.Accounts', N'U') IS NOT NULL
BEGIN
    THROW 51000,
          'FashionShopDB already contains project tables. Use an empty database or a migration script instead of rerunning this clean-install schema.',
          1;
END;
GO

/* =========================================================
   1. CUSTOMERS / EMPLOYEES
   ========================================================= */

CREATE TABLE dbo.Customers (
    customerId VARCHAR(20) NOT NULL,
    username VARCHAR(100) NOT NULL,
    fullName NVARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(15) NULL,
    address NVARCHAR(255) NULL,
    passwordHash VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL
        CONSTRAINT DF_Customers_Status DEFAULT ('Active'),
    avatar VARCHAR(500) NULL,
    createdAt DATETIME NOT NULL
        CONSTRAINT DF_Customers_CreatedAt DEFAULT (GETDATE()),

    CONSTRAINT PK_Customers
        PRIMARY KEY (customerId),

    CONSTRAINT UQ_Customers_Username
        UNIQUE (username),

    CONSTRAINT UQ_Customers_Email
        UNIQUE (email),

    CONSTRAINT CK_Customers_Status
        CHECK (status IN ('Active', 'Inactive', 'Locked'))
);
GO

CREATE TABLE dbo.Employees (
    employeeId VARCHAR(20) NOT NULL,
    username VARCHAR(100) NOT NULL,
    fullName NVARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(15) NULL,
    address NVARCHAR(255) NULL,
    passwordHash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL
        CONSTRAINT DF_Employees_Role DEFAULT ('Staff'),
    salary DECIMAL(12,2) NOT NULL
        CONSTRAINT DF_Employees_Salary DEFAULT (0),
    status VARCHAR(20) NOT NULL
        CONSTRAINT DF_Employees_Status DEFAULT ('Active'),
    avatar VARCHAR(500) NULL,
    createdAt DATETIME NOT NULL
        CONSTRAINT DF_Employees_CreatedAt DEFAULT (GETDATE()),

    CONSTRAINT PK_Employees
        PRIMARY KEY (employeeId),

    CONSTRAINT UQ_Employees_Username
        UNIQUE (username),

    CONSTRAINT UQ_Employees_Email
        UNIQUE (email),

    CONSTRAINT CK_Employees_Role
        CHECK (role IN ('Staff', 'Admin')),

    CONSTRAINT CK_Employees_Status
        CHECK (status IN ('Active', 'Inactive', 'Locked')),

    CONSTRAINT CK_Employees_Salary
        CHECK (salary >= 0)
);
GO

CREATE TRIGGER dbo.TR_Customers_NoEmployeeLoginCollision
ON dbo.Customers
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (
        SELECT 1
        FROM inserted i
        INNER JOIN dbo.Employees e
            ON e.username = i.username
            OR e.email = i.email
    )
    BEGIN
        THROW 51001,
              'Customer username/email must not duplicate an Employee username/email.',
              1;
    END;
END;
GO

CREATE TRIGGER dbo.TR_Employees_NoCustomerLoginCollision
ON dbo.Employees
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (
        SELECT 1
        FROM inserted i
        INNER JOIN dbo.Customers c
            ON c.username = i.username
            OR c.email = i.email
    )
    BEGIN
        THROW 51002,
              'Employee username/email must not duplicate a Customer username/email.',
              1;
    END;
END;
GO

/* =========================================================
   2. CATEGORY / SIZE / COLOR
   ========================================================= */

CREATE TABLE dbo.Categories (
    categoryId VARCHAR(20) NOT NULL,
    name NVARCHAR(100) NOT NULL,
    description NVARCHAR(500) NULL,

    CONSTRAINT PK_Categories
        PRIMARY KEY (categoryId),

    CONSTRAINT UQ_Categories_Name
        UNIQUE (name)
);
GO

CREATE TABLE dbo.Sizes (
    sizeId VARCHAR(20) NOT NULL,
    sizeName VARCHAR(20) NOT NULL,
    categoryId VARCHAR(20) NOT NULL,

    CONSTRAINT PK_Sizes
        PRIMARY KEY (sizeId),

    CONSTRAINT UQ_Sizes_SizeName_Category
        UNIQUE (sizeName, categoryId),

    CONSTRAINT FK_Sizes_Categories
        FOREIGN KEY (categoryId)
        REFERENCES dbo.Categories(categoryId)
);
GO

CREATE TABLE dbo.Colors (
    colorId VARCHAR(20) NOT NULL,
    colorName NVARCHAR(50) NOT NULL,
    hexCode VARCHAR(10) NULL,

    CONSTRAINT PK_Colors
        PRIMARY KEY (colorId),

    CONSTRAINT UQ_Colors_ColorName
        UNIQUE (colorName),

    CONSTRAINT CK_Colors_HexCode
        CHECK (
            hexCode IS NULL
            OR hexCode LIKE '#[0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f]'
        )
);
GO

/* =========================================================
   3. PRODUCT
   ========================================================= */

CREATE TABLE dbo.Products (
    productId VARCHAR(20) NOT NULL,
    categoryId VARCHAR(20) NOT NULL,
    name NVARCHAR(200) NOT NULL,
    description NVARCHAR(MAX) NULL,
    basePrice DECIMAL(12,2) NOT NULL
        CONSTRAINT DF_Products_BasePrice DEFAULT (0),
    status VARCHAR(20) NOT NULL
        CONSTRAINT DF_Products_Status DEFAULT ('Available'),
    createdAt DATETIME NOT NULL
        CONSTRAINT DF_Products_CreatedAt DEFAULT (GETDATE()),
    updatedAt DATETIME NOT NULL
        CONSTRAINT DF_Products_UpdatedAt DEFAULT (GETDATE()),

    CONSTRAINT PK_Products
        PRIMARY KEY (productId),

    CONSTRAINT CK_Products_BasePrice
        CHECK (basePrice >= 0),

    CONSTRAINT CK_Products_Status
        CHECK (status IN ('Available', 'OutOfStock', 'Inactive')),

    CONSTRAINT FK_Products_Categories
        FOREIGN KEY (categoryId)
        REFERENCES dbo.Categories(categoryId)
);
GO

CREATE TABLE dbo.ProductImages (
    imageId VARCHAR(20) NOT NULL,
    productId VARCHAR(20) NOT NULL,
    imageUrl VARCHAR(500) NOT NULL,
    isPrimary BIT NOT NULL
        CONSTRAINT DF_ProductImages_IsPrimary DEFAULT (0),

    CONSTRAINT PK_ProductImages
        PRIMARY KEY (imageId),

    CONSTRAINT FK_ProductImages_Products
        FOREIGN KEY (productId)
        REFERENCES dbo.Products(productId)
        ON DELETE CASCADE
);
GO

CREATE TABLE dbo.ProductVariants (
    variantId VARCHAR(20) NOT NULL,
    productId VARCHAR(20) NOT NULL,
    sizeId VARCHAR(20) NOT NULL,
    colorId VARCHAR(20) NOT NULL,
    sku VARCHAR(50) NULL,

    /* Physical stock currently present in the warehouse. */
    stockQty INT NOT NULL
        CONSTRAINT DF_ProductVariants_StockQty DEFAULT (0),

    /* Units reserved by Pending orders but not yet committed as sold. */
    reservedQty INT NOT NULL
        CONSTRAINT DF_ProductVariants_ReservedQty DEFAULT (0),

    priceOverride DECIMAL(12,2) NULL,
    createdAt DATETIME NOT NULL
        CONSTRAINT DF_ProductVariants_CreatedAt DEFAULT (GETDATE()),

    CONSTRAINT PK_ProductVariants
        PRIMARY KEY (variantId),

    CONSTRAINT UQ_ProductVariants_Product_Size_Color
        UNIQUE (productId, sizeId, colorId),

    CONSTRAINT CK_ProductVariants_StockQty
        CHECK (stockQty >= 0),

    CONSTRAINT CK_ProductVariants_ReservedQty
        CHECK (reservedQty >= 0 AND reservedQty <= stockQty),

    CONSTRAINT CK_ProductVariants_PriceOverride
        CHECK (priceOverride IS NULL OR priceOverride >= 0),

    CONSTRAINT FK_ProductVariants_Products
        FOREIGN KEY (productId)
        REFERENCES dbo.Products(productId)
        ON DELETE CASCADE,

    CONSTRAINT FK_ProductVariants_Sizes
        FOREIGN KEY (sizeId)
        REFERENCES dbo.Sizes(sizeId),

    CONSTRAINT FK_ProductVariants_Colors
        FOREIGN KEY (colorId)
        REFERENCES dbo.Colors(colorId)
);
GO

/* =========================================================
   4. CART
   ========================================================= */

CREATE TABLE dbo.Cart (
    cartId VARCHAR(20) NOT NULL,
    customerId VARCHAR(20) NOT NULL,
    variantId VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,

    CONSTRAINT PK_Cart
        PRIMARY KEY (cartId),

    CONSTRAINT UQ_Cart_Customer_Variant
        UNIQUE (customerId, variantId),

    CONSTRAINT CK_Cart_Quantity
        CHECK (quantity > 0),

    CONSTRAINT FK_Cart_Customers
        FOREIGN KEY (customerId)
        REFERENCES dbo.Customers(customerId)
        ON DELETE CASCADE,

    CONSTRAINT FK_Cart_ProductVariants
        FOREIGN KEY (variantId)
        REFERENCES dbo.ProductVariants(variantId)
        ON DELETE CASCADE
);
GO

/* =========================================================
   5. ORDERS / ORDER ITEMS
   ========================================================= */

CREATE TABLE dbo.Orders (
    orderId VARCHAR(20) NOT NULL,
    customerId VARCHAR(20) NOT NULL,
    orderStatus VARCHAR(30) NOT NULL
        CONSTRAINT DF_Orders_Status DEFAULT ('Pending'),
    shippingAddress NVARCHAR(255) NULL,
    shippingPhone VARCHAR(15) NULL,

    /* Pending-order expiration is DATEADD(DAY, 2, placedAt). */
    placedAt DATETIME NOT NULL
        CONSTRAINT DF_Orders_PlacedAt DEFAULT (GETDATE()),

    totalAmount DECIMAL(12,2) NOT NULL
        CONSTRAINT DF_Orders_TotalAmount DEFAULT (0),

    paymentMethod VARCHAR(30) NULL,
    paymentStatus VARCHAR(30) NOT NULL
        CONSTRAINT DF_Orders_PaymentStatus DEFAULT ('Pending'),
    paidAmount DECIMAL(12,2) NOT NULL
        CONSTRAINT DF_Orders_PaidAmount DEFAULT (0),
    issuedDate DATETIME NOT NULL
        CONSTRAINT DF_Orders_IssuedDate DEFAULT (GETDATE()),

    CONSTRAINT PK_Orders
        PRIMARY KEY (orderId),

    CONSTRAINT CK_Orders_Status
        CHECK (orderStatus IN (
            'Pending',
            'Confirmed',
            'Processing',
            'Shipping',
            'Delivered',
            'Cancelled'
        )),

    CONSTRAINT CK_Orders_TotalAmount
        CHECK (totalAmount >= 0),

    CONSTRAINT CK_Orders_PaymentMethod
        CHECK (
            paymentMethod IS NULL
            OR paymentMethod IN ('VNPay', 'Wallet', 'COD')
        ),

    CONSTRAINT CK_Orders_PaymentStatus
        CHECK (paymentStatus IN (
            'Pending',
            'Paid',
            'Failed',
            'Cancelled',
            'Refunded'
        )),

    CONSTRAINT CK_Orders_PaidAmount
        CHECK (paidAmount >= 0 AND paidAmount <= totalAmount),

    CONSTRAINT FK_Orders_Customers
        FOREIGN KEY (customerId)
        REFERENCES dbo.Customers(customerId)
);
GO

CREATE TABLE dbo.OrderItems (
    orderItemId VARCHAR(20) NOT NULL,
    orderId VARCHAR(20) NOT NULL,
    variantId VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    unitPrice DECIMAL(12,2) NOT NULL,
    discountAmount DECIMAL(12,2) NOT NULL
        CONSTRAINT DF_OrderItems_DiscountAmount DEFAULT (0),

    CONSTRAINT PK_OrderItems
        PRIMARY KEY (orderItemId),

    CONSTRAINT CK_OrderItems_Quantity
        CHECK (quantity > 0),

    CONSTRAINT CK_OrderItems_UnitPrice
        CHECK (unitPrice >= 0),

    CONSTRAINT CK_OrderItems_DiscountAmount
        CHECK (discountAmount >= 0),

    CONSTRAINT CK_OrderItems_DiscountWithinLineTotal
        CHECK (discountAmount <= unitPrice * quantity),

    CONSTRAINT FK_OrderItems_Orders
        FOREIGN KEY (orderId)
        REFERENCES dbo.Orders(orderId)
        ON DELETE CASCADE,

    /* Keep historical order lines valid when a variant has been ordered. */
    CONSTRAINT FK_OrderItems_ProductVariants
        FOREIGN KEY (variantId)
        REFERENCES dbo.ProductVariants(variantId)
);
GO

/* Compatibility view used by older WarehouseDAO code. */
CREATE VIEW dbo.OrderDetails
AS
SELECT
    orderItemId AS orderDetailId,
    orderId,
    variantId,
    quantity,
    unitPrice,
    discountAmount
FROM dbo.OrderItems;
GO

/* =========================================================
   6. WALLET
   ========================================================= */

CREATE TABLE dbo.Wallets (
    walletId VARCHAR(20) NOT NULL,
    customerId VARCHAR(20) NOT NULL,
    balance DECIMAL(12,2) NOT NULL
        CONSTRAINT DF_Wallets_Balance DEFAULT (0),
    walletStatus VARCHAR(20) NOT NULL
        CONSTRAINT DF_Wallets_Status DEFAULT ('Active'),
    createdAt DATETIME NOT NULL
        CONSTRAINT DF_Wallets_CreatedAt DEFAULT (GETDATE()),
    updatedAt DATETIME NOT NULL
        CONSTRAINT DF_Wallets_UpdatedAt DEFAULT (GETDATE()),

    CONSTRAINT PK_Wallets
        PRIMARY KEY (walletId),

    CONSTRAINT UQ_Wallets_CustomerId
        UNIQUE (customerId),

    CONSTRAINT CK_Wallets_Balance
        CHECK (balance >= 0),

    CONSTRAINT CK_Wallets_Status
        CHECK (walletStatus IN ('Active', 'Locked')),

    CONSTRAINT FK_Wallets_Customers
        FOREIGN KEY (customerId)
        REFERENCES dbo.Customers(customerId)
        ON DELETE CASCADE
);
GO

/*
    WalletTransactions is a wallet ledger, not the old order Payment entity.
    Order payment method/status is stored only in Orders.
*/
CREATE TABLE dbo.WalletTransactions (
    transactionId VARCHAR(20) NOT NULL,
    walletId VARCHAR(20) NOT NULL,
    orderId VARCHAR(20) NULL,
    transactionType VARCHAR(30) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    transactionStatus VARCHAR(30) NOT NULL
        CONSTRAINT DF_WalletTransactions_Status DEFAULT ('Pending'),
    externalMethod VARCHAR(30) NULL,
    description NVARCHAR(255) NULL,
    createdAt DATETIME NOT NULL
        CONSTRAINT DF_WalletTransactions_CreatedAt DEFAULT (GETDATE()),
    completedAt DATETIME NULL,

    CONSTRAINT PK_WalletTransactions
        PRIMARY KEY (transactionId),

    CONSTRAINT CK_WalletTransactions_Type
        CHECK (transactionType IN ('Deposit', 'Purchase', 'Refund')),

    CONSTRAINT CK_WalletTransactions_Amount
        CHECK (amount > 0),

    CONSTRAINT CK_WalletTransactions_Status
        CHECK (transactionStatus IN (
            'Pending',
            'Completed',
            'Failed',
            'Cancelled'
        )),

    CONSTRAINT CK_WalletTransactions_ExternalMethod
        CHECK (
            externalMethod IS NULL
            OR externalMethod IN ('VNPay')
        ),

    CONSTRAINT CK_WalletTransactions_CompletedAt
        CHECK (
            (transactionStatus = 'Completed' AND completedAt IS NOT NULL)
            OR
            (transactionStatus <> 'Completed')
        ),

    CONSTRAINT FK_WalletTransactions_Wallets
        FOREIGN KEY (walletId)
        REFERENCES dbo.Wallets(walletId)
        ON DELETE CASCADE,

    CONSTRAINT FK_WalletTransactions_Orders
        FOREIGN KEY (orderId)
        REFERENCES dbo.Orders(orderId)
);
GO

/* =========================================================
   7. COMMENTS
   ========================================================= */

CREATE TABLE dbo.Comments (
    commentId VARCHAR(20) NOT NULL,
    variantId VARCHAR(20) NOT NULL,
    customerId VARCHAR(20) NOT NULL,
    rating INT NULL,
    content NVARCHAR(1000) NULL,
    createdAt DATETIME NOT NULL
        CONSTRAINT DF_Comments_CreatedAt DEFAULT (GETDATE()),
    status VARCHAR(20) NOT NULL
        CONSTRAINT DF_Comments_Status DEFAULT ('Active'),

    CONSTRAINT PK_Comments
        PRIMARY KEY (commentId),

    CONSTRAINT CK_Comments_Rating
        CHECK (rating IS NULL OR rating BETWEEN 1 AND 5),

    CONSTRAINT CK_Comments_Status
        CHECK (status IN ('Active', 'Hidden')),

    CONSTRAINT FK_Comments_ProductVariants
        FOREIGN KEY (variantId)
        REFERENCES dbo.ProductVariants(variantId),

    CONSTRAINT FK_Comments_Customers
        FOREIGN KEY (customerId)
        REFERENCES dbo.Customers(customerId)
        ON DELETE CASCADE
);
GO

/* =========================================================
   8. WISHLIST / FAVORITE PRODUCTS
   ========================================================= */

CREATE TABLE dbo.Wishlists (
    wishlistId VARCHAR(20) NOT NULL,
    customerId VARCHAR(20) NOT NULL,
    productId VARCHAR(20) NOT NULL,
    createdAt DATETIME NOT NULL
        CONSTRAINT DF_Wishlists_CreatedAt DEFAULT (GETDATE()),

    CONSTRAINT PK_Wishlists
        PRIMARY KEY (wishlistId),

    CONSTRAINT UQ_Wishlists_Customer_Product
        UNIQUE (customerId, productId),

    CONSTRAINT FK_Wishlists_Customers
        FOREIGN KEY (customerId)
        REFERENCES dbo.Customers(customerId)
        ON DELETE CASCADE,

    CONSTRAINT FK_Wishlists_Products
        FOREIGN KEY (productId)
        REFERENCES dbo.Products(productId)
        ON DELETE CASCADE
);
GO

/* =========================================================
   9. WAREHOUSE IMPORTS
   ========================================================= */

CREATE TABLE dbo.WarehouseImports (
    importId VARCHAR(20) NOT NULL,
    variantId VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    importPrice DECIMAL(12,2) NOT NULL,
    employeeId VARCHAR(20) NOT NULL,
    importedAt DATETIME NOT NULL
        CONSTRAINT DF_WarehouseImports_ImportedAt DEFAULT (GETDATE()),

    CONSTRAINT PK_WarehouseImports
        PRIMARY KEY (importId),

    CONSTRAINT CK_WarehouseImports_Quantity
        CHECK (quantity > 0),

    CONSTRAINT CK_WarehouseImports_ImportPrice
        CHECK (importPrice >= 0),

    CONSTRAINT FK_WarehouseImports_ProductVariants
        FOREIGN KEY (variantId)
        REFERENCES dbo.ProductVariants(variantId),

    CONSTRAINT FK_WarehouseImports_Employees
        FOREIGN KEY (employeeId)
        REFERENCES dbo.Employees(employeeId)
);
GO

/* =========================================================
   10. INDEXES USED BY THE CURRENT PROJECT
   ========================================================= */

CREATE INDEX IX_Orders_CustomerId_PlacedAt
    ON dbo.Orders(customerId, placedAt DESC);
GO

CREATE INDEX IX_Orders_Status_PlacedAt
    ON dbo.Orders(orderStatus, placedAt)
    INCLUDE (customerId, totalAmount, paymentMethod, paymentStatus, paidAmount);
GO

CREATE INDEX IX_OrderItems_OrderId
    ON dbo.OrderItems(orderId);
GO

CREATE UNIQUE INDEX UX_ProductVariants_SKU_NotNull
    ON dbo.ProductVariants(sku)
    WHERE sku IS NOT NULL;
GO

CREATE INDEX IX_ProductVariants_AvailableStock
    ON dbo.ProductVariants(productId)
    INCLUDE (variantId, sizeId, colorId, sku, stockQty, reservedQty, priceOverride);
GO

CREATE INDEX IX_ProductImages_ProductId_Primary
    ON dbo.ProductImages(productId, isPrimary DESC, imageId);
GO

CREATE INDEX IX_Cart_CustomerId
    ON dbo.Cart(customerId)
    INCLUDE (variantId, quantity);
GO

CREATE INDEX IX_Comments_CustomerId_CreatedAt
    ON dbo.Comments(customerId, createdAt DESC)
    INCLUDE (variantId, rating, status);
GO

CREATE INDEX IX_Comments_VariantId_CreatedAt
    ON dbo.Comments(variantId, createdAt DESC)
    INCLUDE (customerId, rating, status);
GO

CREATE INDEX IX_WalletTransactions_WalletId_CreatedAt
    ON dbo.WalletTransactions(walletId, createdAt DESC)
    INCLUDE (orderId, transactionType, transactionStatus, amount, completedAt);
GO

CREATE INDEX IX_WalletTransactions_OrderId
    ON dbo.WalletTransactions(orderId)
    WHERE orderId IS NOT NULL;
GO

CREATE INDEX IX_WarehouseImports_VariantId_ImportedAt
    ON dbo.WarehouseImports(variantId, importedAt DESC)
    INCLUDE (quantity, importPrice, employeeId);
GO

/* =========================================================
   11. VERIFICATION QUERIES
   ========================================================= */

SELECT
    pv.variantId,
    pv.stockQty AS physicalStock,
    pv.reservedQty,
    pv.stockQty - pv.reservedQty AS availableStock
FROM dbo.ProductVariants pv
ORDER BY pv.variantId;
GO

SELECT
    o.orderId,
    o.customerId,
    o.orderStatus,
    o.paymentMethod,
    o.paymentStatus,
    o.paidAmount,
    o.totalAmount,
    o.placedAt,
    DATEADD(DAY, 2, o.placedAt) AS expiresAt,
    DATEDIFF(SECOND, GETDATE(), DATEADD(DAY, 2, o.placedAt)) AS remainingSeconds
FROM dbo.Orders o
WHERE o.orderStatus = 'Pending'
ORDER BY o.placedAt ASC;
GO

SELECT
    w.walletId,
    w.customerId,
    w.balance,
    w.walletStatus,
    COUNT(wt.transactionId) AS transactionCount
FROM dbo.Wallets w
LEFT JOIN dbo.WalletTransactions wt
    ON wt.walletId = w.walletId
GROUP BY
    w.walletId,
    w.customerId,
    w.balance,
    w.walletStatus;
GO

PRINT 'FashionShopDB normalized schema with Wallet support was created successfully.';
GO
