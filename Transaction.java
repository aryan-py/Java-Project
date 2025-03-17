import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// This class is for transactions
public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;

    // Types of transactions
    public enum TransactionType {
        PURCHASE, SALE, ADJUSTMENT, RETURN
    }

    // Transaction data
    public String id;
    public String productId;
    public TransactionType type;
    public int quantity;
    public LocalDateTime timestamp;
    public String userId;

    // Constructor
    public Transaction(String id, String productId, TransactionType type, int quantity, String userId) {
        this.id = id;
        this.productId = productId;
        this.type = type;
        this.quantity = quantity;
        this.timestamp = LocalDateTime.now();
        this.userId = userId;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // Print transaction info
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDate = timestamp.format(formatter);

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