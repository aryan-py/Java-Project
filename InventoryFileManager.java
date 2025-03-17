import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Handles file operations for the inventory system
 */
public class InventoryFileManager {
    private static final String PRODUCTS_FILE = "products.dat";
    private static final String TRANSACTIONS_FILE = "transactions.dat";

    // Thread safety with read-write locks
    private final ReadWriteLock productsLock = new ReentrantReadWriteLock();
    private final ReadWriteLock transactionsLock = new ReentrantReadWriteLock();

    /**
     * Saves the list of products to file
     */
    public void saveProducts(List<Product> products) throws InventoryException {
        productsLock.writeLock().lock();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PRODUCTS_FILE));
            oos.writeObject(new ArrayList<>(products));
            oos.close();
        } catch (IOException e) {
            throw new InventoryException("Failed to save products to file: " + e.getMessage(),
                    e, InventoryException.ErrorCode.FILE_ACCESS_ERROR);
        } finally {
            productsLock.writeLock().unlock();
        }
    }

    /**
     * Loads the list of products from file
     */
    @SuppressWarnings("unchecked")
    public List<Product> loadProducts() throws InventoryException {
        productsLock.readLock().lock();
        try {
            File file = new File(PRODUCTS_FILE);
            if (!file.exists()) {
                return new ArrayList<>();
            }

            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            List<Product> products = (List<Product>) ois.readObject();
            ois.close();
            return products;
        } catch (ClassNotFoundException e) {
            throw new InventoryException("Invalid data format in products file",
                    e, InventoryException.ErrorCode.DATA_FORMAT_ERROR);
        } catch (IOException e) {
            throw new InventoryException("Failed to load products from file: " + e.getMessage(),
                    e, InventoryException.ErrorCode.FILE_ACCESS_ERROR);
        } finally {
            productsLock.readLock().unlock();
        }
    }

    /**
     * Saves the list of transactions to file
     */
    public void saveTransactions(List<Transaction> transactions) throws InventoryException {
        transactionsLock.writeLock().lock();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(TRANSACTIONS_FILE));
            oos.writeObject(new ArrayList<>(transactions));
            oos.close();
        } catch (IOException e) {
            throw new InventoryException("Failed to save transactions to file: " + e.getMessage(),
                    e, InventoryException.ErrorCode.FILE_ACCESS_ERROR);
        } finally {
            transactionsLock.writeLock().unlock();
        }
    }

    /**
     * Loads the list of transactions from file
     */
    @SuppressWarnings("unchecked")
    public List<Transaction> loadTransactions() throws InventoryException {
        transactionsLock.readLock().lock();
        try {
            File file = new File(TRANSACTIONS_FILE);
            if (!file.exists()) {
                return new ArrayList<>();
            }

            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            List<Transaction> transactions = (List<Transaction>) ois.readObject();
            ois.close();
            return transactions;
        } catch (ClassNotFoundException e) {
            throw new InventoryException("Invalid data format in transactions file",
                    e, InventoryException.ErrorCode.DATA_FORMAT_ERROR);
        } catch (IOException e) {
            throw new InventoryException("Failed to load transactions from file: " + e.getMessage(),
                    e, InventoryException.ErrorCode.FILE_ACCESS_ERROR);
        } finally {
            transactionsLock.readLock().unlock();
        }
    }

    /**
     * Appends a single transaction to the transactions file
     */
    public void appendTransaction(Transaction transaction) throws InventoryException {
        List<Transaction> transactions = loadTransactions();
        transactions.add(transaction);
        saveTransactions(transactions);
    }
}