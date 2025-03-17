import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handles batch processing of inventory operations using multithreading
 */
public class BatchProcessor {
    private InventoryManager inventoryManager;
    private ExecutorService executorService;

    public BatchProcessor(InventoryManager inventoryManager, int threadPoolSize) {
        this.inventoryManager = inventoryManager;
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
    }

    /**
     * Processes a batch of product additions from a CSV file
     * Format: name,category,price,quantity,minStockLevel
     */
    public BatchResult processBatchProductAddition(String filePath) {
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        List<String> errors = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            List<String[]> batchItems = new ArrayList<>();

            // Skip header if exists
            line = reader.readLine();
            if (line != null && line.toLowerCase().contains("name") && line.toLowerCase().contains("category")) {
                // This is a header, skip it
            } else {
                // Not a header, process this line
                batchItems.add(line.split(","));
            }

            // Read remaining lines
            while ((line = reader.readLine()) != null) {
                batchItems.add(line.split(","));
            }

            reader.close();

            // Process batch items in parallel
            for (String[] item : batchItems) {
                executorService.submit(new Runnable() {
                    public void run() {
                        try {
                            if (item.length < 5) {
                                throw new InventoryException("Invalid data format: " + String.join(",", item));
                            }

                            String name = item[0].trim();
                            String category = item[1].trim();
                            double price = Double.parseDouble(item[2].trim());
                            int quantity = Integer.parseInt(item[3].trim());
                            int minStockLevel = Integer.parseInt(item[4].trim());

                            inventoryManager.addProduct(name, category, price, quantity, minStockLevel);
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            failureCount.incrementAndGet();
                            synchronized (errors) {
                                errors.add(String.join(",", item) + " - Error: " + e.getMessage());
                            }
                        }
                    }
                });
            }

            // Shutdown and wait for completion
            executorService.shutdown();
            executorService.awaitTermination(5, TimeUnit.MINUTES);

            return new BatchResult(successCount.get(), failureCount.get(), errors);
        } catch (IOException e) {
            return new BatchResult(successCount.get(), failureCount.get() + 1,
                    List.of("Error processing batch file: " + e.getMessage()));
        } catch (InterruptedException e) {
            return new BatchResult(successCount.get(), failureCount.get() + 1,
                    List.of("Error processing batch file: " + e.getMessage()));
        }
    }

    /**
     * Processes a batch of stock updates from a CSV file
     * Format: productId,quantityChange,transactionType
     */
    public BatchResult processBatchStockUpdate(String filePath, String userId) {
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        List<String> errors = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            List<String[]> batchItems = new ArrayList<>();

            // Skip header if exists
            line = reader.readLine();
            if (line != null && line.toLowerCase().contains("productid") &&
                    line.toLowerCase().contains("quantity")) {
                // This is a header, skip it
            } else {
                // Not a header, process this line
                batchItems.add(line.split(","));
            }

            // Read remaining lines
            while ((line = reader.readLine()) != null) {
                batchItems.add(line.split(","));
            }

            reader.close();

            // Process batch items in parallel
            for (String[] item : batchItems) {
                executorService.submit(new Runnable() {
                    public void run() {
                        try {
                            if (item.length < 3) {
                                throw new InventoryException("Invalid data format: " + String.join(",", item));
                            }

                            String productId = item[0].trim();
                            int quantityChange = Integer.parseInt(item[1].trim());
                            Transaction.TransactionType type = Transaction.TransactionType
                                    .valueOf(item[2].trim().toUpperCase());

                            inventoryManager.updateStock(productId, quantityChange, type, userId);
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            failureCount.incrementAndGet();
                            synchronized (errors) {
                                errors.add(String.join(",", item) + " - Error: " + e.getMessage());
                            }
                        }
                    }
                });
            }

            // Shutdown and wait for completion
            executorService.shutdown();
            executorService.awaitTermination(5, TimeUnit.MINUTES);

            return new BatchResult(successCount.get(), failureCount.get(), errors);
        } catch (IOException e) {
            return new BatchResult(successCount.get(), failureCount.get() + 1,
                    List.of("Error processing batch file: " + e.getMessage()));
        } catch (InterruptedException e) {
            return new BatchResult(successCount.get(), failureCount.get() + 1,
                    List.of("Error processing batch file: " + e.getMessage()));
        }
    }

    /**
     * Represents the result of a batch operation
     */
    public static class BatchResult {
        private final int successCount;
        private final int failureCount;
        private final List<String> errors;

        public BatchResult(int successCount, int failureCount, List<String> errors) {
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.errors = errors;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public int getFailureCount() {
            return failureCount;
        }

        public List<String> getErrors() {
            return errors;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Batch processing completed.\n");
            sb.append("Successful operations: ").append(successCount).append("\n");
            sb.append("Failed operations: ").append(failureCount).append("\n");

            if (!errors.isEmpty()) {
                sb.append("Errors:\n");
                for (String error : errors) {
                    sb.append("- ").append(error).append("\n");
                }
            }

            return sb.toString();
        }
    }
}