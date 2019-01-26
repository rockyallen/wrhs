package nom.rockyallen.wrhswebsite;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for writing asciidoc files with the right attributes to be recognized by JBake
 *
 * @author rocky
 */
public class AsciidocBuilder {

    StringBuilder sb = null;

    public AsciidocBuilder() {
        sb = new StringBuilder();
    }

    public void header(String title) {
        header(title, new HashMap<String, String>());
    }

    public void header(String title, Map<String, String> attributes) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.now();
        sb.append(":jbake-type: page\n");
        sb.append(":jbake-status: published\n");
        for (Map.Entry<String, String> e : attributes.entrySet()) {
            sb.append(":").append(e.getKey()).append(": ").append(e.getValue()).append("\n");
        }
        sb.append("= ").append(title).append("\n");
        sb.append("Auto generated\n");
        sb.append(dtf.format(localDate) + "\n\n");
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

    public static Map<String, String> commonAttributes() {
        Map<String, String> ret = new HashMap<String, String>();
        ret.put("stylesheet", "css/plain.css");
        return ret;
    }

}
