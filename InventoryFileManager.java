import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// This class handles saving and loading data to and from files
// It makes sure we don't lose our inventory data when the program closes
public class InventoryFileManager {
    // These are the names of the files we use to store data
    private static final String PRODUCTS_FILE = "products.dat";
    private static final String TRANSACTIONS_FILE = "transactions.dat";

    // These help us make sure we don't have problems when multiple threads try to
    // read or write files
    private final ReadWriteLock productsLock = new ReentrantReadWriteLock();
    private final ReadWriteLock transactionsLock = new ReentrantReadWriteLock();

    // Save all products to a file
    public void saveProducts(List<Product> products) throws InventoryException {
        productsLock.writeLock().lock(); // Get permission to write to the file
        try {
            // Create a file writer
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PRODUCTS_FILE));
            // Save the products to the file
            oos.writeObject(new ArrayList<>(products));
            oos.close();
        } catch (IOException e) {
            throw new InventoryException("Failed to save products to file: " + e.getMessage(),
                    e, InventoryException.ErrorCode.FILE_ACCESS_ERROR);
        } finally {
            productsLock.writeLock().unlock(); // Always release the lock when we're done
        }
    }

    // Load all products from a file
    @SuppressWarnings("unchecked")
    public List<Product> loadProducts() throws InventoryException {
        productsLock.readLock().lock(); // Get permission to read from the file
        try {
            // Check if the file exists
            File file = new File(PRODUCTS_FILE);
            if (!file.exists()) {
                return new ArrayList<>(); // Return empty list if no file exists
            }

            // Create a file reader
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            // Load the products from the file
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
            productsLock.readLock().unlock(); // Always release the lock when we're done
        }
    }

    // Save all transactions to a file
    public void saveTransactions(List<Transaction> transactions) throws InventoryException {
        transactionsLock.writeLock().lock(); // Get permission to write to the file
        try {
            // Create a file writer
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(TRANSACTIONS_FILE));
            // Save the transactions to the file
            oos.writeObject(new ArrayList<>(transactions));
            oos.close();
        } catch (IOException e) {
            throw new InventoryException("Failed to save transactions to file: " + e.getMessage(),
                    e, InventoryException.ErrorCode.FILE_ACCESS_ERROR);
        } finally {
            transactionsLock.writeLock().unlock(); // Always release the lock when we're done
        }
    }

    // Load all transactions from a file
    @SuppressWarnings("unchecked")
    public List<Transaction> loadTransactions() throws InventoryException {
        transactionsLock.readLock().lock(); // Get permission to read from the file
        try {
            // Check if the file exists
            File file = new File(TRANSACTIONS_FILE);
            if (!file.exists()) {
                return new ArrayList<>(); // Return empty list if no file exists
            }

            // Create a file reader
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            // Load the transactions from the file
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
            transactionsLock.readLock().unlock(); // Always release the lock when we're done
        }
    }

    // Add a new transaction to the transactions file
    public void appendTransaction(Transaction transaction) throws InventoryException {
        // Load all existing transactions
        List<Transaction> transactions = loadTransactions();
        // Add the new transaction
        transactions.add(transaction);
        // Save all transactions back to the file
        saveTransactions(transactions);
    }
}