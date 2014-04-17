package se.zeldaforumet.josjuice.punparse;

import se.zeldaforumet.josjuice.punparse.data.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * @author JosJuice
 */
public class PunParse {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Connecting to SQL database...");
        try (Database database = new Database(args[1], args[2])) {
            File directory = new File(args[0]);
            parseDirectory(directory, database);
        } catch (ClassNotFoundException e) {
            System.err.println("The SQL driver could not be loaded.");
        } catch (SQLException e) {
            System.err.println(e);
        }
    }
    
    /**
     * Parses every file in a directory, including files in subdirectories.
     * The data from the files will be placed in a database.
     * @param directory directory containing zero or more files to parse
     * @param database database to place data into
     */
    public static void parseDirectory(File directory, Database database) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    // Parse subdirectories recursively
                    System.out.println("Entering subdirectory...");
                    parseDirectory(files[i], database);
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
