package Listeners;

import Services.OrderExpirationService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

@WebListener
public class OrderExpirationListener implements ServletContextListener {

    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable, "pending-order-expiration");
                thread.setDaemon(true);
                return thread;
            }
        });

        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    int expiredCount = new OrderExpirationService().expirePendingOrders();
                    if (expiredCount > 0) {
                        System.out.println("Automatically removed " + expiredCount
                                + " expired Pending order(s).");
                    }
                } catch (Exception e) {
                    System.out.println("Pending order expiration task error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }
}
