import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Handles low stock alerts using a separate thread
 */
public class LowStockAlertHandler implements InventoryManager.LowStockObserver, Runnable {
    private BlockingQueue<Product> lowStockQueue;
    private boolean running;
    private Thread alertThread;

    public LowStockAlertHandler() {
        this.lowStockQueue = new LinkedBlockingQueue<>();
        this.running = true;
    }

    /**
     * Starts the alert handler thread
     */
    public void start() {
        if (alertThread == null || !alertThread.isAlive()) {
            alertThread = new Thread(this);
            alertThread.setDaemon(true);
            alertThread.start();
        }
    }

    /**
     * Stops the alert handler thread
     */
    public void stop() {
        running = false;
        if (alertThread != null) {
            alertThread.interrupt();
        }
    }

    @Override
    public void onLowStock(Product product) {
        try {
            lowStockQueue.put(product);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                // Take product from queue (blocks until available)
                Product product = lowStockQueue.take();

                // Process low stock alert
                processLowStockAlert(product);

                // Sleep to avoid overwhelming the system
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Processes a low stock alert
     */
    private void processLowStockAlert(Product product) {
        // In a real system, this might send emails, SMS, or notifications
        System.out.println("LOW STOCK ALERT: " + product.getName() +
                " (ID: " + product.getId() + ") - Current stock: " + product.getQuantity() +
                ", Minimum level: " + product.getMinStockLevel());
    }
}