package se.zeldaforumet.josjuice.punparse;

/**
 * Represents a task of parsing that needs to be carried out.
 * @author JosJuice
 */
public final class ParseTask {
    
    private final byte[] html;  // Can't be a String because charset is unknown
    private final int taskNumber;
    private final int totalTasks;
    private final String name;
    
    /**
     * Creates a {@code ParseTask}.
     * @param html The HTML that needs to be parsed
     * @param name A string representing this task to the user. Typically, the
     * file name or the URL the document is read from is used.
     */
    public ParseTask(byte[] html, String name) {
        this.html = html;
        this.taskNumber = 0;
        this.totalTasks = 0;
        this.name = name;
    }
    
    /**
     * Creates a numbered {@code ParseTask}.
     * @param html The HTML that needs to be parsed
     * @param taskNumber The number of this task (e.g. 1 for the first task).
     * Must be larger than 0.
     * @param totalTasks The total number of tasks in the set of tasks that this
     * task is part of. Must be larger than 0.
     * @param name A string representing this task to the user. Typically, the
     * file name or the URL the document is read from is used.
     */
    public ParseTask(byte[] html, int taskNumber, int totalTasks, String name) {
        this.html = html;
        this.taskNumber = taskNumber;
        this.totalTasks = totalTasks;
        this.name = name;
    }
    
    /**
     * @return the HTML to parse
     */
    public byte[] getHtml() {
        return html;
    }
    
    /**
     * @return A string that is intended to uniquely represent this task to the
     * user. If this task is not numbered in a valid manner, only the name is
     * returned. Otherwise, a string looking like this is returned:
     * {@code [taskNumber]/[totalTasks]: [name]}
     */
    @Override public String toString() {
        if (taskNumber > 0 && totalTasks > 0) {
            return taskNumber + "/" + totalTasks + ": " + name;
        } else {
            return name;
        }
    }
}
