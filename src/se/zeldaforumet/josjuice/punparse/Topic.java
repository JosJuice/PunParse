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
     * Constructs a {@code Topic}.
     * @param element An HTML {@code tr} element from {@code viewforum.php}.
     * @param forumId ID of the forum containing this topic.
     * @throws IllegalArgumentException if required parts of HTML are missing
     */
    public Topic(Element element, int forumId) throws IllegalArgumentException {
        try {
            // Find the main text
            Element tclcon = element.getElementsByClass("tclcon").first();
            // Find the link to the topic
            Element topicLink = tclcon.getElementsByTag("a").first();
            // Find topic ID
            String topicUrl = topicLink.attr("href");
            id = Integer.parseInt(TextParser.getQueryValue(topicUrl, "id"));
            // Find topic subject
            subject = topicLink.text();
            
            try {
                // Find poster username
                poster = getPoster(tclcon); 
            } catch (NullPointerException | IndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Couldn't get poster " +
                                                   "of topic " + id, e);
            }
            
            closed = element.hasClass("iclosed");
            sticky = element.hasClass("isticky");
        } catch (NullPointerException | NumberFormatException e) {
            throw new IllegalArgumentException("Couldn't get topic ID.", e);
        }
        
        try {
            // Find number of replies (in .tc2)
            Element tc2 = element.getElementsByClass("tc2").first();
            if (tc2.hasText() && !tc2.text().equals("\u00A0")) {
                isMoved = false;
                movedTo = 0;
                numReplies = Integer.parseInt(tc2.text());
            } else {
                // If .tc2 is empty, this is a moved topic
                isMoved = true;
                movedTo = id;
                numReplies = 0;
            }
        } catch (NullPointerException | NumberFormatException e) {
            throw new IllegalArgumentException("Couldn't get number of " +
                                               "replies of topic " + id, e);
        }
        
        if (isMoved) {
            numViews = 0;
        } else {
            try {
                // Find number of replies (in .tc3)
                numViews = Integer.parseInt(element.getElementsByClass("tc3").
                                            first().text());
            } catch (NullPointerException | NumberFormatException e) {
                throw new IllegalArgumentException("Couldn't get number of " +
                                                   "views of topic " + id, e);
            }
        }
        
        if (isMoved) {
            lastPostId = 0;
            lastPoster = null;
        } else {
            try {
                // Find information about recent post
                Element tcr = element.getElementsByClass("tcr").first();
                // Find the link to the post
                Element postLink = tcr.getElementsByTag("a").first();
                // Find post ID
                String postUrl = postLink.attr("href");
                lastPostId = Integer.parseInt(
                        TextParser.getQueryValue(postUrl, "pid"));
                try {
                    // Find poster username
                    lastPoster = getPoster(tcr);
                } catch (NullPointerException | IndexOutOfBoundsException e) {
                    throw new IllegalArgumentException("Couldn't get last " +
                                                       "poster in topic " + id,
                                                       e);
                }
            } catch (NullPointerException | NumberFormatException e) {
                throw new IllegalArgumentException("Couldn't get ID of last " +
                                                   "post in topic " + id, e);
            }
        }
        
        // TODO find the actual times
        posted = id;
        lastPosted = lastPostId;
        
        // Set forum ID
        this.forumId = forumId;
    }
    
    /**
     * Gets the username of the poster of a topic or the last poster in a topic.
     * @param element An element from {@code viewtopic.php} containing a link
     * to the topic/post and the username. This is typically a {@code td}
     * element, but {@code .intd} and {@code .tclcon} can also be used. The
     * username of the poster must be preceded by a non-breaking space.
     * @return username of poster
     * @throws NullPointerException if the poster cannot be found
     * @throws IndexOutOfBoundsException if no non-breaking space was found
     */
    private String getPoster(Element element) throws NullPointerException,
                                                     IndexOutOfBoundsException {
        String poster;
        String delimiter;
        
        Element byuser = element.getElementsByClass("byuser").first();
        if (byuser != null) {
            poster = byuser.text();
            delimiter = "\u00A0";
        } else {
            poster = element.getElementsByTag("a").first().
                     nextSibling().toString();
            delimiter = "&nbsp;";
        }
        
        return poster.substring(poster.indexOf(delimiter) + delimiter.length());
    }
    
    /**
     * @return {@code true} if the topic only points at a topic in another forum
     */
    public boolean isMoved() {
        return isMoved;
    }
    
    /**
     * @return Topic ID. If the topic has been moved, this is invalid.
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
     * @return The ID of the last post in the topic, or 0 if it has been moved
     */
    public int getLastPostId() {
        return lastPostId;
    }
    
    /**
     * @return The username of the user that last posted in the topic, or
     * {@code null} if it has been moved
     */
    public String getLastPoster() {
        return lastPoster;
    }
    
    /**
     * @return The number of times the topic has been viewed, or 0 if it has
     * been moved
     */
    public int getNumViews() {
        return numViews;
    }
    
    /**
     * @return The number of posts in the topic (excluding the first post), or 0
     * if it has been moved
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
     * @return The ID of the topic this topic has been moved to, or 0 if it has
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
