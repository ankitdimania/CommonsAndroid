package com.grootcode.android.provider;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

public abstract class GrootCodeSQLiteOpenHelper extends SQLiteOpenHelper {

    private static String DATABASE_NAME;
    protected final Context mContext;

    public GrootCodeSQLiteOpenHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
        DATABASE_NAME = name;
        mContext = context;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public GrootCodeSQLiteOpenHelper(Context context, String name, CursorFactory factory, int version,
            DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
        DATABASE_NAME = name;
        mContext = context;
    }

    public static void deleteDatabase(Context context) {
        context.deleteDatabase(DATABASE_NAME);
    }
}
