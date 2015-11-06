package com.doat.tracklocation.db;

import java.util.concurrent.atomic.AtomicInteger;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBManager {

    private AtomicInteger mOpenCounter = new AtomicInteger();

    private static DBManager dbManagerInstance;
    private static SQLiteOpenHelper dbHelper;
    private SQLiteDatabase db;

    public static synchronized void initDBManagerInstance(SQLiteOpenHelper helper) {
        if (dbManagerInstance == null) {
            dbManagerInstance = new DBManager();
            dbHelper = helper;
        }
    }

    public static synchronized DBManager getDBManagerInstance() {
        if (dbManagerInstance == null) {
            throw new IllegalStateException(DBManager.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }
        return dbManagerInstance;
    }

    public synchronized SQLiteDatabase open() {
        if(mOpenCounter.incrementAndGet() == 1) {
            db = dbHelper.getWritableDatabase();
        }
        return db;
    }

    public synchronized void close() {
        if(mOpenCounter.decrementAndGet() == 0) {
            db.close();
        }
    }
}
