package se.zeldaforumet.josjuice.punparse;

import java.io.File;

/**
 * @author Jos
 */
public class PunParse {
    
    public static void parseFolder(String pathname) {
        File folder = new File(pathname);
        File[] files = folder.listFiles();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO: error handling
        parseFolder(args[0]);
    }

}
