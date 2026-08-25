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

    public String createPendingOrderFromCart(String customerId,
            String initialShippingAddress, String initialPhone,
            List<CartItem> cart, String cartId, String[] cartItemIds) {
        if (isEmpty(customerId) || isEmpty(cartId)
                || cartItemIds == null || cartItemIds.length == 0) {
            return null;
        }

        if (cart == null || cart.isEmpty() || !isValidCart(cart)) {
            return null;
        }

        String orderId = generateOrderId();
        BigDecimal totalAmount = calculateTotalAmount(cart);
        String shippingAddress = initialShippingAddress == null
                ? "" : initialShippingAddress.trim();
        String phone = initialPhone == null ? "" : initialPhone.trim();

        Order order = new Order(
                orderId,
                customerId.trim(),
                OrderStatus.PENDING,
                shippingAddress,
                phone,
                LocalDateTime.now(),
                totalAmount
        );

        List<OrderItem> orderItems = convertCartToOrderItems(orderId, cart);
        boolean created = orderDAO.createOrderFromCart(
                order, orderItems, cartId.trim(), cartItemIds);

        return created ? orderId : null;
    }

    public boolean updateDeliveryInformationForCustomer(String customerId,
            String orderId, String shippingAddress, String phone) {
        if (isEmpty(customerId) || isEmpty(orderId)
                || isEmpty(shippingAddress) || isEmpty(phone)) {
            return false;
        }

        Order order = orderDAO.getOrderByIdAndCustomerId(
                orderId.trim(), customerId.trim());
        if (!canEditDeliveryInformation(order)) {
            return false;
        }

        return orderDAO.updateDeliveryInformationForCustomer(
                orderId.trim(), customerId.trim(),
                shippingAddress.trim(), phone.trim());
    }

    public boolean canEditDeliveryInformation(Order order) {
        if (order == null) {
            return false;
        }

        String status = normalizeOrderStatus(order.getOrderStatus());
        return OrderStatus.PENDING.equals(status)
                || OrderStatus.CONFIRMED.equals(status)
                || OrderStatus.PROCESSING.equals(status);
    }

    public boolean confirmOrder(String orderId) {
        if (isEmpty(orderId)) {
            return false;
        }

        String trimmedOrderId = orderId.trim();
        Order order = orderDAO.getOrderById(trimmedOrderId);

        if (order == null || !OrderStatus.PENDING.equals(
                normalizeOrderStatus(order.getOrderStatus()))) {
            return false;
        }

        // A Pending order created from Cart is incomplete until the customer
        // supplies delivery information and chooses a payment method.
        if (isEmpty(order.getShippingAddress()) || isEmpty(order.getPhone())
                || paymentService.getPaymentByOrderId(trimmedOrderId) == null) {
            return false;
        }

        if (!paymentService.canForwardOrderStatusByPayment(trimmedOrderId)) {
            return false;
        }

        return orderDAO.changeOrderStatusWithInventory(
                trimmedOrderId, OrderStatus.PENDING, OrderStatus.CONFIRMED);
    }

    public boolean cancelOrder(String orderId) {
        if (isEmpty(orderId)) {
            return false;
        }

        String trimmedOrderId = orderId.trim();
        Order order = orderDAO.getOrderById(trimmedOrderId);

        // Staff cannot cancel the incomplete Pending record created before
        // the customer presses Place order.
        if (!canCancelOrder(order)
                || paymentService.getPaymentByOrderId(trimmedOrderId) == null) {
            return false;
        }

        boolean cancelled = orderDAO.cancelOrderAndAdjustInventory(trimmedOrderId);
        if (cancelled) {
            handleCancelledOrderPayment(trimmedOrderId);
        }
        return cancelled;
    }

    public boolean cancelOrder(String orderId, String customerId) {
        if (isEmpty(orderId) || isEmpty(customerId)) {
            return false;
        }

        String trimmedOrderId = orderId.trim();
        Order order = orderDAO.getOrderByIdAndCustomerId(
                trimmedOrderId, customerId.trim());

        if (!canCancelOrder(order)) {
            return false;
        }

        boolean cancelled = orderDAO.cancelOrderAndAdjustInventory(trimmedOrderId);
        if (cancelled) {
            handleCancelledOrderPayment(trimmedOrderId);
        }
        return cancelled;
    }

    private void handleCancelledOrderPayment(String orderId) {
        paymentService.cancelOrderPaymentIfNeeded(orderId);
    }

    public boolean changeShipStatus(String orderId, String newStatus) {
        if (isEmpty(orderId) || isEmpty(newStatus)) {
            return false;
        }

        String trimmedOrderId = orderId.trim();
        String normalizedStatus = normalizeOrderStatus(newStatus);

        Order order = orderDAO.getOrderById(trimmedOrderId);
        if (order == null) {
            return false;
        }

        String currentStatus = normalizeOrderStatus(order.getOrderStatus());

        // Confirm Order owns Pending -> Confirmed. Change Ship Status is a
        // forward-only fulfillment flow after confirmation:
        // Confirmed -> Processing -> Shipping -> Delivered.
        // Keeping this strict prevents the shipping endpoint from undoing a
        // confirmation (and its inventory transaction) or skipping stages.
        String expectedNextStatus = getNextShippingStatus(currentStatus);
        if (expectedNextStatus == null
                || !expectedNextStatus.equals(normalizedStatus)) {
            return false;
        }

        // The Payment row proves that Place order was completed. COD may stay
        // Pending until delivery; VNPay must already be Paid.
        if (!paymentService.canForwardOrderStatusByPayment(trimmedOrderId)) {
            return false;
        }

        boolean updated = orderDAO.changeOrderStatusWithInventory(
                trimmedOrderId, currentStatus, normalizedStatus);

        if (updated && OrderStatus.DELIVERED.equals(normalizedStatus)) {
            paymentService.completeCashPaymentForDeliveredOrder(trimmedOrderId);
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

    public List<Order> viewOrdersForStaff(int page, int pageSize) {
        return viewOrdersForStaff(null, null, null, page, pageSize);
    }

    public List<Order> viewOrdersForStaff(String status, LocalDateTime dateFrom, LocalDateTime dateTo, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return orderDAO.getOrdersPaginated(null, status, dateFrom, dateTo, offset, pageSize);
    }

    public int countOrdersForStaff() {
        return countOrdersForStaff(null, null, null);
    }

    public int countOrdersForStaff(String status, LocalDateTime dateFrom, LocalDateTime dateTo) {
        return orderDAO.countOrders(null, status, dateFrom, dateTo);
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

    public List<Order> searchOrdersForStaff(String keyword, int page, int pageSize) {
        return searchOrdersForStaff(keyword, null, null, null, page, pageSize);
    }

    public List<Order> searchOrdersForStaff(String keyword, String status, LocalDateTime dateFrom, LocalDateTime dateTo, int page, int pageSize) {
        if (isEmpty(keyword) && isEmpty(status) && dateFrom == null && dateTo == null) {
            return viewOrdersForStaff(status, dateFrom, dateTo, page, pageSize);
        }
        int offset = (page - 1) * pageSize;
        return orderDAO.searchOrdersPaginated(emptyToNull(keyword), status, dateFrom, dateTo, offset, pageSize);
    }

    public int countSearchOrdersForStaff(String keyword) {
        return countSearchOrdersForStaff(keyword, null, null, null);
    }

    public int countSearchOrdersForStaff(String keyword, String status, LocalDateTime dateFrom, LocalDateTime dateTo) {
        if (isEmpty(keyword) && isEmpty(status) && dateFrom == null && dateTo == null) {
            return countOrdersForStaff(status, dateFrom, dateTo);
        }
        return orderDAO.countOrders(emptyToNull(keyword), status, dateFrom, dateTo);
    }

    private String emptyToNull(String value) {
        return (value == null || value.trim().isEmpty()) ? null : value.trim();
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

        String status = normalizeOrderStatus(order.getOrderStatus());
        int statusIndex = getOrderStatusIndex(status);

        return statusIndex >= 0
                && statusIndex < getOrderStatusIndex(OrderStatus.SHIPPING);
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

    private String getNextShippingStatus(String currentStatus) {
        if (OrderStatus.CONFIRMED.equals(currentStatus)) {
            return OrderStatus.PROCESSING;
        }
        if (OrderStatus.PROCESSING.equals(currentStatus)) {
            return OrderStatus.SHIPPING;
        }
        if (OrderStatus.SHIPPING.equals(currentStatus)) {
            return OrderStatus.DELIVERED;
        }
        return null;
    }

    private int getOrderStatusIndex(String status) {
        if (OrderStatus.PENDING.equals(status)) {
            return 0;
        }
        if (OrderStatus.CONFIRMED.equals(status)) {
            return 1;
        }
        if (OrderStatus.PROCESSING.equals(status)) {
            return 2;
        }
        if (OrderStatus.SHIPPING.equals(status)) {
            return 3;
        }
        if (OrderStatus.DELIVERED.equals(status)) {
            return 4;
        }

        return -1;
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
