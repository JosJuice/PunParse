package se.zeldaforumet.josjuice.punparse;

import org.jsoup.nodes.Element;

/**
 * Represents a record in the PunBB 'users' table. Immutable.
 * @author JosJuice
 */
public final class User {
    
    // All of the following variables correspond to database columns.
    private final int id;
    private final String username;
    private final String title;
    private final boolean hasAvatar;
    
    /**
     * Constructs a {@code User}.
     * @param element HTML element representing the user information in a post.
     * The element should always have the {@code .postleft} class.
     * @throws IllegalArgumentException if required parts of HTML are missing
     */
    public User(Element element) throws IllegalArgumentException {
        try {
            String userUrl = element.getElementsByTag("a").first().attr("href");
            id = Integer.parseInt(TextParser.getQueryValue(userUrl, "id"));
        } catch (NullPointerException | NumberFormatException e) {
            throw new IllegalArgumentException("Couldn't get user ID.", e);
        }
        
        try {
            username = element.getElementsByTag("dt").first().text();
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Couldn't get username " +
                                               "of user " + id, e);
        }
        
        try {
            title = element.getElementsByClass("usertitle").first().text();
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Couldn't get user title " +
                                               "of user " + id, e);
        }
        
        hasAvatar = !element.getElementsByClass("postavatar").isEmpty();
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
    
}
