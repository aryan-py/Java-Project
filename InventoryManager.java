import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages inventory operations with thread safety
 */
public class InventoryManager {
    private final InventoryFileManager fileManager;
    private final List<Product> products;
    private final List<Transaction> transactions;
    private final ExecutorService transactionExecutor;
    private final List<LowStockObserver> lowStockObservers;

    public InventoryManager() {
        this.fileManager = new InventoryFileManager();
        this.products = new CopyOnWriteArrayList<>();
        this.transactions = new CopyOnWriteArrayList<>();
        this.transactionExecutor = Executors.newFixedThreadPool(3);
        this.lowStockObservers = new ArrayList<>();

        // Load data from files
        try {
            List<Product> loadedProducts = fileManager.loadProducts();
            for (Product p : loadedProducts) {
                this.products.add(p);
            }

            List<Transaction> loadedTransactions = fileManager.loadTransactions();
            for (Transaction t : loadedTransactions) {
                this.transactions.add(t);
            }
        } catch (InventoryException e) {
            System.err.println("Error loading inventory data: " + e.getMessage());
        }
    }

    /**
     * Adds a new product to the inventory
     */
    public synchronized Product addProduct(String name, String category, double price, int quantity, int minStockLevel)
            throws InventoryException {
        // Validate inputs
        if (price <= 0) {
            throw new InventoryException("Price must be greater than zero",
                    InventoryException.ErrorCode.INVALID_PRICE);
        }

        if (quantity < 0) {
            throw new InventoryException("Quantity cannot be negative",
                    InventoryException.ErrorCode.INVALID_QUANTITY);
        }

        // Check for duplicate product name
        boolean duplicateExists = false;
        for (Product p : products) {
            if (p.getName().equalsIgnoreCase(name)) {
                duplicateExists = true;
                break;
            }
        }

        if (duplicateExists) {
            throw new InventoryException("Product with name '" + name + "' already exists",
                    InventoryException.ErrorCode.DUPLICATE_PRODUCT);
        }

        // Create new product
        String id = UUID.randomUUID().toString();
        Product product = new Product(id, name, category, price, quantity, minStockLevel);
        products.add(product);

        // Save to file
        try {
            fileManager.saveProducts(products);

            // Create transaction for initial stock
            if (quantity > 0) {
                recordTransaction(product.getId(), Transaction.TransactionType.PURCHASE, quantity, "system");
            }

            // Check for low stock
            checkLowStock(product);

            return product;
        } catch (InventoryException e) {
            // Rollback
            products.remove(product);
            throw e;
        }
    }

    /**
     * Updates an existing product
     */
    public synchronized Product updateProduct(String id, String name, String category, double price, int minStockLevel)
            throws InventoryException {
        // Find product
        Product product = null;
        for (Product p : products) {
            if (p.getId().equals(id)) {
                product = p;
                break;
            }
        }

        if (product == null) {
            throw new InventoryException("Product not found with ID: " + id,
                    InventoryException.ErrorCode.PRODUCT_NOT_FOUND);
        }

        // Validate inputs
        if (price <= 0) {
            throw new InventoryException("Price must be greater than zero",
                    InventoryException.ErrorCode.INVALID_PRICE);
        }

        // Check for duplicate name if name is changing
        if (!product.getName().equals(name)) {
            boolean duplicateExists = false;
            for (Product p : products) {
                if (p.getName().equalsIgnoreCase(name)) {
                    duplicateExists = true;
                    break;
                }
            }

            if (duplicateExists) {
                throw new InventoryException("Product with name '" + name + "' already exists",
                        InventoryException.ErrorCode.DUPLICATE_PRODUCT);
            }
        }

        // Update product
        product.setName(name);
        product.setCategory(category);
        product.setPrice(price);
        product.setMinStockLevel(minStockLevel);

        // Save to file
        try {
            fileManager.saveProducts(products);

            // Check for low stock
            checkLowStock(product);

            return product;
        } catch (InventoryException e) {
            throw e;
        }
    }

