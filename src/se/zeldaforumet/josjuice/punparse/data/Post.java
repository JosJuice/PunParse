package se.zeldaforumet.josjuice.punparse.data;

import org.jsoup.nodes.Element;

/**
 * @author JosJuice
 */
public class Post {
    
    public static final int UNKNOWN_TOPIC_ID = 0;
    
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
     * Date last edited (Unix timestamp)
     */
    private int edited;
    
    /**
     * Username of the user that edited the post last
     */
    private String editedBy;
    
    /**
     * ID of the topic containing this post
     */
    private int topicId;
    
    /**
     * Constructs a <code>Post</code>.
     * @param postElement HTML element representing a post.
     * The element should always have the <code>.blockpost</code> class.
     * @param topicId ID of the topic containing this post.
     * Use <code>UNKNOWN_TOPIC_ID</code> if unknown.
     */
    public Post(Element postElement, int topicId) {
        // TODO
    }
}
