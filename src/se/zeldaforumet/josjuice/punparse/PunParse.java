package se.zeldaforumet.josjuice.punparse;

import se.zeldaforumet.josjuice.punparse.data.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
     * Parses every file in a directory, including files in subdirectories.
     * @param directory directory containing zero or more files to parse
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
            System.err.println("Could not read directory: " +
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
            Elements postElements = document.getElementsByClass("blockpost");
            for (Element postElement : postElements) {
                Post post = new Post(postElement, Post.UNKNOWN_TOPIC_ID);
                // TODO
            }
            // TODO
        } else if (document.getElementById("punprofile") != null) {
            // TODO
        } else if (document.getElementById("punviewforum") != null) {
            // TODO
        } else if (document.getElementById("punindex") != null) {
            // TODO
        }
    }

}