    /**
     * Updates product quantity and records a transaction
     */
    public synchronized void updateStock(String productId, int quantityChange, Transaction.TransactionType type,
            String userId)
            throws InventoryException {
        // Find product
        Product product = null;
        for (Product p : products) {
            if (p.getId().equals(productId)) {
                product = p;
                break;
            }
        }

        if (product == null) {
            throw new InventoryException("Product not found with ID: " + productId,
                    InventoryException.ErrorCode.PRODUCT_NOT_FOUND);
        }

        // Check for sufficient stock if removing items
        if (type == Transaction.TransactionType.SALE && Math.abs(quantityChange) > product.getQuantity()) {
            throw new InventoryException("Insufficient stock. Available: " + product.getQuantity() +
                    ", Requested: " + Math.abs(quantityChange),
                    InventoryException.ErrorCode.INSUFFICIENT_STOCK);
        }

        // Update quantity
        int newQuantity = product.getQuantity() + quantityChange;
        if (newQuantity < 0) {
            throw new InventoryException("Operation would result in negative stock",
                    InventoryException.ErrorCode.INVALID_QUANTITY);
        }

        product.setQuantity(newQuantity);

        // Save to file
        try {
            fileManager.saveProducts(products);

            // Record transaction asynchronously
            recordTransaction(productId, type, quantityChange, userId);

            // Check for low stock
            checkLowStock(product);
        } catch (InventoryException e) {
            // Rollback
            product.setQuantity(product.getQuantity() - quantityChange);
            throw e;
        }
    }

    /**
     * Removes a product from inventory
     */
    public synchronized void removeProduct(String productId) throws InventoryException {
        // Find product
        Product product = null;
        for (Product p : products) {
            if (p.getId().equals(productId)) {
                product = p;
                break;
            }
        }

        if (product == null) {
            throw new InventoryException("Product not found with ID: " + productId,
                    InventoryException.ErrorCode.PRODUCT_NOT_FOUND);
        }

        // Remove product
        products.remove(product);

        // Save to file
        try {
            fileManager.saveProducts(products);

            // Record transaction
            recordTransaction(productId, Transaction.TransactionType.ADJUSTMENT, -product.getQuantity(), "system");
        } catch (InventoryException e) {
            // Rollback
            products.add(product);
            throw e;
        }
    }

    /**
     * Records a transaction asynchronously
     */
    private void recordTransaction(String productId, Transaction.TransactionType type, int quantity, String userId) {
        transactionExecutor.submit(new Runnable() {
            public void run() {
                try {
                    String transactionId = UUID.randomUUID().toString();
                    Transaction transaction = new Transaction(transactionId, productId, type, quantity, userId);
                    transactions.add(transaction);
                    fileManager.appendTransaction(transaction);
                } catch (InventoryException e) {
                    System.err.println("Error recording transaction: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Finds a product by ID
     */
    public Optional<Product> findProductById(String id) {
        for (Product p : products) {
            if (p.getId().equals(id)) {
                return Optional.of(p);
            }
        }
        return Optional.empty();
    }

    /**
     * Finds products by name (partial match)
     */
    public List<Product> findProductsByName(String name) {
        List<Product> result = new ArrayList<>();
        for (Product p : products) {
            if (p.getName().toLowerCase().contains(name.toLowerCase())) {
                result.add(p);
            }
        }
        return result;
    }

    /**
     * Finds products by category
     */
    public List<Product> findProductsByCategory(String category) {
        List<Product> result = new ArrayList<>();
        for (Product p : products) {
            if (p.getCategory().equalsIgnoreCase(category)) {
                result.add(p);
            }
        }
        return result;
    }

    /**
     * Gets all products
     */
    public List<Product> getAllProducts() {
        List<Product> result = new ArrayList<>();
        for (Product p : products) {
            result.add(p);
        }
        return result;
    }

    /**
     * Gets all transactions
     */
    public List<Transaction> getAllTransactions() {
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : transactions) {
            result.add(t);
        }
        return result;
    }

    /**
     * Gets transactions for a specific product
     */
    public List<Transaction> getTransactionsForProduct(String productId) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t.getProductId().equals(productId)) {
                result.add(t);
            }
        }
        return result;
    }

    /**
     * Gets low stock products
     */
    public List<Product> getLowStockProducts() {
        List<Product> result = new ArrayList<>();
        for (Product p : products) {
            if (p.isLowStock()) {
                result.add(p);
            }
        }
        return result;
    }

    /**
     * Checks if a product is low on stock and notifies observers
     */
    private void checkLowStock(Product product) {
        if (product.isLowStock()) {
            for (int i = 0; i < lowStockObservers.size(); i++) {
                LowStockObserver observer = lowStockObservers.get(i);
                observer.onLowStock(product);
            }
        }
    }

    /**
     * Adds a low stock observer
     */
    public void addLowStockObserver(LowStockObserver observer) {
        lowStockObservers.add(observer);
    }

    /**
     * Removes a low stock observer
     */
    public void removeLowStockObserver(LowStockObserver observer) {
        lowStockObservers.remove(observer);
    }

    /**
     * Shuts down the executor service
     */
    public void shutdown() {
        transactionExecutor.shutdown();
    }

    /**
     * Interface for low stock observers
     */
    public interface LowStockObserver {
        void onLowStock(Product product);
    }
}