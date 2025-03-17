import java.io.Serializable;

// This class represents a product in our inventory system
// Serializable means we can save it to a file
public class Product implements Serializable {
    private static final long serialVersionUID = 1L; // Needed for saving to file

    // These are all the details we store about a product
    public String id; // Unique identifier for the product
    public String name; // Name of the product
    public String category; // What type of product it is
    public double price; // How much it costs
    public int quantity; // How many we have in stock
    public int minStockLevel; // When to warn that we're running low

    // This is how we create a new product
    public Product(String id, String name, String category, double price, int quantity, int minStockLevel) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
        this.minStockLevel = minStockLevel;
    }

    // These methods let us get and set the product's details
    // Getters - get the values
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getMinStockLevel() {
        return minStockLevel;
    }

    // Setters - change the values
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setMinStockLevel(int minStockLevel) {
        this.minStockLevel = minStockLevel;
    }

    // This checks if we're running low on stock
    public boolean isLowStock() {
        if (quantity <= minStockLevel) {
            return true;
        } else {
            return false;
        }
    }

    // This makes the product look nice when we print it
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