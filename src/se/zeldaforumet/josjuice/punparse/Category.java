package se.zeldaforumet.josjuice.punparse;

import org.jsoup.nodes.Element;

/**
 * @author JosJuice
 */
public final class Category {
    
    private final Forum[] forums;
    
    // All of the following variables correspond to database columns.
    private final String name;
    private final int displayPosition;
    
    /**
     * Constructs a {@code Category}.
     * @param element HTML element representing a category.
     * The element should always have the {@code .blocktable} class.
     * @param displayPosition categories will be sorted by this when displayed
     * @throws IllegalArgumentException if required parts of HTML are missing
     */
    public Category(Element element, int displayPosition) {
        try {
            name = element.getElementsByTag("h2").first().text();
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Couldn't get category name.",e);
        }
        
        // TODO use a more accurate value when database already has categories
        this.displayPosition = displayPosition;
        
        // TODO handle forums
        forums = new Forum[0];
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
