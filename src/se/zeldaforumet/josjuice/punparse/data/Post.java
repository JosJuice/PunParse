package se.zeldaforumet.josjuice.punparse.data;

import org.jsoup.nodes.Element;

/**
 * @author JosJuice
 */
public class Post {
    
    public static final int UNKNOWN_TOPIC_ID = 0;
    public static final int UNKNOWN_DATE = 0;
    private static final String POSTER_ID_DELIMITER = "id=";
    
    /**
     * Post ID
     */
    private int id;
    
    /**
     * Username of the user that made the post
     */
    private String poster;
    
    /**
     * ID of the user that made the post
     */
    private int posterId;
    
    /**
     * The content of the post
     */
    private String message;
    
    /**
     * If true, text will never be converted to graphical smilies
     * when the post is displayed
     */
    private boolean hideSmilies;
    
    /**
     * Date posted (Unix timestamp)
     */
    private int posted;
    
    /**
     * Date last edited (Unix timestamp), or UNKNOWN_DATE if not edited
     */
    private int edited;
    
    /**
     * Username of the user that last edited the post, or null if not edited
     */
    private String editedBy;
    
    /**
     * ID of the topic containing this post
     */
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
        // Get URL to poster's profile
        String posterUrl = posterElement.getElementsByAttribute("href").
                                          first().attributes().get("href");
        // Get poster ID from profile URL
        // (this will fail if id isn't the last parameter in the URL)
        try {
            posterId = Integer.parseInt(posterUrl.substring(posterUrl.
                                        lastIndexOf(POSTER_ID_DELIMITER) +
                                        POSTER_ID_DELIMITER.length()));
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Could not determine poster ID" +
                                               "for post #" + this.id);
        }
        
        // Find message text
        // TODO get BBCode, not just plaintext
        message = element.getElementsByClass("postmsg").first().text();
        
        // TODO check if smilies need to be displayed
        hideSmilies = true;
        
        // TODO find date posted
        posted = UNKNOWN_DATE;
        
        // TODO find out if edited
        edited = UNKNOWN_DATE;
        editedBy = null;
        
        // Find topic ID (well, there's not much finding going on, really :))
        this.topicId = topicId;
    }
    
    public int getId() {
        return id;
    }
    
    public String getPoster() {
        return poster;
    }
    
    public int getPosterId() {
        return posterId;
    }
    
    public String getMessage() {
        return message;
    }
    
    public boolean getHideSmilies() {
        return hideSmilies;
    }
    
    public int getPosted() {
        return posted;
    }
    
    public int getEdited() {
        return edited;
    }
    
    public String getEditedBy() {
        return editedBy;
    }
    
    public int getTopicId() {
        return topicId;
    }
    
}
