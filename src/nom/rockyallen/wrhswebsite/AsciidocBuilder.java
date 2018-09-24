package nom.rockyallen.wrhswebsite;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for writing asciidoc files.
 * 
 * @author rocky
 */
public class AsciidocBuilder {

StringBuilder sb = null;

public AsciidocBuilder()
{
  sb = new   StringBuilder();  
}

   public void header(String title) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.now();
        sb.append("= ").append(title).append("\n")
                .append("Auto generated\n")
                .append(dtf.format(localDate) + "\n")
                .append(":jbake-type: page\n")
                .append(":jbake-status: published\n\n");
    }

    public void line(String s) {
        sb.append(s).append("\n");
    }

    public void text(String s) {
        sb.append(s);
    }

    public void write(File file) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(sb.toString());
        writer.close();
    }
}
