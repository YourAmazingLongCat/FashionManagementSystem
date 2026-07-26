package Controllers;

/**
 * Backward-compatible bridge for legacy imports.
 *
 * The canonical implementation lives in Services.OrderExpirationService and
 * is started by Listeners.OrderExpirationListener.
 */
@Deprecated
public class OrderExpirationService extends Services.OrderExpirationService {

    public OrderExpirationService() {
        super();
    }
}
