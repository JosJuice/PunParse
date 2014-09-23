package se.zeldaforumet.josjuice.punparse;

import java.util.List;
import java.util.TreeMap;

/**
 * Contains mappings between post IDs and topic IDs, and post IDs and forum IDs.
 * Thread safe.
 * @author JosJuice
 */
public class IdMappings {
    
    private final TreeMap<Integer, Integer> postTopicMap = new TreeMap<>();
    
    /**
     * Attempts to get the topic ID of a page of posts. This will only work if a
     * mapping has been added for the topic using
     * {@link #setTopicId(Topic) setTopicId}. If that is not the case,
     * {@code null} will be returned and the posts will be queued in this object
     * and sent to the database once a matching mapping is added.
     * 
     * To clarify: If {@code null} is returned, this object will handle sending
     * the posts to the database. If any other value is returned, it will not.
     * 
     * @param posts All posts in a page, preferably in their original order.
     * @return The topic ID, or {@code null} if it could not be found.
     */
    public Integer getTopicId(List<Post> posts) {
        synchronized (postTopicMap) {
            // Loops backwards since the post we're looking for probably is last
            for (int i = posts.size() - 1; i >= 0; i--) {
                Integer possibleId = postTopicMap.get(posts.get(i).getId());
                if (possibleId != null) {
                    return possibleId;
                }
            }
            // TODO handle the case of needing to queue posts
            return null;
        }
    }
    
    /**
     * Adds a mapping between a topic ID and the ID of a topic's last post. If
     * there already is a mapping for the ID of the last post, it will be
     * replaced. If there are queued posts matching this topic, they will be
     * sent to the database synchronously when this method is called.
     * @param topic The topic to get the topic ID and last post ID from.
     * @param database A {@link Database} that data can be sent to.
     * @param ui A {@link UserInterface} for error display, or {@code null}.
     */
    public void setTopicId(Topic topic, Database database, UserInterface ui) {
        synchronized (postTopicMap) {
            postTopicMap.put(topic.getLastPostId(), topic.getId());
            // TODO handle the case of queued posts matching this topic
        }
    }
    
    // TODO forum mappings
    
}
