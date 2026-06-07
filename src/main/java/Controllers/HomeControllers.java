package Controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Controller xử lý trang chủ (Home) cho Fashion Store
 */
@WebServlet(name = "HomeControllers", urlPatterns = {"/home"})
public class HomeControllers extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Bắn log ra console của NetBeans để biết code đã chạy vào đây chưa
        System.out.println("=====> ĐÃ TRUY CẬP VÀO HOMECONTROLLERS <=====");

        try {
            // =========================================================
            // MOCK DATA: TẠO DỮ LIỆU GIẢ ĐỂ TEST GIAO DIỆN
            // =========================================================
            
            List<Map<String, Object>> categories = new ArrayList<>();
            categories.add(createCategory(1, "Tops & Tees"));
            categories.add(createCategory(2, "Bottoms"));
            categories.add(createCategory(3, "Outerwear"));
            categories.add(createCategory(4, "Accessories"));
            request.setAttribute("categories", categories);

            List<Map<String, Object>> dummyProducts = new ArrayList<>();
            dummyProducts.add(createProduct(1, "Streetwear Graphic Tee", 350000, "Assets/Images/Design/fake-product.png"));
            dummyProducts.add(createProduct(2, "Dark Wash Cargo Pants", 650000, "Assets/Images/Design/fake-product.png"));
            dummyProducts.add(createProduct(3, "Oversized Hoodie", 550000, "Assets/Images/Design/fake-product.png"));
            dummyProducts.add(createProduct(4, "Minimalist Beanie", 150000, "Assets/Images/Design/fake-product.png"));
            
            request.setAttribute("newArrivals", dummyProducts);
            request.setAttribute("tops", dummyProducts);
            request.setAttribute("outerwear", dummyProducts);
            request.setAttribute("accessories", dummyProducts);

            // =========================================================
            
            // 3. Chỉ định đường dẫn tới trang nội dung con
            String contentPage = "/Pages/Guest/Home/Content/Content.jsp";
            request.setAttribute("contentPage", contentPage);
            
            // 4. Chuyển tiếp tới Layout tổng
            String layoutPath = "/Pages/Guest/Home/Layout/Layout.jsp";
            System.out.println("=====> Đang chuyển hướng tới file JSP: " + layoutPath);
            
            request.getRequestDispatcher(layoutPath).forward(request, response);
            
        } catch (Exception e) {
            // Nếu có lỗi đường dẫn hoặc lỗi code, in thẳng ra màn hình web bằng chữ to đùng!
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().println("<h1>LỖI SERVER TẠI TRANG CHỦ:</h1>");
            response.getWriter().println("<p style='color:red;'>" + e.getMessage() + "</p>");
            e.printStackTrace();
        }
    }

    private Map<String, Object> createCategory(int id, String name) {
        Map<String, Object> map = new HashMap<>();
        map.put("categoryId", id);
        map.put("name", name);
        return map;
    }

    private Map<String, Object> createProduct(int id, String name, double price, String image) {
        Map<String, Object> map = new HashMap<>();
        map.put("productId", id);
        map.put("name", name);
        map.put("price", price);
        map.put("image", image);
        return map;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Home Controller - Fashion Management System";
    }
}