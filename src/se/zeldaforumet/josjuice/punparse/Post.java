package se.zeldaforumet.josjuice.punparse;

import java.text.ParseException;
import org.jsoup.nodes.Element;

/**
 * Represents a record in the PunBB 'posts' table. Immutable.
 * @author JosJuice
 */
public final class Post {
    
    private final PostUser postUser;
    private final boolean isEdited;
    
    // All of the following variables correspond to database columns.
    private final int id;
    private final String message;
    private final boolean hideSmilies;
    private final long posted;
    private final long edited;
    private final String editedBy;
    
    /**
     * Constructs a {@code Post}.
     * @param element HTML element representing a post.
     * The element should always have the {@code .blockpost} class.
     * @param dateParser A {@link DateParser} for parsing dates.
     * @throws IllegalArgumentException if required parts of HTML are missing
     */
    public Post(Element element, DateParser dateParser)
            throws IllegalArgumentException {
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
        
        // Find poster ID and username
        postUser = new PostUser(element);
        
        try {
            // Find message text
            Element postmsg = element.getElementsByClass("postmsg").first();
            // Set "hide smilies" if there are no smilies in the post
            hideSmilies = !TextParser.containsSmilies(postmsg);
            // Parse message text to BBCode
            message = TextParser.parseMessage(postmsg);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Couldn't get message body " +
                                               "of post " + id, e);
        }
        
        try {
            // Find the date the message was posted
            String dateString = element.getElementsByTag("a").first().text();
            // Parse the date
            posted = dateParser.parse(dateString);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Couldn't get date of post " +
                                               id, e);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e.getLocalizedMessage());
        }
        
        // TODO find out if edited
        isEdited = false;
        edited = 0;
        editedBy = null;
    }
    
    /**
     * @return {@code PostUser} object for the user that made this post
     */
    public PostUser getPostUser() {
        return postUser;
    }
    
    /**
     * @return {@code true} if the post is marked as edited. (Moderators can
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
        return postUser.getUsername();
    }
    
    /**
     * @return ID of the user that made the post
     */
    public int getPosterId() {
        return postUser.getId();
    }
    
    /**
     * @return The content of the post in BBCode
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
    
}
