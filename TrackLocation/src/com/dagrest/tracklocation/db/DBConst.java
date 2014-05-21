package com.dagrest.tracklocation.db;

import com.dagrest.tracklocation.utils.CommonConst;

public final class DBConst {
	// DB Constants
 	public static final String 	DATABASE_NAME = "DB_TrackLocation";
    public static final boolean IS_DEBUG_LOG_ENABLED = true;

    public static final int 	DATABASE_VERSION = 1;
 	public static final String 	LOG_TAG_DB = "TrackLocationDB";
 	
    public static final String TABLE_CONTACTS = "TABLE_CONTACTS"; 
    public static final String CONTACT_NAME = "user_name"; 
    public static final String CONTACT_REG_ID = CommonConst.PREFERENCES_REG_ID; 
    public static final String CONTACT_EMAIL = "user_email"; 
    public static final String CONTACT_GUID = "user_guid"; 
    public static final String CONTACT_DEVICE_GUID = "user_device_guid"; 

    public static final String DEVICE_GUID = "device_guid"; 
    public static final String DEVICE_NAME = "device_name"; 
    public static final String DEVICE_TYPE = "device_type"; 
    public static final String DEVICE_IMEI = "device_imei"; 
    public static final String DEVICE_NUMBER = "device_number"; 
//    public static final String  = ""; 

    public static final String TABLE_DEVICES = "TABLE_DEVICES"; 

    //public static final String ..._TABLE = ""; 
    public static final String[] TABLES_LIST = { TABLE_CONTACTS };

    public static final String TABLE_CONTACTS_CREATE = 
    	      "create table " + TABLE_CONTACTS + "(" +
	    		  "_id integer primary key autoincrement," + 
	    		  CONTACT_GUID + " text not null unique," + // PRIMARY KEY
	    		  CONTACT_NAME + " text not null," +
	    		  CONTACT_REG_ID + " text not null," +
	    		  CONTACT_EMAIL + " text not null," + 
	    		  CONTACT_DEVICE_GUID + " text not null unique," + // PRIMARY & FOREIGN KEY
	    		  "PRIMARY KEY (" + CONTACT_GUID + ", " + CONTACT_EMAIL + ")" +
	    		  ");";

    public static final String TABLE_DEVICE_CREATE = 
  	      "create table " + TABLE_DEVICES + "(" +
  	    		  "_id integer primary key autoincrement," + 
  	    		  DEVICE_GUID + " text not null unique," + // PRIMARY KEY
  	    		  CONTACT_GUID + " text not null unique," + // FOREIGN KEY
  	    		  DEVICE_NAME + " text not null," + 
  	    		  DEVICE_TYPE + " text not null," + 
  	    		  DEVICE_IMEI + " text," + 
  	    		  DEVICE_NUMBER + " text," + 
   	    		  "PRIMARY KEY (" + DEVICE_GUID + ", " + DEVICE_TYPE + ")" +
  	    		  ");";
	
//    public static final String USER_CREATE = 
//      "create table tbl_user(_id integer primary key autoincrement," + 
//                             "user_name text not null," +
//                             "user_imei text not null," +
//                             "user_message text not null);";
//                              
//    public static final String DEVICE_CREATE = 
//      "create table tbl_device(_id integer primary key autoincrement," + 
//                               "device_name text not null," + 
//                               "device_email text not null," + 
//                               "device_regid text not null," + 
//                               "device_imei text not null);";
//
}
