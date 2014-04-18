package se.zeldaforumet.josjuice.punparse;

import se.zeldaforumet.josjuice.punparse.data.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Connects to an SQL database and inserts data.
 * @author Jos
 */
public class Database implements AutoCloseable {
    
    private Connection connection;
    
    private PreparedStatement insertPost;
    
    /**
     * Sets up a connection to a database.
     * @param url the URL used to access the database, for instance
     * <code>mysql://localhost/?user=username&password=password</code>
     * (do not include a preceding <code>jdbc:</code>)
     * @param database the name of the database
     * @throws SQLException if something goes wrong on the SQL side
     */
    public Database(String url, String database) throws ClassNotFoundException,
                                                        SQLException {
        connection = DriverManager.getConnection("jdbc:" + url);
        connection.setCatalog(database);
        
        // Create tables
        Statement statement = connection.createStatement();
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS posts (" +
            "id int(10) unsigned NOT NULL, " +
            "poster varchar(200) NOT NULL DEFAULT '', " +
            "poster_id int(10) unsigned NOT NULL DEFAULT '1', " +
            "poster_ip varchar(15) DEFAULT NULL, " +
            "poster_email varchar(50) DEFAULT NULL, " +
            "message text, " +
            "hide_smilies tinyint(1) NOT NULL DEFAULT '0', " +
            "posted int(10) unsigned NOT NULL DEFAULT '0', " +
            "edited int(10) unsigned DEFAULT NULL, " +
            "edited_by varchar(200) DEFAULT NULL, " +
            "topic_id int(10) unsigned NOT NULL DEFAULT '0', " +
            "PRIMARY KEY (id), " +
            "KEY posts_topic_id_idx (topic_id), " +
            "KEY posts_multi_idx (poster_id,topic_id) " +
        ")");
        
        // Prepare the prepared statements
        insertPost = connection.prepareStatement("INSERT INTO posts (id, " +
                "poster, poster_id, message, hide_smilies, posted, edited, " +
                "edited_by, topic_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
    }
    
    @Override public void close() throws SQLException {
        connection.close();
    }
    
    public void insert(Post post) throws SQLException {
        insertPost.setInt(1, post.getId());
        insertPost.setString(2, post.getPoster());
        insertPost.setInt(3, post.getPosterId());
        insertPost.setString(4, post.getMessage());
        insertPost.setBoolean(5, post.getHideSmilies());
        insertPost.setInt(6, post.getPosted());
        insertPost.setInt(7, post.getEdited());
        insertPost.setString(8, post.getEditedBy());
        insertPost.setInt(9, post.getTopicId());
        insertPost.executeUpdate();
    }
    
}
