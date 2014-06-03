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
        int queryStart = url.indexOf("?") + 1; // Query starts after the ?, or
                                               // at the start if there is no ?
        int queryEnd = url.indexOf("#") - 1; // Query ends before the #,
        if (queryEnd == -2) {                // or at the end if there is no #
            queryEnd = url.length();
        }
        String query = url.substring(queryStart, queryEnd);
        
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
