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
        // TODO: error handling
        parseFolder(args[0]);
    }
    
    /**
     * Parses every file in a folder.
     * @param pathname pathname of folder
     */
    public static void parseFolder(String pathname) {
        File folder = new File(pathname);
        File[] files = folder.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
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
        } else {
            System.out.println("No files were found.");
        }
    }
    
    /**
     * Parses an HTML document.
     * @param document HTML document to parse
     */
    public static void parseDocument(Document document) {
        if (!document.getElementsByClass("pun").isEmpty()) {
            if (document.getElementById("punindex") != null) {
                parseIndex(document);
            } else if (document.getElementById("punmisc") != null) {
                parseMisc(document);
            } else if (document.getElementById("punprofile") != null) {
                parseProfile(document);
            } else if (document.getElementById("punuserlist") != null) {
                parseUserList(document);
            } else if (document.getElementById("punviewforum") != null) {
                parseViewForum(document);
            } else if (document.getElementById("punviewpoll") != null || 
                       document.getElementById("punviewtopic") != null) {
                parseViewTopic(document);
            }
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
