package com.dagrest.tracklocation.db;

import com.dagrest.tracklocation.Controller;
import com.dagrest.tracklocation.datatype.ContactData;
import com.dagrest.tracklocation.datatype.ContactDeviceData;
import com.dagrest.tracklocation.datatype.ContactDeviceDataList;
import com.dagrest.tracklocation.datatype.DeviceData;
import com.dagrest.tracklocation.datatype.DeviceTypeEnum;
import com.dagrest.tracklocation.datatype.JoinRequestStatusEnum;
import com.dagrest.tracklocation.datatype.PermissionsData;
import com.dagrest.tracklocation.datatype.PermissionsDataList;
import com.dagrest.tracklocation.datatype.ReceivedJoinRequestData;
import com.dagrest.tracklocation.datatype.SentJoinRequestData;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBLayer {
	
	private final static String CLASS_NAME  = "com.dagrest.trackloaction.db.DBLayer"; 
	
	protected DBLayer() {
    }
	
//	public static int deleteJoinRequest(String phoneNumber){
//		
//		if(phoneNumber == null || phoneNumber.isEmpty()){
//        	String errMsg = "Delete join request failed - no phone number was provided";
//        	Log.e(DBConst.LOG_TAG_DB, errMsg);
//            LogManager.LogErrorMsg(CLASS_NAME, "deleteJoinRequest", errMsg);
//			return -1;
//		}
//		
//		SQLiteDatabase db = null;
//		try{
//			db = DBManager.getDBManagerInstance().open();
//			 
//			String whereClause = DBConst.PHONE_NUMBER + " = ?";
//			String[] whereArgs = new String[] { phoneNumber };
//			
//			if(!isPhoneInJoinRequestTable(phoneNumber)){
//				return db.delete(DBConst.TABLE_SEND_JOIN_REQUEST, whereClause, whereArgs);
//			}
//		} catch (Throwable t) {
//            if (DBConst.IS_DEBUG_LOG_ENABLED){
//            	String errMsg = "Exception caught: " + t.getMessage();
//            	Log.e(DBConst.LOG_TAG_DB, errMsg, t);
//                LogManager.LogErrorMsg(CLASS_NAME, "deleteJoinRequest", errMsg);
//            }
//		} finally {
//			if(db != null){
//				DBManager.getDBManagerInstance().close();
//			}
//		}
//		return -1;
//	}

	public static long addSentJoinRequest(String phoneNumber, String mutualId, JoinRequestStatusEnum status){
		
		String methodName = "addSentJoinRequest";
		if(phoneNumber == null || phoneNumber.isEmpty()){
        	String errMsg = "Add sent join request failed - no phone number was provided";
        	Log.e(DBConst.LOG_TAG_DB, errMsg);
            LogManager.LogErrorMsg(CLASS_NAME, "addSentJoinRequest", errMsg);
			return -1;
		}
		
		if(mutualId == null || mutualId.isEmpty()){
        	String errMsg = "Add sent join request failed - no mutualId was provided";
        	Log.e(DBConst.LOG_TAG_DB, errMsg);
            LogManager.LogErrorMsg(CLASS_NAME, "addSentJoinRequest", errMsg);
			return -1;
		}
		
		SQLiteDatabase db = null;
		try{
			db = DBManager.getDBManagerInstance().open();
			 
			ContentValues cVal = new ContentValues();
			cVal.put(DBConst.PHONE_NUMBER, phoneNumber);
			cVal.put(DBConst.MUTUAL_ID, mutualId);
			cVal.put(DBConst.STATUS, status.toString());
			cVal.put(DBConst.TIMESTAMP, Controller.getDateTime());
			
			if(!isPhoneInJoinRequestTable(phoneNumber)){
				return db.insert(DBConst.TABLE_SEND_JOIN_REQUEST, null, cVal);
			} else {
				return db.update(DBConst.TABLE_SEND_JOIN_REQUEST, cVal, DBConst.PHONE_NUMBER + " = ? ", new String[] { phoneNumber });
			}
		} catch (Throwable t) {
        	String errMsg = "EXCEPTION caught: " + t.getMessage();
        	Log.e(DBConst.LOG_TAG_DB, errMsg, t);
            LogManager.LogErrorMsg(CLASS_NAME, methodName, errMsg);
		} finally {
			if(db != null){
				DBManager.getDBManagerInstance().close();
			}
		}
		return -1;
	}
	
	public static int deleteSentJoinRequest(String inMutualId){
		
		if(inMutualId == null || inMutualId.isEmpty()){
        	String errMsg = "Delete sent join request failed - no mutualId was provided";
        	Log.e(DBConst.LOG_TAG_DB, errMsg);
            LogManager.LogErrorMsg(CLASS_NAME, "deleteSentJoinRequest", errMsg);
			return -1;
		}
		
		SQLiteDatabase db = null;
		try{
			db = DBManager.getDBManagerInstance().open();
			 
			String whereClause = DBConst.MUTUAL_ID + " = ?";
			String[] whereArgs = new String[] { inMutualId };
			
			return db.delete(DBConst.TABLE_SEND_JOIN_REQUEST, whereClause, whereArgs);
		} catch (Throwable t) {
            if (DBConst.IS_DEBUG_LOG_ENABLED){
            	Log.e(DBConst.LOG_TAG_DB, "Exception: DBHelper.onCreate() exception", t);
            }
        	String errMsg = "Exception caught: " + t.getMessage();
        	Log.e(DBConst.LOG_TAG_DB, errMsg, t);
            LogManager.LogErrorMsg(CLASS_NAME, "deleteSentJoinRequest", errMsg);
		} finally {
			if(db != null){
				DBManager.getDBManagerInstance().close();
			}
		}
		return -1;
	}

	public static SentJoinRequestData getSentJoinRequestByMutualId(String inMutualId){
		SentJoinRequestData sentJoinRequestData = null;
		SQLiteDatabase db = null;
		try{
			db = DBManager.getDBManagerInstance().open();
			
	        // Select All Query
			String selectQuery = "select " + DBConst.PHONE_NUMBER + ", " + 
				DBConst.MUTUAL_ID + ", " + DBConst.STATUS + ", " +  
				DBConst.TIMESTAMP + 
				" from " + DBConst.TABLE_SEND_JOIN_REQUEST + 
				" where " + DBConst.MUTUAL_ID + " = ?";
	        Cursor cursor = db.rawQuery(selectQuery, new String[] { inMutualId });
	  
	        if (cursor.moveToFirst()) {
	        	sentJoinRequestData = new SentJoinRequestData();
            	
            	String phoneNumber = cursor.getString(0);
            	String mutualId = cursor.getString(1);
            	String status = cursor.getString(2);
            	String timestamp = cursor.getString(3);

            	sentJoinRequestData.setPhoneNumber(phoneNumber);
            	sentJoinRequestData.setMutualId(mutualId);
            	sentJoinRequestData.setStatus(status);
            	sentJoinRequestData.setTimestamp(timestamp);
	        }
	        cursor.close();
        } catch (Throwable t) {
        	String errMsg = "Exception caught: " + t.getMessage();
        	Log.e(DBConst.LOG_TAG_DB, errMsg, t);
            LogManager.LogErrorMsg(CLASS_NAME, "getSentJoinRequestByMutualId", errMsg);
		} finally {
			if(db != null){
				DBManager.getDBManagerInstance().close();
			}
		}
    	return sentJoinRequestData;
    }

	public static long addFullPermissions(String email){
		
		if(email == null || email.isEmpty()){
        	String errMsg = "Add permissions failed - no email account was provided";
        	Log.e(DBConst.LOG_TAG_DB, errMsg);
            LogManager.LogErrorMsg(CLASS_NAME, "addPermissions", errMsg);
			return -1;
		}
		
		SQLiteDatabase db = null;
		try{
			db = DBManager.getDBManagerInstance().open();
			 
			ContentValues cVal = new ContentValues();
			cVal.put(DBConst.EMAIL, email);
			cVal.put(DBConst.LOCATION, 1);
			cVal.put(DBConst.COMMAND, 1);
			cVal.put(DBConst.ADMIN_COMMAND, 1);
			
			if(!isEmailInPermissionsTable(email)){
				return db.insert(DBConst.TABLE_PERMISSIONS, null, cVal);
			} 
		} catch (Throwable t) {
        	String errMsg = "Exception caught: " + t.getMessage();
        	Log.e(DBConst.LOG_TAG_DB, errMsg, t);
            LogManager.LogErrorMsg(CLASS_NAME, "addPermissions", errMsg);
		} finally {
			if(db != null){
				DBManager.getDBManagerInstance().close();
			}
		}
		return -1;
	}
	
	public static long updatePermissions(String email, int location, int command, int admin_command){
		
		if(email == null || email.isEmpty()){
        	String errMsg = "Update permissions failed - no email account was provided";
        	Log.e(DBConst.LOG_TAG_DB, errMsg);
            LogManager.LogErrorMsg(CLASS_NAME, "updatePermissions", errMsg);
			return -1;
		}
		
		SQLiteDatabase db = null;
		try{
			db = DBManager.getDBManagerInstance().open();
			 
			ContentValues cVal = new ContentValues();
			cVal.put(DBConst.EMAIL, email);
			cVal.put(DBConst.LOCATION, location);
			cVal.put(DBConst.COMMAND, command);
			cVal.put(DBConst.ADMIN_COMMAND, admin_command);
			
			if(isEmailInPermissionsTable(email)){
				return db.update(DBConst.TABLE_PERMISSIONS, cVal, DBConst.EMAIL + " = ? ", new String[] { email });
			}
//			} else {
//				return db.insert(DBConst.TABLE_PERMISSIONS, null, cVal);
//			}
		} catch (Throwable t) {
        	String errMsg = "Exception caught: " + t.getMessage();
        	Log.e(DBConst.LOG_TAG_DB, errMsg, t);
            LogManager.LogErrorMsg(CLASS_NAME, "updatePermissions", errMsg);
		} finally {
			if(db != null){
				DBManager.getDBManagerInstance().close();
			}
		}
		return -1;
	}

	public static PermissionsData getPermissions (String inEmail){
		PermissionsData permissionsData = null;
		SQLiteDatabase db = null;
		try{
			db = DBManager.getDBManagerInstance().open();
			
	    	// Select All Query
			String selectQuery = "select " + DBConst.EMAIL + ", " + 
				DBConst.LOCATION + ", " + DBConst.COMMAND + ", " +  
				DBConst.ADMIN_COMMAND + 
				" from " + DBConst.TABLE_PERMISSIONS +
				" where " + DBConst.EMAIL + " = ?";
			
			if(inEmail == null || inEmail.isEmpty()){
				return null;
			}
			
	        Cursor cursor = db.rawQuery(selectQuery, new String[] { inEmail });
	  
	        if (cursor.moveToFirst()) {
	        	permissionsData = new PermissionsData();
            	
            	String email = cursor.getString(0);
            	int location = cursor.getInt(1);
            	int command = cursor.getInt(2);
            	int adminCommand = cursor.getInt(3);

            	permissionsData.setEmail(email);
            	permissionsData.setIsLocationSharePermitted(location);
            	permissionsData.setCommand(command);
            	permissionsData.setAdminCommand(adminCommand);
	        }
	        cursor.close();
        } catch (Throwable t) {
        	String errMsg = "Exception caught: " + t.getMessage();
        	Log.e(DBConst.LOG_TAG_DB, errMsg, t);
        	LogManager.LogErrorMsg(CLASS_NAME, "getPermissions", errMsg);
		} finally {
			if(db != null){
				DBManager.getDBManagerInstance().close();
			}
		}
    	return permissionsData;
    }

	public static PermissionsDataList getPermissionsList (String inEmail){
		PermissionsDataList permissionsDataList = null;
		SQLiteDatabase db = null;
		try{
			db = DBManager.getDBManagerInstance().open();
			
	    	// Select All Query
			String selectQuery = "select " + DBConst.EMAIL + ", " + 
				DBConst.LOCATION + ", " + DBConst.COMMAND + ", " +  
				DBConst.ADMIN_COMMAND + 
				" from " + DBConst.TABLE_PERMISSIONS; // +
				//" where " + DBConst.EMAIL + " = ?";
			
//			if(inEmail != null && !inEmail.isEmpty()){
//				return null;
//			}
			
//	        Cursor cursor = db.rawQuery(selectQuery, new String[] { inEmail });
			Cursor cursor = db.rawQuery(selectQuery, null);
	  
	        if (cursor.moveToFirst()) {
	        	permissionsDataList = new PermissionsDataList();
	        	do {
	        		PermissionsData permissionsData = new PermissionsData();
	            	
	            	String email = cursor.getString(0);
	            	int location = cursor.getInt(1);
	            	int command = cursor.getInt(2);
	            	int adminCommand = cursor.getInt(3);
	
	            	permissionsData.setEmail(email);
	            	permissionsData.setIsLocationSharePermitted(location);
	            	permissionsData.setCommand(command);
	            	permissionsData.setAdminCommand(adminCommand);
	            	
	            	permissionsDataList.getPermissionsData().add(permissionsData);
	            	
	        	} while (cursor.moveToNext());
	        }
	        cursor.close();
        } catch (Throwable t) {
        	String errMsg = "Exception caught: " + t.getMessage();
        	Log.e(DBConst.LOG_TAG_DB, errMsg, t);
        	LogManager.LogErrorMsg(CLASS_NAME, "getPermissionsList", errMsg);
		} finally {
			if(db != null){
				DBManager.getDBManagerInstance().close();
			}
		}
    	return permissionsDataList;
    }

	public static long addReceivedJoinRequest(String phoneNumber, String mutualId, String regId, String account, String macAddress){
		
		if(phoneNumber == null || phoneNumber.isEmpty()){
        	String errMsg = "Add received join request failed - no phone number was provided";
        	Log.e(DBConst.LOG_TAG_DB, errMsg);
            LogManager.LogErrorMsg(CLASS_NAME, "addReceivedJoinRequest", errMsg);
			return -1;
		}
		
		if(mutualId == null || mutualId.isEmpty()){
        	String errMsg = "Add received join request failed - no mutualId was provided";
        	Log.e(DBConst.LOG_TAG_DB, errMsg);
            LogManager.LogErrorMsg(CLASS_NAME, "addReceivedJoinRequest", errMsg);
			return -1;
		}
		
		if(regId == null || regId.isEmpty()){
        	String errMsg = "Add received join request failed - no registrationId was provided";
        	Log.e(DBConst.LOG_TAG_DB, errMsg);
            LogManager.LogErrorMsg(CLASS_NAME, "addReceivedJoinRequest", errMsg);
			return -1;
		}

		if(macAddress == null || macAddress.isEmpty()){
        	String errMsg = "Add received join request failed - no macAddress was provided";
        	Log.e(DBConst.LOG_TAG_DB, errMsg);
            LogManager.LogErrorMsg(CLASS_NAME, "addReceivedJoinRequest", errMsg);
			return -1;
		}

		SQLiteDatabase db = null;
		try{
			db = DBManager.getDBManagerInstance().open();
			 
			ContentValues cVal = new ContentValues();
			cVal.put(DBConst.PHONE_NUMBER, phoneNumber);
			cVal.put(DBConst.MUTUAL_ID, mutualId);
			cVal.put(DBConst.REG_ID, regId);
			cVal.put(DBConst.TIMESTAMP, Controller.getDateTime());
			cVal.put(DBConst.MAC_ADDRESS, macAddress);
			cVal.put(DBConst.RECEIVED_JOIN_REQUEST_ACCOUNT, account);
			
			if(!isPhoneInReceivedJoinRequestTable(phoneNumber)){
				return db.insert(DBConst.TABLE_RECEIVED_JOIN_REQUEST, null, cVal);
			} else {
				return db.update(DBConst.TABLE_RECEIVED_JOIN_REQUEST, cVal, DBConst.PHONE_NUMBER + " = ? ", new String[] { phoneNumber });
			}
		} catch (Throwable t) {
        	String errMsg = "Exception caught: " + t.getMessage();
        	Log.e(DBConst.LOG_TAG_DB, errMsg, t);
            LogManager.LogErrorMsg(CLASS_NAME, "addReceivedJoinRequest", errMsg);
		} finally {
			if(db != null){
				DBManager.getDBManagerInstance().close();
			}
		}
		return -1;
	}

	public static int deleteReceivedJoinRequest(String inMutualId){
		
		if(inMutualId == null || inMutualId.isEmpty()){
        	String errMsg = "Delete received join request failed - no mutualId was provided";
        	Log.e(DBConst.LOG_TAG_DB, errMsg);
            LogManager.LogErrorMsg(CLASS_NAME, "deleteReceivedJoinRequest", errMsg);
			return -1;
		}
		
		SQLiteDatabase db = null;
		try{
			db = DBManager.getDBManagerInstance().open();
			 
			String whereClause = DBConst.MUTUAL_ID + " = ?";
			String[] whereArgs = new String[] { inMutualId };
			
			return db.delete(DBConst.TABLE_RECEIVED_JOIN_REQUEST, whereClause, whereArgs);
		} catch (Throwable t) {
            if (DBConst.IS_DEBUG_LOG_ENABLED){
            	Log.e(DBConst.LOG_TAG_DB, "Exception: DBHelper.onCreate() exception", t);
            }
        	String errMsg = "Exception caught: " + t.getMessage();
        	Log.e(DBConst.LOG_TAG_DB, errMsg, t);
            LogManager.LogErrorMsg(CLASS_NAME, "deleteReceivedJoinRequest", errMsg);
		} finally {
			if(db != null){
				DBManager.getDBManagerInstance().close();
			}
		}
		return -1;
	}

	public static SentJoinRequestData getSentJoinRequestByPhone(String phoneNumberIn){
		SentJoinRequestData sentJoinRequestData = null;
		SQLiteDatabase db = null;
		try{
			db = DBManager.getDBManagerInstance().open();
			
	        // Select All Query
			String selectQuery = "select " + DBConst.PHONE_NUMBER + ", " + 
				DBConst.MUTUAL_ID + ", " + DBConst.STATUS + ", " + DBConst.TIMESTAMP + 
				" from " + DBConst.TABLE_SEND_JOIN_REQUEST + 
				" where " + DBConst.PHONE_NUMBER + " = ?";
	        Cursor cursor = db.rawQuery(selectQuery, new String[] { phoneNumberIn });
	  
	        if (cursor.moveToFirst()) {
            	sentJoinRequestData = new SentJoinRequestData();
            	
            	String phoneNumber = cursor.getString(0);
            	String mutualId = cursor.getString(1);
            	String status = cursor.getString(2);
            	String timestamp = cursor.getString(3);

            	sentJoinRequestData.setPhoneNumber(phoneNumber);
            	sentJoinRequestData.setMutualId(mutualId);
            	sentJoinRequestData.setStatus(status);
            	sentJoinRequestData.setTimestamp(timestamp);
	        }
	        cursor.close();
        } catch (Throwable t) {
        	String errMsg = "Exception caught: " + t.getMessage();
        	Log.e(DBConst.LOG_TAG_DB, errMsg, t);
            LogManager.LogErrorMsg(CLASS_NAME, "getSentJoinRequestByPhone", errMsg);
		} finally {
			if(db != null){
				DBManager.getDBManagerInstance().close();
			}
		}
    	return sentJoinRequestData;
    }

	public static ReceivedJoinRequestData getReceivedJoinRequest(String requestMutualId){
		ReceivedJoinRequestData receivedJoinRequestData = null;
		SQLiteDatabase db = null;
		try{
			db = DBManager.getDBManagerInstance().open();
			
	        // Select All Query
			String selectQuery = "select " + DBConst.PHONE_NUMBER + ", " + 
				DBConst.MUTUAL_ID + ", " + DBConst.REG_ID + ", " +  
				DBConst.RECEIVED_JOIN_REQUEST_ACCOUNT + ", " + 
				DBConst.MAC_ADDRESS + ", " + DBConst.TIMESTAMP + 
				" from " + DBConst.TABLE_RECEIVED_JOIN_REQUEST + 
				" where " + DBConst.MUTUAL_ID + " = ?";
	        Cursor cursor = db.rawQuery(selectQuery, new String[] { requestMutualId });
	  
	        if (cursor.moveToFirst()) {
	        	receivedJoinRequestData = new ReceivedJoinRequestData();
            	
            	String phoneNumber = cursor.getString(0);
            	String mutualId = cursor.getString(1);
            	String regId = cursor.getString(2);
            	String account = cursor.getString(3);
            	String macAddress = cursor.getString(4);
            	String timestamp = cursor.getString(5);

            	receivedJoinRequestData.setPhoneNumber(phoneNumber);
            	receivedJoinRequestData.setMutualId(mutualId);
            	receivedJoinRequestData.setRegId(regId);
            	receivedJoinRequestData.setAccount(account);
            	receivedJoinRequestData.setMacAddress(macAddress);
            	receivedJoinRequestData.setTimestamp(timestamp);
	        }
	        cursor.close();
        } catch (Throwable t) {
        	String errMsg = "Exception caught: " + t.getMessage();
        	Log.e(DBConst.LOG_TAG_DB, errMsg, t);
            LogManager.LogErrorMsg(CLASS_NAME, "getReceivedJoinRequest", errMsg);
		} finally {
			if(db != null){
				DBManager.getDBManagerInstance().close();
			}
		}
    	return receivedJoinRequestData;
    }

	// Insert contact data
	public static ContactData addContactData(ContactData contactData) {
		String nick = contactData.getNick();
		String firstName = contactData.getFirstName();
		String lastName = contactData.getLastName();
		String contactEmail = contactData.getEmail();
		if(contactEmail == null || contactEmail.isEmpty()){
			return null;
		}
		return addContactData(nick, firstName, lastName, contactEmail);
	}

	// Insert contact data
	public static ContactData addContactData(String nick, String firstName, String lastName, 
		String contactEmail) 
	{
		ContactData contactData = null;
		SQLiteDatabase db = null;

		if(contactEmail == null || contactEmail.isEmpty()){
        	String errMsg = "Add contact data failed - no email account was provided";
        	Log.e(DBConst.LOG_TAG_DB, errMsg);
            LogManager.LogErrorMsg(CLASS_NAME, "addContactData", errMsg);
			return null;
		}
		
		try{
			db = DBManager.getDBManagerInstance().open();
			
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
        	String errMsg = "Exception caught: " + t.getMessage();
        	Log.e(DBConst.LOG_TAG_DB, errMsg, t);
            LogManager.LogErrorMsg(CLASS_NAME, "addContactData", errMsg);
		} finally {
			if(db != null){
				DBManager.getDBManagerInstance().close();
			}
		}
		return contactData;
	}
    
	// Insert contact data
	public static DeviceData addDeviceData(DeviceData deviceData) {
		String deviceMac = deviceData.getDeviceMac();
		DeviceTypeEnum deviceTypeEnum = deviceData.getDeviceTypeEnum();
		if(deviceTypeEnum == null){
			deviceTypeEnum = DeviceTypeEnum.unknown;
		}
		String deviceName = deviceData.getDeviceName();
		if(deviceMac == null || deviceMac.isEmpty()){
			return null;
		}
		return addDeviceData(deviceMac, deviceName, deviceTypeEnum);
	}

    // Insert device data
    public static DeviceData addDeviceData(String  macAddress, String deviceName, DeviceTypeEnum deviceTypeEnum) 
     {
    	DeviceData deviceData = null;
		SQLiteDatabase db = null;

		if(macAddress == null || macAddress.isEmpty()){
        	String errMsg = "Add device data failed - no … was provided";
        	Log.e(DBConst.LOG_TAG_DB, errMsg);
            LogManager.LogErrorMsg(CLASS_NAME, "addDeviceData", errMsg);
			return null;
		}

		try{
			db = DBManager.getDBManagerInstance().open();
            
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
        	String errMsg = "Exception caught: " + t.getMessage();
        	Log.e(DBConst.LOG_TAG_DB, errMsg, t);
            LogManager.LogErrorMsg(CLASS_NAME, "addDeviceData", errMsg);
		} finally {
			if(db != null){
				DBManager.getDBManagerInstance().close();
			}
		}
        return deviceData;
    }

    // Insert contact/device data
    private static ContactDeviceData addContactDeviceData(String phoneNumber, ContactData  contactData, 
    		DeviceData deviceData, String imei, String registartionId, String guid)
     {
    	ContactDeviceData contactDeviceData = null;
		SQLiteDatabase db = null;
		try{
			db = DBManager.getDBManagerInstance().open();
			
			contactDeviceData = new ContactDeviceData();
			
            contactDeviceData.setPhoneNumber(sqlEscapeString(phoneNumber));
            contactDeviceData.setContactData(contactData);
            contactDeviceData.setDeviceData(deviceData);
            contactDeviceData.setImei(sqlEscapeString(imei));
            contactDeviceData.setRegistration_id(sqlEscapeString(registartionId));
            if(guid == null){
            	contactDeviceData.setGuid(Controller.generateUUID());
            } else {
            	contactDeviceData.setGuid(guid);
            }
            
            ContentValues cVal = new ContentValues();
            cVal.put(DBConst.CONTACT_DEVICE_PHONE_NUMBER, contactDeviceData.getPhoneNumber());
            cVal.put(DBConst.CONTACT_DEVICE_EMAIL, contactDeviceData.getContactData().getEmail());
            cVal.put(DBConst.CONTACT_DEVICE_MAC, contactDeviceData.getDeviceData().getDeviceMac());
            cVal.put(DBConst.CONTACT_DEVICE_REG_ID, contactDeviceData.getRegistration_id());
            cVal.put(DBConst.CONTACT_DEVICE_IMEI, contactDeviceData.getImei());
            cVal.put(DBConst.CONTACT_DEVICE_GUID, contactDeviceData.getGuid());
            
            db.insert(DBConst.TABLE_CONTACT_DEVICE, null, cVal);
        } catch (Throwable t) {
        	contactDeviceData = null;
            Log.e("Database", "Exception caught: " + t.getMessage(), t);
            LogManager.LogErrorMsg(CLASS_NAME, "addContactDeviceData", t.getMessage());
		} finally {
			if(db != null){
				DBManager.getDBManagerInstance().close();
			}
		}
        return contactDeviceData;
    }
    
	public static long updateRegistrationID(String email, String macAddress, String registrationID){
		
		if(email == null || email.isEmpty()){
        	String errMsg = "Update RegistrationID failed - no email account was provided";
        	Log.e(DBConst.LOG_TAG_DB, errMsg);
            LogManager.LogErrorMsg(CLASS_NAME, "updateRegistrationID", errMsg);
			return -1;
		}
		
		if(macAddress == null || macAddress.isEmpty()){
        	String errMsg = "Update RegistrationID failed - no macAddress was provided";
        	Log.e(DBConst.LOG_TAG_DB, errMsg);
            LogManager.LogErrorMsg(CLASS_NAME, "updateRegistrationID", errMsg);
			return -1;
		}

		if(registrationID == null || registrationID.isEmpty()){
        	String errMsg = "Update RegistrationID failed - no registrationID was provided";
        	Log.e(DBConst.LOG_TAG_DB, errMsg);
            LogManager.LogErrorMsg(CLASS_NAME, "updateRegistrationID", errMsg);
			return -1;
		}

		SQLiteDatabase db = null;
		try{
			db = DBManager.getDBManagerInstance().open();
			 
			ContentValues cVal = new ContentValues();
			cVal.put(DBConst.CONTACT_DEVICE_REG_ID, registrationID);
			
			if(isEmailMacAddressInContactDeviceTable(email, macAddress)){
				return db.update(DBConst.TABLE_CONTACT_DEVICE, cVal, 
					DBConst.CONTACT_DEVICE_EMAIL + " = ? and " + DBConst.CONTACT_DEVICE_MAC + " = ?", 
					new String[] { email, macAddress });
			} else {
	        	String errMsg = "Update RegistrationID failed - no email and registrationID were found in ContactDeviceTable";
	        	Log.e(DBConst.LOG_TAG_DB, errMsg);
	            LogManager.LogErrorMsg(CLASS_NAME, "updateRegistrationID", errMsg);
	            return -1;
			}
		} catch (Throwable t) {
        	String errMsg = "Exception caught: " + t.getMessage();
        	Log.e(DBConst.LOG_TAG_DB, errMsg, t);
            LogManager.LogErrorMsg(CLASS_NAME, "updateRegistrationID", errMsg);
		} finally {
			if(db != null){
				DBManager.getDBManagerInstance().close();
			}
		}
		return -1;
	}

    public static boolean isEmailMacAddressInContactDeviceTable(String email, String macAddress){
		String selectQuery = "select " + DBConst.CONTACT_DEVICE_EMAIL + "," + DBConst.CONTACT_DEVICE_MAC + 
				" from " + DBConst.TABLE_CONTACT_DEVICE +
				" where " + DBConst.CONTACT_DEVICE_EMAIL + " = ? and " +
				DBConst.CONTACT_DEVICE_MAC + " = ?";
    	return isFieldExist(selectQuery, new String[] { email, macAddress });
    }
    
	public static ContactDeviceDataList addContactDeviceDataList(ContactDeviceDataList contactDeviceDataList){
    	
    	ContactDeviceDataList contactDeviceDataListInserted = null;
    	
    	if(contactDeviceDataList != null){
			for (ContactDeviceData contactDeviceData : contactDeviceDataList.getContactDeviceDataList()) {
				if(contactDeviceData != null){
					
					ContactData contactData = contactDeviceData.getContactData();
					DeviceData deviceData = contactDeviceData.getDeviceData();
					String phoneNumber = contactDeviceData.getPhoneNumber();
					String registrationId = contactDeviceData.getRegistration_id();
					String imei = contactDeviceData.getImei();
					String guid = contactDeviceData.getGuid();
					
					String email = null;
					if(contactData != null){
						email = contactData.getEmail();
					} else {
						String errMsg = "Unable to add contactDeviceData to DB - no email account was provided";
						Log.e(CommonConst.LOG_TAG, errMsg);
						LogManager.LogErrorMsg("DBLayer", "addContactDeviceDataList", errMsg);
						continue;
					}
					
					String nick = contactData.getNick();
					if( nick == null || nick.isEmpty() ){
						contactData.setNick(Controller.getNickNameFromEmail(email));
					}
					
					String macAddress = null;
					if(deviceData != null){
						macAddress = deviceData.getDeviceMac();
					} else {
						String errMsg = "Unable to add contactDeviceData to DB - no macAddress was provided";
						Log.e(CommonConst.LOG_TAG, errMsg);
						LogManager.LogErrorMsg("DBLayer", "addContactDeviceDataList", errMsg);
						continue;
					}
					
					if(registrationId == null || registrationId.isEmpty()){
						String errMsg = "Unable to add contactDeviceData to DB - no registrationId was provided";
						Log.e(CommonConst.LOG_TAG, errMsg);
						LogManager.LogErrorMsg("DBLayer", "addContactDeviceDataList", errMsg);
						continue;
					}
					
					boolean isEmailExist = isContactWithEmailExist(email);
					boolean isMacAddressExist = isDeviceWithMacAddressExist(macAddress);
					
					if(!isContactDeviceExist(email, macAddress, phoneNumber)){
						ContactData resultContactData = 
							addContactData(contactData);
						DeviceData resultDeviceData = 
							addDeviceData(deviceData);
						ContactDeviceData resultContactDeviceData = addContactDeviceData(phoneNumber, contactData, 
							deviceData, imei, registrationId, guid);
						if(resultContactDeviceData == null){
							String errMsg = "Failed to add contactDeviceData to DB by addContactDeviceData()";
							Log.e(CommonConst.LOG_TAG, errMsg);
							LogManager.LogErrorMsg("DBLayer", "addContactDeviceDataList", errMsg);
						}
						if(addFullPermissions(email) < 0){
							String errMsg = "Failed to add FULL permissions for the following account: " + email;
							Log.e(CommonConst.LOG_TAG, errMsg);
							LogManager.LogErrorMsg("DBLayer", "addContactDeviceDataList", errMsg);
						}
			    	} else {
						String infoMsg = "Account [" + email + "] already exists in DB";
						Log.i(CommonConst.LOG_TAG, infoMsg);
						LogManager.LogInfoMsg("DBLayer", "addContactDeviceDataList", infoMsg);
			    	}
		    	}
			}
			contactDeviceDataListInserted = getContactDeviceDataList(null);
    	}
		return contactDeviceDataListInserted;
    }

    public static ContactData getContactData(){
    	ContactData contactData = null;
		SQLiteDatabase db = null;
		try{
			db = DBManager.getDBManagerInstance().open();
            
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
        	String errMsg = "Exception caught: " + t.getMessage();
        	Log.e(DBConst.LOG_TAG_DB, errMsg, t);
            LogManager.LogErrorMsg(CLASS_NAME, "getContactData", errMsg);
		} finally {
			if(db != null){
				DBManager.getDBManagerInstance().close();
			}
		}
    	return contactData;
    }

    public static DeviceData getDeviceData(){
    	DeviceData deviceData = null;
		SQLiteDatabase db = null;
		try{
			db = DBManager.getDBManagerInstance().open();
			
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
        	String errMsg = "Exception caught: " + t.getMessage();
        	Log.e(DBConst.LOG_TAG_DB, errMsg, t);
            LogManager.LogErrorMsg(CLASS_NAME, "getDeviceData", errMsg);
		} finally {
			if(db != null){
				DBManager.getDBManagerInstance().close();
			}
		}
    	return deviceData;
    }

    public static ContactDeviceData getContactDeviceDataONLY(){
    	ContactDeviceData contactDeviceData = null;
		SQLiteDatabase db = null;
		try{
			db = DBManager.getDBManagerInstance().open();
            
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
	            	String guid = cursor.getString(5);
	            	
	            	ContactData contactData = new ContactData();
	            	contactData.setEmail(email);
	            	
	            	DeviceData deviceData = new DeviceData();
	            	deviceData.setDeviceMac(mac);
	            	
	            	contactDeviceData.setPhoneNumber(phone);
	            	contactDeviceData.setImei(imei);
	            	contactDeviceData.setRegistration_id(regId);
	            	contactDeviceData.setContactData(contactData);
	            	contactDeviceData.setDeviceData(deviceData);
	            	contactDeviceData.setGuid(guid);

	            } while (cursor.moveToNext());
	        }
	        cursor.close();
        } catch (Throwable t) {
        	String errMsg = "Exception caught: " + t.getMessage();
        	Log.e(DBConst.LOG_TAG_DB, errMsg, t);
            LogManager.LogErrorMsg(CLASS_NAME, "getContactDeviceDataONLY", errMsg);
		} finally {
			if(db != null){
				DBManager.getDBManagerInstance().close();
			}
		}
    	return contactDeviceData;
    }

