package se.zeldaforumet.josjuice.punparse;

import org.jsoup.nodes.Element;

/**
 * Represents a record in the PunBB 'posts' table. Immutable.
 * @author JosJuice
 */
public final class Post {
    
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
     * Constructs a {@code Post}.
     * @param element HTML element representing a post.
     * The element should always have the {@code .blockpost} class.
     * @param topicId ID of the topic containing this post.
     * @throws IllegalArgumentException if required parts of HTML are missing
     */
    public Post(Element element, int topicId) throws IllegalArgumentException {
        try {
            // Find post ID
            String idText = element.id();
            try {
                // Skip first character 'p' and use the rest of the string as ID
                id = Integer.parseInt(idText.substring(1));
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Invalid post ID: "
                                                   + idText, e);
            }
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Couldn't get post ID.", e);
        }
        
        try {
            // Find poster username and ID
            Element posterElement = element.getElementsByTag("dt").first();
            // Get poster username
            poster = posterElement.text();
            // Check if posted by guest
            Element posterLink = posterElement.getElementsByTag("a").first();
            if (posterLink == null) {
                posterId = User.GUEST_ID;
            } else {
                // Get poster ID from profile URL
                try {
                    posterId = Integer.parseInt(TextParser.getQueryValue(
                               posterLink.attr("href"), "id"));
                } catch (NullPointerException | NumberFormatException e) {
                    throw new IllegalArgumentException("Couldn't get poster " +
                                                       "ID of post " + id, e);
                }
            }
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Couldn't get poster " +
                                               "of post " + id, e);
        }
        
        try {
            // Find message text
            Element postmsg = element.getElementsByClass("postmsg").first();
            message = TextParser.parseMessage(postmsg);
            // Set "hide smilies" if there are no smilies to display
            hideSmilies = !TextParser.containsSmilies(postmsg);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Couldn't get message body " +
                                               "of post " + id, e);
        }
        
        // TODO find time posted
        posted = id;
        
        // TODO find out if edited
        isEdited = false;
        edited = 0;
        editedBy = null;
        
        // Set topic ID
        this.topicId = topicId;
    }
    
    /**
     * @return {@code true} if the post is marked as edited. (It's possible to
     * edit a post without marking it as edited by using silent edit.)
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
     * @return If {@code true}, text will never be converted to graphical
     * smilies when the post is displayed
     */
    public boolean getHideSmilies() {
        return hideSmilies;
    }
    
    /**
     * @return Time posted (Unix timestamp)
     */
    public long getPosted() {
        return posted;
    }
    
    /**
     * @return Time last edited (Unix timestamp), or 0 if not marked as edited
     */
    public long getEdited() {
        return edited;
    }
    
    /**
     * @return The username of user that last edited the post, or {@code null}
     * if the post is not marked as edited
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
