package com.doat.tracklocation.db;

import com.doat.tracklocation.utils.CommonConst;

public final class DBConst {
	// DB Constants
 	public static final String 	DATABASE_NAME = "DB_TrackLocation";
    public static final boolean IS_DEBUG_LOG_ENABLED = true;

    // DATABASE_VERSION = 1 until application version 5/0.0.5
    // DATABASE_VERSION = 2 until application version ...
    // Added a new fields to TABLE_CONTACT_DEVICE:
    // CONTACT_DEVICE_LOCATION_SHARING and CONTACT_DEVICE_TRACKING
    // DATABASE_VERSION = 3 until application version ...
    // Added a new field to TABLE_CONTACT: CONTACT_PHOTO
    public static final int 	DATABASE_VERSION = 4;
 	public static final String 	LOG_TAG_DB = "TrackLocationDB";
 	
 	public static final String TIMESTAMP = "DATETIME";
 	
    public static final String TABLE_CONTACT = "TABLE_CONTACT"; 
    public static final String CONTACT_FIRST_NAME = "contact_first_name"; 	// OPTIONAL 
    public static final String CONTACT_LAST_NAME = "contact_last_name"; 	// OPTIONAL 
    public static final String CONTACT_NICK = "contact_nick"; 				// OPTIONAL 
    public static final String CONTACT_EMAIL = "contact_email"; 			// KEY + NOT EMPTY
    public static final String CONTACT_PHOTO = "contact_photo";

    public static final String TABLE_DEVICE = "TABLE_DEVICE"; 
    public static final String DEVICE_NAME = "device_name"; 	// OPTIONAL 
    public static final String DEVICE_TYPE = "device_type"; 	// NOT EMPTY
    public static final String DEVICE_MAC = "device_mac"; 		// KEY + NOT EMPTY
    public static final String DEVICE_IMEI = "device_imei";		// OPTIONAL 
    
    public static final String TABLE_CONTACT_DEVICE = "TABLE_CONTACT_DEVICE"; 
    public static final String CONTACT_DEVICE_PHONE_NUMBER = "contact_device_phone_number";	// phone number + KEY + NOT EMPTY
    public static final String CONTACT_DEVICE_MAC = "contact_device_mac"; 					// MAC Address + KEY + NOT EMPTY
    public static final String CONTACT_DEVICE_EMAIL = "contact_device_email";				// KEY + NOT EMPTY
    public static final String CONTACT_DEVICE_IMEI = "contact_device_imei"; 				// OPTIONAL
    public static final String CONTACT_DEVICE_REG_ID = CommonConst.PREFERENCES_REG_ID;  
    public static final String CONTACT_DEVICE_GUID = "contact_device_guid";					// generated 
    public static final String CONTACT_DEVICE_LOCATION_SHARING = "contact_device_location_sharing";
    public static final String CONTACT_DEVICE_TRACKING = "contact_device_tracking";
    public static final String CONTACT_DEVICE_IS_FAVORITE = "is_favorite";


    public static final String TABLE_SEND_JOIN_REQUEST = "TABLE_SEND_JOIN_REQUEST";
    public static final String PHONE_NUMBER = "phone_number";
    public static final String MUTUAL_ID = "mutual_id";
    public static final String STATUS = "status";
    public static final String MAC_ADDRESS = "mac_address";

//    CommonConst.JOIN_FLAG_SMS + 
//	CommonConst.DELIMITER_COMMA + registrationId + CommonConst.DELIMITER_COMMA +
//	mutualId + CommonConst.DELIMITER_COMMA + phoneNumber
	
    public static final String TABLE_RECEIVED_JOIN_REQUEST = "TABLE_RECEIVED_JOIN_REQUEST";
    public static final String REG_ID = CommonConst.PREFERENCES_REG_ID;
    public static final String RECEIVED_JOIN_REQUEST_ACCOUNT = "account";

    public static final String TABLE_PERMISSIONS = "TABLE_PERMISSIONS";
    public static final String EMAIL = "email";
    public static final String LOCATION = "location"; 
    public static final String COMMAND = "command";
    public static final String ADMIN_COMMAND = "admin_command"; 
    
    public static final String TABLE_TRACKING = "TABLE_TRACKING";
    
    public static final String[] TABLES_LIST = { TABLE_CONTACT, TABLE_DEVICE, TABLE_CONTACT_DEVICE, TABLE_SEND_JOIN_REQUEST, TABLE_RECEIVED_JOIN_REQUEST, TABLE_PERMISSIONS };

	//    create table if not exists TABLE_CONTACT (
    //        contact_first_name text,contact_last_name text, contact_nick text, 
    //        contact_email text not null unique,PRIMARY KEY (contact_email));
    public static final String TABLE_CONTACT_CREATE = 
      "create table if not exists " + TABLE_CONTACT + " (" +
		  //"_id integer primary key autoincrement," + 
		  CONTACT_FIRST_NAME + " text," +
		  CONTACT_LAST_NAME + " text," +
		  CONTACT_NICK + " text," +
		  CONTACT_EMAIL + " text not null unique," + // PRIMARY KEY
		  CONTACT_PHOTO + " BLOB," + 
		  "PRIMARY KEY (" + CONTACT_EMAIL + ")" +
		  ");";

