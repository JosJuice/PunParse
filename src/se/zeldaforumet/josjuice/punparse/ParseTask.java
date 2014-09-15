package se.zeldaforumet.josjuice.punparse;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * A task of parsing that needs to be carried out.
 * @author JosJuice
 */
public final class ParseTask implements Runnable {
    
    private final File file;
    private final Database database;
    private final UserInterface ui;
    
    /**
     * Creates a {@code ParseThread}.
     * @param file The {@code File} that is to be parsed.
     * @param database A {@link Database} to send data to.
     * @param ui A {@link UserInterface} for progress display, or {@code null}.
     */
    public ParseTask(File file, Database database, UserInterface ui) {
        this.file = file;
        this.database = database;
        this.ui = ui;
    }
    
    /**
     * Runs this task. Progress are displayed to the user using the
     * {@link UserInterface}, and results are sent to the {@link Database}.
     */
    @Override public void run() {
        try {
            ArrayList<String> errors = parseDocument(Jsoup.parse(file, null));
            if (ui != null) {
                ui.addToProgress(file.getName(), errors);
            }
        } catch (IOException e) {
            if (ui != null) {
                ui.addToProgress(file.getName(), "Couldn't read file.");
            }
        }
    }
    
    /**
     * Parses a PunBB HTML document. The data will be placed in a database.
     * If parsing an item (for instance, a post) fails, it will be skipped and a
     * string describing the error will be added to the return value.
     * @param document HTML document to parse
     * @param database database to place data into
     * @return errors encountered (empty if there were no errors)
     */
    private ArrayList<String> parseDocument(Document document) {
        Element punElement = document.getElementsByClass("pun").first();
        if (punElement != null) {
            switch (punElement.id()) {
                case "punviewpoll":
                case "punviewtopic":
                    return parseViewtopic(punElement);
                case "punviewforum":
                    return parseViewforum(punElement);
                case "punindex":
                    return parseIndex(punElement);
            }
        }
        // If this is reached, there's nothing to parse, so there are no errors
        return new ArrayList<>();
    }
    
    /**
     * Parses a {@code #punviewtopic} element. The data will be placed in a
     * database. If parsing a post or a user fails, it will be skipped and a
     * string describing the error will be added to the return value.
     * @param element {@code #punviewtopic} element
     * @param database database to place data into
     * @return errors encountered (empty if there were no errors)
     */
    private ArrayList<String> parseViewtopic(Element element) {
        ArrayList<String> errors = new ArrayList<>();
        int topicId = findContainerId(element);
        
        // Add all posts and their user information to database
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
            try {
                User user = new User(postElement.getElementsByClass("postleft").
                                     first());
                database.insert(user);
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
     * database. If parsing a topic fails, it will be skipped and a string
     * describing the error will be added to the return value.
     * @param element {@code #punviewforum} element
     * @param database database to place data into
     * @return errors encountered (empty if there were no errors)
     */
    private ArrayList<String> parseViewforum(Element element) {
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
     * database. If parsing a category or forum fails, it will be skipped and a
     * string describing the error will be added to the return value.
     * @param element {@code #punindex} element
     * @param database database to place data into
     * @return errors encountered (empty if there were no errors)
     */
    private ArrayList<String> parseIndex(Element element) {
        ArrayList<String> errors = new ArrayList<>();
        
        // Add all categories to database
        Elements categoryElements = element.getElementsByClass("blocktable");
        int categoryPosition = 0;
        for (Element categoryElement : categoryElements) {
            try {
                Category category = new Category(categoryElement,
                                                 categoryPosition);
                database.insert(category);
                
                // Add all forums to database
                Elements forumElements =
                        categoryElement.getElementsByTag("tr");
                int forumPosition = -1;
                for (Element forumElement : forumElements) {
                    // This if skips the first row, which only has headings
                    if (forumPosition >= 0) {
                        try {
                            Forum forum = new Forum(forumElement, forumPosition,
                                                    category.getId());
                            database.insert(forum);
                        } catch (IllegalArgumentException e) {
                            errors.add("Error in input data: " +
                                       e.getLocalizedMessage());
                        } catch (SQLException e) {
                            errors.add("SQL error: " + e.getLocalizedMessage());
                        }
                    }
                    forumPosition++;
                }
            } catch (IllegalArgumentException e) {
                errors.add("Error in input data: " + e.getLocalizedMessage());
            } catch (SQLException e) {
                errors.add("SQL error: " + e.getLocalizedMessage());
            }
            categoryPosition++;
        }
        return errors;
    }
    
    /**
     * Attempts to find the ID of a topic or forum based on page links. If there
     * are no page links, this method will fail and return 0.
     * @param element element containing at least one {@code .pagelink} element
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
