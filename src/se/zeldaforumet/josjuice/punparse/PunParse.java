package se.zeldaforumet.josjuice.punparse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;

/**
 * @author JosJuice
 */
public class PunParse {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO: error handling
        parseFolder(args[0]);
    }
    
    public static void parseFolder(String pathname) {
        File folder = new File(pathname);
        File[] files = folder.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                Document document;
                try {
                    document = Jsoup.parse(files[i], null);
                    System.out.println(document);
                    System.out.println("Processed file " +
                                       (i + 1) + "/" + files.length + ": " +
                                       files[i].getName());
                } catch (IOException e) {
                    System.out.println("Could not read file " +
                                       (i + 1) + "/" + files.length + ": " +
                                       files[i].getName());
                }
            }
        } else {
            System.out.println("No files were found.");
        }
    }

}
