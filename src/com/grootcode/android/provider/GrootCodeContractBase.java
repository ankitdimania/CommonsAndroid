package com.grootcode.android.provider;

import android.net.Uri;
import android.provider.ContactsContract;

public class GrootCodeContractBase {

    /**
     * Special value for {@link SyncColumns#UPDATED} indicating that an entry
     * has never been updated, or doesn't exist yet.
     */
    public static final long UPDATED_NEVER = -2;

    /**
     * Special value for {@link SyncColumns#UPDATED} indicating that the last
     * update time is unknown, usually when inserted from a local file source.
     */
    public static final long UPDATED_UNKNOWN = -1;

    public interface SyncColumns {
        /** Last time this entry was updated or synchronized. */
        String UPDATED = "updated";
    }

    public static String CONTENT_AUTHORITY;
    public static Uri BASE_CONTENT_URI;

    public static void init(String contentAuthority, Uri baseContentUri) {
        CONTENT_AUTHORITY = contentAuthority;
        BASE_CONTENT_URI = baseContentUri;
    }

    public static Uri addCallerIsSyncAdapterParameter(Uri uri) {
        return uri.buildUpon().appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true").build();
    }

    public static boolean hasCallerIsSyncAdapterParameter(Uri uri) {
        return Boolean.parseBoolean(uri.getQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER));
    }
}