	//    create table if not exists TABLE_DEVICE (device_mac text not null unique, 
    //        device_name  text, device_type  text not null, device_imei text, 
    //        PRIMARY KEY ( device_mac ));
    public static final String TABLE_DEVICE_CREATE = 
	  "create table if not exists " + TABLE_DEVICE + " (" +
		  //"_id integer primary key autoincrement," + 
		  DEVICE_MAC + " text not null unique," + // PRIMARY KEY
		  DEVICE_NAME + " text," + 
		  DEVICE_TYPE + " text not null," + 
		  DEVICE_IMEI + " text," + 
		  "PRIMARY KEY (" + DEVICE_MAC + ")" +
		  ");";
	
	//    create table if not exists TABLE_CONTACT_DEVICE (contact_device_mac text not null, 
    //        contact_device_phone_number text not null, contact_device_email text not null, 
    //        contact_device_imei text, registration_id text, PRIMARY KEY ( contact_device_phone_number, 
    //        contact_device_mac, contact_device_email ));
    
    // create table TABLE_CONTACT_DEVICE  ( CONTACT_DEVICE_MAC text not null, CONTACT_DEVICE_PHONE_NUMBER text not null, CONTACT_DEVICE_EMAIL text not null,
    // CONTACT_DEVICE_IMEI text, CONTACT_DEVICE_REG_ID text, PRIMARY KEY (  CONTACT_DEVICE_MAC, CONTACT_DEVICE_PHONE_NUMBER, CONTACT_DEVICE_EMAIL ));
    public static final String TABLE_CONTACT_DEVICE_CREATE = 
      "create table if not exists " + TABLE_CONTACT_DEVICE + " (" +
		  //"_id integer primary key autoincrement," + 
		  CONTACT_DEVICE_MAC + " text not null," + // PRIMARY KEY
		  CONTACT_DEVICE_PHONE_NUMBER + " text," + 
		  CONTACT_DEVICE_EMAIL + " text not null," + 
		  CONTACT_DEVICE_IMEI + " text," + 
		  CONTACT_DEVICE_REG_ID + " text," +
		  CONTACT_DEVICE_LOCATION_SHARING + " integer, " +
		  CONTACT_DEVICE_TRACKING + " integer, " +
		  CONTACT_DEVICE_GUID + " text not null unique," +
		  CONTACT_DEVICE_IS_FAVORITE + " integer, " +
		  "PRIMARY KEY (" + CONTACT_DEVICE_MAC + ", " + 
		  CONTACT_DEVICE_EMAIL + ")" +
		  ");";

	//    create table if not exists TABLE_SEND_JOIN_REQUEST (phone_number text not null unique, mutual_id text not null);
    public static final String TABLE_SEND_JOIN_REQUEST_CREATE = 
      "create table if not exists " + TABLE_SEND_JOIN_REQUEST + " (" +
    	  PHONE_NUMBER + " text not null unique, " +
    	  MUTUAL_ID + " text not null unique, " +
    	  STATUS + " text not null, " +
    	  TIMESTAMP + " datetime not null" + 
    	  ");";

	//    create table if not exists TABLE_RECEIVED_JOIN_REQUEST (phone_number text not null unique, mutual_id text not null);
    //	  Content means that join request was received but was not approved
    public static final String TABLE_RECEIVED_JOIN_REQUEST_CREATE = 
      "create table if not exists " + TABLE_RECEIVED_JOIN_REQUEST + " (" +
    	  PHONE_NUMBER + " text not null unique, " +
    	  MUTUAL_ID + " text not null, " +
    	  REG_ID + " text not null, " +
    	  RECEIVED_JOIN_REQUEST_ACCOUNT + " text, " +
    	  MAC_ADDRESS + " text not null, " +
    	  TIMESTAMP + " datetime not null" + 
    	  ");";

    //    create table if not exists TABLE_PERMISSIONS (email text not null unique, location integer, command integer, 
    //        admin_command integer);
    // 	  LOCATION 		: 0 = PROHIBITED / 1 = PERMITTED
    // 	  COMMAND		: RESERVED
    //	  ADMIN_COMMAND	: RESERVED
    public static final String TABLE_PERMISSIONS_CREATE = 
      "create table if not exists " + TABLE_PERMISSIONS + " (" +
    	  EMAIL + " text not null unique, " +	  
    	  LOCATION + " integer, " +	  
    	  COMMAND + " integer, " +	  
    	  ADMIN_COMMAND + " integer " +	  
    	  ");";

	// create table if not exists TABLE_TRACKING (
    // CONTACT_DEVICE_MAC text not null, 
    // CONTACT_DEVICE_EMAIL text not null,
    // STATUS text, 
    // PRIMARY KEY (  CONTACT_DEVICE_MAC, CONTACT_DEVICE_EMAIL ));
    public static final String TABLE_TRACKING_CREATE = 
	  "create table if not exists " + TABLE_TRACKING + " (" +
		  CONTACT_DEVICE_MAC + " text not null," + // PRIMARY KEY
		  CONTACT_DEVICE_EMAIL + " text not null," + // PRIMARY KEY
		  STATUS + " text not null," + 
		  "PRIMARY KEY (" + CONTACT_DEVICE_MAC + ", " + 
		  		CONTACT_DEVICE_EMAIL +")" +
		  ");";

}
