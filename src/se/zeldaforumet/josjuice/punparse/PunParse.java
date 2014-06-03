package se.zeldaforumet.josjuice.punparse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Parses data from PunBB HTML output to an SQL database.
 * @author JosJuice
 */
public class PunParse {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Find optional arguments
        boolean append = false;
        for (String arg : args) {
            if (arg.equals("--append")) {
                append = true;
            }
        }
        
        // Do the work
        System.out.println("Connecting to SQL database...");
        try (Database database = new Database(args[1], null)) {
            if (!append) {
                System.out.println("Creating tables and indexes...");
                database.createTablesAndIndexes();
            }
            System.out.println("Parsing files...");
            File directory = new File(args[0]);
            parseDirectory(directory, database);
        } catch (SQLException e) {
            System.err.println("SQL error: " + e.getLocalizedMessage());
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
                        int errors = parseDocument(document, database);
                        if (errors == 0) {
                            System.out.println("Processed file " + (i + 1) +
                                    "/" + files.length + ": " +
                                    files[i].getName());
                        } else {
                            System.err.println(errors + "error(s) found when " +
                                    "processing file " + (i + 1) + "/" +
                                    files.length + ": " + files[i].getName());
                        }
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
     * Parses a PunBB HTML document. The data will be placed in a database.
     * If parsing an individual piece of data (for instance, a post) fails,
     * it will be skipped and the returned error count will be increased by one.
     * @param document HTML document to parse
     * @param database database to place data into
     * @return number of errors encountered
     */
    public static int parseDocument(Document document, Database database) {
        int errors = 0;
        // There should only be one .pun element, but handling more doesn't hurt
        for (Element element : document.getElementsByClass("pun")) {
            switch (element.id()) {
                case "punviewpoll":
                case "punviewtopic":
                    errors += parseViewtopic(element, database);
                    break;
                case "punprofile":
                    // TODO
                    break;
                case "punviewforum":
                    // TODO
                    break;
                case "punindex":
                    // TODO
                    break;
            }
        }
        return errors;
    }
    
    /**
     * Parses a <code>#punviewtopic</code> element. The data will be placed in a
     * database. If parsing a piece of data (for instance, a post) fails, it
     * will be skipped and the returned error count will be increased by one.
     * @param element #punviewtopic element
     * @param database database to place data into
     * @return number of errors encountered
     */
    public static int parseViewtopic(Element element, Database database) {
        int errors = 0;
        int topicId = findContainerId(element);
        
        // Add all posts to database
        Elements postElements = element.getElementsByClass("blockpost");
        for (Element postElement : postElements) {
            try {
                Post post = new Post(postElement, topicId);
                database.insert(post);
            } catch (IllegalArgumentException e) {
                errors++;
                System.err.println("Error in input data: " +
                                   e.getLocalizedMessage());
            } catch (SQLException e) {
                System.err.println("SQL error: " + e.getLocalizedMessage());
            }
        }
        
        return errors;
    }
    
    /**
     * Attempts to find the ID of a topic or forum based on page links. If there
     * are no page links, this method will fail and return 0.
     * @param element An HTML element containing at least one
     * <code>.pagelink</code> element
     * @return the ID indicated in the page links, or 0 when failing
     */
    public static int findContainerId(Element element) {
        try {
            String topicUrl = element.getElementsByClass("pagelink").first().
                getElementsByAttribute("href").first().attributes().get("href");
            return Integer.parseInt(Parser.getUrlQueryValue(topicUrl, "id"));
        } catch (NumberFormatException | NullPointerException e) {
            return 0;
        }
    }

}
