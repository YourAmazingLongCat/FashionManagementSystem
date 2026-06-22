package Controllers;

import DALs.ProductDAO;
import DALs.WarehouseDAO;
import Models.Account;
import Models.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "WarehouseServlet", urlPatterns = {
    "/admin/warehouse",
    "/admin/warehouse/inventory",
    "/admin/warehouse/import",
    "/admin/warehouse/export"
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
                showInventory(request, response);
                break;
            case "/admin/warehouse/import":
                showImport(request, response);
                break;
            case "/admin/warehouse/export":
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

        if (action == null) {
            response.sendRedirect(request.getContextPath() + "/admin/warehouse/inventory");
            return;
        }

        switch (action) {
            case "import":
                if (handleImport(request)) {
                    message = "Stock in successful!";
                    messageType = "success";
                } else {
                    message = "Stock in failed. Please try again.";
                    messageType = "error";
                }
                response.sendRedirect(request.getContextPath() + "/admin/warehouse/import?message=" + 
                        java.net.URLEncoder.encode(message, "UTF-8") + "&messageType=" + messageType);
                break;
            case "export":
                if (handleExport(request)) {
                    message = "Stock out successful!";
                } else {
                    message = "Stock out failed. Check available stock.";
                    messageType = "error";
                }
                response.sendRedirect(request.getContextPath() + "/admin/warehouse/inventory?message=" + 
                        java.net.URLEncoder.encode(message, "UTF-8") + "&messageType=" + messageType);
                break;
            default:
                message = "Invalid action.";
                messageType = "error";
                response.sendRedirect(request.getContextPath() + "/admin/warehouse/inventory?message=" + 
                        java.net.URLEncoder.encode(message, "UTF-8") + "&messageType=" + messageType);
        }
    }

    private void showInventory(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Object[]> inventory = warehouseDAO.getInventorySummary();
        List<Object[]> lowStock = warehouseDAO.getLowStockItems(10);

        int totalItems = inventory.size();
        int totalStock = 0;
        int lowStockCount = lowStock.size();

        for (Object[] row : inventory) {
            totalStock += (int) row[8];
        }

        request.setAttribute("inventory", inventory);
        request.setAttribute("lowStock", lowStock);
        request.setAttribute("totalItems", totalItems);
        request.setAttribute("totalStock", totalStock);
        request.setAttribute("lowStockCount", lowStockCount);
        request.setAttribute("activeTab", "inventory");

        if (request.getParameter("message") != null) {
            request.setAttribute("message", request.getParameter("message"));
            request.setAttribute("messageType", request.getParameter("messageType"));
        }

        request.getRequestDispatcher("/views/pages/productManagement/warehouse/warehouseInventory.jsp").forward(request, response);
    }

    private void showImport(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Object[]> inventory = warehouseDAO.getInventorySummary();
        List<Product> products = productDAO.getAllProducts();

        request.setAttribute("inventory", inventory);
        request.setAttribute("products", products);
        request.setAttribute("activeTab", "import");

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

    private boolean handleImport(HttpServletRequest request) {
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

        return warehouseDAO.addStock(variantId, quantity);
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
