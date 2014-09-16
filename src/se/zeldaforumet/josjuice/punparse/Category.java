package se.zeldaforumet.josjuice.punparse;

import org.jsoup.nodes.Element;

/**
 * Represents a record in the PunBB 'categories' table. Immutable.
 * @author JosJuice
 */
public final class Category {
    
    // All of the following variables correspond to database columns.
    private final int id;
    private final String name;
    private final int displayPosition;
    
    /**
     * Constructs a {@code Category}.
     * @param element HTML element representing a category.
     * The element should always have the {@code .blocktable} class.
     * @param displayPosition categories will be sorted by this when displayed
     * @throws IllegalArgumentException if required parts of HTML are missing
     */
    public Category(Element element, int displayPosition)
            throws IllegalArgumentException {
        try {
            name = element.getElementsByTag("h2").first().text();
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Couldn't get category name.",e);
        }
        
        // TODO use a more accurate value when database already has categories
        this.displayPosition = displayPosition;
        
        // TODO this isn't the real ID. Might cause problems with existing data
        id = displayPosition + 1;
    }
    
    /**
     * @return Category ID
     */
    public int getId() {
        return id;
    }
    
    /**
     * @return The name of the category
     */
    public String getName() {
        return name;
    }
    
    /**
     * @return The list position this category will be displayed at
     */
    public int getDisplayPosition() {
        return displayPosition;
    }
    
}
