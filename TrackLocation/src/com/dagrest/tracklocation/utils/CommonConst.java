package com.dagrest.tracklocation.utils;

public class CommonConst {
	
	public static final String 	LOG_TAG = "TrackLocation";
	public static final String 	PROPERTY_APP_VERSION = "AppVersion";
	public static final int 	PROPERTY_APP_VERSION_VALUE = 1;

	public static final String 	PREFERENCES_PHONE_NUMBER 		= "PhoneNumber";
	public static final String 	PREFERENCES_PHONE_MAC_ADDRESS 	= "PhoneMacAddress";
	public static final String 	PREFERENCES_PHONE_ACCOUNT 		= "PhoneAccount";
	
	public static final String 	SHARED_PREFERENCES_NAME = "TRACK_LOCATION";
	// public static final String 	LOCATION_PROVIDER_NAME = "LOCATION_PROVIDER_NAME";
	public static final String 	LOCATION_SERVICE_INTERVAL = "LOCATION_SERVICE_INTERVAL";
	public static final String 	LOCATION_DEFAULT_UPDATE_INTERVAL = "1000"; // in milliseconds: 1000 ms = 1 second
	public static final String 	GPS = "GPS";
	public static final String 	NETWORK = "NETWORK";
	public static final String 	LOCATION_LISTENER = "LocationListener";
	public static final String 	LOCATION_INFO_ = "LOCATION_INFO_";
	public static final String 	LOCATION_INFO_GPS = "LOCATION_INFO_GPS";
	public static final String 	LOCATION_INFO_NETWORK = "LOCATION_INFO_NETWORK";
	// public static final String 	IS_LOCATION_PROVIDER_AVAILABLE = "IS_LOCATION_PROVIDER_AVAILABLE";
	public static final String 	TRACK_LOCATION_WAKE_LOCK = "TRACK_LOCATION_WAKE_LOCK";
	
	public static final String 	PREFERENCES_REG_ID = "registration_id";
	// Registry ID list of contacts that will be updated by requested info: location/status/...
	public static final String 	PREFERENCES_RETURN_TO_REG_ID_LIST = "return_to_reg_id_list";
	public static final String 	JOIN_FLAG_SMS = "JOIN_TRACK_LOCATION";
	public static final String 	JOIN_COMPLETED = "JOIN_COMPLETED";

 	public static final String 	DELIMITER = "\t";
 	public static final String 	DELIMITER_COMMA = ",";
 	public static final String 	DELIMITER_UNDERLINE = "_";
 	public static final String 	DELIMITER_ARROW = "->";
 	public static final String 	DELIMITER_COLON = ":";
 	public static final String 	DELIMITER_STRING = "####";
 	public static final String 	DELIMITER_AT = "@";
 	public static final String 	SMS_URI = "content://sms";
 	
	public static final String 	LOG_DIRECTORY_PATH = "TrackLocation";         
	public static final String 	LOG_FILE_NAME = "TrackLocation.log";          
	public static final String 	ENABLE_LOG_DIRECTORY = "enable_log";
	public static final String 	CONTACT_DTAT_INPUT_FILE = "contact_device_list.dat";

 	//public static final String 	JSON_STRING_CONTACT_DEVICE_DATA = "jsonStringContactDeviceData";
 	public static final String 	JSON_STRING_CONTACT_DEVICE_DATA_LIST = "jsonStringContactDeviceDataList";
 	public static final String 	CONTACT_LIST_SELECTED_VALUE = "selectedValue";
 	public static final String 	CONTACT_REGISTRATION_ID = "registration_id";
 	
 	// BROADCAST ACTIONS
 	// deprecated: "com.dagrest.tracklocation.service.GcmIntentService.GCM_UPDATED" use:
 	public static final String 	BROADCAST_LOCATION_UPDATED 		= "com.dagrest.tracklocation.service.GcmIntentService.LOCATION_UPDATED";
 	public static final String 	BROADCAST_JOIN 					= "com.dagrest.tracklocation.JoinContactList.BROADCAST_JOIN";
 	public static final String 	BROADCAST_LOCATION_KEEP_ALIVE 	= "com.dagrest.tracklocation.Map.KEEP_ALIVE";
 	
 	public static final int 	REQUEST_SELECT_PHONE_NUMBER = 1;

}
