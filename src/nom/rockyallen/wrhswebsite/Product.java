package nom.rockyallen.wrhswebsite;

import java.io.File;

/**
 * Helper to hold data for a product.
 * Alphabetically ordered by name.
 */
class Product implements Comparable{

    public String category = "";
    public String name = "";
    public String description = "";
    public double price = -999.0;
    public int stock = -999;
    public File image = null;
    public String message = "";
    public int onOrder;

    @Override
    public String toString() {
        return "{cat=" + category + ",name=" + name + ",price=" + price + ",stock=" + stock + "," + image + "]";
    }

    @Override
    public int compareTo(Object t) {
        return name.compareToIgnoreCase(((Product)t).name);
    }    
}
