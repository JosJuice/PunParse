package se.zeldaforumet.josjuice.punparse;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

/**
 * Connects to an SQL database and inserts data. This class is thread-safe, but
 * performance may be affected if many threads access the same object since the
 * connection to the database only can be used by one thread at a time.
 * @author JosJuice
 */
public class Database implements AutoCloseable {
    
    private final Connection connection;
    private final Type type;
    private final String prefix;
    private boolean isClosed = false;
    
    private final PreparedStatement insertUser;
    private final PreparedStatement insertPost;
    private final PreparedStatement insertTopic;
    private final PreparedStatement insertForum;
    private final PreparedStatement insertRedirectForum;
    private final PreparedStatement insertCategory;
    
    /**
     * Sets up a a database. A connection will be established and prepared
     * statements will be initialized. When this database is not going to be
     * used anymore, call the {@link close()} method to free up resources.
     * @param url the URL used to access the database, for instance
     * {@code postgresql://localhost/database?user=username&password=password}
     * or {@code mysql://localhost/database?user=username&password=password}
     * or {@code sqlite:database.db}. Do not include a preceding {@code jdbc:}.
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
        
        insertUser = connection.prepareStatement("INSERT " + type.ignore +
                "INTO " + prefix + "users (id, username, title, use_avatar) " +
                "VALUES(?, ?, ?, ?);");
        insertPost = connection.prepareStatement("INSERT " + type.ignore +
                "INTO " + prefix + "posts (id, poster, poster_id, message, " +
                "hide_smilies, posted, edited, edited_by, topic_id) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);");
        insertTopic = connection.prepareStatement("INSERT " + type.ignore +
                "INTO " + prefix + "topics (id, poster, subject, posted, " +
                "last_post, last_post_id, last_poster, num_views, " +
                "num_replies, closed, sticky, forum_id) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
        insertCategory = connection.prepareStatement("INSERT INTO " + prefix +
                "categories (cat_name, disp_position) VALUES(?, ?);");
        insertForum = connection.prepareStatement("INSERT " + type.ignore +
                "INTO " + prefix + "forums (id, forum_name, forum_desc, " +
                "num_topics, num_posts, last_post, last_post_id, " +
                "last_poster, sort_by, disp_position, cat_id) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
        insertRedirectForum = connection.prepareStatement("INSERT " + 
                type.ignore + "INTO " + prefix + "forums (forum_name, " +
                "forum_desc, redirect_url, disp_position, cat_id) " +
                "VALUES(?, ?, ?, ?, ?);");
    }
    
    @Override public synchronized void close() throws SQLException {
        isClosed = true;
        connection.close();
        insertPost.close();
        insertTopic.close();
        insertForum.close();
        insertRedirectForum.close();
        insertCategory.close();
    }
    
    /**
     * Inserts a user into the database.
     * @param user the user to insert
     * @throws SQLException if something goes wrong on the SQL side
     * @throws IllegalStateException if used after calling {@link close()}
     */
    public synchronized void insert(User user) throws SQLException {
        if (isClosed) {
            throw new IllegalStateException("Closed databases cannot be used.");
        }
        
        insertUser.setInt(1, user.getId());
        insertUser.setString(2, user.getUsername());
        insertUser.setString(3, user.getTitle());
        insertUser.setBoolean(4, user.getHasAvatar());
        insertUser.executeUpdate();
    }
    
