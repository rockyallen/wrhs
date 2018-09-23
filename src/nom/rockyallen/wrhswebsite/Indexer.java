package nom.rockyallen.wrhswebsite;

import java.io.File;
import java.io.FileFilter;

/**
 * Write index.html in the given folder and thumbnail all the images in it
 * @author rocky
 */
public class Indexer {
    
    public static void main (String[] args)
    {
        new Indexer().build(args[0]);
    }

    private void build(String folderName) {
     File folder = new File(folderName);
     File[] files = folder.listFiles(new ImageTypeFilter());
     
//     StringBuilder sb = new StringBuilder();
//     Builder.header("Folder contents");
//     
//     for (File f: files)
//     {
//         sb.line(f.getAbsolutePath()+"\n\n");
//     }
    }
    
}
