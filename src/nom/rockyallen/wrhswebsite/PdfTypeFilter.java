package nom.rockyallen.wrhswebsite;

import java.io.File;
import java.io.FileFilter;

/**
 *
 * @author rocky
 */
public class PdfTypeFilter implements FileFilter{

    public PdfTypeFilter() {
    }

    @Override
    public boolean accept(File file) {
        String fname = file.getName().toLowerCase();
        return fname.endsWith(".pdf");
    }
    
}
