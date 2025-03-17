// This is for errors in the inventory system
public class InventoryException extends Exception {
    private static final long serialVersionUID = 1L;

    // Error codes
    public enum ErrorCode {
        PRODUCT_NOT_FOUND,
        INSUFFICIENT_STOCK,
        DUPLICATE_PRODUCT,
        INVALID_QUANTITY,
        INVALID_PRICE,
        FILE_ACCESS_ERROR,
        DATA_FORMAT_ERROR,
        TRANSACTION_FAILED
    }

    // Error code
    public ErrorCode errorCode;

    // Constructors
    public InventoryException(String message) {
        super(message);
    }

    public InventoryException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public InventoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public InventoryException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    // Get error code
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}