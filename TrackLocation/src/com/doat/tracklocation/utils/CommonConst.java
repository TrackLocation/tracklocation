package com.doat.tracklocation.utils;

import java.util.regex.Pattern;

import android.net.Uri;

public class CommonConst {
	
	public static final String 	APP_NAME 						= "TrackLocation";
	public static final String 	LOG_TAG 						= "TrackLocation";
	public static final String 	LOG_TAG_TLS 					= "TrackLocation_TLS";
	public static final String 	STACK_TRACE						= "TRUE";
	public static final String 	TRACK_LOCATION_PROJECT_PREFIX 	= "com.doat.tracklocation";
	public static final String 	GOOGLE_PROJECT_NUMBER 			= "GoogleProjectNumber";
	public static final String 	APP_INST_DETAILS 				= "AppInstDetails";
	public static final int 	UNHANDLED_EXCEPTION				= 1973;
	public static final String 	UNHANDLED_EXCEPTION_EXTRA		= "unhandledException";
	public static final String 	SUPPORT_MAIL 					= "track.and.location@gmail.com";

	public static final String 	PREFERENCES_PHONE_NUMBER 		= "PhoneNumber";
	public static final String 	PREFERENCES_PHONE_MAC_ADDRESS 	= "PhoneMacAddress";
	public static final String 	PREFERENCES_PHONE_ACCOUNT 		= "PhoneAccount";
	
	public static final String 	PREFERENCES_HANDLED_SMS_LIST 	= "HandledSMSList";
	
	// checkPlayServices
	public static final String 	PLAYSERVICES_ERROR	 			= "ERROR";
	public static final String 	PLAYSERVICES_DEVICE_NOT_SUPPORTED= "DEVICE_NOT_SUPPORTED";
	
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
	public static final long    TLS_AUTOSTOP_PERIOD_DEFAULT = 60000 * 2; // 2 minutes;
	public static final long    REPEAT_PERIOD_DEFAULT = 60000 * 2; // 2 minutes;
	public static final long    REPEAT_PERIOD_DEFAULT_TRACKING_AUTOSTARTER = 60000; // 1 munite
	
	public static final String 	PREFERENCES_REG_ID = "registration_id";
	public static final String  REGISTRATION_ID_TO_RETURN_MESSAGE_TO = "regIDToReturnMessageTo";
	
	// Map<account, registrationId> of requesters that want to see location of the contact on Map 
	public static final String 	PREFERENCES_LOCATION_REQUESTER_MAP__ACCOUNT_AND_REG_ID = "account_and_regid_map";
	// TlsRequesterList - for tracking feature
	public static final String 	PREFERENCES_TLS_SERVICE_INFO = "";
	
	public static final String 	JOIN_FLAG_SMS = "JOIN_TRACK_LOCATION";
	public static final int	 	JOIN_SMS_PARAMS_NUMBER = 6;

 	public static final String 	DELIMITER = "\t";
 	public static final String 	DELIMITER_COMMA = ",";
 	public static final String 	DELIMITER_UNDERLINE = "_";
 	public static final String 	DELIMITER_ARROW = "->";
 	public static final String 	DELIMITER_COLON = ":";
 	public static final String 	DELIMITER_STRING = "####";
 	public static final String 	DELIMITER_AT = "@";
 	public static final String 	SMS_URI = "content://sms";
 	
	public static final String 	TRACK_LOCATION_DIRECTORY_PATH = "TrackLocation";         
	public static final String 	TRACK_LOCATION_LOG_FILE_NAME = "TrackLocation";          
	public static final String 	TRACK_LOCATION_LOG_FILE_EXT = ".log";          
	public static final String 	TRACK_LOCATION_BACKUP_FILE_NAME = "TrackLocationBackUp.dat";          
	public static final String 	ENABLE_LOG_DIRECTORY = "enable_log";
	public static final String 	CONTACT_DTAT_INPUT_FILE = "contact_device_list.dat";

 	//public static final String 	JSON_STRING_CONTACT_DEVICE_DATA = "jsonStringContactDeviceData";
 	public static final String 	CONTACT_DEVICE_DATA_LIST = "ContactDeviceDataList";
 	public static final String 	JSON_STRING_CONTACT_DATA = "jsonStringContactData";
 	public static final String 	CONTACT_LIST_SELECTED_VALUE = "selectedValue";
 	public static final String 	CONTACT_REGISTRATION_ID = "registration_id";
 	
 	public static final String	TRACK_LOCATION_URL_ON_GOOGLE = "https://goo.gl/FgvM4X";
 	public static final String	JOIN_SMS_PREFIX = 
 		APP_NAME + " application (" + Uri.parse(TRACK_LOCATION_URL_ON_GOOGLE) + "):\n";
 	public static final String 	JOIN_CONTACT_BY_SMS = "sms";
 	public static final String 	JOIN_CONTACT_BY_EMAIL = "email";
 	public static final String 	JOIN_CONTACT_BY = "join_contact_by";
 	
 	// APPLICATION INFO
 	public static final String	PREFERENCES_VERSION_NUMBER = "ApplicationNumber";
 	public static final String	PREFERENCES_VERSION_NAME = "ApplicationName";
 	
 	public static final String THEME_CHANGED = "THEME_CHANGED";
 	
 	public static final int 	REQUEST_SELECT_PHONE_NUMBER = 1;
 	
 	public static final long    KEEP_ALIVE_TIMER_REQUEST_FROM_MAP_DELAY = 40000; // 40 seconds

 	// COMMON DIALOG
 	public final static int STYLE_NORMAL = 0;
 	
 	// SEND COMMAND
 	public static final String	START_CMD_SENDER_MESSAGE_DATA_CONTACT_DETAILS = "SenderMessageDataContactDetails";
 	public static final String	PREFERENCES_SEND_COMMAND_TO_ACCOUNTS = "send_command_to_accounts";
 	public static final String	PREFERENCES_SEND_IS_ONLINE_TO_ACCOUNTS = "send_is_online_to_accounts";
 	public static final String	PREFERENCES_SEND_LOCATION_TO_ACCOUNTS = "send_location_to_accounts";
 	
 	public static final String	PREFERENCES_LOCATION_REQUESTERS_ACCOUNTS_LIST = "loc_req_accounts";
 	public static final String	PREFERENCES_TRACKING_REQUESTERS_ACCOUNTS_LIST = "trk_req_accounts";

 	public static final int 	MAX_RINGTIME_WITH_MAX_VOLUME = 5; // [minutes]
 	public static final String	NOBODY_RESPONDED = "nobody_responded";
 	
 	public static final Pattern VALID_EMAIL_ADDRESS_REGEX = 
 		    Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

 	public static final int 	CONTACT_STATUS_START_CONNECT = 0; 
 	public static final int 	CONTACT_STATUS_CONNECTED = 1;
 	public static final int 	CONTACT_STATUS_PENDING = 2;
 	
}
