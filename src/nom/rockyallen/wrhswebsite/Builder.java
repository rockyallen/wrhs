package nom.rockyallen.wrhswebsite;

import com.csvreader.CsvReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Ugly class to convert CSV files to web site.
 *
 * @author rocky
 */
public class Builder {

    private static final String TRADINGPOST_IMAGES = "assets/images/";
    private static final String STOCK_FILE = "tradingpost/WRHSTradingPost_Self_StockImportTemplate.csv";
    private static final String PRODUCT_FILE = "tradingpost/WRHSTradingPost_Self_ProductUpdateTemplate.csv";
    private static final String OUTPUT_FOLDER = "content/tradingpost/";
    private static final String CATEGORY_MAPPING = "tradingpost/categorymapping.csv";

    public static void main(String[] args) throws Exception {

        try {
            new Builder().build();
        } catch (Exception ex) {
            Logger.getLogger(Builder.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex);
            System.exit(1);
        }
        System.exit(0);
    }

    public void build() throws Exception {

        Map<String, Product> products = new TreeMap<String, Product>();

        readProducts(products);

        updateStockLevels(products);

        addImages(products);

        mergeAndRenameCategories(products);

        synthesiseMessages(products);

        makeWeb(products);
    }

    /**
     * Load all products from the product file.
     *
     * ProductID,Product Name,Product Description,Cost Price,Selling Price,Eat
     * Out Price,Popup Notes,Tax Percentage,Eat Out Tax Percentage,Category
     * Name,Brand Name,Measurement Scheme,Unit Of Sale,Volume Of Sale,Unit Of
     * Purchase, Volume Of Purchase,Barcode,Supplier,OrderCode,RRP,Product
     * Type,Article Code,Tare Weight,Button Colour,Multiple Choice Note
     * Name,Till Order,SKU,Sell On Till,Sell On Web, Is Variable Price, Is Tax
     * Exempt Eligible, Is Excluded From Loyalty Points Gain, Additional
     * Suppliers,Delete Product
     *
     * @param items
     * @throws IOException
     * @throws NumberFormatException
     */
    private void readProducts(Map<String, Product> items) throws IOException, NumberFormatException {
        System.out.println("Reading products...");
        CsvReader r1 = open(PRODUCT_FILE);
        int r1_idColumn = r1.getIndex("ProductID");
        int r1_nameColumn = r1.getIndex("Product Name");
        int r1_categoryColumn = r1.getIndex("Category Name");
        int r1_descriptionColumn = r1.getIndex("Description");
        int r1_priceColumn = r1.getIndex("Selling Price");

        while (r1.readRecord()) {
            Product r = new Product();
            String id = r1.get(r1_idColumn);
            r.category = r1.get(r1_categoryColumn);
            r.name = r1.get(r1_nameColumn);
            r.description = r1.get(r1_descriptionColumn);
            r.price = Double.parseDouble(r1.get(r1_priceColumn));

            items.put(id, r);
        }
    }

    /**
     * Prepare fname for reading.
     *
     * After this, the first call to readRecord will return the first data row.
     *
     * @param fname
     * @return
     */
    private CsvReader open(String fname) throws FileNotFoundException, IOException {
        File f = new File(fname);
        if (!f.exists()) {
            throw new FileNotFoundException(f.getAbsolutePath());
        }
        CsvReader r = new CsvReader(new FileReader(f));
        r.readHeaders();
        return r;
    }

    /**
     * Update stock level from the stock level file
     *
     * // ProductID,LocationID,Location Name,Product Name,Product
     * Barcode,Product Description,Product Order Code,Sale Price,Category
     * Name,Current Stock,Min Stock,Max Stock,Current Volume,On
     * Order,Alerts,Cost Price, Supplier Name,Unit of Sale,Volume of Sale,Unit
     * of Purchase,Volume of Purchase
     *
     * @param items
     * @throws IOException
     * @throws NumberFormatException
     * @throws DataException if
     */
    private void updateStockLevels(Map<String, Product> items) throws IOException, NumberFormatException, Exception {
        System.out.println("Updating stock levels...");
        CsvReader reader = open(STOCK_FILE);
        int idColumn = reader.getIndex("ProductID");
        int stockColumn = reader.getIndex("Current Stock");

        if (idColumn < 0) {
            throw new DataException("Column ProductID not found in stock file");
        }
        if (stockColumn < 0) {
            throw new DataException("Column Current Stock not found in stock file");
        }
        while (reader.readRecord()) {
            String id = reader.get(idColumn);
            Product r = items.get(id);
            if (r != null) {
                String stockLevel = reader.get(stockColumn);
                //System.out.println("stocklevel=["+stockLevel+ "]");
                if (!"".equals(stockLevel)) {
                    r.stock = (int) (Double.parseDouble(stockLevel));
                }
            } else {
                throw new DataException("Product listed in stock level file but not product file " + r);
            }

            items.put(id, r);
        }
    }

