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
                if (handleImport(request, user.getAccountId())) {
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
                if (handleExport(request)) {
                    message = "Stock out successful!";
                } else {
                    message = "Stock out failed. Check available stock.";
                    messageType = "error";
                }
                response.sendRedirect(basePath + "/inventory?message=" + 
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
        List<Object[]> inventory = warehouseDAO.getInventorySummary();
        List<Object[]> lowStock = warehouseDAO.getLowStockItems(10);

        request.setAttribute("inventory", inventory);
        request.setAttribute("lowStock", lowStock);
        request.setAttribute("activeTab", "export");

        request.getRequestDispatcher("/views/pages/productManagement/warehouse/warehouseExport.jsp").forward(request, response);
    }

    private boolean handleImport(HttpServletRequest request, String importedBy) {
        String variantId = trim(request.getParameter("variantId"));
        String quantityStr = trim(request.getParameter("quantity"));
        String importPriceStr = trim(request.getParameter("importPrice"));

        if (variantId == null || variantId.isBlank()) return false;
        if (quantityStr == null || quantityStr.isBlank()) return false;

        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            return false;
        }

        if (quantity <= 0) return false;

        double importPrice = 0;
        if (importPriceStr != null && !importPriceStr.isBlank()) {
            try {
                importPrice = Double.parseDouble(importPriceStr);
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return warehouseDAO.importStock(variantId, quantity, importPrice, importedBy);
    }

    private boolean handleExport(HttpServletRequest request) {
        String variantId = trim(request.getParameter("variantId"));
        String quantityStr = trim(request.getParameter("quantity"));

        if (variantId == null || variantId.isBlank()) return false;
        if (quantityStr == null || quantityStr.isBlank()) return false;

        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            return false;
        }

        if (quantity <= 0) return false;

        return warehouseDAO.deductStock(variantId, quantity);
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
