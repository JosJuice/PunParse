package se.zeldaforumet.josjuice.punparse;

/**
 * @author JosJuice
 */
public final class Topic {
    
    private final boolean isMoved;
    
    // All of the following variables correspond to database columns.
    
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
        
    }
    
    /**
     * @return true if the topic has been moved and is located in another forum
     */
    public boolean getIsMoved() {
        return isMoved;
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
