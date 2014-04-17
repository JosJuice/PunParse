package se.zeldaforumet.josjuice.punparse;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Connects to an SQL database and inserts data.
 * @author Jos
 */
public class Database implements AutoCloseable {
    
    private Connection connection;
    
    /**
     * Sets up a connection to a database.
     * @param url the URL to the database, for instance
     * <code>mysql://localhost/test?user=username&password=password</code>
     * (do not include a preceding <code>jdbc:</code>)
     * @throws SQLException if something goes wrong on the SQL side
     */
    public Database(String url) throws ClassNotFoundException, SQLException {
        connection = DriverManager.getConnection("jdbc:" + url);
    }
    
    @Override public void close() throws SQLException {
        connection.close();
    }
    
}
