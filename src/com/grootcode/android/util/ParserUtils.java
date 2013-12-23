package com.grootcode.android.util;

import java.util.regex.Pattern;

import android.content.ContentProvider;
import android.net.Uri;
import android.text.format.Time;

/**
 * Various utility methods for Parsing
 */
public class ParserUtils {
    /** Used to sanitize a string to be {@link Uri} safe. */
    private static final Pattern sSanitizePattern = Pattern.compile("[^a-z0-9-_]");

    private static final Time sTime = new Time();

    /**
     * Sanitize the given string to be {@link Uri} safe for building
     * {@link ContentProvider} paths.
     */
    public static String sanitizeId(String input) {
        if (input == null) {
            return null;
        }
        return sSanitizePattern.matcher(input.replace("+", "plus").toLowerCase()).replaceAll("");
    }

    /**
     * Parse the given string as a RFC 3339 timestamp, returning the value as
     * milliseconds since the epoch.
     */
    public static long parseTime(String time) {
        sTime.parse3339(time);
        return sTime.toMillis(false);
    }
}
