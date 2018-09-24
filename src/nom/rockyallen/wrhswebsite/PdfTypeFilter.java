package nom.rockyallen.wrhswebsite;

import java.io.File;
import java.io.FileFilter;

/**
 *
 * @author rocky
 */
public class ImageTypeFilter implements FileFilter{

    public ImageTypeFilter() {
    }

    @Override
    public boolean accept(File file) {
        String fname = file.getName().toLowerCase();
        return fname.endsWith(".png") || fname.endsWith(".jpg") || fname.endsWith(".gif");
    }
    
}
