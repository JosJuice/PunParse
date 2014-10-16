package se.zeldaforumet.josjuice.punparse;

import java.sql.SQLException;
import java.util.List;
import java.util.TreeMap;

/**
 * Contains mappings between post IDs and topic IDs, and post IDs and forum IDs.
 * Thread safe.
 * @author JosJuice
 */
public final class IdMappings {
    
    /*
     * Maps topic IDs to posts.
     * Key: Post ID
     * Value: Topic ID
     */
    private final TreeMap<Integer, Integer> postTopicMap = new TreeMap<>();
    
    /*
     * Stores posts so that they can be sent to a database later when the
     * topic ID is found. To make lookups easy, each page of posts is present
     * one time for each post ID. (A page with 15 posts has 15 key-value pairs)
     * Key: Post ID
     * Value: Pages of posts
     */
    private final TreeMap<Integer, List<Post>> postQueue = new TreeMap<>();
    
    /**
     * Attempts to get the topic ID of a page of posts. This will only work if a
     * mapping has been added for the topic using
     * {@link #setTopicId(Topic) setTopicId}. If that is not the case,
     * {@code null} will be returned and the posts will be queued in this object
     * and sent to the database later once a matching mapping is added.
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
            // If this is reached, the ID wasn't found. The posts will be queued
            for (Post post : posts) {
                postQueue.put(post.getId(), posts);
            }
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
        List<Post> queuedPosts;
        synchronized (postTopicMap) {
            postTopicMap.put(topic.getLastPostId(), topic.getId());
            
            // Check if there any queued posts match, and prepare for submitting
            queuedPosts = postQueue.get(topic.getLastPostId());
            if (queuedPosts != null) {
                for (Post post : queuedPosts) {
                    postQueue.remove(post.getId());
                }
            }
        }
        
        // Submit matching queued posts if there are any
        // This is done outside the synchronized block so that other threads can
        // use the postTopicMap while this thread only is using the database
        if (queuedPosts != null) {
            for (Post post : queuedPosts) {
                try {
                    database.insert(post, topic.getId());
                } catch (SQLException e) {
                    System.err.println("SQL error when submitting queued post "+
                            post.getId() + ": " + e.getLocalizedMessage());
                }
            }
        }
    }
    
    // TODO forum mappings
    
    /**
     * Submits all queued posts to a database. Intended to be used before
     * exiting to submit posts that couldn't be associated with a topic.
     * @param topicId The topic ID associate the posts with.
     * @param database A {@link Database} that data can be sent to.
     */
    public void submitAllQueuedPosts(int topicId, Database database) {
        synchronized (postTopicMap) {
            while (!postQueue.isEmpty()) {
                for (Post post : postQueue.firstEntry().getValue()) {
                    postQueue.remove(post.getId());
                    try {
                        database.insert(post, topicId);
                    } catch (SQLException e) {
                        System.err.println("SQL error when submitting queued " +
                                           "post " + post.getId() + ": " +
                                           e.getLocalizedMessage());
                    }
                }
            }
        }
    }
    
}
