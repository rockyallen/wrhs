package nom.rockyallen.wrhswebsite;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Cheap method to produce a thumbnail page for a folder of pdf files.
 *
 * Not very clever:
 *
 * Each pdf must have a matching .png thumbnail.
 *
 * Files are presented by name in alphabetical order.
 *
 * Writes .adoc
 *
 * @author rocky
 */
public class CatalogueFolder extends Task {

    private File folderToSearch = null;
    private File asciidocFileToWrite = null;
    private String title = null;
    private String before = "";
    private String between = " ";
    private String after = "";

    @Override
    public void execute() throws BuildException {
        try {
            if (folderToSearch == null) {
                throw new ParamaterNotSetException("folderToSearch");
            }
            if (asciidocFileToWrite == null) {
                throw new ParamaterNotSetException("asciidocFileToWrite");
            }
            if (title == null) {
                throw new ParamaterNotSetException("title");
            }
            File[] files = folderToSearch.listFiles(new PdfTypeFilter());

            // Order files alphabetically by name
            Set<String> names = new TreeSet<String>();
            for (File f : files) {
                names.add(f.getName());
            }
            AsciidocBuilder sb = new AsciidocBuilder();

            sb.header(title, AsciidocBuilder.commonAttributes());

             sb.text("\n");
             sb.text("\n");

             sb.text(before);
            
            for (String s : names) {
                sb.text("image:" + s.substring(0, s.length() - 4) + ".png[" + s + ",link=" + s + "]");
                sb.text(between);
            }

             sb.text("\n");
             sb.text("\n");

             sb.text(after);

            sb.write(asciidocFileToWrite);
        } catch (Exception ex) {
            throw new BuildException(ex);
        }
    }

    /**
     *
     * @throws ParamaterNotSetException
     */
    @Override
    public void init() throws BuildException {
    }

    /**
     * @param folderToSearch the folderToSearch to set
     */
    public void setFolder(File folderToSearch) {
        this.folderToSearch = folderToSearch;
    }

    /**
     * @param asciidocFileToWrite the asciidocFileToWrite to set
     */
    public void setOutfile(File asciidocFileToWrite) {
        this.asciidocFileToWrite = asciidocFileToWrite;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @param header the header to set
     */
    public void setBefore(String header) {
        this.before = header;
    }

    /**
     * @param footer the footer to set
     */
    public void setAfter(String footer) {
        this.after = footer;
    }

    /**
     * @param between the between to set
     */
    public void setBetween(String between) {
        this.between = between;
    }
}
