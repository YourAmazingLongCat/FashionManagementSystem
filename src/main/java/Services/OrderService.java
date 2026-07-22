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

    /**
     * Creates the Pending order as soon as the customer presses Checkout in
     * the cart. Delivery details may still be empty at this point and are
     * completed later from the checkout/order screen.
     *
     * Order creation, stock reservation and selected-cart-row removal are
     * committed by OrderDAO in one transaction.
     */
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

        String status = order.getOrderStatus();
        return OrderStatus.PENDING.equals(status)
                || OrderStatus.CONFIRMED.equals(status)
                || OrderStatus.PROCESSING.equals(status);
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

        // A cart checkout creates an incomplete Pending order immediately.
        // Staff must not confirm it until delivery details and payment method
        // have been completed by the customer.
        if (isEmpty(order.getShippingAddress()) || isEmpty(order.getPhone())
                || paymentService.getPaymentByOrderId(orderId.trim()) == null) {
            return false;
        }

        if (!paymentService.canForwardOrderStatusByPayment(orderId.trim())) {
            return false;
        }

        return orderDAO.changeOrderStatusWithInventory(
                orderId.trim(), OrderStatus.PENDING, OrderStatus.CONFIRMED);
    }

    public boolean cancelOrder(String orderId) {
        if (isEmpty(orderId)) {
            return false;
        }

        Order order = orderDAO.getOrderById(orderId.trim());

        // This overload is used by Staff/Admin. A Cart Checkout only creates
        // an incomplete Pending record; staff cannot change any status,
        // including Cancelled, until the customer presses Place order.
        if (!canCancelOrder(order)
                || paymentService.getPaymentByOrderId(orderId.trim()) == null) {
            return false;
        }

        boolean cancelled = orderDAO.cancelOrderAndAdjustInventory(orderId.trim());
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

        boolean cancelled = orderDAO.cancelOrderAndAdjustInventory(orderId.trim());
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

        if (!isOrderProgressStatus(normalizedStatus)) {
            return false;
        }

        Order order = orderDAO.getOrderById(orderId.trim());

        if (order == null) {
            return false;
        }

        // A Payment row is the project marker that the customer has pressed
        // Place order. Before that point staff must not move the status in
        // either direction.
        if (paymentService.getPaymentByOrderId(orderId.trim()) == null) {
            return false;
        }

        String currentStatus = normalizeOrderStatus(order.getOrderStatus());

        if (OrderStatus.PENDING.equals(currentStatus)
                && OrderStatus.CONFIRMED.equals(normalizedStatus)
                && (isEmpty(order.getShippingAddress()) || isEmpty(order.getPhone()))) {
            return false;
        }

        int currentIndex = getOrderStatusIndex(currentStatus);
        int newIndex = getOrderStatusIndex(normalizedStatus);

        if (currentIndex < 0 || newIndex < 0 || currentIndex == newIndex) {
            return false;
        }

        boolean isForward = newIndex > currentIndex;
        boolean isBackward = newIndex < currentIndex;

        /*
         * Business rule:
         * - Forward: only one status level each time.
         * - Backward: only one status level each time.
         * - Payment check only applies to Wallet and VNPay when moving forward.
         * - COD is not blocked by payment status and becomes Paid automatically when Delivered.
         */
        if (isForward) {
            if (newIndex - currentIndex != 1) {
                return false;
            }

            if (!paymentService.canForwardOrderStatusByPayment(orderId.trim())) {
                return false;
            }
        }

        if (isBackward && currentIndex - newIndex != 1) {
            return false;
        }

        boolean updated = orderDAO.changeOrderStatusWithInventory(
                orderId.trim(), currentStatus, normalizedStatus);

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

    private boolean isOrderProgressStatus(String status) {
        return OrderStatus.PENDING.equals(status)
                || OrderStatus.CONFIRMED.equals(status)
                || OrderStatus.PROCESSING.equals(status)
                || OrderStatus.SHIPPING.equals(status)
                || OrderStatus.DELIVERED.equals(status);
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
