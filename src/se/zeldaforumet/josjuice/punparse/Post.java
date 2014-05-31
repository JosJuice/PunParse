package se.zeldaforumet.josjuice.punparse;

import org.jsoup.nodes.Element;

/**
 * @author JosJuice
 */
public final class Post {
    
    public static final int UNKNOWN_TOPIC_ID = 0;
    
    private final boolean isEdited;
    
    // All of the following variables correspond to database columns.
    private final int id;
    private final String poster;
    private final int posterId;
    private final String message;
    private final boolean hideSmilies;
    private final long posted;
    private final long edited;
    private final String editedBy;
    private final int topicId;
    
    /**
     * Constructs a <code>Post</code>.
     * @param element HTML element representing a post.
     * The element should always have the <code>.blockpost</code> class.
     * @param topicId ID of the topic containing this post.
     * Use <code>UNKNOWN_TOPIC_ID</code> if unknown.
     * @throws IllegalArgumentException if some required data is invalid
     */
    public Post(Element element, int topicId) throws IllegalArgumentException {
        try {
            // Find post ID
            String idText = element.id();
            try {
                // Skip first character 'p' and use the rest of the string as ID
                id = Integer.parseInt(idText.substring(1));
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Invalid post ID: "+ idText);
            }
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Couldn't find post ID.");
        }
        
        try {
            // Find poster username and ID
            Element posterElement = element.getElementsByTag("dt").first();
            // Get poster username
            poster = posterElement.text();
            // Check if posted by guest
            if (posterElement.getElementsByAttribute("href").isEmpty()) {
                posterId = User.GUEST_ID;
            } else {
                // Get URL to poster's profile
                String posterUrl = posterElement.getElementsByAttribute("href").
                        first().attributes().get("href");
                // Get poster ID from profile URL
                // (this will fail if id isn't the last parameter in the URL)
                try {
                    final String DELIMITER = "id=";
                    posterId = Integer.parseInt(posterUrl.substring(posterUrl.
                               lastIndexOf(DELIMITER) + DELIMITER.length()));
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    throw new IllegalArgumentException("Couldn't find poster " +
                                                       "ID of post " + this.id);
                }
            }
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Couldn't find poster " +
                                               "of post " + this.id);
        }
        
        try {
        // Find message text
        // TODO get BBCode, not just plaintext
        message = element.getElementsByClass("postmsg").first().text();
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Couldn't find message body " +
                                               "of post " + this.id);
        }
        
        // TODO check if smilies need to be displayed
        hideSmilies = true;
        
        // TODO find date posted
        posted = id;
        
        // TODO find out if edited
        isEdited = false;
        edited = 0;
        editedBy = null;
        
        // Find topic ID (well, there's not much finding going on, really :))
        this.topicId = topicId;
    }
    
    /**
     * @return true if the post is marked as edited. (It is possible to edit a
     * post without marking it as edited if the silent edit feature is used.)
     */
    public boolean isEdited() {
        return isEdited;
    }
    
    /**
     * @return Post ID
     */
    public int getId() {
        return id;
    }
    
    /**
     * @return Username of the user that made the post
     */
    public String getPoster() {
        return poster;
    }
    
    /**
     * @return ID of the user that made the post
     */
    public int getPosterId() {
        return posterId;
    }
    
    /**
     * @return The content of the post
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * @return If true, text will never be converted to graphical smilies
     * when the post is displayed
     */
    public boolean getHideSmilies() {
        return hideSmilies;
    }
    
    /**
     * @return Date posted (Unix timestamp)
     */
    public long getPosted() {
        return posted;
    }
    
    /**
     * @return Date last edited (Unix timestamp), or 0 if unedited
     */
    public long getEdited() {
        return edited;
    }
    
    /**
     * @return Username of last user that edited the post, or null if unedited
     */
    public String getEditedBy() {
        return editedBy;
    }
    
    /**
     * @return ID of the topic containing this post
     */
    public int getTopicId() {
        return topicId;
    }
    
}