    /**
     * Find a suitable image for each product. All images are in
     * tradingpost/images. Looks for these files and uses the first one it
     * finds:
     *
     * {product id}.png
     *
     * {product id}.jpg
     *
     * {product name}.png
     *
     * {product name}.jpg
     *
     * {category}.png
     *
     * {category}.jpg
     *
     * default (placeholder.png).
     *
     * @param items
     * @throws FileNotFoundException
     */
    private void addImages(Map<String, Product> items) throws FileNotFoundException {
        System.out.println("Adding images...");
        File root = new File(TRADINGPOST_IMAGES);
        Collection<String> notFound = new TreeSet<String>();

        for (Map.Entry<String, Product> e : items.entrySet()) {

            String cat = e.getValue().category;
            String name = sanitise(e.getValue().name);

            System.out.println(cat + ":" + name);

            String id = e.getKey();

            File categoryFolder = new File(root, cat);
            if (!categoryFolder.exists()) {
                notFound.add(cat);
            } else {
                File[] testFiles = new File[]{
                    new File(categoryFolder, name + ".png"),
                    new File(categoryFolder, name + ".jpg"),
                    new File(categoryFolder, id + ".png"),
                    new File(categoryFolder, id + ".jpg"),
                    new File(categoryFolder, "CATEGORY.png"),
                    new File(categoryFolder, "CATEGORY.jpg"),};

                for (File f : testFiles) {
                    if (f.exists()) {
                        {
                            e.getValue().image = f;
                            break;
                        }
                    }
                }
            }
            if (e.getValue().image == null) {
                e.getValue().image = new File(root, "placeholder.png");
            }
        }
        if (!notFound.isEmpty()) {
            for (String s : notFound) {
                System.out.println("WARNING: No image folder found for category " + s);
            }
        }
    }

    /**
     *
     * @param products
     * @throws FileNotFoundException
     * @throws IOException
     * @throws DataException
     */
    private void mergeAndRenameCategories(Map<String, Product> products) throws FileNotFoundException, IOException, DataException {
        System.out.println("Mapping categories...");
        CsvReader reader = open(CATEGORY_MAPPING);
        Map<String, String> mapping = new HashMap<String, String>();

        int categorycol = reader.getIndex("category");
        int mappingcol = reader.getIndex("mapping");

        if (categorycol < 0) {
            throw new DataException("column category not found in mapping file");
        }
        if (mappingcol < 0) {
            throw new DataException("column mapping not found in mapping file");
        }

        while (reader.readRecord()) {
            mapping.put(reader.get(categorycol), reader.get(mappingcol));
        }
//        for (Map.Entry<String, String> e : mapping.entrySet()) {
//            System.out.println("Mapping " + e.getKey() + "=" + e.getValue());
//        }

        for (Product p : products.values()) {
            String to = mapping.get(p.category);
            if (to == null) {
                throw new DataException("Category not listed in " + CATEGORY_MAPPING + ": [" + p.category + "]");
            }
            p.category = to;
        }
    }

    /**
     *
     * @param products
     */
    private void makeWeb(Map<String, Product> products) throws IOException {
        System.out.println("Generating source files ...");

        // list the unique categories
        SortedSet<String> categories = new TreeSet<String>();
        for (Product p : products.values()) {
            if (!p.category.equals("")) {
                categories.add(p.category);
            }
        }
        //System.out.println("Visible categories: " + Arrays.toString(categories.toArray()));

        AsciidocBuilder sb = new AsciidocBuilder();
        sb.header("THE TRADING POST");
        sb.line("Open to Members only: Wednesday 2.00pm - 4.00pm Saturday 9.30am - 12.30pm\n");

        for (String category : categories) {
            //      if (new File(TRADINGPOST_IMAGES, category).exists()) {
            Set<Product> itemsInCategory = new TreeSet<Product>();
            for (Product p : products.values()) {
                if (p.category.equals(category)) {
                    itemsInCategory.add(p);
                }
            }

            String filename = sanitise(category);
            sb.line("* link:" + filename + ".html[" + category + "] (" + itemsInCategory.size() + " products)");

            makeCategoryPage(category, itemsInCategory, filename);
        }
        sb.line(
                "\nAll information on this website is offered in good faith but is used entirely at the user's own risk. ");

        sb.write(new File(OUTPUT_FOLDER, "categories.adoc"));
    }

    /**
     * Write a page for the category
     *
     * @param category Display name of the category
     * @param inCategory products in the category
     * @param filename sanitised filename for the page (no filetype)
     * @throws IOException
     */
    private void makeCategoryPage(String category, Collection<Product> inCategory, String filename) throws IOException {
        //System.out.println("Creating page for " + category);
        int cols = 6;
        AsciidocBuilder sb = new AsciidocBuilder();
        sb.header(category);
        sb.line("[header=false,cols=" + cols + ",grid=1,frame=1]");
        sb.line("|===");
        //sb.line("| |Name |Description |price |Stock (at {localdate}) |Notes");
        for (Product p : inCategory) {

            String image = null;
            if (p.image == null) {
                image = "/images/placeholder.png[]";
            } else {
                image = "/images/" + url(p.image.getParentFile().getName() + "/" + p.image.getName()) + "[]";
            }

            sb.line("a|image::" + image);
            sb.line("|" + p.name);
            sb.line("\n" + p.description);
            sb.line("\n&#163;" + p.price);
            sb.line("\nstock=" + p.stock);
            sb.line("\n" + p.message);
        }

        // table must have complete rows otherwise the last row is truncated
        int emptyCells = cols - (inCategory.size()) * 2 % cols;
        while (emptyCells-- > 0) {
            sb.line("|");
        }

        sb.line("|===");
        sb.write(new File(OUTPUT_FOLDER, filename + ".adoc"));
    }

    public static String sanitise(String s) {
        return s.replaceAll("\\s", "_").replaceAll("&", "_and_").replaceAll(":", "_").replaceAll("/", "_").replaceAll("__", "_");
    }

    /**
     * Sets p.message to:
     *
     * ON ORDER
     *
     * LAST FEW
     *
     * etc
     *
     * @param products
     */
    private void synthesiseMessages(Map<String, Product> products) {
        for (Product p : products.values()) {
            p.message = "TBD";
        }
    }

    public static String url(String s) {
        return s.replace(" ", "%20");
    }
}
