package com.grootcode.android.util;

import android.content.Context;
import android.text.format.DateUtils;
import android.text.format.Time;

public class TimeUtils {

    // private static final int SECOND = 1000;
    // private static final int MINUTE = 60 * SECOND;
    // private static final int HOUR = 60 * MINUTE;
    // private static final int DAY = 24 * HOUR;

    public static CharSequence getTimeDiff(long time, Context ctx) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = UIUtils.getCurrentTime(ctx);
        if (time <= 0) {
            return null;
        }

        return DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);
        // if (now > time) {
        // final long diff = now - time;
        //
        // if (diff < MINUTE) {
        // return "just now";
        // } else if (diff < 2 * MINUTE) {
        // return "a minute ago";
        // } else if (diff < 50 * MINUTE) {
        // return diff / MINUTE + " minutes ago";
        // } else if (diff < 90 * MINUTE) {
        // return "an hour ago";
        // } else if (diff < 24 * HOUR) {
        // return diff / HOUR + " hours ago";
        // } else if (diff < 48 * HOUR) {
        // return "yesterday";
        // } else {
        // return diff / DAY + " days ago";
        // }
        // } else {
        // final long diff = time - now;
        //
        // if (diff < MINUTE) {
        // return "in a moment";
        // } else if (diff < 2 * MINUTE) {
        // return "in a minute";
        // } else if (diff < 50 * MINUTE) {
        // return "in" + diff / MINUTE + " minutes";
        // } else if (diff < 90 * MINUTE) {
        // return "in an hour";
        // } else if (diff < 24 * HOUR) {
        // return "in " + diff / HOUR + " hours";
        // } else if (diff < 48 * HOUR) {
        // return "tomorrow";
        // } else {
        // return "in" + diff / DAY + " days";
        // }
        // }
    }

    /**
     * The difference between this and {@link DateUtils#getRelativeTimeSpanString(Context, long)} is
     * the this returns alphabetic date if year is not same.
     * {@link DateUtils#getRelativeTimeSpanString(Context, long)} returns numeric date if year is
     * not same.
     * 
     * @param context
     * @param when
     * @param fullFormat
     * @return
     */
    public static String formatTimeStampString(Context context, long when, boolean fullFormat) {
        Time then = new Time();
        then.set(when);
        Time now = new Time();
        now.setToNow();

        // Basic settings for formatDateTime() we want for all cases.
        int format_flags = DateUtils.FORMAT_NO_NOON_MIDNIGHT | DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_CAP_AMPM;

        // If the message is from a different year, show the date and year.
        if (then.year != now.year) {
            format_flags |= DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_DATE;
        } else if (then.yearDay != now.yearDay) {
            // If it is from a different day than today, show only the date.
            format_flags |= DateUtils.FORMAT_SHOW_DATE;
        } else {
            // Otherwise, if the message is from today, show the time.
            format_flags |= DateUtils.FORMAT_SHOW_TIME;
        }

        // If the caller has asked for full details, make sure to show the date and time no matter
        // what we've determined above (but still make showing the year only happen if it is a
        // different year from today).
        if (fullFormat) {
            format_flags |= (DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
        }

        return DateUtils.formatDateTime(context, when, format_flags);
    }

}
