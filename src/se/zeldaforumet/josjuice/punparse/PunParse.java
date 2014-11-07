package se.zeldaforumet.josjuice.punparse;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Parses data from PunBB HTML output to an SQL database.
 * @author JosJuice
 */
public final class PunParse {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Find optional arguments
        boolean append = false;
        String dateFormat = "yyyy-MM-dd HH:mm:ss";
        for (String arg : args) {
            if (arg.equals("--append")) {
                append = true;
            }
            if (arg.startsWith("--dateformat=")) {
                dateFormat = arg.substring(13);
            }
        }
        DateParser dateParser = new DateParser(dateFormat);
        
        // Do the work
        System.out.println("Connecting to SQL database...");
        try (Database database = new Database(args[1], null)) {
            IdMappings idMappings = new IdMappings();
            if (append) {
                // TODO load IdMappings from database
            } else {
                System.out.println("Creating tables...");
                database.createTables();
            }

            System.out.println("Finding files to parse...");
            File directory = new File(args[0]);
            ArrayList<File> files = getFilesInDirectory(directory);

            if (files.size() <= 0) {
                System.out.println("No files were found.");
            } else {
                System.out.println("Parsing files...");
                UserInterface ui = new UserInterface(files.size());
                int threads = Runtime.getRuntime().availableProcessors() + 1;
                if (threads > files.size()) {
                    threads = files.size();
                }
                ExecutorService es = Executors.newFixedThreadPool(threads);
                try {
                    for (File file : files) {
                        es.execute(new ParseTask(file, database, ui, idMappings,
                                   dateParser));
                    }
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid date format: " + dateFormat);
                }

                // Wait for threads to finish
                es.shutdown();
                boolean isDone = false;
                while (!isDone) {
                    try {
                        isDone = es.awaitTermination(1, TimeUnit.DAYS);
                    } catch (InterruptedException e) {}
                }
                
                // Cleanup
                idMappings.submitAllQueuedPosts(0, database);
            }
        } catch (SQLException e) {
            System.err.println("SQL error: " + e.getLocalizedMessage());
        }
    }
    
    /**
     * Finds all files in a folder, including subfolders.
     * @param directory The directory to find files in. If this is not a
     * directory, it will be treated as a directory containing no files.
     * @return a queue of {@link File} objects for all files in the directory
     */
    private static ArrayList<File> getFilesInDirectory(File directory) {
        // Note: listFiles takes minutes to run if there are 100000s of files
        File[] files = directory.listFiles();
        ArrayList<File> result = new ArrayList<>(files.length);
        for (File file : files) {
            if (file.isDirectory()) {
                result.addAll(getFilesInDirectory(file));
            } else {
                result.add(file);
            }
        }
        return result;
    }
    
}
