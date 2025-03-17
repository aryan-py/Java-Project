// This class is for handling errors in our inventory system
// It helps us know what went wrong when something fails
public class InventoryException extends Exception {
    private static final long serialVersionUID = 1L; // Needed for saving to file

    // These are all the different types of errors that can happen
    public enum ErrorCode {
        PRODUCT_NOT_FOUND, // We tried to find a product that doesn't exist
        INSUFFICIENT_STOCK, // We tried to sell more items than we have
        DUPLICATE_PRODUCT, // We tried to add a product that already exists
        INVALID_QUANTITY, // We tried to set a quantity that doesn't make sense
        INVALID_PRICE, // We tried to set a price that doesn't make sense
        FILE_ACCESS_ERROR, // We couldn't read or write to a file
        DATA_FORMAT_ERROR, // The data in a file is not in the right format
        TRANSACTION_FAILED // Something went wrong during a transaction
    }

    // This tells us what kind of error happened
    public ErrorCode errorCode;

    // These are different ways we can create an error message
    // This one just takes a message
    public InventoryException(String message) {
        super(message);
    }

    // This one takes a message and tells us what kind of error it is
    public InventoryException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    // This one takes a message and another error that caused this one
    public InventoryException(String message, Throwable cause) {
        super(message, cause);
    }

    // This one takes a message, another error, and tells us what kind of error it
    // is
    public InventoryException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    // This lets us get the error code
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}