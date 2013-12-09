package com.grootcode.android.util;

import java.util.ArrayList;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;

public class DatabaseUtils {

    public static ContentProviderResult[] applyBatch(Context context, String contentAuthority,
            ArrayList<ContentProviderOperation> batch) {
        final ContentResolver resolver = context.getContentResolver();
        try {
            // Apply all queued up batch operations for local data.
            return resolver.applyBatch(contentAuthority, batch);
        } catch (RemoteException e) {
            throw new RuntimeException("Problem applying batch operation", e);
        } catch (OperationApplicationException e) {
            throw new RuntimeException("Problem applying batch operation", e);
        }
    }

    public static ContentProviderResult[] applyIndependentOperationsBatch(Context context, String contentAuthority,
            ArrayList<ContentProviderOperation> operations) {
        try {
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            ContentProvider contentProvider = context.getContentResolver()
                    .acquireContentProviderClient(contentAuthority).getLocalContentProvider();
            for (int i = 0; i < numOperations; ++i) {
                results[i] = operations.get(i).apply(contentProvider, results, i);
            }
            return results;
        } catch (OperationApplicationException e) {
            throw new RuntimeException("Problem applying batch operation", e);
        }
    }
}
