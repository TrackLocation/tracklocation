package com.dagrest.tracklocation.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper{

                                
    public DBHelper(Context context) {
        super(context, DBConst.DATABASE_NAME, null, DBConst.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (DBConst.IS_DEBUG_LOG_ENABLED)
            Log.i(DBConst.DB_LOG_TAG, "new create");
        try {
            //db.execSQL(USER_MAIN_CREATE);
            db.execSQL(DBConst.USER_CREATE);
            db.execSQL(DBConst.DEVICE_CREATE);

        } catch (Exception exception) {
            if (DBConst.IS_DEBUG_LOG_ENABLED)
                Log.i(DBConst.DB_LOG_TAG, "Exception onCreate() exception");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (DBConst.IS_DEBUG_LOG_ENABLED)
            Log.w(DBConst.DB_LOG_TAG, "Upgrading database from version" + oldVersion
                    + "to" + newVersion + "...");

        for (String table : DBConst.TABLES_LIST) {
            db.execSQL("DROP TABLE IF EXISTS " + table);
        }
        onCreate(db);
    }
}
