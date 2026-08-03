package Controllers;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import DALs.ProductDAO;
import DALs.WarehouseDAO;
import Models.Account;
import Models.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "WarehouseServlet", urlPatterns = {
    "/admin/warehouse",
    "/admin/warehouse/inventory",
    "/admin/warehouse/import",
    "/admin/warehouse/export",
    "/staff/warehouse",
    "/staff/warehouse/inventory",
    "/staff/warehouse/import",
    "/staff/warehouse/export"
})
public class WarehouseServlet extends HttpServlet {

    private WarehouseDAO warehouseDAO;
    private ProductDAO productDAO;

    @Override
    public void init() throws ServletException {
        warehouseDAO = new WarehouseDAO();
        productDAO = new ProductDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        request.setCharacterEncoding("UTF-8");

        Account user = getLoggedInUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        request.setAttribute("userName", user.getFullName());

        switch (path) {
            case "/admin/warehouse":
            case "/admin/warehouse/inventory":
            case "/staff/warehouse":
            case "/staff/warehouse/inventory":
                showInventory(request, response);
                break;
            case "/admin/warehouse/import":
            case "/staff/warehouse/import":
                showImport(request, response);
                break;
            case "/admin/warehouse/export":
            case "/staff/warehouse/export":
                showExport(request, response);
                break;
            default:
                showInventory(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        Account user = getLoggedInUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String action = request.getParameter("action");
        String message = "";
        String messageType = "success";

        String servletPath = request.getServletPath();
        String basePath = servletPath.startsWith("/staff/warehouse") ? request.getContextPath() + "/staff/warehouse" : request.getContextPath() + "/admin/warehouse";

        if (action == null) {
            response.sendRedirect(basePath + "/inventory");
            return;
        }

        switch (action) {
            case "import":
                boolean importOk = handleImport(request, user.getAccountId());
                Object batchAttr = request.getAttribute("batchResult");
                if (batchAttr instanceof DALs.WarehouseDAO.BatchImportResult) {
                    DALs.WarehouseDAO.BatchImportResult br = (DALs.WarehouseDAO.BatchImportResult) batchAttr;
                    if (br.allOk()) {
                        message = "Stock in successful! Added " + br.successCount + " item(s).";
                        messageType = "success";
                    } else if (br.partial()) {
                        message = "Stock in partially completed: " + br.successCount + " added, " + br.failCount + " failed.";
                        messageType = "error";
                    } else {
                        message = "Stock in failed. Please try again.";
                        messageType = "error";
                    }
                } else if (importOk) {
                    message = "Stock in successful!";
                    messageType = "success";
                } else {
                    message = "Stock in failed. Please try again.";
                    messageType = "error";
                }
                response.sendRedirect(basePath + "/import?message=" +
                        java.net.URLEncoder.encode(message, "UTF-8") + "&messageType=" + messageType);
                break;
            case "export":
                boolean exportOk = handleExport(request, user.getAccountId());
                Object exportBatchAttr = request.getAttribute("batchResult");
                if (exportBatchAttr instanceof DALs.WarehouseDAO.BatchImportResult) {
                    DALs.WarehouseDAO.BatchImportResult br = (DALs.WarehouseDAO.BatchImportResult) exportBatchAttr;
                    if (br.allOk()) {
                        message = "Stock out successful! Reduced " + br.successCount + " item(s).";
                        messageType = "success";
                    } else if (br.partial()) {
                        message = "Stock out partially completed: " + br.successCount + " reduced, " + br.failCount + " failed.";
                        messageType = "error";
                    } else {
                        message = "Stock out failed. " + (br.firstError == null ? "" : br.firstError);
                        messageType = "error";
                    }
                } else if (exportOk) {
                    message = "Stock out successful!";
                    messageType = "success";
                } else {
                    message = "Stock out failed. Check available stock.";
                    messageType = "error";
                }
                response.sendRedirect(basePath + "/export?message=" +
                        java.net.URLEncoder.encode(message, "UTF-8") + "&messageType=" + messageType);
                break;
            default:
                message = "Invalid action.";
                messageType = "error";
                response.sendRedirect(basePath + "/inventory?message=" + 
                        java.net.URLEncoder.encode(message, "UTF-8") + "&messageType=" + messageType);
        }
    }

    private void showInventory(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword = trim(request.getParameter("keyword"));
        String productFilter = trim(request.getParameter("productFilter"));
        String sizeFilter = trim(request.getParameter("sizeFilter"));
        String colorFilter = trim(request.getParameter("colorFilter"));

        List<Object[]> inventory = warehouseDAO.getInventorySummary(keyword, sizeFilter, colorFilter, productFilter);
        List<Product> products = productDAO.getAllProducts();
        List<Object[]> lowStock = warehouseDAO.getLowStockItems(10);
        List<Object[]> allSizes = warehouseDAO.getAllSizes();
        List<Object[]> allColors = warehouseDAO.getAllColors();

        int totalItems = inventory.size();
        int totalStock = 0;
        int totalAvailable = 0;
        int lowStockCount = lowStock.size();

        for (Object[] row : inventory) {
            int stockQty = (int) row[8];
            int reservedQty = (int) row[9];
            totalStock += stockQty;
            totalAvailable += (stockQty - reservedQty);
        }

        request.setAttribute("inventory", inventory);
        request.setAttribute("products", products);
        request.setAttribute("lowStock", lowStock);
        request.setAttribute("allSizes", allSizes);
        request.setAttribute("allColors", allColors);
        request.setAttribute("totalItems", totalItems);
        request.setAttribute("totalStock", totalStock);
        request.setAttribute("totalAvailable", totalAvailable);
        request.setAttribute("lowStockCount", lowStockCount);
        request.setAttribute("activeTab", "inventory");
        request.setAttribute("currentKeyword", keyword);
        request.setAttribute("currentProductFilter", productFilter);
        request.setAttribute("currentSizeFilter", sizeFilter);
        request.setAttribute("currentColorFilter", colorFilter);

        if (request.getParameter("message") != null) {
            request.setAttribute("message", request.getParameter("message"));
            request.setAttribute("messageType", request.getParameter("messageType"));
        }

        request.getRequestDispatcher("/views/pages/productManagement/warehouse/warehouseInventory.jsp").forward(request, response);
    }

    private void showImport(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword = trim(request.getParameter("keyword"));
        String productFilter = trim(request.getParameter("productFilter"));
        String colorFilter = trim(request.getParameter("colorFilter"));
        int invPage = 1;
        try {
            if (request.getParameter("invPage") != null && !request.getParameter("invPage").isBlank()) {
                invPage = Math.max(1, Integer.parseInt(request.getParameter("invPage")));
            }
        } catch (NumberFormatException ignored) {}
        int invPageSize = 10;

        String importProductFilter = trim(request.getParameter("importProductFilter"));
        String importImporterFilter = trim(request.getParameter("importImporterFilter"));
        String importDateFrom = trim(request.getParameter("importDateFrom"));
        String importDateTo = trim(request.getParameter("importDateTo"));
        String importSearch = trim(request.getParameter("importSearch"));
        int importPage = 1;
        try {
            if (request.getParameter("importPage") != null && !request.getParameter("importPage").isBlank()) {
                importPage = Math.max(1, Integer.parseInt(request.getParameter("importPage")));
            }
        } catch (NumberFormatException ignored) {}
        int importPageSize = 10;

        Map<String, Object> invResult = warehouseDAO.getInventorySummaryPaginated(keyword, null, colorFilter, productFilter, invPage, invPageSize);
        @SuppressWarnings("unchecked")
        List<Object[]> inventory = (List<Object[]>) invResult.get("data");
        int invTotalRecords = (int) invResult.get("totalRecords");
        int invTotalPages = (int) invResult.get("totalPages");
        List<Product> products = productDAO.getAllProducts();
        List<Object[]> allColors = warehouseDAO.getAllColors();
        Map<String, Object> importResult = warehouseDAO.getRecentImportsPaginated(
                importProductFilter, importImporterFilter, importDateFrom, importDateTo, importSearch, importPage, importPageSize);
        @SuppressWarnings("unchecked")
        List<Object[]> recentImports = (List<Object[]>) importResult.get("data");
        int importTotalRecords = (int) importResult.get("totalRecords");
        int importTotalPages = (int) importResult.get("totalPages");
        List<Object[]> importers = warehouseDAO.getAllImporters();

        request.setAttribute("inventory", inventory);
        request.setAttribute("products", products);
        request.setAttribute("allColors", allColors);
        request.setAttribute("recentImports", recentImports);
        request.setAttribute("importers", importers);
        request.setAttribute("activeTab", "import");
        request.setAttribute("currentKeyword", keyword);
        request.setAttribute("currentProductFilter", productFilter);
        request.setAttribute("currentColorFilter", colorFilter);
        request.setAttribute("invPage", invPage);
        request.setAttribute("invTotalPages", invTotalPages);
        request.setAttribute("invTotalRecords", invTotalRecords);
        request.setAttribute("importProductFilter", importProductFilter);
        request.setAttribute("importImporterFilter", importImporterFilter);
        request.setAttribute("importDateFrom", importDateFrom);
        request.setAttribute("importDateTo", importDateTo);
        request.setAttribute("importSearch", importSearch);
        request.setAttribute("importPage", importPage);
        request.setAttribute("importTotalPages", importTotalPages);
        request.setAttribute("importTotalRecords", importTotalRecords);

        if (request.getParameter("message") != null) {
            request.setAttribute("message", request.getParameter("message"));
            request.setAttribute("messageType", request.getParameter("messageType"));
        }

        request.getRequestDispatcher("/views/pages/productManagement/warehouse/warehouseImport.jsp").forward(request, response);
    }

    private void showExport(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword = trim(request.getParameter("keyword"));
        String productFilter = trim(request.getParameter("productFilter"));
        String colorFilter = trim(request.getParameter("colorFilter"));
        int expPage = 1;
        try {
            if (request.getParameter("expPage") != null && !request.getParameter("expPage").isBlank()) {
                expPage = Math.max(1, Integer.parseInt(request.getParameter("expPage")));
            }
        } catch (NumberFormatException ignored) {}
        int expPageSize = 10;

        String exportProductFilter = trim(request.getParameter("exportProductFilter"));
        String exportExporterFilter = trim(request.getParameter("exportExporterFilter"));
        String exportDateFrom = trim(request.getParameter("exportDateFrom"));
        String exportDateTo = trim(request.getParameter("exportDateTo"));
        String exportSearch = trim(request.getParameter("exportSearch"));
        int exportHistoryPage = 1;
        try {
            if (request.getParameter("exportPage") != null && !request.getParameter("exportPage").isBlank()) {
                exportHistoryPage = Math.max(1, Integer.parseInt(request.getParameter("exportPage")));
            }
        } catch (NumberFormatException ignored) {}
        int exportHistoryPageSize = 10;

        Map<String, Object> invResult = warehouseDAO.getInventorySummaryPaginated(keyword, null, colorFilter, productFilter, expPage, expPageSize);
        @SuppressWarnings("unchecked")
        List<Object[]> inventory = (List<Object[]>) invResult.get("data");
        int invTotalRecords = (int) invResult.get("totalRecords");
        int invTotalPages = (int) invResult.get("totalPages");
        List<Product> products = productDAO.getAllProducts();
        List<Object[]> allColors = warehouseDAO.getAllColors();
        Map<String, Object> exportResult = warehouseDAO.getRecentExportsPaginated(
                exportProductFilter, exportExporterFilter, exportDateFrom, exportDateTo, exportSearch, exportHistoryPage, exportHistoryPageSize);
        @SuppressWarnings("unchecked")
        List<Object[]> recentExports = (List<Object[]>) exportResult.get("data");
        int exportTotalRecords = (int) exportResult.get("totalRecords");
        int exportTotalPages = (int) exportResult.get("totalPages");
        List<Object[]> exporters = warehouseDAO.getAllExporters();

        request.setAttribute("inventory", inventory);
        request.setAttribute("products", products);
        request.setAttribute("allColors", allColors);
        request.setAttribute("recentExports", recentExports);
        request.setAttribute("exporters", exporters);
        request.setAttribute("activeTab", "export");
        request.setAttribute("currentKeyword", keyword);
        request.setAttribute("currentProductFilter", productFilter);
        request.setAttribute("currentColorFilter", colorFilter);
        request.setAttribute("expPage", expPage);
        request.setAttribute("expTotalPages", invTotalPages);
        request.setAttribute("expTotalRecords", invTotalRecords);
        request.setAttribute("exportProductFilter", exportProductFilter);
        request.setAttribute("exportExporterFilter", exportExporterFilter);
        request.setAttribute("exportDateFrom", exportDateFrom);
        request.setAttribute("exportDateTo", exportDateTo);
        request.setAttribute("exportSearch", exportSearch);
        request.setAttribute("exportHistoryPage", exportHistoryPage);
        request.setAttribute("exportHistoryTotalPages", exportTotalPages);
        request.setAttribute("exportHistoryTotalRecords", exportTotalRecords);

        if (request.getParameter("message") != null) {
            request.setAttribute("message", request.getParameter("message"));
            request.setAttribute("messageType", request.getParameter("messageType"));
        }

        request.getRequestDispatcher("/views/pages/productManagement/warehouse/warehouseExport.jsp").forward(request, response);
    }

    private boolean handleImport(HttpServletRequest request, String importedBy) {
        // Batch form submits parallel arrays: variantId[], quantity[], importPrice[].
        // Fallback to single-value fields so the old single-form still works.
        String[] variantIds = request.getParameterValues("variantId");
        String[] quantities = request.getParameterValues("quantity");
        String[] prices = request.getParameterValues("importPrice");

        // Single-mode fallback
        if (variantIds == null) {
            String singleVariant = trim(request.getParameter("variantId"));
            if (singleVariant == null || singleVariant.isEmpty()) return false;
            String qtyStr = trim(request.getParameter("quantity"));
            String priceStr = trim(request.getParameter("importPrice"));
            if (qtyStr == null || qtyStr.isBlank()) return false;
            int qty;
            double price = 0;
            try { qty = Integer.parseInt(qtyStr); } catch (NumberFormatException e) { return false; }
            if (qty <= 0) return false;
            if (priceStr != null && !priceStr.isBlank()) {
                try { price = Double.parseDouble(priceStr); } catch (NumberFormatException e) { return false; }
            }
            return warehouseDAO.importStock(singleVariant, qty, price, importedBy);
        }

        java.util.List<DALs.WarehouseDAO.ImportItem> items = new java.util.ArrayList<>();
        for (int i = 0; i < variantIds.length; i++) {
            String variantId = trim(variantIds[i]);
            if (variantId == null || variantId.isEmpty()) continue;
            // Skip rows the user left blank
            String qtyStr = (quantities != null && i < quantities.length) ? trim(quantities[i]) : null;
            if (qtyStr == null || qtyStr.isBlank()) continue;
            int qty;
            double price = 0;
            try { qty = Integer.parseInt(qtyStr); } catch (NumberFormatException e) { continue; }
            if (qty <= 0) continue;
            String priceStr = (prices != null && i < prices.length) ? trim(prices[i]) : null;
            if (priceStr != null && !priceStr.isBlank()) {
                try { price = Double.parseDouble(priceStr); } catch (NumberFormatException e) { continue; }
            }
            items.add(new DALs.WarehouseDAO.ImportItem(variantId, qty, price));
        }

        if (items.isEmpty()) return false;
        DALs.WarehouseDAO.BatchImportResult result = warehouseDAO.importStockBatch(items, importedBy);
        // Stash result on request so doPost can build a richer message
        request.setAttribute("batchResult", result);
        return result.successCount > 0;
    }

    private boolean handleExport(HttpServletRequest request, String exportedBy) {
        String[] variantIds = request.getParameterValues("variantId");
        String[] quantities = request.getParameterValues("quantity");
        String defaultReason = trim(request.getParameter("reason"));

        // Single-mode fallback (old form used quantity + reason per row)
        if (variantIds == null) {
            String singleVariant = trim(request.getParameter("variantId"));
            if (singleVariant == null || singleVariant.isEmpty()) return false;
            String qtyStr = trim(request.getParameter("quantity"));
            if (qtyStr == null || qtyStr.isBlank()) return false;
            int qty;
            try { qty = Integer.parseInt(qtyStr); } catch (NumberFormatException e) { return false; }
            if (qty <= 0) return false;
            return warehouseDAO.exportStock(singleVariant, qty, exportedBy, defaultReason);
        }

        java.util.List<DALs.WarehouseDAO.ImportItem> items = new java.util.ArrayList<>();
        for (int i = 0; i < variantIds.length; i++) {
            String variantId = trim(variantIds[i]);
            if (variantId == null || variantId.isEmpty()) continue;
            String qtyStr = (quantities != null && i < quantities.length) ? trim(quantities[i]) : null;
            if (qtyStr == null || qtyStr.isBlank()) continue;
            int qty;
            try { qty = Integer.parseInt(qtyStr); } catch (NumberFormatException e) { continue; }
            if (qty <= 0) continue;
            items.add(new DALs.WarehouseDAO.ImportItem(variantId, qty, 0));
        }

        if (items.isEmpty()) return false;
        DALs.WarehouseDAO.BatchImportResult result = warehouseDAO.exportStockBatch(items, exportedBy, defaultReason);
        request.setAttribute("batchResult", result);
        return result.successCount > 0;
    }

    private Account getLoggedInUser(HttpServletRequest request) {
        Object userObject = request.getSession(false) != null ? 
                request.getSession(false).getAttribute("USER") : null;
        return userObject instanceof Account ? (Account) userObject : null;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
