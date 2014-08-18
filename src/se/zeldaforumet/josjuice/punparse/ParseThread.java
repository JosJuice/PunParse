package se.zeldaforumet.josjuice.punparse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Consumes {@link ParseTask}s and writes data from them to a {@link Database}.
 * @author JosJuice
 */
public class ParseThread extends Thread {
    
    private BlockingQueue<ParseTask> queue;
    private Database database;
    private UserInterface ui;
    
    /**
     * Creates a {@code ParseThread}.
     * @param queue A queue containing {@link ParseTask}s to parse. Other
     * threads are expected to add {@code ParseTask}s to it while this thread is
     * running, but it could also contain {@code ParseTask}s from the start.
     * @param database A database to place data into.
     * @param ui A {@link UserInterface} for progress display, or {@code null}.
     */
    public ParseThread(BlockingQueue<ParseTask> queue, Database database,
                       UserInterface ui) {
        this.queue = queue;
        this.database = database;
        this.ui = ui;
    }
    
    /**
     * Parses {@link ParseTask}s from the queue. Progress will be displayed to
     * the user using command-line output. Interrupting this thread while this
     * is running is used to signal that no more {@code ParseTask}s will be
     * added to the queue. After being interrupted, this will continue parsing
     * until the queue is empty, at which point the database will be closed.
     */
    @Override public void run() {
        boolean hasBeenInterrupted = false;
        while (!hasBeenInterrupted || !queue.isEmpty()) {
            try {
                ParseTask task = queue.take();
                try {
                    // Parse HTML bytes into a jsoup Document that we can use
                    InputStream is = new ByteArrayInputStream(task.getHtml());
                    Document doc = Jsoup.parse(is, null, "");

                    // Get the data we want from the Document
                    ArrayList<String> errors = parseDocument(doc, database);
                    if (ui != null) {
                        ui.addToProgress(task.getName(), errors);
                    }
                } catch (IOException e) {
                    if (ui != null) {
                        ui.addToProgress(task.getName(), "Couldn't get data.");
                    }
                }
            } catch (InterruptedException e) {
                hasBeenInterrupted = true;
            }
        }
        try {
            database.close();
        } catch (SQLException e) {
            if (ui != null) {
                ui.printError("SQL error: " + e.getLocalizedMessage());
            }
        }
    }
    
    /**
     * Parses a PunBB HTML document. The data will be placed in a database.
     * If parsing an individual piece of data (for instance, a post) fails,
     * it will be skipped and the returned error count will be increased by one.
     * @param document HTML document to parse
     * @param database database to place data into
     * @return errors encountered (empty if there were no errors)
     */
    private static ArrayList<String> parseDocument(Document document,
                                                   Database database) {
        Element punElement = document.getElementsByClass("pun").first();
        if (punElement != null) {
            switch (punElement.id()) {
                case "punviewpoll":
                case "punviewtopic":
                    return parseViewtopic(punElement, database);
                case "punprofile":
                    // TODO
                    break;
                case "punviewforum":
                    return parseViewforum(punElement, database);
                case "punindex":
                    return parseIndex(punElement, database);
            }
        }
        // If this is reached, there's nothing to parse, so there are no errors
        return new ArrayList<>();
    }
    
    /**
     * Parses a {@code #punviewtopic} element. The data will be placed in a
     * database. If parsing a piece of data (for instance, a post) fails, it
     * will be skipped and the returned error count will be increased by one.
     * @param element {@code #punviewtopic} element
     * @param database database to place data into
     * @return errors encountered (empty if there were no errors)
     */
    private static ArrayList<String> parseViewtopic(Element element,
                                                     Database database) {
        ArrayList<String> errors = new ArrayList<>();
        int topicId = findContainerId(element);
        
        // Add all posts to database
        Elements postElements = element.getElementsByClass("blockpost");
        for (Element postElement : postElements) {
            try {
                Post post = new Post(postElement, topicId);
                database.insert(post);
            } catch (IllegalArgumentException e) {
                errors.add("Error in input data: " + e.getLocalizedMessage());
            } catch (SQLException e) {
                errors.add("SQL error: " + e.getLocalizedMessage());
            }
        }
        
        return errors;
    }
    
    /**
     * Parses a {@code #punviewforum} element. The data will be placed in a
     * database. If parsing a piece of data (for instance, a topic) fails, it
     * will be skipped and the returned error count will be increased by one.
     * @param element {@code #punviewforum} element
     * @param database database to place data into
     * @return errors encountered (empty if there were no errors)
     */
    private static ArrayList<String> parseViewforum(Element element,
                                                    Database database) {
        ArrayList<String> errors = new ArrayList<>();
        int forumId = findContainerId(element);
        
        // Add all topics to database
        Elements topicElements = element.getElementsByTag("tr");
        for (Element topicElement : topicElements) {
            try {
                // Skip the top row, which only contains headings
                if (!topicElement.getElementsByClass("tclcon").isEmpty()) {
                    Topic topic = new Topic(topicElement, forumId);
                    if (!topic.isMoved()) { // Moved topics not supported yet
                        database.insert(topic);
                    }
                }
            } catch (IllegalArgumentException e) {
                errors.add("Error in input data: " + e.getLocalizedMessage());
            } catch (SQLException e) {
                errors.add("SQL error: " + e.getLocalizedMessage());
            }
        }
        
        return errors;
    }
    
    /**
     * Parses a {@code #punindex} element. The data will be placed in a
     * database. If parsing any part of a category fails, the entire category
     * will be skipped and the returned error count will be increased by one.
     * @param element {@code #punindex} element
     * @param database database to place data into
     * @return errors encountered (empty if there were no errors)
     */
    private static ArrayList<String> parseIndex(Element element,
                                                Database database) {
        ArrayList<String> errors = new ArrayList<>();
        
        // Add all categories, including their forums, to database
        Elements categoryElements = element.getElementsByClass("blocktable");
        int position = 0;
        for (Element categoryElement : categoryElements) {
            try {
                Category category = new Category(categoryElement, position);
                database.insert(category);
            } catch (IllegalArgumentException e) {
                errors.add("Error in input data: " + e.getLocalizedMessage());
            } catch (SQLException e) {
                errors.add("SQL error: " + e.getLocalizedMessage());
            } finally {
                position++;
            }
        }
        
        return errors;
    }
    
    /**
     * Attempts to find the ID of a topic or forum based on page links. If there
     * are no page links, this method will fail and return 0.
     * @param element An HTML element containing at least one {@code .pagelink}
     * element
     * @return the ID indicated in the page links, or 0 when failing
     */
    private static int findContainerId(Element element) {
        try {
            String topicUrl = element.getElementsByClass("pagelink").first().
                              getElementsByTag("a").first().attr("href");
            return Integer.parseInt(TextParser.getQueryValue(topicUrl, "id"));
        } catch (NumberFormatException | NullPointerException e) {
            return 0;
        }
    }

}
