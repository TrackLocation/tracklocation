package com.dagrest.tracklocation.db;

import com.dagrest.tracklocation.datatype.BackupDataOperations;
import com.dagrest.tracklocation.log.LogManager;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper{

	private String className;
	private String methodName;
	private String logMessage;
	
	// http://blog.lemberg.co.uk/concurrent-database-access
    public DBHelper(Context context) {
        super(context, DBConst.DATABASE_NAME, null, DBConst.DATABASE_VERSION);
    	className = this.getClass().getName(); 
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    	methodName = "onCreate";
        if (DBConst.IS_DEBUG_LOG_ENABLED){
        	logMessage = "Creating an application tables if not exist";
    		LogManager.LogInfoMsg(className, methodName, logMessage);
    		Log.i(DBConst.LOG_TAG_DB, "[INFO] {" + className + "} -> " + logMessage);
        }
        try {
            db.execSQL(DBConst.TABLE_CONTACT_CREATE);
            db.execSQL(DBConst.TABLE_DEVICE_CREATE);
            db.execSQL(DBConst.TABLE_CONTACT_DEVICE_CREATE);
            db.execSQL(DBConst.TABLE_SEND_JOIN_REQUEST_CREATE);
            db.execSQL(DBConst.TABLE_RECEIVED_JOIN_REQUEST_CREATE);
            db.execSQL(DBConst.TABLE_PERMISSIONS_CREATE);

        } catch (Exception exception) {
        	Log.e(DBConst.LOG_TAG_DB, "[EXCEPTION] {" + className + "} -> Exception DBHelper.onCreate()", exception);
        	LogManager.LogException(exception, className, methodName);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	methodName = "onUpgrade";
        if (DBConst.IS_DEBUG_LOG_ENABLED) {
        	logMessage = "Upgrading database version from [" + oldVersion
        			+ "] to [" + newVersion +"]";
    		LogManager.LogInfoMsg(className, methodName, logMessage);
    		Log.i(DBConst.LOG_TAG_DB, "[INFO] {" + className + "} -> " + logMessage);
        }

		BackupDataOperations backupData = new BackupDataOperations();
		boolean isBackUpDataSuccess = backupData.backUp();
		if(isBackUpDataSuccess != true){
			logMessage = methodName + " -> Backp process failed.";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(DBConst.LOG_TAG_DB, "[ERROR] {" + className + "} -> " + logMessage);
		}

        try {
			for (String table : DBConst.TABLES_LIST) {
			    db.execSQL("DROP TABLE IF EXISTS " + table);
			}
		} catch (SQLException e) {
        	Log.e(DBConst.LOG_TAG_DB, "[EXCEPTION] {" + className + "} -> Exception DBHelper.onCreate()", e);
        	LogManager.LogException(e, className, methodName);
		}
        
        boolean isBackUpDataRestoreSuccess = backupData.restore();
		if(isBackUpDataRestoreSuccess != true){
			logMessage = methodName + " -> Backup restore process failed.";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(DBConst.LOG_TAG_DB, "[ERROR] {" + className + "} -> " + logMessage);
		}

        onCreate(db);
    }
}
