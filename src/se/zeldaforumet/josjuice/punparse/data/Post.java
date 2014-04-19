package se.zeldaforumet.josjuice.punparse.data;

import org.jsoup.nodes.Element;

/**
 * @author JosJuice
 */
public class Post {
    
    public static final int UNKNOWN_TOPIC_ID = 0;
    public static final int UNKNOWN_DATE = 0;
    
    private boolean isEdited;
    
    // All of the following variables correspond to database columns.
    private int id;
    private String poster;
    private int posterId;
    private String message;
    private boolean hideSmilies;
    private int posted;
    private int edited;
    private String editedBy;
    private int topicId;
    
    /**
     * Constructs a <code>Post</code>.
     * @param element HTML element representing a post.
     * The element should always have the <code>.blockpost</code> class.
     * @param topicId ID of the topic containing this post.
     * Use <code>UNKNOWN_TOPIC_ID</code> if unknown.
     * @throws NullPointerException if a required child element does not exist
     * @throws IllegalArgumentException if some required data is invalid
     */
    public Post(Element element, int topicId) throws NullPointerException,
                                                     IllegalArgumentException {
        // Find post ID
        String id = element.id();
        try {
            // Skip the first character 'p' and use the rest of the string as ID
            this.id = Integer.parseInt(id.substring(1));
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Invalid post ID: " + id);
        }
        
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
                throw new IllegalArgumentException("Could not find poster ID" +
                                                   "for post #" + this.id);
            }
        }
        
        // Find message text
        // TODO get BBCode, not just plaintext
        message = element.getElementsByClass("postmsg").first().text();
        
        // TODO check if smilies need to be displayed
        hideSmilies = true;
        
        // TODO find date posted
        posted = UNKNOWN_DATE;
        
        // TODO find out if edited
        isEdited = false;
        edited = UNKNOWN_DATE;
        editedBy = null;
        
        // Find topic ID (well, there's not much finding going on, really :))
        this.topicId = topicId;
    }
    
    /**
     * @return true if the post has been edited. (It is possible to edit a post
     * without marking it as edited if the silent edit feature is used.)
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
    public int getPosted() {
        return posted;
    }
    
    /**
     * @return Date last edited (Unix timestamp), or UNKNOWN_DATE if unedited
     */
    public int getEdited() {
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
