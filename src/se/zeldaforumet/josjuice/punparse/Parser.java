package se.zeldaforumet.josjuice.punparse;

/**
 * Contains various methods for parsing text.
 * @author Jos
 */
public class Parser {
    
    /**
     * Gets the value of a field in a URL query string.
     * Example: Looking for the field "id" in "id=37&p=1" will return "37".
     * @param url a full URL or the query string part of a URL
     * @param field the field to get the value of
     * @return the value of the field, or <code>null</code> if no value exists
     */
    public static String getUrlQueryValue(String url, String field) {
        String query;
        int questionMark = url.indexOf("?");
        if (questionMark == -1) {   // The input is only the query string
            query = url;
        } else {                    // The input is a full URL
            query = url.substring(questionMark + 1);    // Get only query string
        }
        
        String[] parameters = query.split("&");
        for (String parameter : parameters) {
            String[] parameterParts = parameter.split("=");
            if (parameterParts.length == 2) {   // Ignore invalid parameters
                if (parameterParts[0].equals(field)) {
                    return parameterParts[1];
                }
            }
        }
        
        return null;            // If this is reached, the field was not found
    }
    
}
