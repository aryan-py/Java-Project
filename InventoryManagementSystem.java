import java.util.List;
import java.util.Scanner;

/**
 * Main application class for the Inventory Management System
 */
public class InventoryManagementSystem {
    // These are the main objects we need to run the system
    private static InventoryManager inventoryManager = new InventoryManager(); // Manages all inventory operations
    private static LowStockAlertHandler alertHandler = new LowStockAlertHandler(); // Handles low stock warnings
    private static Scanner scanner = new Scanner(System.in); // For reading user input
    private static String CURRENT_USER = "admin"; // Who is using the system (in a real system, this would come from
                                                  // login)

    // This is where the program starts
    public static void main(String[] args) {
        System.out.println("Starting Inventory Management System...");

        // Set up the low stock warning system
        inventoryManager.addLowStockObserver(alertHandler);
        alertHandler.start();

        // Main program loop - keeps running until user chooses to exit
        boolean running = true;
        while (running) {
            try {
                // Show the menu and get user's choice
                displayMainMenu();
                int choice = getIntInput("Enter your choice: ");

                // Do different things based on what the user chose
                if (choice == 1) {
                    listAllProducts(); // Show all products
                } else if (choice == 2) {
                    addNewProduct(); // Add a new product
                } else if (choice == 3) {
                    updateExistingProduct(); // Change a product's details
                } else if (choice == 4) {
                    updateProductStock(); // Change how many items we have
                } else if (choice == 5) {
                    removeProduct(); // Delete a product
                } else if (choice == 6) {
                    searchProducts(); // Look for specific products
                } else if (choice == 7) {
                    viewTransactions(); // See all the changes made
                } else if (choice == 8) {
                    viewLowStockProducts(); // See which items are running low
                } else if (choice == 9) {
                    batchProcessing(); // Do multiple changes at once
                } else if (choice == 0) {
                    running = false; // Exit the program
                } else {
                    System.out.println("Invalid choice. Please try again.");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }

        // Clean up before we exit
        inventoryManager.shutdown();
        alertHandler.stop();
        scanner.close();

        System.out.println("Inventory Management System shutdown complete.");
    }

    // Shows the main menu to the user
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

    // Shows the menu for batch operations (doing many things at once)
    private static void batchProcessing() {
        System.out.println("\n===== BATCH PROCESSING =====");
        System.out.println("1. Import Products from CSV");
        System.out.println("2. Update Stock from CSV");
        System.out.println("0. Back to Main Menu");

        int choice = getIntInput("Enter your choice: ");

        if (choice == 1) {
            importProductsFromCSV(); // Add many products from a file
        } else if (choice == 2) {
            updateStockFromCSV(); // Update many products' stock from a file
        } else if (choice == 0) {
            return; // Go back to main menu
        } else {
            System.out.println("Invalid choice. Please try again.");
        }
    }

    // Adds many products from a CSV file
    private static void importProductsFromCSV() {
        try {
            System.out.println("\n===== IMPORT PRODUCTS FROM CSV =====");
            String filePath = getStringInput("Enter CSV file path: ");

            // Create a processor that can handle many products at once
            BatchProcessor batchProcessor = new BatchProcessor(inventoryManager, 4);

            System.out.println("Processing batch import...");
            BatchProcessor.BatchResult result = batchProcessor.processBatchProductAddition(filePath);

            System.out.println(result);
        } catch (Exception e) {
            System.err.println("Error processing batch: " + e.getMessage());
        }
    }

    // Updates many products' stock from a CSV file
    private static void updateStockFromCSV() {
        try {
            System.out.println("\n===== UPDATE STOCK FROM CSV =====");
            String filePath = getStringInput("Enter CSV file path: ");

            // Create a processor that can handle many updates at once
            BatchProcessor batchProcessor = new BatchProcessor(inventoryManager, 4);

            System.out.println("Processing batch stock update...");
            BatchProcessor.BatchResult result = batchProcessor.processBatchStockUpdate(filePath, CURRENT_USER);

            System.out.println(result);
        } catch (Exception e) {
            System.err.println("Error processing batch: " + e.getMessage());
        }
    }

    // Shows all products in the system
    private static void listAllProducts() {
        List<Product> products = inventoryManager.getAllProducts();

        if (products.isEmpty()) {
            System.out.println("No products found in inventory.");
            return;
        }

        // Print a nice table of all products
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

    // Adds a new product to the system
    private static void addNewProduct() {
        try {
            System.out.println("\n===== ADD NEW PRODUCT =====");

            // Get all the details about the new product
            String name = getStringInput("Enter product name: ");
            String category = getStringInput("Enter product category: ");
            double price = getDoubleInput("Enter product price: ");
            int quantity = getIntInput("Enter initial quantity: ");
            int minStockLevel = getIntInput("Enter minimum stock level: ");

            // Add the product to the system
            Product product = inventoryManager.addProduct(name, category, price, quantity, minStockLevel);

            System.out.println("Product added successfully!");
            System.out.println("Product ID: " + product.getId());
        } catch (InventoryException e) {
            System.err.println("Failed to add product: " + e.getMessage());
        }
    }

    // Changes an existing product's details
    private static void updateExistingProduct() {
        try {
            System.out.println("\n===== UPDATE PRODUCT =====");

            // Show all products and let user pick one
            listAllProducts();
            String id = getStringInput("Enter product ID to update: ");

            // Check if the product exists
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

            // Get the new details for the product
            String name = getStringInput("Enter new product name: ");
            String category = getStringInput("Enter new product category: ");
            double price = getDoubleInput("Enter new product price: ");
            int minStockLevel = getIntInput("Enter new minimum stock level: ");

            // Update the product in the system
            Product product = inventoryManager.updateProduct(id, name, category, price, minStockLevel);

            System.out.println("Product updated successfully!");
            System.out.println(product);
        } catch (InventoryException e) {
            System.err.println("Failed to update product: " + e.getMessage());
        }
    }

    // Changes how many items we have of a product
    private static void updateProductStock() {
        try {
            System.out.println("\n===== UPDATE STOCK =====");

            // Show all products and let user pick one
            listAllProducts();
            String id = getStringInput("Enter product ID to update stock: ");

            // Check if the product exists
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

            // Show options for updating stock
            System.out.println("1. Add stock (Purchase)");
            System.out.println("2. Remove stock (Sale)");
            System.out.println("3. Adjust stock");
            System.out.println("4. Return stock");

            int choice = getIntInput("Enter your choice: ");
            Transaction.TransactionType type;
            int quantityChange;

            // Handle different types of stock updates
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

            // Update the stock in the system
            inventoryManager.updateStock(id, quantityChange, type, CURRENT_USER);

            // Get the new quantity
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

    // Removes a product from the system
    private static void removeProduct() {
        try {
            System.out.println("\n===== REMOVE PRODUCT =====");

            // Show all products and let user pick one
            listAllProducts();
            String id = getStringInput("Enter product ID to remove: ");

            // Check if the product exists
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

            // Ask for confirmation before removing
            String confirm = getStringInput("Are you sure you want to remove this product? (y/n): ");
            if (!confirm.equalsIgnoreCase("y")) {
                System.out.println("Operation cancelled.");
                return;
            }

            // Remove the product from the system
            inventoryManager.removeProduct(id);

            System.out.println("Product removed successfully!");
        } catch (InventoryException e) {
            System.err.println("Failed to remove product: " + e.getMessage());
        }
    }

    // Looks for products in the system
    private static void searchProducts() {
        System.out.println("\n===== SEARCH PRODUCTS =====");
        System.out.println("1. Search by name");
        System.out.println("2. Search by category");

        int choice = getIntInput("Enter your choice: ");
        List<Product> results;

        // Search based on user's choice
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

        // Show the search results
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

    // Shows all the changes made to inventory
    private static void viewTransactions() {
        System.out.println("\n===== VIEW TRANSACTIONS =====");
        System.out.println("1. View all transactions");
        System.out.println("2. View transactions for a specific product");

        int choice = getIntInput("Enter your choice: ");
        List<Transaction> transactions;

        // Get transactions based on user's choice
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

        // Show the transactions
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

    // Shows which products are running low on stock
    private static void viewLowStockProducts() {
        List<Product> lowStockProducts = inventoryManager.getLowStockProducts();

        if (lowStockProducts.isEmpty()) {
            System.out.println("No products are currently low on stock.");
            return;
        }

        // Print a nice table of low stock products
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

    // Helper methods for getting input from the user

    // Get text input from user
    private static String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    // Get a whole number from user
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

    // Get a decimal number from user
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