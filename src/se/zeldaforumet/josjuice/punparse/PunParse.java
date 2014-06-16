package se.zeldaforumet.josjuice.punparse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

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
        try {
            Database database = new Database(args[1], null);
            if (!append) {
                System.out.println("Creating tables...");
                database.createTables();
            }
            
            System.out.println("Finding files to parse...");
            File directory = new File(args[0]);
            LinkedList<File> files = getFilesInDirectory(directory);
            
            /*
             * 16 is just an arbitrary size for the queue. It can be adjusted to
             * something else without any trouble. Just don't make it way too
             * large - we don't want to be able to fill RAM with documents.
             */
            System.out.println("Parsing files...");
            ArrayBlockingQueue<ParseTask> queue = new ArrayBlockingQueue<>(16);
            UserInterface ui = new UserInterface(files.size());
            ParseThread parseThread = new ParseThread(queue, database, ui);
            parseThread.start();
            filesToParseTasks(files, queue, ui);
            parseThread.interrupt();
        } catch (SQLException e) {
            System.err.println("SQL error: " + e.getLocalizedMessage());
        }
    }
    
    /**
     * Finds all files in a folder, including subfolders.
     * @param directory The directory to find files in. If this is not a
     * directory, it will be treated as a directory containing no files.
     * @return a queue of {@code File} objects for all files in the directory
     */
    private static LinkedList<File> getFilesInDirectory(File directory) {
        LinkedList<File> result = new LinkedList<>();
        /*
         * Note: listFiles takes several minutes to run if there are many files
         * (300000 or so). Is it like this on OSes other than Windows too?
         */
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                result.addAll(getFilesInDirectory(file));
            } else {
                result.add(file);
            }
        }
        return result;
    }
    
    /**
     * Loads files to create {@link ParseTask}s that are added to a queue using
     * blocking operations. This method will hang unless there is a thread
     * emptying the queue while it is being filled, or unless there are fewer
     * files than there is empty space in the queue.
     * @param files the files to load
     * @param queue the {@code BlockingQueue} to add {@code ParseTask}s to
     */
    private static void filesToParseTasks(Collection<File> files,
                                         BlockingQueue<ParseTask> queue,
                                         UserInterface ui) {
        for (File file : files) {
            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                ParseTask parseTask = new ParseTask(bytes, file.getName());
                queue.put(parseTask);
            } catch (IOException e) {
                if (ui != null) {
                    ui.addToProgress(file.getName(), "Couldn't read file.");
                }
            } catch (InterruptedException e) {}
        }
    }
    
}
