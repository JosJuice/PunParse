package se.zeldaforumet.josjuice.punparse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;

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
        File directory = new File(args[0]);
        parseDirectory(directory);
    }
    
    /**
     * Parses every file in a directory.
     * @param directory directory containing files to parse
     */
    public static void parseDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    // Parse subdirectories recursively
                    System.out.println("Entering subdirectory...");
                    parseDirectory(files[i]);
                    System.out.println("Exiting subdirectory...");
                } else {
                    try {
                        Document document = Jsoup.parse(files[i], null);
                        parseDocument(document);
                        System.out.println("Processed file " +
                                           (i + 1) + "/" + files.length + ": " +
                                           files[i].getName());
                    } catch (IOException e) {
                        System.err.println("Could not read file " +
                                           (i + 1) + "/" + files.length + ": " +
                                           files[i].getName());
                    }
                }
            }
        } else {
            System.err.println("The directory could not be read: " +
                               directory.getPath());
        }
    }
    
    /**
     * Parses an HTML document.
     * @param document HTML document to parse
     */
    public static void parseDocument(Document document) {
        if (document.getElementById("punviewtopic") != null ||
                document.getElementById("punviewpoll") != null) {
            parseViewTopic(document);
        } else if (document.getElementById("punprofile") != null) {
            parseProfile(document);
        } else if (document.getElementById("punviewforum") != null) {
            parseViewForum(document);
        } else if (document.getElementById("punuserlist") != null) {
            parseUserList(document);
        } else if (document.getElementById("punmisc") != null) {
            parseMisc(document);
        } else if (document.getElementById("punindex") != null) {
            parseIndex(document);
        }
    }
    
    public static void parseIndex(Document document) {
        System.out.println("Found index");
    }
    
    public static void parseMisc(Document document) {
        System.out.println("Found misc");
    }
    
    public static void parseProfile(Document document) {
        System.out.println("Found profile");
    }
    
    public static void parseUserList(Document document) {
        System.out.println("Found userlist");
    }
    
    public static void parseViewForum(Document document) {
        System.out.println("Found viewforum");
    }
    
    public static void parseViewTopic(Document document) {
        System.out.println("Found viewtopic");
    }

}
