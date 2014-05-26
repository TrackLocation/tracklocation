package com.dagrest.tracklocation.db;

import com.dagrest.tracklocation.datatype.ContactData;
import com.dagrest.tracklocation.datatype.ContactDeviceData;
import com.dagrest.tracklocation.datatype.ContactDeviceDataList;
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
		ContactData contactData = null;
		SQLiteDatabase db = null;
		try{
			db = open();
			
			contactData = new ContactData();
			
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
			contactData = null;
			Log.i(DBConst.LOG_TAG_DB, "Exception caught: " + t.getMessage(), t);
		} finally {
			if(db != null){
				db.close(); // Closing database connection
			}
		}
		return contactData;
	}
    
    // Insert installing device data
    public static DeviceData addDeviceData(String  macAddress, String deviceName, DeviceTypeEnum deviceTypeEnum) 
     {
    	DeviceData deviceData = null;
		SQLiteDatabase db = null;
		try{
			db = open();
            
			deviceData = new DeviceData();
			
            deviceData.setDeviceName(sqlEscapeString(deviceName));
            deviceData.setDeviceTypeEnum(deviceTypeEnum);
            deviceData.setDeviceMac(sqlEscapeString(macAddress));
             
            ContentValues cVal = new ContentValues();
            cVal.put(DBConst.DEVICE_NAME, deviceData.getDeviceName());
            cVal.put(DBConst.DEVICE_TYPE, deviceTypeEnum.toString());
            cVal.put(DBConst.DEVICE_MAC, deviceData.getDeviceMac());
            
            db.insert(DBConst.TABLE_DEVICE, null, cVal);
        } catch (Throwable t) {
        	deviceData = null;
            Log.i("Database", "Exception caught: " + t.getMessage(), t);
		} finally {
			if(db != null){
				db.close(); // Closing database connection
			}
		}
        return deviceData;
    }
     
    // Insert installing contact/device data
    private static ContactDeviceData addContactDeviceData(String phoneNumber, ContactData  contactData, 
    		DeviceData deviceData, String imei, String registartionId)
     {
    	ContactDeviceData contactDeviceData = null;
		SQLiteDatabase db = null;
		try{
			db = open();
            
			contactDeviceData = new ContactDeviceData();
			
            contactDeviceData.setPhoneNumber(sqlEscapeString(phoneNumber));
            contactDeviceData.setContactData(contactData);
            contactDeviceData.setDeviceData(deviceData);
            contactDeviceData.setImei(sqlEscapeString(imei));
            contactDeviceData.setRegistration_id(sqlEscapeString(registartionId));
             
            ContentValues cVal = new ContentValues();
            cVal.put(DBConst.CONTACT_DEVICE_PHONE_NUMBER, contactDeviceData.getPhoneNumber());
            cVal.put(DBConst.CONTACT_DEVICE_EMAIL, contactDeviceData.getContactData().getEmail());
            cVal.put(DBConst.CONTACT_DEVICE_MAC, contactDeviceData.getDeviceData().getDeviceMac());
            cVal.put(DBConst.CONTACT_DEVICE_REG_ID, contactDeviceData.getRegistration_id());
            cVal.put(DBConst.CONTACT_DEVICE_IMEI, contactDeviceData.getImei());
            
            db.insert(DBConst.TABLE_CONTACT_DEVICE, null, cVal);
        } catch (Throwable t) {
        	contactDeviceData = null;
            Log.i("Database", "Exception caught: " + t.getMessage(), t);
		} finally {
			if(db != null){
				db.close(); // Closing database connection
			}
		}
        return contactDeviceData;
    }
    
    public static void addContactDeviceDataList(ContactDeviceDataList contactDeviceDataList){
    	if(contactDeviceDataList != null){
			for (ContactDeviceData contactDeviceData : contactDeviceDataList.getContactDeviceDataList()) {
				if(contactDeviceData != null){
					
					ContactData contactData = contactDeviceData.getContactData();
					DeviceData deviceData = contactDeviceData.getDeviceData();
					String phoneNumber = contactDeviceData.getPhoneNumber();
					String registrationId = contactDeviceData.getRegistration_id();
					String imei =contactDeviceData.getImei();
					
					String email = null;
					if(contactData != null){
						email = contactData.getEmail();
					} else {
						// TODO: Notify - impossible to add contactDeviceData - NO EMAIL
						continue;
					}
					
					String macAddress = null;
					if(deviceData != null){
						macAddress = deviceData.getDeviceMac();
					} else {
						// TODO: Notify - impossible to add contactDeviceData - NO MAC ADDRESS 
						continue;
					}
					
					if(registrationId == null || registrationId.isEmpty()){
						// TODO: Notify - impossible to add contactDeviceData - NO registartionID
						continue;
					}
					
					if(!isContactWithEmailExist(email) && !isDeviceWithMacAddressExist(macAddress) &&
						!isContactDeviceExist(phoneNumber, email, macAddress)){
						ContactData resultContactData = 
							addContactData(contactData.getNick(), contactData.getFirstName(), contactData.getLastName(), email);
						DeviceData resultDeviceData = 
							addDeviceData(macAddress, deviceData.getDeviceName(), deviceData.getDeviceTypeEnum());
						ContactDeviceData resultContactDeviceData = addContactDeviceData(phoneNumber, contactData, 
							deviceData, imei, registrationId);
						if(resultContactDeviceData == null){
							// TODO: Notify - impossible to add contactDeviceData
						}
			    	}
		    	}
			}
    	}
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
	            	
	            	String email = cursor.getString(0);
	            	String phone = cursor.getString(1);
	            	String mac = cursor.getString(2);
	            	String imei = cursor.getString(3);
	            	String regId = cursor.getString(4);
	            	
	            	ContactData contactData = new ContactData();
	            	contactData.setEmail(email);
	            	
	            	DeviceData deviceData = new DeviceData();
	            	deviceData.setDeviceMac(mac);
	            	
	            	contactDeviceData.setPhoneNumber(phone);
	            	contactDeviceData.setImei(imei);
	            	contactDeviceData.setRegistration_id(regId);
	            	contactDeviceData.setContactData(contactData);
	            	contactDeviceData.setDeviceData(deviceData);
	            	
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

    public static ContactDeviceDataList getContactDeviceDataList(){
 
    	ContactDeviceDataList contactDeviceDataList = null;
    	ContactDeviceData contactDeviceData = null;
		
    	SQLiteDatabase db = null;
		try{
			db = open();
            
	        // Select All Query
	        String selectQuery = 
	        "select " +		
	        "contact_first_name, contact_last_name, contact_nick, contact_email, device_mac, " +
	        "device_name, device_type, contact_device_imei, contact_device_phone_number, registration_id " +
	        "from TABLE_CONTACT_DEVICE as CD " +
	        "join TABLE_CONTACT as C " +
	        "on CD.contact_device_email = C.contact_email " +
	        "join TABLE_DEVICE as D " +
	        "on CD.contact_device_mac = D.device_mac";	  
	        Cursor cursor = db.rawQuery(selectQuery, null);
	  
	        // looping through all rows and adding to list
	        if (cursor.moveToFirst()) {
	        	contactDeviceDataList = new ContactDeviceDataList();
	            do {
	            	contactDeviceData = new ContactDeviceData();
	            	
	            	String contact_first_name = cursor.getString(0);
	            	String contact_last_name = cursor.getString(1);
	            	String contact_nick = cursor.getString(2);
	            	String contact_email = cursor.getString(3);
	            	String device_mac = cursor.getString(4);
	            	String device_name = cursor.getString(5);
	            	String device_type = cursor.getString(6);
	            	String contact_device_imei = cursor.getString(7);
	            	String contact_device_phone_number = cursor.getString(8);
	            	String registration_id = cursor.getString(9);
	            	
	            	ContactData contactData = new ContactData();
	            	contactData.setEmail(contact_email);
	            	contactData.setFirstName(contact_first_name);
	            	contactData.setLastName(contact_last_name);
	            	contactData.setNick(contact_nick);
	            	
	            	DeviceData deviceData = new DeviceData();
	            	deviceData.setDeviceMac(device_mac);
	            	deviceData.setDeviceName(device_name);
	            	deviceData.setDeviceTypeEnum(DeviceTypeEnum.getValue(device_type));
	            	
	            	contactDeviceData.setPhoneNumber(contact_device_phone_number);
	            	contactDeviceData.setImei(contact_device_imei);
	            	contactDeviceData.setRegistration_id(registration_id);
	            	contactDeviceData.setContactData(contactData);
	            	contactDeviceData.setDeviceData(deviceData);

	            	contactDeviceDataList.getContactDeviceDataList().add(contactDeviceData);
	            	
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
    	return contactDeviceDataList;
    }
    
    public static boolean isContactWithEmailExist(String email){
		String selectQuery = "select contact_email from " + DBConst.TABLE_CONTACT +
				" where contact_email = ?";
		// TODO: check that email is valid value to avoid SQL injection
		return isFieldExist(selectQuery, new String[] { email });
    }
    
    public static boolean isContactWithNickExist(String nick){
		String selectQuery = "select contact_nick from " + DBConst.TABLE_CONTACT +
				" where contact_nick = ?";
		// TODO: check that nick is valid value to avoid SQL injection
		return isFieldExist(selectQuery, new String[] { nick });
    }

    public static boolean isDeviceWithMacAddressExist(String macAddress){
		String selectQuery = "select device_mac from " + DBConst.TABLE_DEVICE +
				" where device_mac = ?";
		// TODO: check that macAddress is valid value to avoid SQL injection
		return isFieldExist(selectQuery, new String[] { macAddress});
    }

    public static boolean isContactDeviceExist(String phoneNumber, String email, String macAddress){
		String selectQuery = "select contact_device_email from " + DBConst.TABLE_CONTACT_DEVICE +
				" where contact_device_phone_number = ? and contact_device_email = ? " +
				"and contact_device_mac = ?";
		// TODO: check that phoneNumber, email and macAddress are valid values to avoid SQL injection
		return isFieldExist(selectQuery, new String[] {phoneNumber, email, macAddress});
    }

    private static boolean isFieldExist(String selectQuery, String[] val){
    	boolean result = false;
		SQLiteDatabase db = null;
		try{
			db = open();
            
	        Cursor cursor = db.rawQuery(selectQuery, val);
	        int count = cursor.getCount();
	        cursor.close();
	        if(count > 0){
	        	result = true;
	        }        
	    } catch (Throwable t) {
            Log.i("Database", "Exception caught: " + t.getMessage(), t);
		} finally {
			if(db != null){
				db.close(); // Closing database connection
			}
		}
		return result;
    }
    
    public static boolean isDeviceExist(){
    	return false;
    }

    public static boolean isContactDeviceExist(){
    	return false;
    }

    public static ContactDeviceData getContactDeviceData(){
    	ContactDeviceData contactDeviceData = null;
		SQLiteDatabase db = null;
		try{
			db = open();
            
	        // Select All Query
	        String selectQuery = 
	        "select " +		
	        "contact_first_name, contact_last_name, contact_nick, contact_email, " +
	        "device_mac, device_name, device_type, contact_device_imei, contact_device_phone_number, registration_id " +
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
	            	String contact_device_imei = cursor.getString(7);
	            	String contact_device_phone_number = cursor.getString(8);
	            	String registration_id = cursor.getString(9);
	            	
	            	ContactData contactData = new ContactData();
	            	contactData.setEmail(contact_email);
	            	contactData.setFirstName(contact_first_name);
	            	contactData.setLastName(contact_last_name);
	            	contactData.setNick(contact_nick);
	            	
	            	DeviceData deviceData = new DeviceData();
	            	deviceData.setDeviceMac(device_mac);
	            	deviceData.setDeviceName(device_name);
	            	deviceData.setDeviceTypeEnum(DeviceTypeEnum.getValue(device_type));
	            	
	            	contactDeviceData.setPhoneNumber(contact_device_phone_number);
	            	contactDeviceData.setImei(contact_device_imei);
	            	contactDeviceData.setRegistration_id(registration_id);
	            	contactDeviceData.setContactData(contactData);
	            	contactDeviceData.setDeviceData(deviceData);

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
