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
public class PunParse {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Find optional arguments
        boolean append = false;
        for (String arg : args) {
            if (arg.equals("--append")) {
                append = true;
            }
        }
        
        // Do the work
        System.out.println("Connecting to SQL database...");
        try (Database database = new Database(args[1], null)) {
            if (!append) {
                System.out.println("Creating tables...");
                database.createTables();
            }

            System.out.println("Finding files to parse...");
            File directory = new File(args[0]);
            ArrayList<File> files = getFilesInDirectory(directory);

            System.out.println("Parsing files...");
            UserInterface ui = new UserInterface(files.size());
            int threads = Runtime.getRuntime().availableProcessors() + 1;
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            for (File file : files) {
                executor.execute(new ParseTask(file, database, ui));
            }

            // Wait for threads to finish before closing database connection
            executor.shutdown();
            boolean isDone = false;
            while (!isDone) {
                try {
                    isDone = executor.awaitTermination(1, TimeUnit.DAYS);
                } catch (InterruptedException e) {}
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
        /*
         * Note: listFiles takes several minutes to run if there are many files
         * (300000 or so). Is it like this on OSes other than Windows too?
         */
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
