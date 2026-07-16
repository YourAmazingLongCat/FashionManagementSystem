package Services;

import DALs.OrderDAO;
import DALs.OrderItemDAO;
import Models.CartItem;
import Models.Order;
import Models.OrderItem;
import Utils.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OrderService {

    private final OrderDAO orderDAO;
    private final OrderItemDAO orderItemDAO;
    private final PaymentService paymentService;

    public OrderService() {
        orderDAO = new OrderDAO();
        orderItemDAO = new OrderItemDAO();
        paymentService = new PaymentService();
    }

    public Order reviewOrder(String customerId, String shippingAddress, String phone, List<CartItem> cart) {
        if (isEmpty(customerId) || isEmpty(shippingAddress) || isEmpty(phone)) {
            return null;
        }

        if (cart == null || cart.isEmpty() || !isValidCart(cart)) {
            return null;
        }

        BigDecimal totalAmount = calculateTotalAmount(cart);

        return new Order(
                null,
                customerId.trim(),
                OrderStatus.PENDING,
                shippingAddress.trim(),
                phone.trim(),
                LocalDateTime.now(),
                totalAmount
        );
    }

    public String checkout(String customerId, String shippingAddress, String phone, List<CartItem> cart) {
        if (isEmpty(customerId) || isEmpty(shippingAddress) || isEmpty(phone)) {
            return null;
        }

        if (cart == null || cart.isEmpty() || !isValidCart(cart)) {
            return null;
        }

        String orderId = generateOrderId();
        BigDecimal totalAmount = calculateTotalAmount(cart);

        Order order = new Order(
                orderId,
                customerId.trim(),
                OrderStatus.PENDING,
                shippingAddress.trim(),
                phone.trim(),
                LocalDateTime.now(),
                totalAmount
        );

        List<OrderItem> orderItems = convertCartToOrderItems(orderId, cart);
        boolean result = orderDAO.createOrder(order, orderItems);

        return result ? orderId : null;
    }

    public boolean confirmOrder(String orderId) {
        if (isEmpty(orderId)) {
            return false;
        }

        Order order = orderDAO.getOrderById(orderId.trim());

        if (order == null) {
            return false;
        }

        if (!OrderStatus.PENDING.equals(order.getOrderStatus())) {
            return false;
        }

        return orderDAO.updateOrderStatus(orderId.trim(), OrderStatus.CONFIRMED);
    }

    public boolean cancelOrder(String orderId) {
        if (isEmpty(orderId)) {
            return false;
        }

        Order order = orderDAO.getOrderById(orderId.trim());

        if (!canCancelOrder(order)) {
            return false;
        }

        boolean cancelled = orderDAO.updateOrderStatus(orderId.trim(), OrderStatus.CANCELLED);
        if (cancelled) {
            paymentService.refundWalletPaymentIfNeeded(orderId.trim());
        }
        return cancelled;
    }

    public boolean cancelOrder(String orderId, String customerId) {
        if (isEmpty(orderId) || isEmpty(customerId)) {
            return false;
        }

        Order order = orderDAO.getOrderByIdAndCustomerId(orderId.trim(), customerId.trim());

        if (!canCancelOrder(order)) {
            return false;
        }

        boolean cancelled = orderDAO.updateOrderStatus(orderId.trim(), OrderStatus.CANCELLED);
        if (cancelled) {
            paymentService.refundWalletPaymentIfNeeded(orderId.trim());
        }
        return cancelled;
    }

    public boolean changeShipStatus(String orderId, String newStatus) {
        if (isEmpty(orderId) || isEmpty(newStatus)) {
            return false;
        }

        String normalizedStatus = normalizeOrderStatus(newStatus);

        if (!isShippingStatus(normalizedStatus)) {
            return false;
        }

        Order order = orderDAO.getOrderById(orderId.trim());

        if (order == null) {
            return false;
        }

        String currentStatus = order.getOrderStatus();

        if (OrderStatus.CANCELLED.equals(currentStatus)
                || OrderStatus.DELIVERED.equals(currentStatus)
                || OrderStatus.PENDING.equals(currentStatus)) {
            return false;
        }

        if (OrderStatus.PROCESSING.equals(normalizedStatus)
                && !OrderStatus.CONFIRMED.equals(currentStatus)) {
            return false;
        }

        if (OrderStatus.SHIPPING.equals(normalizedStatus)
                && !OrderStatus.PROCESSING.equals(currentStatus)) {
            return false;
        }

        if (OrderStatus.DELIVERED.equals(normalizedStatus)
                && !OrderStatus.SHIPPING.equals(currentStatus)) {
            return false;
        }

        if (!paymentService.canMoveToShippingStatus(orderId.trim(), normalizedStatus)) {
            return false;
        }

        boolean updated = orderDAO.updateOrderStatus(orderId.trim(), normalizedStatus);

        if (updated && OrderStatus.DELIVERED.equals(normalizedStatus)) {
            paymentService.completeCashPaymentForDeliveredOrder(orderId.trim());
        }

        return updated;
    }

    public boolean changePaymentStatus(String orderId, String paymentStatus) {
        // TODO: Implement this after adding BillDAO/Bill model.
        // Current uploaded database structure stores payment status in Bills, not Orders.
        return false;
    }

    public List<Order> viewOrderHistory(String customerId) {
        if (isEmpty(customerId)) {
            return new ArrayList<>();
        }

        return orderDAO.getOrdersByCustomerId(customerId.trim());
    }

    public List<Order> searchOrderHistory(String customerId, String keyword) {
        if (isEmpty(customerId)) {
            return new ArrayList<>();
        }

        if (isEmpty(keyword)) {
            return orderDAO.getOrdersByCustomerId(customerId.trim());
        }

        return orderDAO.searchOrdersByCustomerId(customerId.trim(), keyword.trim());
    }

    public Order viewOrderDetailForCustomer(String customerId, String orderId) {
        if (isEmpty(customerId) || isEmpty(orderId)) {
            return null;
        }

        return orderDAO.getOrderByIdAndCustomerId(orderId.trim(), customerId.trim());
    }

    public List<OrderItem> viewOrderItemsForCustomer(String customerId, String orderId) {
        Order order = viewOrderDetailForCustomer(customerId, orderId);

        if (order == null) {
            return new ArrayList<>();
        }

        return orderItemDAO.getOrderItemsByOrderId(orderId.trim());
    }

    public List<Order> viewOrdersForStaff() {
        return orderDAO.getAllOrders();
    }

    public Order viewOrderDetailForStaff(String orderId) {
        if (isEmpty(orderId)) {
            return null;
        }

        return orderDAO.getOrderById(orderId.trim());
    }

    public List<OrderItem> viewOrderItemsForStaff(String orderId) {
        if (isEmpty(orderId)) {
            return new ArrayList<>();
        }

        return orderItemDAO.getOrderItemsByOrderId(orderId.trim());
    }

    public Order searchOrderDetailForCustomer(String customerId, String orderId) {
        return viewOrderDetailForCustomer(customerId, orderId);
    }

    public Order searchOrderDetailForStaff(String orderId) {
        return viewOrderDetailForStaff(orderId);
    }

    public List<Order> searchOrdersForStaff(String keyword) {
        if (isEmpty(keyword)) {
            return orderDAO.getAllOrders();
        }

        return orderDAO.searchOrdersForStaff(keyword.trim());
    }

    private boolean canCancelOrder(Order order) {
        if (order == null) {
            return false;
        }

        String status = order.getOrderStatus();

        return !OrderStatus.SHIPPING.equals(status)
                && !OrderStatus.DELIVERED.equals(status)
                && !OrderStatus.CANCELLED.equals(status);
    }

    private BigDecimal calculateTotalAmount(List<CartItem> cart) {
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem item : cart) {
            BigDecimal unitPrice = item.getUnitPrice();
            BigDecimal subTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(subTotal);
        }

        return totalAmount;
    }

    private List<OrderItem> convertCartToOrderItems(String orderId, List<CartItem> cart) {
        List<OrderItem> orderItems = new ArrayList<>();
        int index = 1;

        for (CartItem item : cart) {
            OrderItem orderItem = new OrderItem(
                    generateOrderItemId(index),
                    orderId,
                    item.getVariantId().trim(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    BigDecimal.ZERO
            );

            orderItems.add(orderItem);
            index++;
        }

        return orderItems;
    }

    private boolean isValidCart(List<CartItem> cart) {
        for (CartItem item : cart) {
            if (item == null) {
                return false;
            }

            if (isEmpty(item.getVariantId())) {
                return false;
            }

            if (item.getQuantity() <= 0) {
                return false;
            }

            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                return false;
            }
        }

        return true;
    }

    private boolean isShippingStatus(String status) {
        return OrderStatus.PROCESSING.equals(status)
                || OrderStatus.SHIPPING.equals(status)
                || OrderStatus.DELIVERED.equals(status);
    }

    private String normalizeOrderStatus(String status) {
        if (isEmpty(status)) {
            return status;
        }

        String value = status.trim();

        if (OrderStatus.PENDING.equalsIgnoreCase(value)) {
            return OrderStatus.PENDING;
        }
        if (OrderStatus.CONFIRMED.equalsIgnoreCase(value)) {
            return OrderStatus.CONFIRMED;
        }
        if (OrderStatus.PROCESSING.equalsIgnoreCase(value)) {
            return OrderStatus.PROCESSING;
        }
        if (OrderStatus.SHIPPING.equalsIgnoreCase(value)) {
            return OrderStatus.SHIPPING;
        }
        if (OrderStatus.DELIVERED.equalsIgnoreCase(value)) {
            return OrderStatus.DELIVERED;
        }
        if (OrderStatus.CANCELLED.equalsIgnoreCase(value)) {
            return OrderStatus.CANCELLED;
        }

        return value;
    }

    private String generateOrderId() {
        Random random = new Random();
        int number = random.nextInt(900) + 100;

        return "OD" + System.currentTimeMillis() + number;
    }

    private String generateOrderItemId(int index) {
        return "OI" + System.currentTimeMillis() + String.format("%03d", index);
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
