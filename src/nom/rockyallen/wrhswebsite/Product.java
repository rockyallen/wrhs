package nom.rockyallen.wrhswebsite;

import java.io.File;

/**
 * Helper to hold data for a product.
 * Alphabetically ordered by name.
 */
class Product implements Comparable{

    String category = "";
    String name = "";
    String description = "";
    double price = -999.0;
    int stock = -999;
    File image = null;
    String message = "";

    @Override
    public String toString() {
        return "{cat=" + category + ",name=" + name + ",price=" + price + ",stock=" + stock + "," + image + "]";
    }

    @Override
    public int compareTo(Object t) {
        return name.compareToIgnoreCase(((Product)t).name);
    }    
}
