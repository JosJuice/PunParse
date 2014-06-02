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
    
    /**
     * Creates all necessary tables.
     * @throws SQLException if something goes wrong on the SQL side
     */
    private void createTables() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + prefix + "bans (" +
                    "id " + type.primaryKey + ", " +
                    "username VARCHAR(200), " +
                    "ip VARCHAR(255), " +
                    "email VARCHAR(50), " +
                    "message VARCHAR(255), " +
                    "expire " + type.integer + ", " +
                    "PRIMARY KEY (id)" +
                    ")" + type.myIASM + ";");
            
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + prefix + "categories (" +
                    "id " + type.primaryKey + ", " +
                    "cat_name VARCHAR(80) NOT NULL DEFAULT 'New Category', " +
                    "disp_position " + type.integer + " NOT NULL DEFAULT 0, " +
                    "PRIMARY KEY (id)" +
                    ")" + type.myIASM + ";");
            
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + prefix + "censoring (" +
                    "id " + type.primaryKey + ", " +
                    "search_for VARCHAR(60) NOT NULL DEFAULT '', " +
                    "replace_with VARCHAR(60) NOT NULL DEFAULT '', " +
                    "PRIMARY KEY (id)" +
                    ")" + type.myIASM + ";");
            
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + prefix + "config (" +
                    "conf_name VARCHAR(255) NOT NULL DEFAULT '', " +
                    "conf_value TEXT, " +
                    "PRIMARY KEY (conf_name)" +
                    ")" + type.myIASM + ";");
            
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + prefix + "forum_perms (" +
                    "group_id " + type.integer + " NOT NULL DEFAULT 0, " +
                    "forum_id " + type.integer + " NOT NULL DEFAULT 0, " +
                    "read_forum " + type.bool + " NOT NULL DEFAULT 1, " +
                    "read_replies " + type.bool + " NOT NULL DEFAULT 1, " +
                    "post_topics " + type.bool + " NOT NULL DEFAULT 1, " +
                    "PRIMARY KEY (group_id, forum_id)" +
                    ")" + type.myIASM + ";");
            
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + prefix + "forums (" +
                    "id " + type.primaryKey + ", " +
                    "forum_name VARCHAR(80) NOT NULL DEFAULT 'New forum', " +
                    "forum_desc TEXT, " +
                    "redirect_url VARCHAR(100), " +
                    "moderators TEXT, " +
                    "num_topics " + type.mediumInt + " NOT NULL DEFAULT 0, " +
                    "num_posts " + type.mediumInt + " NOT NULL DEFAULT 0, " +
                    "last_post " + type.integer + ", " +
                    "last_post_id " + type.integer + ", " +
                    "last_poster VARCHAR(200), " +
                    "sort_by " + type.bool + " NOT NULL DEFAULT 0, " +
                    "disp_position " + type.integer + " NOT NULL DEFAULT 0, " +
                    "cat_id " + type.integer + " NOT NULL DEFAULT 0, " +
                    "PRIMARY KEY (id)" +
                    ")" + type.myIASM + ";");
            
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + prefix + "groups (" +
                    "g_id " + type.primaryKey + ", " +
                    "g_title VARCHAR(50) NOT NULL DEFAULT '', " +
                    "g_user_title VARCHAR(50), " +
                    "g_read_board " + type.bool + " NOT NULL DEFAULT 1, " +
                    "g_post_replies " + type.bool + " NOT NULL DEFAULT 1, " +
                    "g_post_topics " + type.bool + " NOT NULL DEFAULT 1, " +
                    "g_post_polls " + type.bool + " NOT NULL DEFAULT 1, " +
                    "g_edit_posts " + type.bool + " NOT NULL DEFAULT 1, " +
                    "g_delete_posts " + type.bool + " NOT NULL DEFAULT 1, " +
                    "g_delete_topics " + type.bool + " NOT NULL DEFAULT 1, " +
                    "g_set_title " + type.bool + " NOT NULL DEFAULT 1, " +
                    "g_search " + type.bool + " NOT NULL DEFAULT 1, " +
                    "g_search_users " + type.bool + " NOT NULL DEFAULT 1, " +
                    "g_edit_subjects_interval " + type.smallInt + " NOT NULL DEFAULT 300, " +
                    "g_post_flood " + type.smallInt + " NOT NULL DEFAULT 30, " +
                    "g_search_flood " + type.smallInt + " NOT NULL DEFAULT 30, " +
                    "PRIMARY KEY (g_id)" +
                    ")" + type.myIASM + ";");
            
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + prefix + "online (" +
                    "user_id " + type.integer + " NOT NULL DEFAULT 1, " +
                    "ident VARCHAR(200) NOT NULL DEFAULT '', " +
                    "logged " + type.integer + " NOT NULL DEFAULT 0, " +
                    "idle " + type.bool + " NOT NULL DEFAULT 0" +
                    ")" + type.memory + ";");
            
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + prefix + "posts (" +
                    "id " + type.primaryKey + ", " +
                    "poster VARCHAR(200) NOT NULL DEFAULT '', " +
                    "poster_id " + type.integer + " NOT NULL DEFAULT 1, " +
                    "poster_ip VARCHAR(15), " +
                    "poster_email VARCHAR(50), " +
                    "message TEXT, " +
                    "hide_smilies " + type.bool + " NOT NULL DEFAULT 0, " +
                    "posted " + type.integer + " NOT NULL DEFAULT 0, " +
                    "edited " + type.integer + ", " +
                    "edited_by VARCHAR(200), " +
                    "topic_id " + type.integer + " NOT NULL DEFAULT 0, " +
                    "PRIMARY KEY (id)" +
                    ")" + type.myIASM + ";");
            
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + prefix + "ranks (" +
                    "id " + type.primaryKey + ", " +
                    "rank VARCHAR(50) NOT NULL DEFAULT '', " +
                    "min_posts " + type.mediumInt + " NOT NULL DEFAULT 0, " +
                    "PRIMARY KEY (id)" +
                    ")" + type.myIASM + ";");
            
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + prefix + "reports (" +
                    "id " + type.primaryKey + ", " +
                    "post_id " + type.integer + " NOT NULL DEFAULT 0, " +
                    "topic_id " + type.integer + " NOT NULL DEFAULT 0, " +
                    "forum_id " + type.integer + " NOT NULL DEFAULT 0, " +
                    "reported_by " + type.integer + " NOT NULL DEFAULT 0, " +
                    "created " + type.integer + " NOT NULL DEFAULT 0, " +
                    "message TEXT, " +
                    "zapped " + type.integer + ", " +
                    "zapped_by " + type.integer + ", " +
                    "PRIMARY KEY (id)" +
                    ")" + type.myIASM + ";");
            
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + prefix + "search_cache (" +
                    "id " + type.integer + " NOT NULL DEFAULT 0, " +
                    "ident VARCHAR(200) NOT NULL DEFAULT '', " +
                    "search_data TEXT, " +
                    "PRIMARY KEY (id)" +
                    ")" + type.myIASM + ";");
            
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + prefix + "search_matches (" +
                    "post_id " + type.integer + " NOT NULL DEFAULT 0, " +
                    "word_id " + type.mediumInt + " NOT NULL DEFAULT 0, " +
                    "subject_match " + type.bool + " NOT NULL DEFAULT 0" +
                    ")" + type.myIASM + ";");
            
            switch (type) {
                case MYSQL:
                    statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + prefix + "search_words (" +
                    "id MEDIUMINT(8) UNSIGNED NOT NULL AUTO_INCREMENT, " +
                    "word VARCHAR(20) BINARY NOT NULL DEFAULT '', " +
                    "PRIMARY KEY (word), " +
                    "KEY " + prefix + "search_words_id_idx (id)" +
                    ") ENGINE=MyISAM;");
                    break;
                case POSTGRESQL:
                    statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + prefix + "search_words (" +
                    "id SERIAL, " +
                    "word VARCHAR(20) NOT NULL DEFAULT '', " +
                    "PRIMARY KEY (word)" +
                    ");");
                    break;
                case SQLITE:
                default:
                    statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + prefix + "search_words (" +
                    "id INTEGER NOT NULL, " +
                    "word VARCHAR(20) NOT NULL DEFAULT '', " +
                    "PRIMARY KEY (id), " +
                    "UNIQUE (word)" +
                    ");");
                    break;
            }
            
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + prefix + "subscriptions (" +
                    "user_id " + type.integer + " NOT NULL DEFAULT 0, " +
                    "topic_id " + type.integer + " NOT NULL DEFAULT 0, " +
                    "PRIMARY KEY (user_id, topic_id)" +
                    ")" + type.myIASM + ";");
            
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + prefix + "topics (" +
                    "id " + type.primaryKey + ", " +
                    "poster VARCHAR(200) NOT NULL DEFAULT '', " +
                    "subject VARCHAR(255) NOT NULL DEFAULT '', " +
                    "posted " + type.integer + " NOT NULL DEFAULT 0, " +
                    "last_post " + type.integer + " NOT NULL DEFAULT 0, " +
                    "last_post_id " + type.integer + " NOT NULL DEFAULT 0, " +
                    "last_poster VARCHAR(200), " +
                    "num_views " + type.mediumInt + " NOT NULL DEFAULT 0, " +
                    "num_replies " + type.mediumInt + " NOT NULL DEFAULT 0, " +
                    "closed " + type.bool + " NOT NULL DEFAULT 0, " +
                    "sticky " + type.bool + " NOT NULL DEFAULT 0, " +
                    "moved_to " + type.integer + ", " +
                    "forum_id " + type.integer + " NOT NULL DEFAULT 0, " +
                    "PRIMARY KEY (id)" +
                    ")" + type.myIASM + ";");
            
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + prefix + "users (" +
                    "id " + type.primaryKey + ", " +
                    "group_id " + type.integer + " NOT NULL DEFAULT 4, " +
                    "username VARCHAR(200) NOT NULL DEFAULT '', " +
                    "password VARCHAR(40) NOT NULL DEFAULT '', " +
                    "email VARCHAR(50) NOT NULL DEFAULT '', " +
                    "title VARCHAR(50), " +
                    "realname VARCHAR(40), " +
                    "url VARCHAR(100), " +
                    "jabber VARCHAR(75), " +
                    "icq VARCHAR(12), " +
                    "msn VARCHAR(50), " +
                    "aim VARCHAR(30), " +
                    "yahoo VARCHAR(30), " +
                    "location VARCHAR(30), " +
                    "use_avatar " + type.bool + " NOT NULL DEFAULT 0, " +
                    "signature TEXT, " +
                    "disp_topics " + type.tinyInt + ", " +
                    "disp_posts " + type.tinyInt + ", " +
                    "email_setting " + type.bool + " NOT NULL DEFAULT 1, " +
                    "save_pass " + type.bool + " NOT NULL DEFAULT 1, " +
                    "notify_with_post " + type.bool + " NOT NULL DEFAULT 0, " +
                    "show_smilies " + type.bool + " NOT NULL DEFAULT 1, " +
                    "show_img " + type.bool + " NOT NULL DEFAULT 1, " +
                    "show_img_sig " + type.bool + " NOT NULL DEFAULT 1, " +
                    "show_avatars " + type.bool + " NOT NULL DEFAULT 1, " +
                    "show_sig " + type.bool + " NOT NULL DEFAULT 1, " +
                    "timezone " + type.real + " NOT NULL DEFAULT 0, " +
                    "language VARCHAR(25) NOT NULL DEFAULT 'English', " +
                    "style VARCHAR(25) NOT NULL DEFAULT 'Oxygen', " +
                    "num_posts " + type.integer + " NOT NULL DEFAULT 0, " +
                    "last_post " + type.integer + ", " +
                    "registered " + type.integer + " NOT NULL DEFAULT 0, " +
                    "registration_ip VARCHAR(15) NOT NULL DEFAULT '0.0.0.0', " +
                    "last_visit " + type.integer + " NOT NULL DEFAULT 0, " +
                    "admin_note VARCHAR(30), " +
                    "activate_string VARCHAR(50), " +
                    "activate_key VARCHAR(8), " +
                    "PRIMARY KEY (id)" +
                    ")" + type.myIASM + ";");
        }
    }
    
    private enum Type {
        MYSQL("INT(10) UNSIGNED", "MEDIUMINT(8) UNSIGNED", "SMALLINT(6)",
              "TINYINT(3) UNSIGNED", "TINYINT(1)", "FLOAT",
              "INT(10) UNSIGNED NOT NULL AUTO_INCREMENT",
              " ENGINE=MyISAM", " ENGINE=MEMORY", "IGNORE "),
        POSTGRESQL("INT", "INT", "SMALLINT",
                   "SMALLINT", "SMALLINT", "REAL",
                   "SERIAL",
                   "", "", ""),   // TODO ignore existing records
        SQLITE("INTEGER", "INTEGER", "INTEGER",
               "INTEGER", "INTEGER", "FLOAT",
               "INTEGER NOT NULL",
               "", "", "ON CONFLICT IGNORE ");
        
        public final String integer;
        public final String mediumInt;
        public final String smallInt;
        public final String tinyInt;
        public final String bool;
        public final String real;
        public final String primaryKey;
        public final String myIASM;
        public final String memory;
        public final String ignoreInsert;
        
        private Type(String integer, String mediumInt, String smallInt,
                     String tinyInt, String bool, String real,
                     String primaryKey,
                     String myIASM, String memory, String ignoreInsert) {
            this.integer = integer;
            this.mediumInt = mediumInt;
            this.smallInt = smallInt;
            this.tinyInt = tinyInt;
            this.bool = bool;
            this.real = real;
            this.primaryKey = primaryKey;
            this.myIASM = myIASM;
            this.memory = memory;
            this.ignoreInsert = ignoreInsert;
        }
    }
    
}
