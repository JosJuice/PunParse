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
        setUpDatabase();
    }
    
    @Override public void close() throws SQLException {
        connection.close();
    }
    
    /**
     * Creates all necessary tables and <code>PreparedStatement</code>s.
     * @throws SQLException if something goes wrong on the SQL side
     */
    private void setUpDatabase() throws SQLException {
        // Create tables
        Statement statement = connection.createStatement();
        statement.executeUpdate("CREATE TABLE posts (" +
                "id INT(10) UNSIGNED NOT NULL AUTO_INCREMENT," +
                "poster VARCHAR(200) NOT NULL DEFAULT ''," +
                "poster_id INT(10) UNSIGNED NOT NULL DEFAULT 1," +
                "poster_ip VARCHAR(15)," +
                "poster_email VARCHAR(50)," +
                "message TEXT," +
                "hide_smilies TINYINT(1) NOT NULL DEFAULT 0," +
                "posted INT(10) UNSIGNED NOT NULL DEFAULT 0," +
                "edited INT(10) UNSIGNED," +
                "edited_by VARCHAR(200)," +
                "topic_id INT(10) UNSIGNED NOT NULL DEFAULT 0," +
                "PRIMARY KEY (id)" +
                ") ENGINE=MyISAM;");
        
        // Prepare the prepared statements
        insertPost = connection.prepareStatement("INSERT INTO posts (id, " +
                "poster, poster_id, message, hide_smilies, posted, edited, " +
                "edited_by, topic_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);");
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
