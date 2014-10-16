package se.zeldaforumet.josjuice.punparse;

import java.util.Collection;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

/**
 * Contains various static methods for parsing text.
 * @author JosJuice
 */
public final class TextParser {
    
    /**
     * Converts a message (such as a post or signature) from HTML to BBCode.
     * @param element a {@code .postmsg} or {@code postsignature} element
     * @return the message in BBCode
     */
    public static String parseMessage(Element element) {
        return parseMessage(new StringBuilder(), element).toString();
    }
    
    /**
     * Converts a message or a part of a message from HTML to BBCode.
     * @param sb a StringBuilder to add the converted text to
     * @param elem a {@code .postmsg} or {@code postsignature} element,
     * or a part of one
     * @return the StringBuilder that was used as a parameter
     */
    private static StringBuilder parseMessage(StringBuilder sb, Element elem) {
        // TODO more types of BBCode
        switch (elem.tagName()) {
            case "a":
                sb.append("[url=").append(elem.attr("href")).append("]");
                parseMessage(sb, elem.childNodes());
                sb.append("[/url]");
                break;
            case "b":
                sb.append("[b]");
                parseMessage(sb, elem.childNodes());
                sb.append("[/b]");
                break;
            case "blockquote":
                String quoteAuthor = null;
                Element incqbox = elem.children().first();
                if (incqbox != null) {
                    Element h4 = incqbox.children().first();
                    if (h4 != null && h4.tagName().equals("h4")) {
                        quoteAuthor = h4.text();
                    }
                }
                sb.append("[quote");
                if (quoteAuthor != null) {
                    sb.append("=");
                    sb.append(quoteAuthor);     // TODO get rid of " wrote:"
                }
                sb.append("]");
                parseMessage(sb, elem.childNodes());
                sb.append("[/quote]");
                break;
            case "br":
                sb.append('\n');
                break;
            case "div":
                if (elem.hasClass("codebox")) {
                    sb.append("[code]");
                    parseMessage(sb, elem.childNodes());
                    sb.append("[/code]");
                } else {
                    parseMessage(sb, elem.childNodes());
                }
                break;
            case "h4":
                // Handled by case "blockquote"
                break;
            case "i":
                sb.append("[i]");
                parseMessage(sb, elem.childNodes());
                sb.append("[/i]");
                break;
            case "img":
                if (elem.hasClass("postimg") || elem.hasClass("sigimage")) {
                    sb.append("[img]");
                    sb.append(elem.attr("src"));
                    sb.append("[/img]");
                } else { // Smiley
                    // TODO in vanilla PunBB this is a filename, not smiley text
                    sb.append(elem.attr("alt"));
                }
                break;
            case "span":
                if (elem.hasClass("bbu")) {
                    sb.append("[u]");
                    parseMessage(sb, elem.childNodes());
                    sb.append("[/u]");
                } else {
                    parseMessage(sb, elem.childNodes());
                }
                break;
            default:
                parseMessage(sb, elem.childNodes());
                break;
        }
        return sb;
    }
    
    /**
     * Converts parts of a message from HTML to BBCode.
     * @param sb a StringBuilder to add the converted text to
     * @param nodes parts of {@code .postmsg} or {@code postsignature} elements
     * @return the StringBuilder that was used as a parameter
     */
    private static StringBuilder parseMessage(StringBuilder sb,
                                              Collection<Node> nodes) {
        for (Node node : nodes) {
            if (node instanceof TextNode) {
                sb.append(((TextNode) node).text());
            } else if (node instanceof Element) {
                parseMessage(sb, (Element) node);
            }
        }
        return sb;
    }
    
    /**
     * @param element a {@code .postmsg} or {@code postsignature} element
     * @return {@code true} if there is at least one smiley in the element
     */
    public static boolean containsSmilies(Element element) {
        for (Element img : element.getElementsByTag("img")) {
            if (!img.hasClass("postimg")) {
                return true;
            }
        }
        return false;
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
