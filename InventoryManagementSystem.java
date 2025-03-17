import java.util.List;
import java.util.Scanner;

/**
 * Main application class for the Inventory Management System
 */
public class InventoryManagementSystem {
    private static InventoryManager inventoryManager = new InventoryManager();
    private static LowStockAlertHandler alertHandler = new LowStockAlertHandler();
    private static Scanner scanner = new Scanner(System.in);
    private static String CURRENT_USER = "admin"; // In a real system, this would be from login

    public static void main(String[] args) {
        System.out.println("Starting Inventory Management System...");

        // Register and start the low stock alert handler
        inventoryManager.addLowStockObserver(alertHandler);
        alertHandler.start();

        boolean running = true;
        while (running) {
            try {
                displayMainMenu();
                int choice = getIntInput("Enter your choice: ");

                switch (choice) {
                    case 1:
                        listAllProducts();
                        break;
                    case 2:
                        addNewProduct();
                        break;
                    case 3:
                        updateExistingProduct();
                        break;
                    case 4:
                        updateProductStock();
                        break;
                    case 5:
                        removeProduct();
                        break;
                    case 6:
                        searchProducts();
                        break;
                    case 7:
                        viewTransactions();
                        break;
                    case 8:
                        viewLowStockProducts();
                        break;
                    case 9:
                        batchProcessing();
                        break;
                    case 0:
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }

        // Shutdown resources
        inventoryManager.shutdown();
        alertHandler.stop();
        scanner.close();

        System.out.println("Inventory Management System shutdown complete.");
    }

    private static void displayMainMenu() {
        System.out.println("\n===== INVENTORY MANAGEMENT SYSTEM =====");
        System.out.println("1. List All Products");
        System.out.println("2. Add New Product");
        System.out.println("3. Update Product");
        System.out.println("4. Update Stock");
        System.out.println("5. Remove Product");
        System.out.println("6. Search Products");
        System.out.println("7. View Transactions");
        System.out.println("8. View Low Stock Products");
        System.out.println("9. Batch Processing");
        System.out.println("0. Exit");
        System.out.println("=======================================");
    }

    private static void batchProcessing() {
        System.out.println("\n===== BATCH PROCESSING =====");
        System.out.println("1. Import Products from CSV");
        System.out.println("2. Update Stock from CSV");
        System.out.println("0. Back to Main Menu");

        int choice = getIntInput("Enter your choice: ");

        switch (choice) {
            case 1:
                importProductsFromCSV();
                break;
            case 2:
                updateStockFromCSV();
                break;
            case 0:
                return;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    private static void importProductsFromCSV() {
        try {
            System.out.println("\n===== IMPORT PRODUCTS FROM CSV =====");
            String filePath = getStringInput("Enter CSV file path: ");

            // Create batch processor with 4 threads
            BatchProcessor batchProcessor = new BatchProcessor(inventoryManager, 4);

            System.out.println("Processing batch import...");
            BatchProcessor.BatchResult result = batchProcessor.processBatchProductAddition(filePath);

            System.out.println(result);
        } catch (Exception e) {
            System.err.println("Error processing batch: " + e.getMessage());
        }
    }

    private static void updateStockFromCSV() {
        try {
            System.out.println("\n===== UPDATE STOCK FROM CSV =====");
            String filePath = getStringInput("Enter CSV file path: ");

            // Create batch processor with 4 threads
            BatchProcessor batchProcessor = new BatchProcessor(inventoryManager, 4);

            System.out.println("Processing batch stock update...");
            BatchProcessor.BatchResult result = batchProcessor.processBatchStockUpdate(filePath, CURRENT_USER);

            System.out.println(result);
        } catch (Exception e) {
            System.err.println("Error processing batch: " + e.getMessage());
        }
    }

    private static void listAllProducts() {
        List<Product> products = inventoryManager.getAllProducts();

        if (products.isEmpty()) {
            System.out.println("No products found in inventory.");
            return;
        }

        System.out.println("\n===== PRODUCT LIST =====");
        System.out.printf("%-36s %-20s %-15s %-10s %-10s %-15s%n",
                "ID", "NAME", "CATEGORY", "PRICE", "QUANTITY", "MIN STOCK LEVEL");

        for (Product product : products) {
            System.out.printf("%-36s %-20s %-15s $%-9.2f %-10d %-15d%n",
                    product.getId(),
                    product.getName(),
                    product.getCategory(),
                    product.getPrice(),
                    product.getQuantity(),
                    product.getMinStockLevel());
        }
    }

    private static void addNewProduct() {
        try {
            System.out.println("\n===== ADD NEW PRODUCT =====");

            String name = getStringInput("Enter product name: ");
            String category = getStringInput("Enter product category: ");
            double price = getDoubleInput("Enter product price: ");
            int quantity = getIntInput("Enter initial quantity: ");
            int minStockLevel = getIntInput("Enter minimum stock level: ");

            Product product = inventoryManager.addProduct(name, category, price, quantity, minStockLevel);

            System.out.println("Product added successfully!");
            System.out.println("Product ID: " + product.getId());
        } catch (InventoryException e) {
            System.err.println("Failed to add product: " + e.getMessage());
        }
    }

    private static void updateExistingProduct() {
        try {
            System.out.println("\n===== UPDATE PRODUCT =====");

            listAllProducts();
            String id = getStringInput("Enter product ID to update: ");

            // Check if product exists
            boolean productExists = false;
            for (Product p : inventoryManager.getAllProducts()) {
                if (p.getId().equals(id)) {
                    productExists = true;
                    break;
                }
            }

            if (!productExists) {
                System.err.println("Product not found with ID: " + id);
                return;
            }

            String name = getStringInput("Enter new product name: ");
            String category = getStringInput("Enter new product category: ");
            double price = getDoubleInput("Enter new product price: ");
            int minStockLevel = getIntInput("Enter new minimum stock level: ");

            Product product = inventoryManager.updateProduct(id, name, category, price, minStockLevel);

            System.out.println("Product updated successfully!");
            System.out.println(product);
        } catch (InventoryException e) {
            System.err.println("Failed to update product: " + e.getMessage());
        }
    }

    private static void updateProductStock() {
        try {
            System.out.println("\n===== UPDATE STOCK =====");

            listAllProducts();
            String id = getStringInput("Enter product ID to update stock: ");

            // Check if product exists
            boolean productExists = false;
            for (Product p : inventoryManager.getAllProducts()) {
                if (p.getId().equals(id)) {
                    productExists = true;
                    break;
                }
            }

            if (!productExists) {
                System.err.println("Product not found with ID: " + id);
                return;
            }

            System.out.println("1. Add stock (Purchase)");
            System.out.println("2. Remove stock (Sale)");
            System.out.println("3. Adjust stock");
            System.out.println("4. Return stock");

            int choice = getIntInput("Enter your choice: ");
            Transaction.TransactionType type;
            int quantityChange;

            if (choice == 1) {
                type = Transaction.TransactionType.PURCHASE;
                quantityChange = getIntInput("Enter quantity to add: ");
            } else if (choice == 2) {
                type = Transaction.TransactionType.SALE;
                quantityChange = -getIntInput("Enter quantity to remove: ");
            } else if (choice == 3) {
                type = Transaction.TransactionType.ADJUSTMENT;

                // Get current quantity
                int currentQuantity = 0;
                for (Product p : inventoryManager.getAllProducts()) {
                    if (p.getId().equals(id)) {
                        currentQuantity = p.getQuantity();
                        break;
                    }
                }

                System.out.println("Current quantity: " + currentQuantity);
                int newQuantity = getIntInput("Enter new quantity: ");
                quantityChange = newQuantity - currentQuantity;
            } else if (choice == 4) {
                type = Transaction.TransactionType.RETURN;
                quantityChange = getIntInput("Enter quantity to return: ");
            } else {
                System.err.println("Invalid choice.");
                return;
            }

            inventoryManager.updateStock(id, quantityChange, type, CURRENT_USER);

            // Get updated quantity
            int updatedQuantity = 0;
            for (Product p : inventoryManager.getAllProducts()) {
                if (p.getId().equals(id)) {
                    updatedQuantity = p.getQuantity();
                    break;
                }
            }

            System.out.println("Stock updated successfully!");
            System.out.println("New quantity: " + updatedQuantity);
        } catch (InventoryException e) {
            System.err.println("Failed to update stock: " + e.getMessage());
        }
    }

    private static void removeProduct() {
        try {
            System.out.println("\n===== REMOVE PRODUCT =====");

            listAllProducts();
            String id = getStringInput("Enter product ID to remove: ");

            // Check if product exists
            boolean productExists = false;
            for (Product p : inventoryManager.getAllProducts()) {
                if (p.getId().equals(id)) {
                    productExists = true;
                    break;
                }
            }

            if (!productExists) {
                System.err.println("Product not found with ID: " + id);
                return;
            }

            String confirm = getStringInput("Are you sure you want to remove this product? (y/n): ");
            if (!confirm.equalsIgnoreCase("y")) {
                System.out.println("Operation cancelled.");
                return;
            }

            inventoryManager.removeProduct(id);

            System.out.println("Product removed successfully!");
        } catch (InventoryException e) {
            System.err.println("Failed to remove product: " + e.getMessage());
        }
    }

    private static void searchProducts() {
        System.out.println("\n===== SEARCH PRODUCTS =====");
        System.out.println("1. Search by name");
        System.out.println("2. Search by category");

        int choice = getIntInput("Enter your choice: ");
        List<Product> results;

        if (choice == 1) {
            String name = getStringInput("Enter product name to search: ");
            results = inventoryManager.findProductsByName(name);
        } else if (choice == 2) {
            String category = getStringInput("Enter product category to search: ");
            results = inventoryManager.findProductsByCategory(category);
        } else {
            System.err.println("Invalid choice.");
            return;
        }

        if (results.isEmpty()) {
            System.out.println("No products found matching your search criteria.");
            return;
        }

        System.out.println("\n===== SEARCH RESULTS =====");
        System.out.printf("%-36s %-20s %-15s %-10s %-10s %-15s%n",
                "ID", "NAME", "CATEGORY", "PRICE", "QUANTITY", "MIN STOCK LEVEL");

        for (Product product : results) {
            System.out.printf("%-36s %-20s %-15s $%-9.2f %-10d %-15d%n",
                    product.getId(),
                    product.getName(),
                    product.getCategory(),
                    product.getPrice(),
                    product.getQuantity(),
                    product.getMinStockLevel());
        }
    }

    private static void viewTransactions() {
        System.out.println("\n===== VIEW TRANSACTIONS =====");
        System.out.println("1. View all transactions");
        System.out.println("2. View transactions for a specific product");

        int choice = getIntInput("Enter your choice: ");
        List<Transaction> transactions;

        if (choice == 1) {
            transactions = inventoryManager.getAllTransactions();
        } else if (choice == 2) {
            listAllProducts();
            String productId = getStringInput("Enter product ID: ");
            transactions = inventoryManager.getTransactionsForProduct(productId);
        } else {
            System.err.println("Invalid choice.");
            return;
        }

        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }

        System.out.println("\n===== TRANSACTION LIST =====");
        System.out.printf("%-36s %-36s %-12s %-10s %-20s %-15s%n",
                "TRANSACTION ID", "PRODUCT ID", "TYPE", "QUANTITY", "TIMESTAMP", "USER");

        for (Transaction transaction : transactions) {
            System.out.printf("%-36s %-36s %-12s %-10d %-20s %-15s%n",
                    transaction.getId(),
                    transaction.getProductId(),
                    transaction.getType(),
                    transaction.getQuantity(),
                    transaction.getTimestamp().toString(),
                    transaction.getUserId());
        }
    }

    private static void viewLowStockProducts() {
        List<Product> lowStockProducts = inventoryManager.getLowStockProducts();

        if (lowStockProducts.isEmpty()) {
            System.out.println("No products are currently low on stock.");
            return;
        }

        System.out.println("\n===== LOW STOCK PRODUCTS =====");
        System.out.printf("%-36s %-20s %-15s %-10s %-10s %-15s%n",
                "ID", "NAME", "CATEGORY", "PRICE", "QUANTITY", "MIN STOCK LEVEL");

        for (Product product : lowStockProducts) {
            System.out.printf("%-36s %-20s %-15s $%-9.2f %-10d %-15d%n",
                    product.getId(),
                    product.getName(),
                    product.getCategory(),
                    product.getPrice(),
                    product.getQuantity(),
                    product.getMinStockLevel());
        }
    }

    // Helper methods for input handling

    private static String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();
                int value = Integer.parseInt(input);
                return value;
            } catch (NumberFormatException e) {
                System.err.println("Please enter a valid number.");
            }
        }
    }

    private static double getDoubleInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();
                double value = Double.parseDouble(input);
                return value;
            } catch (NumberFormatException e) {
                System.err.println("Please enter a valid number.");
            }
        }
    }
}