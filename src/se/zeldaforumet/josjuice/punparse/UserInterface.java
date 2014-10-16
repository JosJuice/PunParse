package se.zeldaforumet.josjuice.punparse;

import java.util.Collection;

/**
 * Displays progress and other information to the user using command-line
 * output. Thread safe.
 * @author Jos
 */
public final class UserInterface {
    
    private int progress = 0;
    private int goal = 0;
    
    /**
     * Creates a {@code UserInterface}. The goal and progress will start at 0.
     */
    public UserInterface() {}
    
    /**
     * Creates a {@code UserInterface} with a specified goal. The progress will
     * start at 0.
     * @param goal the goal to use
     */
    public UserInterface(int goal) {
        this.goal = goal;
    }
    
    /**
     * Adds 1 to the progress, then displays the progress and goal.
     * @param name A way of identifying the last processed item. This will be
     * displayed to the user.
     */
    public synchronized void addToProgress(String name) {
        progress++;
        System.out.println("Processed " + progress + "/" + goal + ": " + name);
    }
    
    /**
     * Adds 1 to the progress, then displays the progress and goal.
     * @param name A way of identifying the last processed item. This will be
     * displayed to the user.
     * @param errors Errors to display that are related to the last item.
     * If the {@code Collection} is empty, the item is treated as error-free.
     */
    public synchronized void addToProgress(String name,
                                           Collection<String> errors) {
        if (errors == null || errors.isEmpty()) {
            addToProgress(name);
            return;
        }
        progress++;
        for (String error : errors) {
            System.err.println(error);
        }
        System.err.println(errors.size() + " errors occured when processing " +
                           progress + "/" + goal + ": " + name);
    }
    
    /**
     * Adds 1 to the progress, then displays the progress and goal.
     * @param name A way of identifying the last processed item. This will be
     * displayed to the user.
     * @param error An error to display that is related to the last item.
     */
    public synchronized void addToProgress(String name, String error) {
        if (error == null) {
            addToProgress(name);
            return;
        }
        progress++;
        System.err.println(error);
        System.err.println("An error occured when processing " +
                           progress + "/" + goal + ": " + name);
    }
    
    /**
     * Adds an amount to the goal.
     * @param amount the amount to add
     */
    public synchronized void addToGoal(int amount) {
        goal += amount;
    }
    
    /**
     * Displays a message to the user.
     * @param message the message to display
     */
    public synchronized void print(String message) {
        System.out.println(message);
    }
    
    /**
     * Displays an error message to the user.
     * @param message the message to display
     */
    public synchronized void printError(String message) {
        System.err.println(message);
    }
    
}
