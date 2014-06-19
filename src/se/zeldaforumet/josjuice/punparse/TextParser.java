package se.zeldaforumet.josjuice.punparse;

import org.jsoup.nodes.Element;

/**
 * Contains various static methods for parsing text.
 * @author JosJuice
 */
public class TextParser {
    
    /**
     * Converts a message in HTML (such as a post or signature) into BBCode.
     * @param element a {@code .postmsg} or {@code postsignature} element
     * @return the raw text of the message
     */
    public static String parseMessage(Element element) {
        Element elem = element.clone();
        
        // Remove the unneeded hr element at the beginning of signatures
        if (elem.hasClass("postsignature")) {
            Element hr = elem.getElementsByTag("hr").first();
            if (hr != null) {
                hr.remove();
            }
        }
        
        // Replace newlines with a temporary private use character
        final char tempChar = '\uFDD0';
        final String tempString = String.valueOf(tempChar);
        for (Element br : elem.getElementsByTag("br")) {
            br.after(tempString).remove();
        }
        
        // Return the text, inserting newlines
        return elem.text().replace(tempChar, '\n');
    }
    
    /**
     * Gets the value of a field in a URL query string.
     * Example: Looking for the field "id" in "id=37&p=1" will return "37".
     * @param url a full URL or the query string part of a URL
     * @param field the field to get the value of
     * @return the value of the field, or {@code null} if no value exists
     */
    public static String getQueryValue(String url, String field) {
        int queryStart = url.indexOf("?") + 1;  // Query starts after the ?, or
                                                // at the start if there is no ?
        int queryEnd = url.indexOf("#");    // Query ends at the #,
        if (queryEnd == -1) {               // or at the end if there is no #
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
