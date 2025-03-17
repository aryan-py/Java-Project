import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

// This class watches for when products are running low on stock
// It runs in its own thread so it doesn't slow down the main program
public class LowStockAlertHandler implements InventoryManager.LowStockObserver, Runnable {
    // This is a special list that can safely handle multiple threads
    private BlockingQueue<Product> lowStockQueue;
    // This tells us if the alert handler should keep running
    private boolean running;
    // This is the thread that runs our alert handler
    private Thread alertThread;

    // Set up the alert handler
    public LowStockAlertHandler() {
        this.lowStockQueue = new LinkedBlockingQueue<>();
        this.running = true;
    }

    // Start the alert handler running in its own thread
    public void start() {
        if (alertThread == null || !alertThread.isAlive()) {
            alertThread = new Thread(this);
            alertThread.setDaemon(true); // This means the thread will stop when the main program stops
            alertThread.start();
        }
    }

    // Stop the alert handler
    public void stop() {
        running = false;
        if (alertThread != null) {
            alertThread.interrupt();
        }
    }

    // This is called when a product is running low on stock
    @Override
    public void onLowStock(Product product) {
        try {
            // Add the product to our list of low stock items
            lowStockQueue.put(product);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // This is what the alert handler thread does
    @Override
    public void run() {
        while (running) {
            try {
                // Get the next product that's low on stock
                // This will wait until there is one
                Product product = lowStockQueue.take();

                // Handle the low stock alert
                processLowStockAlert(product);

                // Wait a bit before checking for more alerts
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    // This handles what to do when a product is low on stock
    private void processLowStockAlert(Product product) {
        // In a real system, this might send emails or notifications
        // For now, we just print a message
        System.out.println("LOW STOCK ALERT: " + product.getName() +
                " (ID: " + product.getId() + ") - Current stock: " + product.getQuantity() +
                ", Minimum level: " + product.getMinStockLevel());
    }
}