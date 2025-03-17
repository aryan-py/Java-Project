import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// This class keeps track of all changes made to inventory
// Serializable means we can save it to a file
public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L; // Needed for saving to file

    // These are the different types of changes we can make
    public enum TransactionType {
        PURCHASE, // When we buy more items
        SALE, // When we sell items
        ADJUSTMENT, // When we fix a mistake in the count
        RETURN // When customers return items
    }

    // These are all the details we store about a transaction
    public String id; // Unique identifier for the transaction
    public String productId; // Which product was changed
    public TransactionType type; // What kind of change was made
    public int quantity; // How many items were changed
    public LocalDateTime timestamp; // When the change happened
    public String userId; // Who made the change

    // This is how we create a new transaction
    public Transaction(String id, String productId, TransactionType type, int quantity, String userId) {
        this.id = id;
        this.productId = productId;
        this.type = type;
        this.quantity = quantity;
        this.timestamp = LocalDateTime.now(); // Set the time to right now
        this.userId = userId;
    }

    // These methods let us get and set the transaction's details
    // Getters - get the values
    public String getId() {
        return id;
    }

    public String getProductId() {
        return productId;
    }

    public TransactionType getType() {
        return type;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getUserId() {
        return userId;
    }

    // Setters - change the values
    public void setId(String id) {
        this.id = id;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // This makes the transaction look nice when we print it
    @Override
    public String toString() {
        // Format the date and time nicely
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDate = timestamp.format(formatter);

        // Create a string with all the transaction details
        String result = "Transaction{" +
                "id='" + id + '\'' +
                ", productId='" + productId + '\'' +
                ", type=" + type +
                ", quantity=" + quantity +
                ", timestamp=" + formattedDate +
                ", userId='" + userId + '\'' +
                '}';
        return result;
    }
}