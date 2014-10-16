package se.zeldaforumet.josjuice.punparse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

/**
 * Parses date strings to Unix timestamps. Thread safe.
 * @author JosJuice
 */
public final class DateParser {
    
    private final SimpleDateFormat dateFormat;
    
    /**
     * Creates a {@code DateParser}.
     * @param pattern the {@link SimpleDateFormat} pattern to use for parsing
     * @throws IllegalArgumentException if the pattern is invalid
     * @throws NullPointerException if the pattern is {@code null}
     */
    public DateParser(String pattern) {
        dateFormat = new SimpleDateFormat(pattern);
    }
    
    /**
     * Parses a date that matches this {@code DateParser}'s pattern.
     * @return date as a Unix timestamp (seconds since 1970-01-01 00:00:00 UTC)
     * @throws ParseException if the date cannot be parsed
     */
    public synchronized long parse(String date) throws ParseException {
        // TODO time zones
        long dateMilliseconds = dateFormat.parse(date).getTime();
        return TimeUnit.MILLISECONDS.toSeconds(dateMilliseconds);
    }
    
}
