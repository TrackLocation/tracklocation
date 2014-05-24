package com.dagrest.tracklocation.db;

import com.dagrest.tracklocation.datatype.ContactData;
import com.dagrest.tracklocation.datatype.ContactDeviceData;
import com.dagrest.tracklocation.datatype.DeviceData;
import com.dagrest.tracklocation.datatype.DeviceTypeEnum;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBLayer {
	protected DBLayer() {
    }
	
	// Used to open database in synchronized way
    private static DBHelper dbHelper = null;
    
	// Initialize database
    public static void init(Context context) {
        if (dbHelper == null) {
            if (DBConst.IS_DEBUG_LOG_ENABLED){
                Log.i(DBConst.LOG_TAG_DB, context.toString());
            }
            dbHelper = new DBHelper(context);
        }
    }
    
    // Open database for insert,update,delete in synchronized way 
    private static synchronized SQLiteDatabase open() throws SQLException {
        return dbHelper.getWritableDatabase();
    }	

	// Insert installing contact data
	public static ContactData addContactData(String nick, String firstName, String lastName, 
		String contactEmail) 
	{
		ContactData contactData = new ContactData();
		SQLiteDatabase db = null;
		try{
			db = open();
			
			contactData.setFirstName(sqlEscapeString(firstName));
			contactData.setLastName(sqlEscapeString(lastName));
			contactData.setNick(sqlEscapeString(nick));
			contactData.setEmail(sqlEscapeString(contactEmail));
			           
			ContentValues cVal = new ContentValues();
			cVal.put(DBConst.CONTACT_FIRST_NAME, contactData.getFirstName());
			cVal.put(DBConst.CONTACT_LAST_NAME, contactData.getLastName());
			cVal.put(DBConst.CONTACT_NICK, contactData.getNick());
			cVal.put(DBConst.CONTACT_EMAIL, contactData.getEmail());
			           
			db.insert(DBConst.TABLE_CONTACT, null, cVal);
		} catch (Throwable t) {
			Log.i(DBConst.LOG_TAG_DB, "Exception caught: " + t.getMessage(), t);
		} finally {
			if(db != null){
				db.close(); // Closing database connection
			}
		}
		return contactData;
	}
    
    // Insert installing device data
    public static DeviceData addDeviceData(String  contactMac, String deviceName, DeviceTypeEnum deviceTypeEnum) 
     {
    	DeviceData deviceData = new DeviceData();
		SQLiteDatabase db = null;
		try{
			db = open();
            
            deviceData.setDeviceName(sqlEscapeString(deviceName));
            deviceData.setDeviceTypeEnum(deviceTypeEnum);
            deviceData.setDeviceMac(sqlEscapeString(contactMac));
             
            ContentValues cVal = new ContentValues();
            cVal.put(DBConst.DEVICE_NAME, deviceData.getDeviceName());
            cVal.put(DBConst.DEVICE_TYPE, deviceTypeEnum.toString());
            cVal.put(DBConst.DEVICE_MAC, deviceData.getDeviceMac());
            
            db.insert(DBConst.TABLE_DEVICE, null, cVal);
        } catch (Throwable t) {
            Log.i("Database", "Exception caught: " + t.getMessage(), t);
		} finally {
			if(db != null){
				db.close(); // Closing database connection
			}
		}
        return deviceData;
    }
     
    // Insert installing contact/device data
    public static DeviceData addContactDeviceData(String phoneNumber, ContactData  contactData, 
    		DeviceData deviceData, String imei)
     {
    	ContactDeviceData contactDeviceData = new ContactDeviceData();
		SQLiteDatabase db = null;
		try{
			db = open();
            
            contactDeviceData.setPhoneNumber(sqlEscapeString(phoneNumber));
            contactDeviceData.setContactData(contactData);
            contactDeviceData.setDeviceData(deviceData);
            contactDeviceData.setImei(sqlEscapeString(imei));
             
            ContentValues cVal = new ContentValues();
            cVal.put(DBConst.CONTACT_DEVICE_PHONE_NUMBER, contactDeviceData.getPhoneNumber());
            cVal.put(DBConst.CONTACT_DEVICE_MAC, contactDeviceData.getContactData().getEmail());
            cVal.put(DBConst.CONTACT_DEVICE_EMAIL, contactDeviceData.getDeviceData().getDeviceMac());
            cVal.put(DBConst.CONTACT_DEVICE_REG_ID, contactDeviceData.getContactData().getRegistration_id());
            cVal.put(DBConst.CONTACT_DEVICE_IMEI, contactDeviceData.getImei());
            
            db.insert(DBConst.TABLE_CONTACT_DEVICE, null, cVal);
        } catch (Throwable t) {
            Log.i("Database", "Exception caught: " + t.getMessage(), t);
		} finally {
			if(db != null){
				db.close(); // Closing database connection
			}
		}
        return deviceData;
    }

    public static ContactData getContactData(){
    	ContactData contactData = null;
		SQLiteDatabase db = null;
		try{
			db = open();
            
	        // Select All Query
			String selectQuery = "select * from " + DBConst.TABLE_CONTACT;
	        Cursor cursor = db.rawQuery(selectQuery, null);
	  
	        // looping through all rows and adding to list
	        if (cursor.moveToFirst()) {
	            do {
	            	contactData = new ContactData();
	            	
	            	String contact_first_name = cursor.getString(0);
	            	String contact_last_name = cursor.getString(1);
	            	String contact_nick = cursor.getString(2);
	            	String contact_email = cursor.getString(3);
	            	
	            	contactData.setEmail(contact_email);
	            	contactData.setFirstName(contact_first_name);
	            	contactData.setLastName(contact_last_name);
	            	contactData.setNick(contact_nick);
	            	
	            } while (cursor.moveToNext());
	        }
	        cursor.close();
        } catch (Throwable t) {
            Log.i("Database", "Exception caught: " + t.getMessage(), t);
		} finally {
			if(db != null){
				db.close(); // Closing database connection
			}
		}
    	return contactData;
    }

    public static DeviceData getDeviceData(){
    	DeviceData deviceData = null;
		SQLiteDatabase db = null;
		try{
			db = open();
            
	        // Select All Query
			String selectQuery = "select * from " + DBConst.TABLE_DEVICE;
	        Cursor cursor = db.rawQuery(selectQuery, null);
	  
	        // looping through all rows and adding to list
	        if (cursor.moveToFirst()) {
	            do {
	            	deviceData = new DeviceData();
	            	
	            	String device_mac = cursor.getString(0);
	            	String device_name = cursor.getString(1);
	            	String device_type = cursor.getString(2);

	            	deviceData.setDeviceMac(device_mac);
	            	deviceData.setDeviceName(device_name);
	            	deviceData.setDeviceTypeEnum(DeviceTypeEnum.getValue(device_type));
	            	
	            } while (cursor.moveToNext());
	        }
	        cursor.close();
        } catch (Throwable t) {
            Log.i("Database", "Exception caught: " + t.getMessage(), t);
		} finally {
			if(db != null){
				db.close(); // Closing database connection
			}
		}
    	return deviceData;
    }

    public static ContactDeviceData getContactDeviceDataONLY(){
    	ContactDeviceData contactDeviceData = null;
		SQLiteDatabase db = null;
		try{
			db = open();
            
	        // Select All Query
			String selectQuery = "select * from " + DBConst.TABLE_CONTACT_DEVICE;
	        Cursor cursor = db.rawQuery(selectQuery, null);
	  
	        // looping through all rows and adding to list
	        if (cursor.moveToFirst()) {
	            do {
	            	contactDeviceData = new ContactDeviceData();
	            	
	            	String contact_first_name = cursor.getString(0);
	            	String contact_last_name = cursor.getString(1);
	            	String contact_nick = cursor.getString(2);
	            	String contact_email = cursor.getString(3);
	            	String device_mac = cursor.getString(4);
//	            	String device_name = cursor.getString(5);
//	            	String device_type = cursor.getString(6);
//	            	String device_imei = cursor.getString(7);
//	            	String contact_device_phone_number = cursor.getString(8);
//	            	String registration_id = cursor.getString(9);
//	                data.setID(Integer.parseInt(cursor.getString(0)));
	            	int i = 0;
	            } while (cursor.moveToNext());
	        }
	        cursor.close();
        } catch (Throwable t) {
            Log.i("Database", "Exception caught: " + t.getMessage(), t);
		} finally {
			if(db != null){
				db.close(); // Closing database connection
			}
		}
    	return contactDeviceData;
    }

    public static ContactDeviceData getContactDeviceData(){
    	ContactDeviceData contactDeviceData = null;
		SQLiteDatabase db = null;
		try{
			db = open();
            
	        // Select All Query
//			String selectQuery = "select * from " + DBConst.TABLE_CONTACT;
	        String selectQuery = 
	        "select contact_first_name, contact_last_name, contact_nick, contact_email," +
	        "device_mac, device_name, device_type, device_imei, contact_device_phone_number, registration_id " +
	        "from TABLE_CONTACT_DEVICE as CD " +
	        "join TABLE_CONTACT as C " +
	        "on CD.contact_device_email = C.contact_email " +
	        "join TABLE_DEVICE as D " +
	        "on CD.contact_device_mac = D.device_mac";	  
	        Cursor cursor = db.rawQuery(selectQuery, null);
	  
	        // looping through all rows and adding to list
	        if (cursor.moveToFirst()) {
	            do {
	            	contactDeviceData = new ContactDeviceData();
	            	
	            	String contact_first_name = cursor.getString(0);
	            	String contact_last_name = cursor.getString(1);
	            	String contact_nick = cursor.getString(2);
	            	String contact_email = cursor.getString(3);
	            	String device_mac = cursor.getString(4);
	            	String device_name = cursor.getString(5);
	            	String device_type = cursor.getString(6);
	            	String device_imei = cursor.getString(7);
	            	String contact_device_phone_number = cursor.getString(8);
	            	String registration_id = cursor.getString(9);
//	                data.setID(Integer.parseInt(cursor.getString(0)));
	            	int i = 0;
	            } while (cursor.moveToNext());
	        }
	        cursor.close();
        } catch (Throwable t) {
            Log.i("Database", "Exception caught: " + t.getMessage(), t);
		} finally {
			if(db != null){
				db.close(); // Closing database connection
			}
		}
    	return contactDeviceData;
    }
     
//    // Getting single user data
//    public static UserData getUserData(int id) {
//        final SQLiteDatabase db = open();
//  
//        Cursor cursor = db.query(USER_TABLE, new String[] { KEY_ID,
//                KEY_USER_NAME, KEY_USER_IMEI,KEY_USER_MESSAGE}, KEY_ID + "=?",
//                new String[] { String.valueOf(id) }, null, null, null, null);
//        if (cursor != null)
//            cursor.moveToFirst();
//  
//        UserData data = new UserData(Integer.parseInt(cursor.getString(0)),
//                cursor.getString(1), cursor.getString(2), cursor.getString(3));
//        // return contact
//        return data;
//    }
  
//    // Getting All user data
//    public static List<UserData> getAllUserData() {
//        List<UserData> contactList = new ArrayList<UserData>();
//        // Select All Query
//        String selectQuery = "SELECT  * FROM " + USER_TABLE+" ORDER BY "+KEY_ID+" desc";
//  
//        final SQLiteDatabase db = open();
//        Cursor cursor = db.rawQuery(selectQuery, null);
//  
//        // looping through all rows and adding to list
//        if (cursor.moveToFirst()) {
//            do {
//                UserData data = new UserData();
//                data.setID(Integer.parseInt(cursor.getString(0)));
//                data.setName(cursor.getString(1));
//                data.setIMEI(cursor.getString(2));
//                data.setMessage(cursor.getString(3));
//                // Adding contact to list
//                contactList.add(data);
//            } while (cursor.moveToNext());
//        }
//        cursor.close();
//        // return contact list
//        return contactList;
//    }
     
//    // Getting users Count
//    public static int getUserDataCount() {
//        String countQuery = "SELECT  * FROM " + USER_TABLE;
//        final SQLiteDatabase db = open();
//        Cursor cursor = db.rawQuery(countQuery, null);
//         
//        int count = cursor.getCount();
//        cursor.close();
//         
//        // return count
//        return count;
//    }
     
//    // Getting installed device have self data or not
//    public static int validateDevice() {
//        String countQuery = "SELECT  * FROM " + DEVICE_TABLE;
//        final SQLiteDatabase db = open();
//        Cursor cursor = db.rawQuery(countQuery, null);
//         
//        int count = cursor.getCount();
//        cursor.close();
//         
//        // return count
//        return count;
//    }
     
//    // Getting distinct user data use in spinner
//    public static List<UserData> getDistinctUser() {
//        List<UserData> contactList = new ArrayList<UserData>();
//        // Select All Query
//        String selectQuery = "SELECT  distinct(user_imei),user_name 
//                             FROM " + USER_TABLE+"
//                             ORDER BY "+KEY_ID+" desc";
//         
//        final SQLiteDatabase db = open();
//        Cursor cursor = db.rawQuery(selectQuery, null);
//  
//        // looping through all rows and adding to list
//        if (cursor.moveToFirst()) {
//            do {
//                UserData data = new UserData();
//                 
//                data.setIMEI(cursor.getString(0));
//                data.setName(cursor.getString(1));
//                // Adding contact to list
//                contactList.add(data);
//            } while (cursor.moveToNext());
//        }
//        cursor.close();
//         
//        return contactList;
//    }
     
//    // Getting imei already in user table or not 
//    public static int validateNewMessageUserData(String IMEI) {
//         int count = 0;
//        try {
//            String countQuery = "SELECT "+KEY_ID+" 
//                                 FROM " + USER_TABLE + "
//                                 WHERE user_imei='"+IMEI+"'";
//                                  
//            final SQLiteDatabase db = open();
//            Cursor cursor = db.rawQuery(countQuery, null);
//             
//            count = cursor.getCount();
//            cursor.close();
//        } catch (Throwable t) {
//            count = 10;
//            Log.i("Database", "Exception caught: " + t.getMessage(), t);
//        }
//        return count;
//    }
 
     
    // Escape string for single quotes (Insert,Update)
    private static String sqlEscapeString(String aString) {
        String aReturn = "";
         
        if (null != aString) {
            //aReturn = aString.replace("'", "''");
            aReturn = DatabaseUtils.sqlEscapeString(aString);
            // Remove the enclosing single quotes ...
            aReturn = aReturn.substring(1, aReturn.length() - 1);
        }
         
        return aReturn;
    }
    // UnEscape string for single quotes (show data)
    private static String sqlUnEscapeString(String aString) {
         
        String aReturn = "";
         
        if (null != aString) {
            aReturn = aString.replace("''", "'");
        }
         
        return aReturn;
    }
}
