package se.zeldaforumet.josjuice.punparse;

/**
 * Represents a task of parsing that needs to be carried out.
 * @author JosJuice
 */
public final class ParseTask {
    
    private final byte[] html;  // Can't be a String because charset is unknown
    private final String name;
    
    /**
     * Creates a {@code ParseTask}.
     * @param html The HTML that needs to be parsed
     * @param name A string representing this task to the user. Typically, the
     * file name or the URL the document is read from is used.
     */
    public ParseTask(byte[] html, String name) {
        this.html = html;
        this.name = name;
    }
    
    /**
     * @return the HTML to parse
     */
    public byte[] getHtml() {
        return html;
    }
    
    /**
     * @return the name supplied when creating this object
     */
    public String getName() {
        return name;
    }
    
    /**
     * @return the name supplied when creating this object
     */
    @Override public String toString() {
        return name;
    }
}
