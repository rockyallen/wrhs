package nom.rockyallen.wrhswebsite;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Ugly class to convert CSV files to web site via Asciidoc files.
 *
 * @author rocky
 */
public class TradingPostPageBuilder extends Task {

    private String STOCK_FILE = null;
    private String PRODUCT_FILE = null;
    private String OUTPUT_FOLDER = null;
    private String CATEGORY_MAPPING = null;

    private String TITLE = null;
    private String SUBTITLE = null;
    private String FOOTER = null;
    private String PLACEHOLDER = null;
    private String INFO_EXPORT = null;

    //private File root = null;
    private int items_page_cols = 8;
    private String IMAGESDIR = "";
    Map<String, Product> products = new TreeMap<String, Product>();

    @Override
    public void execute() throws BuildException {

        try {
            readProducts(products, new File(PRODUCT_FILE));

            updateStockLevels(products, new File(STOCK_FILE));

            readCategoriesAndImages(products, new File(CATEGORY_MAPPING));

            synthesiseMessages(products);

            makeWeb(products);

            if (INFO_EXPORT != null) {
                exportInfoFile(INFO_EXPORT);
            }

        } catch (Exception ex) {
            throw new BuildException(ex);
        }
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
     * @param f a product file in CSV format
     * @throws IOException
     * @throws NumberFormatException
     */
    private void readProducts(Map<String, Product> items, File f) throws IOException, NumberFormatException {
        //System.out.println("Reading products...");
        CsvReader r1 = openCsv(f);
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
     * Convenience method to prepare fname for reading.
     *
     * After this, the first call to readRecord will return the first data row.
     *
     * @param f File to open
     * @return The column headings
     */
    private CsvReader openCsv(File f) throws FileNotFoundException, IOException {
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
     * @param products
     * @param f a stock file in CSV format
     *
     * @throws IOException
     * @throws NumberFormatException
     * @throws DataException if
     */
    private void updateStockLevels(Map<String, Product> products, File f) throws IOException, NumberFormatException, Exception {
        CsvReader reader = openCsv(f);
        int idColumn = reader.getIndex("ProductID");
        int stockColumn = reader.getIndex("Current Stock");
        int orderColumn = reader.getIndex("On Order");

        if (idColumn < 0) {
            throw new DataException("Column 'ProductID' not found in stock file");
        }
        if (stockColumn < 0) {
            throw new DataException("Column 'Current Stock' not found in stock file");
        }
        if (orderColumn < 0) {
            throw new DataException("Column 'On Order' not found in stock file");
        }
        while (reader.readRecord()) {
            String id = reader.get(idColumn);
            Product r = products.get(id);
            if (r != null) {
                String order = reader.get(orderColumn).trim();
                if ("".equals(order)) {
                    r.onOrder = 0;
                } else {
                    r.onOrder = Integer.parseInt(order);
                }
                String stockLevel = reader.get(stockColumn);
                //System.out.println("stocklevel=["+stockLevel+ "]");
                if (!"".equals(stockLevel)) {
                    r.stock = (int) (Double.parseDouble(stockLevel));
                }
            } else {
                throw new DataException("Product listed in stock level file but not product file " + r);
            }

            products.put(id, r);
        }
    }

    /**
     * Post conditions: (1) product.category is set to the page on which it
     * appears. (2) product.image is set.
     *
     * @param products
     * @param f an image mapping file in CSV format
     *
     * @throws FileNotFoundException
     * @throws IOException
     * @throws DataException
     */
    private void readCategoriesAndImages(Map<String, Product> products, File f) throws FileNotFoundException, IOException, DataException {
        CsvReader reader = openCsv(f);
        int productidcol = reader.getIndex("productid");
        int mappingcol = reader.getIndex("page");
        int imagecol = reader.getIndex("image");
        int showcol = reader.getIndex("show");
        int infocol = reader.getIndex("info");

        if (productidcol < 0) {
            throw new DataException("column productid not found in mapping file");
        }
        if (mappingcol < 0) {
            throw new DataException("column page not found in mapping file");
        }
        if (imagecol < 0) {
            throw new DataException("column image not found in mapping file");
        }
        if (showcol < 0) {
            throw new DataException("column show not found in mapping file");
        }
        if (infocol < 0) {
            throw new DataException("column info not found in mapping file");
        }

        while (reader.readRecord()) {
            String[] values = reader.getValues();
            String id = values[productidcol];
            if (!id.trim().isEmpty()) {
                Product p = products.get(id);
                if (p == null) {
                    String err = "Warning: Product listed in mapping file, but not found in product file: [" + id + "]";
                    System.out.println(err);
                    //throw new DataException(err);
                } else {
                    p.category = values[mappingcol];
                    p.image = values[imagecol];
                    p.show = Integer.parseInt(values[showcol]) > 0;
                    p.info = values[infocol];
                }
            }
        }
    }

    /**
     * Generating source files for product web pages.
     *
     * @param products
     */
    private void makeWeb(Map<String, Product> products) throws IOException {

        // list the unique categories in alphabetic order
        SortedSet<String> categories = new TreeSet<String>();
        for (Product p : products.values()) {
            if (!p.category.isEmpty() && p.show) {
                categories.add(p.category);
            }
        }

        AsciidocBuilder sb = new AsciidocBuilder();
        sb.header(TITLE, AsciidocBuilder.commonAttributes());
        sb.text(SUBTITLE+"\n");
        sb.text("\n");
        for (String category : categories) {
            Set<Product> itemsInCategory = new TreeSet<Product>();
            for (Product p : products.values()) {
                if (p.category.equals(category)) {
                    itemsInCategory.add(p);
                }
            }
            // Mangle s into a string suitable for a Windows or Unix filename.
            String filename = category.replaceAll("[^a-zA-Z0-9&]+", "");
            sb.text("* link:" + filename + ".html[" + category + "] (" + itemsInCategory.size() + " products)\n");

            makeCategoryPage(category, itemsInCategory, filename);
        }
        sb.text(FOOTER+"\n");

        sb.write(new File(new File(OUTPUT_FOLDER), "categories.adoc"));
    }

    /**
     * Write a page for the category. Currently outputs a table with
     *
     * @param category Display name of the category
     * @param inCategory products in the category
     * @param filename sanitised filename for the page (no filetype)
     * @throws IOException
     */
    private void makeCategoryPage(String category, Collection<Product> inCategory, String filename) throws IOException {
        //System.out.println("Creating page for " + category);
        AsciidocBuilder sb = new AsciidocBuilder();
        sb.header(category);
        sb.text("[options=noheader,cols=" + items_page_cols + ",grid=1,frame=1]\n");
        sb.text("|===\n");
        //sb.line("| |Name |Description |price |Stock (at {localdate}) |Notes");
        for (Product p : inCategory) {

            String image = null;
            if (p.image == null || p.image.isEmpty()) {
                image = PLACEHOLDER;
            } else {
                image = IMAGESDIR + p.image;
            }

            /*
            
             +<div class="tooltip">+Hover over me<span class="tooltiptext">Tooltip text</span></div> 
             */
            if (p.info == null | p.info.isEmpty()) {
                sb.text("| **" + p.name + "**\n");
            } else {
                sb.text("| **pass:[<abbr title=\"" + p.info + "\">" + p.name + "</abbr>]**\n");
            }
            sb.text("\n" + p.description+ "\n");
            sb.text("\n" + String.format("&#163;%4.2f", p.price)+"\n");
            sb.text("\n" + p.message+"\n");
            sb.text("a|image::" + image + "[height=40]\n");
        }

        // table must have complete rows otherwise the last row is truncated
        int emptyCells = items_page_cols - (inCategory.size()) * 2 % items_page_cols;
        while (emptyCells-- > 0) {
            sb.text("|\n");
        }

        sb.text("|===\n");
        sb.write(new File(OUTPUT_FOLDER, filename + ".adoc"));
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
     * The algorithm probably needs twiddling.
     *
     * @param products
     */
    private void synthesiseMessages(Map<String, Product> products) {
        for (Product p : products.values()) {
            p.message = "Stock: " + Math.max(0, p.stock) + (p.onOrder > 0 ? " ON ORDER" : "");
        }
    }

    /**
     * Sets the base folder for all files. Usually set to ${basedir}.
     *
     * @param root the root to set
     */
//    public void setRoot(File root) {
//        this.root = root;
//    }
    /**
     * Resize output table
     *
     * @param i Number of columns - must be even. Default is 8.
     */
    public void setCols(int i) {
        this.items_page_cols = i;
    }

    /**
     * @param STOCK_FILE the STOCK_FILE to set
     */
    public void setSTOCK_FILE(String STOCK_FILE) {
        this.STOCK_FILE = STOCK_FILE;
    }

    /**
     * @param PRODUCT_FILE the PRODUCT_FILE to set
     */
    public void setPRODUCT_FILE(String PRODUCT_FILE) {
        this.PRODUCT_FILE = PRODUCT_FILE;
    }

    /**
     * @param OUTPUT_FOLDER the OUTPUT_FOLDER to set
     */
    public void setOUTPUT_FOLDER(String OUTPUT_FOLDER) {
        this.OUTPUT_FOLDER = OUTPUT_FOLDER;
    }

    /**
     * @param CATEGORY_MAPPING the CATEGORY_MAPPING to set
     */
    public void setCATEGORY_MAPPING(String CATEGORY_MAPPING) {
        this.CATEGORY_MAPPING = CATEGORY_MAPPING;
    }

    /**
     * @param TITLE the TITLE to set
     */
    public void setTITLE(String TITLE) {
        this.TITLE = TITLE;
    }

    /**
     * @param SUBTITLE the SUBTITLE to set
     */
    public void setSUBTITLE(String SUBTITLE) {
        this.SUBTITLE = SUBTITLE;
    }

    /**
     * @param FOOTER the FOOTER to set
     */
    public void setFOOTER(String FOOTER) {
        this.FOOTER = FOOTER;
    }

    /**
     * @param PLACEHOLDER the PLACEHOLDER to set
     */
    public void setPLACEHOLDER(String PLACEHOLDER) {
        this.PLACEHOLDER = PLACEHOLDER;
    }

    /**
     * @param IMAGESDIR the IMAGESDIR to set
     */
    public void setIMAGESDIR(String IMAGESDIR) {
        this.IMAGESDIR = IMAGESDIR;
    }

    private void exportInfoFile(String INFO_EXPORT) throws IOException {
        CsvWriter w = new CsvWriter(INFO_EXPORT);
        w.writeRecord(new String[]{"productid", "description", "page", "image", "info","show"});
        for (Map.Entry<String, Product> e : products.entrySet()) {
            Product p = e.getValue();
            w.writeRecord(new String[]{e.getKey(), p.name, p.category, p.image, p.info, (p.show  ? "1" : "0")});
        }
        w.close();
    }

    /**
     * @param INFO_EXPORT the INFO_EXPORT to set
     */
    public void setINFO_EXPORT(String INFO_EXPORT) {
        this.INFO_EXPORT = INFO_EXPORT;
    }
}
