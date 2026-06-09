package Utils;

import Models.CartItem;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

public final class SessionCartUtil {

    public static final String CART_SESSION_KEY = "GUEST_CART_ITEMS";

    private SessionCartUtil() {
    }

    @SuppressWarnings("unchecked")
    public static List<CartItem> getCart(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object value = session.getAttribute(CART_SESSION_KEY);
        if (value instanceof List<?>) {
            return (List<CartItem>) value;
        }
        List<CartItem> cart = new ArrayList<>();
        session.setAttribute(CART_SESSION_KEY, cart);
        return cart;
    }

    public static int getCartCount(HttpServletRequest request) {
        int total = 0;
        for (CartItem item : getCart(request)) {
            total += Math.max(0, item.getQuantity());
        }
        return total;
    }
}
