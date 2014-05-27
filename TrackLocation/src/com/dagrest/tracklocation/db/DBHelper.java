package com.dagrest.tracklocation.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper{

	// http://blog.lemberg.co.uk/concurrent-database-access
    public DBHelper(Context context) {
        super(context, DBConst.DATABASE_NAME, null, DBConst.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (DBConst.IS_DEBUG_LOG_ENABLED)
            Log.i(DBConst.LOG_TAG_DB, "new create");
        try {
            db.execSQL(DBConst.TABLE_CONTACT_CREATE);
            db.execSQL(DBConst.TABLE_DEVICE_CREATE);
            db.execSQL(DBConst.TABLE_CONTACT_DEVICE_CREATE);
            db.execSQL(DBConst.TABLE_JOIN_REQUEST_CREATE);
            db.execSQL(DBConst.TABLE_PERMISSIONS_CREATE);

        } catch (Exception exception) {
            if (DBConst.IS_DEBUG_LOG_ENABLED)
                Log.i(DBConst.LOG_TAG_DB, "Exception onCreate() exception");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (DBConst.IS_DEBUG_LOG_ENABLED)
            Log.w(DBConst.LOG_TAG_DB, "Upgrading database from version" + oldVersion
                    + "to" + newVersion + "...");

        for (String table : DBConst.TABLES_LIST) {
            db.execSQL("DROP TABLE IF EXISTS " + table);
        }
        onCreate(db);
    }
}
