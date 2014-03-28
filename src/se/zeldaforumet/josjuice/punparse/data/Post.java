package se.zeldaforumet.josjuice.punparse.data;

import org.jsoup.nodes.Element;

/**
 * @author JosJuice
 */
public class Post {
    
    public static final int UNKNOWN_TOPIC_ID = 0;
    
    /**
     * Constructs a Post.
     * @param postElement HTML element representing a post (.blockpost)
     * @param topicId ID of the topic containing this post
     * (use UNKNOWN_TOPIC_ID if unknown)
     */
    public Post(Element postElement, int topicId) {
        // TODO
    }
}
