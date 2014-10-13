package se.zeldaforumet.josjuice.punparse;

import org.jsoup.nodes.Element;

/**
 * Represents a record in the PunBB 'users' table. Only contains the columns
 * that can be found in a post, not ones that only are in profiles. Immutable.
 * @author JosJuice
 */
public final class PostUser {
    
    // All of the following variables correspond to database columns.
    private final int id;
    private final String username;
    private final String title;
    private final boolean hasAvatar;
    private final String signature;
    
    /**
     * Constructs a {@code User}.
     * @param element HTML element representing the user information in a post.
     * The element should always have the {@code .blockpost} class.
     * @throws IllegalArgumentException if required parts of HTML are missing
     */
    public PostUser(Element element) throws IllegalArgumentException {
        try {
            // Find username and ID
            Element posterElement = element.getElementsByTag("dt").first();
            // Get username
            username = posterElement.text();
            // Check if this is a guest
            Element posterLink = posterElement.getElementsByTag("a").first();
            if (posterLink == null) {
                id = 1; // The ID 1 is used by all guests
            } else {
                // Get ID from profile URL
                try {
                    id = Integer.parseInt(TextParser.getQueryValue(
                                          posterLink.attr("href"), "id"));
                } catch (NullPointerException | NumberFormatException e) {
                    throw new IllegalArgumentException("Couldn't get poster " +
                                                       "ID of post", e);
                }
            }
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Couldn't get poster " +
                                               "of post", e);
        }
        
        try {
            // Get title
            title = element.getElementsByClass("usertitle").first().text();
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Couldn't get user title " +
                                               "of user " + id, e);
        }
        
        // Find out whether the user is using an avatar
        hasAvatar = !element.getElementsByClass("postavatar").isEmpty();
        
        // Find signature
        Element postsignature =
                element.getElementsByClass("postsignature").first();
        if (postsignature == null) {
            signature = null;
        } else {
            // Parse signature to BBCode
            signature = TextParser.parseMessage(postsignature);
        }
    }
    
    /**
     * @return User ID
     */
    public int getId() {
        return id;
    }
    
    /**
     * @return Username
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * @return User title
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * @return {@code true} if the user has an avatar that is set to be used
     */
    public boolean getHasAvatar() {
        return hasAvatar;
    }
    
    /**
     * @return Signature in BBCode, or {@code null} if there is no signature
     */
    public String getSignature() {
        return signature;
    }
    
}
