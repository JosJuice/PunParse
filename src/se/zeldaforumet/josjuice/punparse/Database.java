package se.zeldaforumet.josjuice.punparse;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

/**
 * Connects to an SQL database and inserts data.
 * @author Jos
 */
public class Database implements AutoCloseable {
    
    private final Connection connection;
    private final Type type;
    private final String prefix;
    
    private final PreparedStatement insertPost;
    
    /**
     * Sets up a a database. First, a connection will be established.
     * Then, tables will be created and prepared statements will be initialized.
     * @param url the URL used to access the database, for instance
     * mysql://localhost/database?user=username&password=password or
     * postgresql://localhost/database?user=username&password=password or
     * sqlite:database.db
     * (do not include a preceding jdbc:)
     * @param prefix a short string to prefix table names with (can be null)
     * @throws SQLException if something goes wrong on the SQL side
     */
    public Database(String url, String tablePrefix) throws SQLException {
        if (tablePrefix == null) {
            prefix = "";
        } else {
            prefix = tablePrefix;
        }
        connection = DriverManager.getConnection("jdbc:" + url);
        type = Type.MYSQL;          // TODO detect database type
        createTables();
        
        insertPost = connection.prepareStatement("INSERT " + type.ignoreInsert +
                "INTO " + prefix + "posts (id, poster, poster_id, message, " +
                "hide_smilies, posted, edited, edited_by, topic_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);");
    }
    
    /**
     * Creates all necessary tables.
     * @throws SQLException if something goes wrong on the SQL side
     */
    private void createTables() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + prefix + "posts (" +
                        "id " + type.primaryKey + "," +
                        "poster VARCHAR(200) NOT NULL DEFAULT ''," +
                        "poster_id " + type.integer + " NOT NULL DEFAULT 1," +
                        "message TEXT," +
                        "hide_smilies " + type.bool + " NOT NULL DEFAULT 0," +
                        "posted " + type.integer + " NOT NULL DEFAULT 0," +
                        "edited " + type.integer + "," +
                        "edited_by VARCHAR(200)," +
                        "topic_id " + type.integer + " NOT NULL DEFAULT 0," +
                        "PRIMARY KEY (id)" +
                    ")" + type.engine + ";");
        }
    }
    
    @Override public void close() throws SQLException {
        connection.close();
        insertPost.close();
    }
    
    /**
     * Inserts a post into the database.
     * @param post the post to insert
     * @throws SQLException if something goes wrong on the SQL side
     */
    public void insert(Post post) throws SQLException {
        insertPost.setInt(1, post.getId());
        insertPost.setString(2, post.getPoster());
        insertPost.setInt(3, post.getPosterId());
        insertPost.setString(4, post.getMessage());
        insertPost.setBoolean(5, post.getHideSmilies());
        insertPost.setLong(6, post.getPosted());
        if (post.isEdited()) {
            insertPost.setLong(7, post.getEdited());
            insertPost.setString(8, post.getEditedBy());
        } else {
            insertPost.setNull(7, Types.INTEGER);
            insertPost.setNull(8, Types.VARCHAR);
        }
        insertPost.setInt(9, post.getTopicId());
        insertPost.executeUpdate();
    }
    
    private enum Type {
        MYSQL("INT(10) UNSIGNED", "TINYINT(1)",
              "INT(10) UNSIGNED NOT NULL AUTO_INCREMENT",
              " ENGINE=MyISAM", "IGNORE "),
        POSTGRESQL("INT", "SMALLINT", "SERIAL",
                   "", ""),   // TODO ignore existing rows
        SQLITE("INTEGER", "INTEGER", "INTEGER NOT NULL",
               "", "ON CONFLICT IGNORE ");
        
        public final String integer;
        public final String bool;
        public final String primaryKey;
        public final String engine;
        public final String ignoreInsert;
        
        private Type(String integer, String bool, String primaryKey,
                     String engine, String ignoreInsert) {
            this.integer = integer;
            this.bool = bool;
            this.primaryKey = primaryKey;
            this.engine = engine;
            this.ignoreInsert = ignoreInsert;
        }
    }
    
}
