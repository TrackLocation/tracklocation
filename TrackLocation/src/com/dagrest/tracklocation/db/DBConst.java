package com.dagrest.tracklocation.db;

public final class DBConst {
	// DB Constants
	public static final boolean IS_DEBUG_LOG_ENABLED = true;
	public static final String DB_LOG_TAG = "DB_LOG";
	
    public static final String DATABASE_NAME = "DB_TrackLocation";
    public static final int DATABASE_VERSION = 1;

    public static final String CONTACT_TABLE = "CONTACT_TABLE"; 
    //public static final String ..._TABLE = ""; 
    public static final String[] TABLES_LIST = { CONTACT_TABLE };

    // Create table syntax 
    public static final String USER_CREATE = 
      "create table tbl_user(_id integer primary key autoincrement," + 
                             "user_name text not null," +
                             "user_imei text not null," +
                             "user_message text not null);";
                              
    public static final String DEVICE_CREATE = 
      "create table tbl_device(_id integer primary key autoincrement," + 
                               "device_name text not null," + 
                               "device_email text not null," + 
                               "device_regid text not null," + 
                               "device_imei text not null);";

}
