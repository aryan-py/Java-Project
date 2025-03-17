import java.io.Serializable;

/**
 * Represents a product in the inventory system
 */
public class Product implements Serializable {
    private static final long serialVersionUID = 1L;

    // Variables for product
    public String id;
    public String name;
    public String category;
    public double price;
    public int quantity;
    public int minStockLevel;

    // Constructor
    public Product(String id, String name, String category, double price, int quantity, int minStockLevel) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
        this.minStockLevel = minStockLevel;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getMinStockLevel() {
        return minStockLevel;
    }

    public void setMinStockLevel(int minStockLevel) {
        this.minStockLevel = minStockLevel;
    }

    // Check if stock is low
    public boolean isLowStock() {
        if (quantity <= minStockLevel) {
            return true;
        } else {
            return false;
        }
    }

    // Print product info
    @Override
    public String toString() {
        String result = "Product{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", minStockLevel=" + minStockLevel +
                '}';
        return result;
    }
}