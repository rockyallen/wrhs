package nom.rockyallen.wrhswebsite;

/**
 * Helper to hold data for a product.
 * Comparable: Alphabetically ordered by name, ignoring case.
 */
class Product implements Comparable{

    boolean show = false;
    public String category = "";
    public String name = "";
    public String description = "";
    public double price = -999.0;
    public int stock = -999;
    public String image = null;
    public String message = "";
    public int onOrder = 0;
    public String info;

    @Override
    public String toString() {
        return "{cat=" + category + ",name=" + name + ",price=" + price + ",stock=" + stock + "," + image + "]";
    }

    @Override
    public int compareTo(Object t) {
        return name.compareToIgnoreCase(((Product)t).name);
    }    
}