//    public static ContactDeviceDataList getContactDeviceDataList(){
// 
//    	ContactDeviceDataList contactDeviceDataList = null;
//    	ContactDeviceData contactDeviceData = null;
//		
//    	SQLiteDatabase db = null;
//		try{
//			db = DBManager.getDBManagerInstance().open();
//            
//	        // Select All Query
//	        String selectQuery = 
//	        "select " +		
//	        "contact_first_name, contact_last_name, contact_nick, contact_email, device_mac, " +
//	        "device_name, device_type, contact_device_imei, contact_device_phone_number, " +
//	        "registration_id, contact_device_guid  " +
//	        "from TABLE_CONTACT_DEVICE as CD " +
//	        "join TABLE_CONTACT as C " +
//	        "on CD.contact_device_email = C.contact_email " +
//	        "join TABLE_DEVICE as D " +
//	        "on CD.contact_device_mac = D.device_mac";	  
//	        Cursor cursor = db.rawQuery(selectQuery, null);
//	  
//	        // looping through all rows and adding to list
//	        if (cursor.moveToFirst()) {
//	        	contactDeviceDataList = new ContactDeviceDataList();
//	            do {
//	            	contactDeviceData = new ContactDeviceData();
//	            	
//	            	String contact_first_name = cursor.getString(0);
//	            	String contact_last_name = cursor.getString(1);
//	            	String contact_nick = cursor.getString(2);
//	            	String contact_email = cursor.getString(3);
//	            	String device_mac = cursor.getString(4);
//	            	String device_name = cursor.getString(5);
//	            	String device_type = cursor.getString(6);
//	            	String contact_device_imei = cursor.getString(7);
//	            	String contact_device_phone_number = cursor.getString(8);
//	            	String registration_id = cursor.getString(9);
//	            	String guid = cursor.getString(10);
//	            	
//	            	ContactData contactData = new ContactData();
//	            	contactData.setEmail(contact_email);
//	            	contactData.setFirstName(contact_first_name);
//	            	contactData.setLastName(contact_last_name);
//	            	contactData.setNick(contact_nick);
//	            	
//	            	DeviceData deviceData = new DeviceData();
//	            	deviceData.setDeviceMac(device_mac);
//	            	deviceData.setDeviceName(device_name);
//	            	deviceData.setDeviceTypeEnum(DeviceTypeEnum.getValue(device_type));
//	            	
//	            	contactDeviceData.setPhoneNumber(contact_device_phone_number);
//	            	contactDeviceData.setImei(contact_device_imei);
//	            	contactDeviceData.setRegistration_id(registration_id);
//	            	contactDeviceData.setContactData(contactData);
//	            	contactDeviceData.setDeviceData(deviceData);
//	            	contactDeviceData.setGuid(guid);
//
//	            	contactDeviceDataList.getContactDeviceDataList().add(contactDeviceData);
//	            	
//	            } while (cursor.moveToNext());
//	        }
//	        cursor.close();
//        } catch (Throwable t) {
//            Log.i("Database", "Exception caught: " + t.getMessage(), t);
//		} finally {
//			if(db != null){
//				DBManager.getDBManagerInstance().close();
//			}
//		}
//    	return contactDeviceDataList;
//    }
    
    public static ContactDeviceDataList getContactDeviceDataList(String email){
    	 
    	ContactDeviceDataList contactDeviceDataList = null;
    	ContactDeviceData contactDeviceData = null;
		
    	SQLiteDatabase db = null;
		try{
			db = DBManager.getDBManagerInstance().open();
            
	        // Select All Query
	        String selectQuery = 
	        "select " +		
	        "contact_first_name, contact_last_name, contact_nick, contact_email, device_mac, " +
	        "device_name, device_type, contact_device_imei, contact_device_phone_number, " +
	        "registration_id, contact_device_guid " +
	        "from TABLE_CONTACT_DEVICE as CD " +
	        "join TABLE_CONTACT as C " +
	        "on CD.contact_device_email = C.contact_email " +
	        "join TABLE_DEVICE as D " +
	        "on CD.contact_device_mac = D.device_mac ";
	        
	        String[] val = null;
	        if(email !=  null && !email.isEmpty()){
	        	selectQuery = selectQuery + "where CD.contact_device_email = ?";
	        	val = new String[] { email };
	        }
	        
	        Cursor cursor = db.rawQuery(selectQuery, val);
	  
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
	            	String guid = cursor.getString(10);
	            	
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
	            	contactDeviceData.setGuid(guid);

	            	contactDeviceDataList.getContactDeviceDataList().add(contactDeviceData);
	            	
	            } while (cursor.moveToNext());
	        }
	        cursor.close();
        } catch (Throwable t) {
        	String errMsg = "Exception caught: " + t.getMessage();
        	Log.e(DBConst.LOG_TAG_DB, errMsg, t);
            LogManager.LogErrorMsg(CLASS_NAME, "getContactDeviceDataList", errMsg);
		} finally {
			if(db != null){
				DBManager.getDBManagerInstance().close();
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

    public static boolean isContactDeviceExist(String email, String macAddress, String phoneNumber){
		String selectQuery = "select " +		
	        "contact_first_name, contact_last_name, contact_nick, contact_email, device_mac, " +
	        "device_name, device_type, contact_device_imei, contact_device_phone_number, " +
	        "registration_id, contact_device_guid " +
	        "from " + DBConst.TABLE_CONTACT_DEVICE + " as CD " +
	        "join " + DBConst.TABLE_CONTACT + " as C " +
	        "on CD.contact_device_email = C.contact_email " +
	        "join " + DBConst.TABLE_DEVICE + " as D " +
	        "on CD.contact_device_mac = D.device_mac " +
			"where CD.contact_device_email = ? " +
			"and CD.contact_device_mac = ? " + 
			"and CD.contact_device_phone_number = ? ";
		// TODO: check that phoneNumber, email and macAddress are valid values to avoid SQL injection
		return isFieldExist(selectQuery, new String[] {email, macAddress, phoneNumber});
    }

    public static boolean isPhoneInJoinRequestTable(String phoneNumber){
		String selectQuery = "select " + DBConst.PHONE_NUMBER + " from " + DBConst.TABLE_SEND_JOIN_REQUEST +
				" where " + DBConst.PHONE_NUMBER + " = ?";
		// TODO: check that macAddress is valid value to avoid SQL injection
		return isFieldExist(selectQuery, new String[] { phoneNumber });
    }

    public static boolean isEmailInPermissionsTable(String email){
		String selectQuery = "select " + DBConst.EMAIL + " from " + DBConst.TABLE_PERMISSIONS +
				" where " + DBConst.EMAIL + " = ?";
		// TODO: check that email is valid value to avoid SQL injection
		return isFieldExist(selectQuery, new String[] { email });
    }

    public static boolean isPhoneInReceivedJoinRequestTable(String phoneNumber){
		String selectQuery = "select " + DBConst.PHONE_NUMBER + " from " + DBConst.TABLE_RECEIVED_JOIN_REQUEST +
				" where " + DBConst.PHONE_NUMBER + " = ?";
		// TODO: check that macAddress is valid value to avoid SQL injection
		return isFieldExist(selectQuery, new String[] { phoneNumber });
    }

    private static boolean isFieldExist(String selectQuery, String[] val){
    	boolean result = false;
		SQLiteDatabase db = null;
		try{
			db = DBManager.getDBManagerInstance().open();
            
	        Cursor cursor = db.rawQuery(selectQuery, val);
	        int count = cursor.getCount();
	        cursor.close();
	        if(count > 0){
	        	result = true;
	        }        
	    } catch (Throwable t) {
        	String errMsg = "Exception caught: " + t.getMessage();
        	Log.e(DBConst.LOG_TAG_DB, errMsg, t);
            LogManager.LogErrorMsg(CLASS_NAME, "isFieldExist", errMsg);
		} finally {
			if(db != null){
				DBManager.getDBManagerInstance().close();
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
			db = DBManager.getDBManagerInstance().open();
			
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
        	String errMsg = "Exception caught: " + t.getMessage();
        	Log.e(DBConst.LOG_TAG_DB, errMsg, t);
            LogManager.LogErrorMsg(CLASS_NAME, "getContactDeviceData", errMsg);
		} finally {
			if(db != null){
				DBManager.getDBManagerInstance().close();
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
