package se.zeldaforumet.josjuice.punparse;

import org.jsoup.nodes.Element;

/**
 * @author JosJuice
 */
public final class Forum {
    
    private final boolean isRedirect;
    
    // All of the following variables correspond to database columns.
    private final int id;
    private final String name;
    private final String description;
    private final String redirectUrl;
    // TODO moderators
    private final int numTopics;
    private final int numPosts;
    private final int lastPosted;
    private final int lastPostId;
    private final String lastPoster;
    private final boolean sortByTopicStart;
    private final int displayPosition;
    private final int categoryId;
    
    /**
     * Constructs a {@code Forum}.
     * @param element An HTML {@code tr} element from {@code index.php}.
     * @throws IllegalArgumentException if required parts of HTML are missing
     */
    public Forum(Element element, int displayPosition, int categoryId)
            throws IllegalArgumentException {
        // Reject elements that would cause problems later
        if (element == null || !element.tagName().equals("tr")) {
            throw new IllegalArgumentException("Invalid forum element.");
        }
        
        // Check if this is a redirect. We don't need to wrap this in a try
        // statement, becuase we already know that element is not null
        isRedirect = element.hasClass("iredirect");
        
        try {
            // Get the link to the forum
            String url = element.getElementsByTag("a").first().attr("href");
            if (isRedirect) {
                // Store the destination of the link as a redirect URL
                id = 0;
                redirectUrl = url;
            } else {
                try {
                    // Find the forum ID
                    id = Integer.parseInt(Parser.getQueryValue(url, "id"));
                } catch (NullPointerException | NumberFormatException e) {
                    throw new IllegalArgumentException("Couldn't get forum ID.",
                                                       e);
                }
                redirectUrl = null;
            }
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Couldn't get forum URL.", e);
        }
        
        try {
            // Find the name
            name = element.getElementsByTag("h3").first().text();
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Couldn't get name of forum " +
                                               id, e);
        }
        
        // Find the description. It may be null
        Element tclcon = element.getElementsByClass("tclcon").first();
        if (tclcon != null) {
            description = tclcon.ownText();
        } else {
            description = null;
        }
        
        if (isRedirect) {
            numTopics = 0;
            numPosts = 0;
            lastPosted = 0;
            lastPostId = 0;
            lastPoster = null;
        } else {
            try {
                // Find number of topics (in .tc2)
                numTopics = Integer.parseInt(element.getElementsByClass("tc2").
                                             first().text());
            } catch (NullPointerException | NumberFormatException e) {
                throw new IllegalArgumentException("Couldn't get number of " +
                                                   "topics in forum " + id, e);
            }
            
            try {
                // Find number of posts (in .tc3)
                numPosts = Integer.parseInt(element.getElementsByClass("tc3").
                                            first().text());
            } catch (NullPointerException | NumberFormatException e) {
                throw new IllegalArgumentException("Couldn't get number of " +
                                                   "posts in forum " + id, e);
            }
            
            try {
                // Find information about recent post
                Element tcr = element.getElementsByClass("tcr").first();
                // Find the link to the post
                Element postLink = tcr.getElementsByTag("a").first();
                // Find post ID
                String postUrl = postLink.attr("href");
                lastPostId = Integer.parseInt(
                        Parser.getQueryValue(postUrl, "pid"));
                
                try {
                    // Find poster username
                    // TODO remove the "by " at the beginning
                    // TODO make this line 80 characters long LIKE IT SHOULD BE
                    lastPoster = tcr.getElementsByClass("byuser").first().text();
                } catch (NullPointerException | IndexOutOfBoundsException e) {
                    throw new IllegalArgumentException("Couldn't get last " +
                                                       "poster in forum " + id,
                                                       e);
                }
            } catch (NullPointerException | NumberFormatException e) {
                throw new IllegalArgumentException("Couldn't get ID of last " +
                                                   "post in forum " + id, e);
            }
            
            // TODO find the actual time
            lastPosted = lastPostId;
        }
        
        // TODO get the actual value of this somehow
        sortByTopicStart = false;
        
        // Set display position
        this.displayPosition = displayPosition;
        
        // Set category ID
        this.categoryId = categoryId;
    }
    
    /**
     * @return {@code true} if this forum only is a link, not a proper forum
     */
    public boolean isRedirect() {
        return isRedirect;
    }
    
    /**
     * @return Forum ID, or 0 if this is a redirect forum
     */
    public int getId() {
        return id;
    }
    
    /**
     * @return The name of this forum 
     */
    public String getName() {
        return name;
    }
    
    /**
     * @return The description of this forum, or {@code null} if there is none
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * @return The URL this forum leads to, or null if it isn't a redirect forum
     */
    public String getRedirectUrl() {
        return redirectUrl;
    }
    
    /**
     * @return The number of topics, or 0 if this is a redirect forum
     */
    public int getNumTopics() {
        return numTopics;
    }
    
    /**
     * @return The number of posts, or 0 if this is a redirect forum
     */
    public int getNumPosts() {
        return numPosts;
    }
    
    /**
     * @return The time the last post was posted (Unix timestamp), or 0 if this
     * is a redirect forum
     */
    public int getLastPosted() {
        return lastPosted;
    }
    
    /**
     * @return The ID of the last post, or 0 if this is a redirect forum
     */
    public int getLastPostId() {
        return lastPostId;
    }
    
    /**
     * @return The username of the user that last posted in the forum, or
     * {@code null} if this is a redirect forum
     */
    public String getLastPoster() {
        return lastPoster;
    }
    
    /**
     * @return true if the forum is sorted by when topics were started, false if
     * the forum is sorted by when topics were last posted in
     */
    public boolean getSortByTopicStart() {
        return sortByTopicStart;
    }
    
    /**
     * @return The position this forum will be displayed at within its category
     */
    public int getDisplayPosition() {
        return displayPosition;
    }
    
    /**
     * @return The ID of the category containing this forum
     */
    public int getCategoryId() {
        return categoryId;
    }
    
}
