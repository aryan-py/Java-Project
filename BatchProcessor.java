import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

// This class helps us process many changes at once from a CSV file
// It can handle adding many products or updating many products' stock at once
public class BatchProcessor {
    // This manages our inventory
    private InventoryManager inventoryManager;
    // This helps us do many things at the same time
    private ExecutorService executorService;

    // Set up the batch processor
    public BatchProcessor(InventoryManager inventoryManager, int threadPoolSize) {
        this.inventoryManager = inventoryManager;
        // Create a pool of workers to process items
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
    }

    // Add many products from a CSV file
    // The file should have lines like: name,category,price,quantity,minStockLevel
    public BatchResult processBatchProductAddition(String filePath) {
        // Keep track of how many items we successfully processed
        AtomicInteger successCount = new AtomicInteger(0);
        // Keep track of how many items failed
        AtomicInteger failureCount = new AtomicInteger(0);
        // Keep track of any errors that happened
        List<String> errors = new ArrayList<>();

        try {
            // Open the CSV file
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            List<String[]> batchItems = new ArrayList<>();

            // Read the first line to see if it's a header
            line = reader.readLine();
            if (line != null && line.toLowerCase().contains("name") && line.toLowerCase().contains("category")) {
                // This is a header, skip it
            } else {
                // Not a header, process this line
                batchItems.add(line.split(","));
            }

            // Read all the remaining lines
            while ((line = reader.readLine()) != null) {
                batchItems.add(line.split(","));
            }

            reader.close();

            // Process each item in the file
            for (String[] item : batchItems) {
                executorService.submit(new Runnable() {
                    public void run() {
                        try {
                            // Check if we have all the data we need
                            if (item.length < 5) {
                                throw new InventoryException("Invalid data format: " + String.join(",", item));
                            }

                            // Get the data from the CSV line
                            String name = item[0].trim();
                            String category = item[1].trim();
                            double price = Double.parseDouble(item[2].trim());
                            int quantity = Integer.parseInt(item[3].trim());
                            int minStockLevel = Integer.parseInt(item[4].trim());

                            // Add the product to our inventory
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

            // Wait for all items to be processed
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

    // Update many products' stock from a CSV file
    // The file should have lines like: productId,quantityChange,transactionType
    public BatchResult processBatchStockUpdate(String filePath, String userId) {
        // Keep track of how many items we successfully processed
        AtomicInteger successCount = new AtomicInteger(0);
        // Keep track of how many items failed
        AtomicInteger failureCount = new AtomicInteger(0);
        // Keep track of any errors that happened
        List<String> errors = new ArrayList<>();

        try {
            // Open the CSV file
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            List<String[]> batchItems = new ArrayList<>();

            // Read the first line to see if it's a header
            line = reader.readLine();
            if (line != null && line.toLowerCase().contains("productid") &&
                    line.toLowerCase().contains("quantity")) {
                // This is a header, skip it
            } else {
                // Not a header, process this line
                batchItems.add(line.split(","));
            }

            // Read all the remaining lines
            while ((line = reader.readLine()) != null) {
                batchItems.add(line.split(","));
            }

            reader.close();

            // Process each item in the file
            for (String[] item : batchItems) {
                executorService.submit(new Runnable() {
                    public void run() {
                        try {
                            // Check if we have all the data we need
                            if (item.length < 3) {
                                throw new InventoryException("Invalid data format: " + String.join(",", item));
                            }

                            // Get the data from the CSV line
                            String productId = item[0].trim();
                            int quantityChange = Integer.parseInt(item[1].trim());
                            Transaction.TransactionType type = Transaction.TransactionType
                                    .valueOf(item[2].trim().toUpperCase());

                            // Update the product's stock in our inventory
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

            // Wait for all items to be processed
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

    // This class holds the results of processing a batch of items
    public static class BatchResult {
        private final int successCount; // How many items were processed successfully
        private final int failureCount; // How many items failed to process
        private final List<String> errors; // What went wrong with the failed items

        // Create a new batch result
        public BatchResult(int successCount, int failureCount, List<String> errors) {
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.errors = errors;
        }

        // Get how many items were processed successfully
        public int getSuccessCount() {
            return successCount;
        }

        // Get how many items failed to process
        public int getFailureCount() {
            return failureCount;
        }

        // Get what went wrong with the failed items
        public List<String> getErrors() {
            return errors;
        }

        // Make the result look nice when we print it
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