    /**
     * Inserts a post into the database.
     * @param post the post to insert
     * @throws SQLException if something goes wrong on the SQL side
     * @throws IllegalStateException if used after calling {@link close()}
     */
    public synchronized void insert(Post post) throws SQLException {
        if (isClosed) {
            throw new IllegalStateException("Closed databases cannot be used.");
        }
        
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
     * Inserts a topic into the database.
     * @param topic the topic to insert
     * @throws SQLException if something goes wrong on the SQL side
     * @throws IllegalStateException if used after calling {@link close()}
     */
    public synchronized void insert(Topic topic) throws SQLException {
        if (isClosed) {
            throw new IllegalStateException("Closed databases cannot be used.");
        }
        
        // TODO make moved topics work
        if (topic.isMoved()) {
            throw new SQLException("A moved topic was not inserted because " +
                                   "the IDs of moved topics are unknown");
        }
        insertTopic.setInt(1, topic.getId());
        insertTopic.setString(2, topic.getPoster());
        insertTopic.setString(3, topic.getSubject());
        insertTopic.setLong(4, topic.getPosted());
        insertTopic.setLong(5, topic.getLastPosted());
        insertTopic.setInt(6, topic.getLastPostId());
        insertTopic.setString(7, topic.getLastPoster());
        insertTopic.setInt(8, topic.getNumViews());
        insertTopic.setInt(9, topic.getNumReplies());
        insertTopic.setBoolean(10, topic.getClosed());
        insertTopic.setBoolean(11, topic.getSticky());
        insertTopic.setInt(12, topic.getForumId());
        insertTopic.executeUpdate();
    }
    
    /**
     * Inserts a forum into the database.
     * @param forum the forum to insert
     * @throws SQLException if something goes wrong on the SQL side
     * @throws IllegalStateException if used after calling {@link close()}
     */
    public synchronized void insert(Forum forum) throws SQLException {
        if (isClosed) {
            throw new IllegalStateException("Closed databases cannot be used.");
        }
        
        if (forum.isRedirect()) {
            insertRedirectForum.setString(1, forum.getName());
            insertRedirectForum.setString(2, forum.getDescription());
            insertRedirectForum.setString(3, forum.getRedirectUrl());
            insertRedirectForum.setInt(4, forum.getDisplayPosition());
            insertRedirectForum.setInt(5, forum.getCategoryId());
            insertRedirectForum.executeUpdate();
        } else {
            insertForum.setInt(1, forum.getId());
            insertForum.setString(2, forum.getName());
            insertForum.setString(3, forum.getDescription());
            insertForum.setInt(4, forum.getNumTopics());
            insertForum.setInt(5, forum.getNumPosts());
            insertForum.setInt(6, forum.getLastPosted());
            insertForum.setInt(7, forum.getLastPostId());
            insertForum.setString(8, forum.getLastPoster());
            insertForum.setBoolean(9, forum.getSortByTopicStart());
            insertForum.setInt(10, forum.getDisplayPosition());
            insertForum.setInt(11, forum.getCategoryId());
            insertForum.executeUpdate();
        }
    }
    
    /**
     * Inserts a category into the database.
     * @param category the category to insert
     * @throws SQLException if something goes wrong on the SQL side
     * @throws IllegalStateException if used after calling {@link close()}
     */
    public synchronized void insert(Category category) throws SQLException {
        if (isClosed) {
            throw new IllegalStateException("Closed databases cannot be used.");
        }
        
        insertCategory.setString(1, category.getName());
        insertCategory.setInt(2, category.getDisplayPosition());
        insertCategory.executeUpdate();
    }
    
    /**
     * Creates all necessary tables. This includes indexes, the guest user and
     * the four default user groups. Don't use this if the tables already exist.
     * @throws SQLException if something goes wrong on the SQL side
     * @throws IllegalStateException if used after calling {@link close()}
     */
    public synchronized void createTables() throws SQLException {
        if (isClosed) {
            throw new IllegalStateException("Closed databases cannot be used.");
        }
        
        try (Statement statement = connection.createStatement()) {
            // Create tables
            
            statement.executeUpdate(
                    "CREATE TABLE " + prefix + "bans (" +
                    "id " + type.primaryKey + ", " +
                    "username VARCHAR(200), " +
                    "ip VARCHAR(255), " +
                    "email VARCHAR(50), " +
                    "message VARCHAR(255), " +
                    "expire " + type.integer + ", " +
                    "PRIMARY KEY (id)" +
                    ")" + type.myIASM + ";");
            
            statement.executeUpdate(
                    "CREATE TABLE " + prefix + "categories (" +
                    "id " + type.primaryKey + ", " +
                    "cat_name VARCHAR(80) NOT NULL DEFAULT 'New Category', " +
                    "disp_position " + type.integer + " NOT NULL DEFAULT 0, " +
                    "PRIMARY KEY (id)" +
                    ")" + type.myIASM + ";");
            
            statement.executeUpdate(
                    "CREATE TABLE " + prefix + "censoring (" +
                    "id " + type.primaryKey + ", " +
                    "search_for VARCHAR(60) NOT NULL DEFAULT '', " +
                    "replace_with VARCHAR(60) NOT NULL DEFAULT '', " +
                    "PRIMARY KEY (id)" +
                    ")" + type.myIASM + ";");
            
            statement.executeUpdate(
                    "CREATE TABLE " + prefix + "config (" +
                    "conf_name VARCHAR(255) NOT NULL DEFAULT '', " +
                    "conf_value TEXT, " +
                    "PRIMARY KEY (conf_name)" +
                    ")" + type.myIASM + ";");
            
            statement.executeUpdate(
                    "CREATE TABLE " + prefix + "forum_perms (" +
                    "group_id " + type.integer + " NOT NULL DEFAULT 0, " +
                    "forum_id " + type.integer + " NOT NULL DEFAULT 0, " +
                    "read_forum " + type.bool + " NOT NULL DEFAULT 1, " +
                    "read_replies " + type.bool + " NOT NULL DEFAULT 1, " +
                    "post_topics " + type.bool + " NOT NULL DEFAULT 1, " +
                    "PRIMARY KEY (group_id, forum_id)" +
                    ")" + type.myIASM + ";");
            
            statement.executeUpdate(
                    "CREATE TABLE " + prefix + "forums (" +
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
                    "CREATE TABLE " + prefix + "groups (" +
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
                    "CREATE TABLE " + prefix + "online (" +
                    "user_id " + type.integer + " NOT NULL DEFAULT 1, " +
                    "ident VARCHAR(200) NOT NULL DEFAULT '', " +
                    "logged " + type.integer + " NOT NULL DEFAULT 0, " +
                    "idle " + type.bool + " NOT NULL DEFAULT 0" +
                    ")" + type.memory + ";");
            
            statement.executeUpdate(
                    "CREATE TABLE " + prefix + "posts (" +
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
                    "CREATE TABLE " + prefix + "ranks (" +
                    "id " + type.primaryKey + ", " +
                    "rank VARCHAR(50) NOT NULL DEFAULT '', " +
                    "min_posts " + type.mediumInt + " NOT NULL DEFAULT 0, " +
                    "PRIMARY KEY (id)" +
                    ")" + type.myIASM + ";");
            
            statement.executeUpdate(
                    "CREATE TABLE " + prefix + "reports (" +
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
                    "CREATE TABLE " + prefix + "search_cache (" +
                    "id " + type.integer + " NOT NULL DEFAULT 0, " +
                    "ident VARCHAR(200) NOT NULL DEFAULT '', " +
                    "search_data TEXT, " +
                    "PRIMARY KEY (id)" +
                    ")" + type.myIASM + ";");
            
            statement.executeUpdate(
                    "CREATE TABLE " + prefix + "search_matches (" +
                    "post_id " + type.integer + " NOT NULL DEFAULT 0, " +
                    "word_id " + type.mediumInt + " NOT NULL DEFAULT 0, " +
                    "subject_match " + type.bool + " NOT NULL DEFAULT 0" +
                    ")" + type.myIASM + ";");
            
            switch (type) {
                case MYSQL:
                    statement.executeUpdate(
                    "CREATE TABLE " + prefix + "search_words (" +
                    "id MEDIUMINT(8) UNSIGNED NOT NULL AUTO_INCREMENT, " +
                    "word VARCHAR(20) BINARY NOT NULL DEFAULT '', " +
                    "PRIMARY KEY (word), " +
                    "KEY " + prefix + "search_words_id_idx (id)" +
                    ") ENGINE=MyISAM;");
                    break;
                case POSTGRESQL:
                    statement.executeUpdate(
                    "CREATE TABLE " + prefix + "search_words (" +
                    "id SERIAL, " +
                    "word VARCHAR(20) NOT NULL DEFAULT '', " +
                    "PRIMARY KEY (word)" +
                    ");");
                    break;
                case SQLITE:
                    statement.executeUpdate(
                    "CREATE TABLE " + prefix + "search_words (" +
                    "id INTEGER NOT NULL, " +
                    "word VARCHAR(20) NOT NULL DEFAULT '', " +
                    "PRIMARY KEY (id), " +
                    "UNIQUE (word)" +
                    ");");
                    break;
                default:
                    throw new SQLException("Found unsupported database type " +
                                           "when creating search_words");
            }
            
            statement.executeUpdate(
                    "CREATE TABLE " + prefix + "subscriptions (" +
                    "user_id " + type.integer + " NOT NULL DEFAULT 0, " +
                    "topic_id " + type.integer + " NOT NULL DEFAULT 0, " +
                    "PRIMARY KEY (user_id, topic_id)" +
                    ")" + type.myIASM + ";");
            
            statement.executeUpdate(
                    "CREATE TABLE " + prefix + "topics (" +
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
                    "CREATE TABLE " + prefix + "users (" +
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
            
            // Create indexes
            statement.executeUpdate("CREATE " + type.unique + "INDEX " + prefix +
                    "online_user_id_idx ON " + prefix + "online(user_id);");
            statement.executeUpdate("CREATE INDEX " + prefix +
                    "posts_topic_id_idx ON " + prefix + "posts(topic_id);");
            statement.executeUpdate("CREATE INDEX " + prefix +
                    "posts_multi_idx ON " + prefix + "posts(poster_id, topic_id);");
            statement.executeUpdate("CREATE INDEX " + prefix +
                    "reports_zapped_idx ON " + prefix + "reports(zapped);");
            statement.executeUpdate("CREATE INDEX " + prefix +
                    "search_matches_word_id_idx ON " + prefix + "search_matches(word_id);");
            statement.executeUpdate("CREATE INDEX " + prefix +
                    "search_matches_post_id_idx ON " + prefix + "search_matches(post_id);");
            statement.executeUpdate("CREATE INDEX " + prefix +
                    "topics_forum_id_idx ON " + prefix + "topics(forum_id);");
            statement.executeUpdate("CREATE INDEX " + prefix +
                    "topics_moved_to_idx ON " + prefix + "topics(moved_to);");
            statement.executeUpdate("CREATE INDEX " + prefix +
                    "users_registered_idx ON " + prefix + "users(registered);");
            statement.executeUpdate("CREATE INDEX " + prefix +
                    "users_username_idx ON " + prefix + "users(username);");
            statement.executeUpdate("CREATE INDEX " + prefix +
                    "search_cache_ident_idx ON " + prefix + "search_cache(ident);");
            if (type != Type.MYSQL) {       // For MySQL, this is already done
                statement.executeUpdate("CREATE INDEX " + prefix +
                        "search_words_id_idx ON " + prefix + "search_words(id);");
            }
            
            // Create the four default groups
            statement.executeUpdate("INSERT INTO " + prefix + "groups " +
                    "(g_id, g_title, g_user_title, g_read_board, " +
                    "g_post_replies, g_post_topics, g_post_polls, " +
                    "g_edit_posts, g_delete_posts, g_delete_topics, " +
                    "g_set_title, g_search, g_search_users, " +
                    "g_edit_subjects_interval, g_post_flood, g_search_flood) " +
                    "VALUES(1, 'Administrators', 'Administrator', 1, 1, 1, " +
                    "1, 1, 1, 1, 1, 1, 1, 0, 0, 0);");
            statement.executeUpdate("INSERT INTO " + prefix + "groups " +
                    "(g_id, g_title, g_user_title, g_read_board, " +
                    "g_post_replies, g_post_topics, g_post_polls, " +
                    "g_edit_posts, g_delete_posts, g_delete_topics, " +
                    "g_set_title, g_search, g_search_users, " +
                    "g_edit_subjects_interval, g_post_flood, g_search_flood) " +
                    "VALUES(2, 'Moderators', 'Moderator', 1, 1, 1, 1, 1, 1, " +
                    "1, 1, 1, 1, 0, 0, 0);");
            statement.executeUpdate("INSERT INTO " + prefix + "groups " +
                    "(g_id, g_title, g_user_title, g_read_board, " +
                    "g_post_replies, g_post_topics, g_post_polls, " +
                    "g_edit_posts, g_delete_posts, g_delete_topics, " +
                    "g_set_title, g_search, g_search_users, " +
                    "g_edit_subjects_interval, g_post_flood, g_search_flood) " +
                    "VALUES(3, 'Guest', NULL, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, " +
                    "0, 0, 0);");
            statement.executeUpdate("INSERT INTO " + prefix + "groups " +
                    "(g_id, g_title, g_user_title, g_read_board, " +
                    "g_post_replies, g_post_topics, g_post_polls, " +
                    "g_edit_posts, g_delete_posts, g_delete_topics, " +
                    "g_set_title, g_search, g_search_users, " +
                    "g_edit_subjects_interval, g_post_flood, g_search_flood) " +
                    "VALUES(4, 'Members', NULL, 1, 1, 1, 1, 1, 1, 1, 0, 1, " +
                    "1, 300, 60, 30);");
            
            // Create guest user
            statement.executeUpdate("INSERT INTO " + prefix + "users " +
                    "(id, group_id, username, password, email) " +
                    "VALUES(1, 3, 'Guest', 'Guest', 'Guest');");
        }
    }
    
    private enum Type {
        MYSQL("INT(10) UNSIGNED", "MEDIUMINT(8) UNSIGNED", "SMALLINT(6)",
              "TINYINT(3) UNSIGNED", "TINYINT(1)", "FLOAT",
              "INT(10) UNSIGNED NOT NULL AUTO_INCREMENT", "UNIQUE ",
              " ENGINE=MyISAM", " ENGINE=MEMORY", "IGNORE "),
        POSTGRESQL("INT", "INT", "SMALLINT",
                   "SMALLINT", "SMALLINT", "REAL",
                   "SERIAL", "",
                   "", "", ""),   // TODO ignore existing records
        SQLITE("INTEGER", "INTEGER", "INTEGER",
               "INTEGER", "INTEGER", "FLOAT",
               "INTEGER NOT NULL", "",
               "", "", "ON CONFLICT IGNORE ");
        
        public final String integer;
        public final String mediumInt;
        public final String smallInt;
        public final String tinyInt;
        public final String bool;
        public final String real;
        public final String primaryKey;
        public final String unique;
        public final String myIASM;
        public final String memory;
        public final String ignore;
        
        private Type(String integer, String mediumInt, String smallInt,
                     String tinyInt, String bool, String real,
                     String primaryKey, String unique,
                     String myIASM, String memory, String ignore) {
            this.integer = integer;
            this.mediumInt = mediumInt;
            this.smallInt = smallInt;
            this.tinyInt = tinyInt;
            this.bool = bool;
            this.real = real;
            this.primaryKey = primaryKey;
            this.unique = unique;
            this.myIASM = myIASM;
            this.memory = memory;
            this.ignore = ignore;
        }
    }
    
}
