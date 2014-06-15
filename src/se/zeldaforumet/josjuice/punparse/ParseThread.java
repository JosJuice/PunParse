package se.zeldaforumet.josjuice.punparse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
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
    
    /**
     * Creates a {@code ParseThread}.
     * @param queue A queue containing {@link ParseTask}s to parse. Other
     * threads are expected to add {@code ParseTask}s to it while this thread is
     * running, but it could also contain {@code ParseTask}s from the start.
     * @param database database to place data into
     */
    public ParseThread(BlockingQueue<ParseTask> queue, Database database) {
        this.queue = queue;
        this.database = database;
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
                // Parse the HTML bytes into a jsoup Document that we can use
                ParseTask task = queue.take();
                InputStream is = new ByteArrayInputStream(task.getHtml());
                Document document = Jsoup.parse(is, null, "");
                
                // Get the data we want from the Document
                int errors = parseDocument(document, database);
                if (errors == 0) {
                    System.out.println("Processed " + task);
                } else {
                    System.err.println(errors + " errors encountered " +
                                       "when parsing " + task);
                }
            } catch (InterruptedException e) {
                hasBeenInterrupted = true;
            } catch (IOException e) {
                System.out.println("Could");
            }
        }
        try {
            database.close();
        } catch (SQLException e) {
            System.err.println("SQL error: " + e.getLocalizedMessage());
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
                    errors += parseViewforum(element, database);
                    break;
                case "punindex":
                    errors += parseIndex(element, database);
                    break;
            }
        }
        return errors;
    }
    
    /**
     * Parses a {@code #punviewtopic} element. The data will be placed in a
     * database. If parsing a piece of data (for instance, a post) fails, it
     * will be skipped and the returned error count will be increased by one.
     * @param element {@code #punviewtopic} element
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
     * Parses a {@code #punviewforum} element. The data will be placed in a
     * database. If parsing a piece of data (for instance, a topic) fails, it
     * will be skipped and the returned error count will be increased by one.
     * @param element {@code #punviewforum} element
     * @param database database to place data into
     * @return number of errors encountered
     */
    public static int parseViewforum(Element element, Database database) {
        int errors = 0;
        int forumId = findContainerId(element);
        
        // Add all topics to database
        Elements topicElements = element.getElementsByTag("tr");
        for (Element topicElement : topicElements) {
            try {
                // Skip the top row, which only contains headings
                if (!topicElement.getElementsByClass("tclcon").isEmpty()) {
                    Topic topic = new Topic(topicElement, forumId);
                    if (!topic.isMoved()) {
                        database.insert(topic);
                    } else {
                        System.out.println("Skipped a moved topic");
                    }
                }
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
     * Parses a {@code #punindex} element. The data will be placed in a
     * database. If parsing any part of a category fails, the entire category
     * will be skipped and the returned error count will be increased by one.
     * @param element {@code #punindex} element
     * @param database database to place data into
     * @return number of errors encountered
     */
    public static int parseIndex(Element element, Database database) {
        int errors = 0;
        
        // Add all categories, including their forums, to database
        Elements categoryElements = element.getElementsByClass("blocktable");
        int position = 0;
        for (Element categoryElement : categoryElements) {
            try {
                Category category = new Category(categoryElement, position);
                database.insert(category);
            } catch (IllegalArgumentException e) {
                errors++;
                System.err.println("Error in input data: " +
                                   e.getLocalizedMessage());
            } catch (SQLException e) {
                System.err.println("SQL error: " + e.getLocalizedMessage());
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