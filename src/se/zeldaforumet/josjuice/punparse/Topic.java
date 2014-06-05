package se.zeldaforumet.josjuice.punparse;

import org.jsoup.nodes.Element;

/**
 * @author JosJuice
 */
public final class Topic {
    
    private final boolean isMoved;
    
    // All of the following variables correspond to database columns.
    
    private final int id;
    private final String poster;
    private final String subject;
    private final long posted;
    private final long lastPosted;
    private final int lastPostId;
    private final String lastPoster;
    private final int numViews;
    private final int numReplies;
    private final boolean closed;
    private final boolean sticky;
    private final int movedTo;
    private final int forumId;
    
    /**
     * Constructs a <code>Topic</code>.
     * @param element An HTML <code>tr</code> element from viewtopic.php.
     * @param forumId ID of the forum containing this topic.
     * @throws IllegalArgumentException if required parts of HTML are missing
     */
    public Topic(Element element, int forumId) throws IllegalArgumentException {
        try {
            // Find the main text
            Element tclcon = element.getElementsByClass("tclcon").first();
            // Find the link to the topic
            Element topicLink = tclcon.getElementsByAttribute("href").first();
            // Find topic ID
            String topicUrl = topicLink.attr("href");
            id = Integer.parseInt(Parser.getQueryValue(topicUrl, "id"));
            // Find topic subject
            subject = topicLink.text();
            
            try {
                // Find poster username
                poster = getPoster(tclcon); 
            } catch (NullPointerException e) {
                throw new IllegalArgumentException("Couldn't get poster " +
                                                   "of topic " + this.id);
            }
            
            // TODO find out if closed or stickied
            closed = element.hasClass("iclosed");
            sticky = element.hasClass("isticky");
            
            // TODO find out if moved
            isMoved = false;
            movedTo = 0;
        } catch (NullPointerException | NumberFormatException e) {
            throw new IllegalArgumentException("Couldn't get topic ID.");
        }
        
        try {
            // Find number of replies (in .tc2)
            numReplies = Integer.parseInt(element.getElementsByClass("tc2").
                                          first().text());
        } catch (NullPointerException | NumberFormatException e) {
            throw new IllegalArgumentException("Couldn't get number of " +
                                               "replies of topic " + this.id);
        }
        
        try {
            // Find number of replies (in .tc3)
            numViews = Integer.parseInt(element.getElementsByClass("tc3").
                                        first().text());
        } catch (NullPointerException | NumberFormatException e) {
            throw new IllegalArgumentException("Couldn't get number of " +
                                               "views of topic " + this.id);
        }
        
        try {
            // Find information about recent post
            Element tcr = element.getElementsByClass("tcr").first();
            // Find the link to the post
            Element postLink = tcr.getElementsByAttribute("href").first();
            // Find post ID
            String postUrl = postLink.attr("href");
            lastPostId = Integer.parseInt(Parser.getQueryValue(postUrl, "pid"));
            try {
                // Find poster username
                lastPoster = getPoster(tcr);
            } catch (NullPointerException e) {
                throw new IllegalArgumentException("Couldn't get last poster " +
                                                   "in topic " + this.id);
            }
        } catch (NullPointerException | NumberFormatException e) {
            throw new IllegalArgumentException("Couldn't get ID of last " +
                                               "post in topic " + this.id);
        }
        
        // TODO find the actual times
        posted = id;
        lastPosted = lastPostId;
        
        // Set forum ID
        this.forumId = forumId;
    }
    
    /**
     * Gets the username of the poster of a topic or the last poster in a topic.
     * @param element An element from <code>viewtopic.php</code> containing a
     * link to the topic/post and the username. This is typically a
     * <code>td</code> element, but <code>.intd</code> and <code>.tclcon</code>
     * can also be used.
     * @return username of poster
     * @throws NullPointerException if the poster cannot be found
     */
    private String getPoster(Element element) throws NullPointerException {
        // TODO remove the "by " at the beginning
        Element byuser = element.getElementsByClass("byuser").first();
        if (byuser != null) {
            return byuser.text();
        } else {
            return element.getElementsByAttribute("href").first().
                    nextSibling().toString();
        }
    }
    
    /**
     * @return true if the topic has been moved and is located in another forum
     */
    public boolean isMoved() {
        return isMoved;
    }
    
    /**
     * @return Topic ID 
     */
    public int getId() {
        return id;
    }
    
    /**
     * @return Username of the user that created the topic
     */
    public String getPoster() {
        return poster;
    }
    
    /**
     * @return The subject of the topic
     */
    public String getSubject() {
        return subject;
    }
    
    /**
     * @return Time posted (Unix timestamp)
     */
    public long getPosted() {
        return posted;
    }
    
    /**
     * @return The time the last post was posted (Unix timestamp)
     */
    public long getLastPosted() {
        return lastPosted;
    }
    
    /**
     * @return The ID of the last post in the topic
     */
    public int getLastPostId() {
        return lastPostId;
    }
    
    /**
     * @return The username of the user that last posted in the topic
     */
    public String getLastPoster() {
        return lastPoster;
    }
    
    /**
     * @return The number of times the topic has been viewed
     */
    public int getNumViews() {
        return numViews;
    }
    
    /**
     * @return The number of posts in the topic, excluding the first post
     */
    public int getNumReplies() {
        return numReplies;
    }
    
    /**
     * @return true if the topic is closed
     */
    public boolean getClosed() {
        return closed;
    }
    
    /**
     * @return true if the topic is stickied
     */
    public boolean getSticky() {
        return sticky;
    }
    
    /**
     * @return The ID of the forum this topic has been moved to, or 0 if it has
     * not been moved
     */
    public int getMovedTo() {
        return movedTo;
    }
    
    /**
     * @return The ID of the forum containing this topic
     */
    public int getForumId() {
        return forumId;
    }
    
}
