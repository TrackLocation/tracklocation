package com.doat.tracklocation.db;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.doat.tracklocation.Controller;
import com.doat.tracklocation.datatype.ContactData;
import com.doat.tracklocation.datatype.ContactDeviceData;
import com.doat.tracklocation.datatype.ContactDeviceDataList;
import com.doat.tracklocation.datatype.DeviceData;
import com.doat.tracklocation.datatype.DeviceTypeEnum;
import com.doat.tracklocation.datatype.JoinRequestStatusEnum;
import com.doat.tracklocation.datatype.PermissionsData;
import com.doat.tracklocation.datatype.PermissionsDataList;
import com.doat.tracklocation.datatype.ReceivedJoinRequestData;
import com.doat.tracklocation.datatype.SentJoinRequestData;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;

public class DBLayer {
	
	private static DBLayer _instance = null;
	
    private String className;
    private String logMessage;
    private String methodName;
    
    private static class DbBitmapUtility {
        // convert from bitmap to byte array
        public static byte[] getBytes(Bitmap bitmap) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(CompressFormat.PNG, 0, stream);
            return stream.toByteArray();
        }

        // convert from byte array to bitmap
        public static Bitmap getImage(byte[] image) {
            return BitmapFactory.decodeByteArray(image, 0, image.length);
        }
    }

    protected DBLayer() {
    	className = this.getClass().getName();		
    }
	
	public static  DBLayer getInstance(){
		if (_instance == null){
			_instance = new DBLayer();
		}
		return _instance;
	}
	
	public  long addSentJoinRequest(String phoneNumber, String mutualId, JoinRequestStatusEnum status){
		
		String methodName = "addSentJoinRequest";
		if(phoneNumber == null || phoneNumber.isEmpty()){
        	String errMsg = "Add sent join request failed - no phone number was provided";
        	Log.e(DBConst.LOG_TAG_DB, errMsg);
            LogManager.LogErrorMsg(className, "addSentJoinRequest", errMsg);
			return -1;
		}
		
		if(mutualId == null || mutualId.isEmpty()){
        	String errMsg = "Add sent join request failed - no mutualId was provided";
        	Log.e(DBConst.LOG_TAG_DB, errMsg);
            LogManager.LogErrorMsg(className, "addSentJoinRequest", errMsg);
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
			
			if(!isPhoneInJoinRequestTable(phoneNumber, db)){
				return db.insert(DBConst.TABLE_SEND_JOIN_REQUEST, null, cVal);
			} else {
				return db.update(DBConst.TABLE_SEND_JOIN_REQUEST, cVal, DBConst.PHONE_NUMBER + " = ? ", new String[] { phoneNumber });
			}
		} catch (Throwable t) {
        	String errMsg = "EXCEPTION caught: " + t.getMessage();
        	Log.e(DBConst.LOG_TAG_DB, errMsg, t);
            LogManager.LogErrorMsg(className, methodName, errMsg);
		} finally {
			if(db != null){
				DBManager.getDBManagerInstance().close();
			}
		}
		return -1;
	}
	
	public  int deleteSentJoinRequest(String inMutualId){
		
		if(inMutualId == null || inMutualId.isEmpty()){
        	String errMsg = "Delete sent join request failed - no mutualId was provided";
        	Log.e(DBConst.LOG_TAG_DB, errMsg);
            LogManager.LogErrorMsg(className, "deleteSentJoinRequest", errMsg);
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
            LogManager.LogErrorMsg(className, "deleteSentJoinRequest", errMsg);
		} finally {
			if(db != null){
				DBManager.getDBManagerInstance().close();
			}
		}
		return -1;
	}

	public  SentJoinRequestData getSentJoinRequestByMutualId(String inMutualId){
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
            LogManager.LogErrorMsg(className, "getSentJoinRequestByMutualId", errMsg);
		} finally {
			if(db != null){
				DBManager.getDBManagerInstance().close();
			}
		}
    	return sentJoinRequestData;
    }

	private  long addFullPermissions(String email, SQLiteDatabase db){
		
		if(email == null || email.isEmpty()){
        	String errMsg = "Add permissions failed - no email account was provided";
        	Log.e(DBConst.LOG_TAG_DB, errMsg);
            LogManager.LogErrorMsg(className, "addPermissions", errMsg);
			return -1;
		}
		
		boolean bNeedOpenDb  = db == null;
		try{
			if (bNeedOpenDb){
				db = DBManager.getDBManagerInstance().open();
			}
			if(!isEmailInPermissionsTable(email, db)){
				ContentValues cVal = new ContentValues();
				cVal.put(DBConst.EMAIL, email);
				cVal.put(DBConst.LOCATION, 1);
				cVal.put(DBConst.COMMAND, 1);
				cVal.put(DBConst.ADMIN_COMMAND, 1);
						
				return db.insert(DBConst.TABLE_PERMISSIONS, null, cVal);
			} 
		} catch (Throwable t) {
        	String errMsg = "Exception caught: " + t.getMessage();
        	Log.e(DBConst.LOG_TAG_DB, errMsg, t);
            LogManager.LogErrorMsg(className, "addPermissions", errMsg);
		} finally {
			if(db != null && bNeedOpenDb){
				DBManager.getDBManagerInstance().close();
			}
		}
		return -1;
	}
	
	public  long updatePermissions(String email, int location, int command, int admin_command){
		
		if(email == null || email.isEmpty()){
        	String errMsg = "Update permissions failed - no email account was provided";
        	Log.e(DBConst.LOG_TAG_DB, errMsg);
            LogManager.LogErrorMsg(className, "updatePermissions", errMsg);
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
			
			if(isEmailInPermissionsTable(email, db)){
				return db.update(DBConst.TABLE_PERMISSIONS, cVal, DBConst.EMAIL + " = ? ", new String[] { email });
			}
//			} else {
//				return db.insert(DBConst.TABLE_PERMISSIONS, null, cVal);
//			}
		} catch (Throwable t) {
        	String errMsg = "Exception caught: " + t.getMessage();
        	Log.e(DBConst.LOG_TAG_DB, errMsg, t);
            LogManager.LogErrorMsg(className, "updatePermissions", errMsg);
		} finally {
			if(db != null){
				DBManager.getDBManagerInstance().close();
			}
		}
		return -1;
	}

	public  PermissionsData getPermissions (String inEmail){
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
        	LogManager.LogErrorMsg(className, "getPermissions", errMsg);
		} finally {
			if(db != null){
				DBManager.getDBManagerInstance().close();
			}
		}
    	return permissionsData;
    }

	public  PermissionsDataList getPermissionsList (String inEmail){
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
        	LogManager.LogErrorMsg(className, "getPermissionsList", errMsg);
		} finally {
			if(db != null){
				DBManager.getDBManagerInstance().close();
			}
		}
    	return permissionsDataList;
    }

	public  long addReceivedJoinRequest(String phoneNumber, String mutualId, String regId, String account, String macAddress){
		
    	logMessage = "";
    	methodName = "addReceivedJoinRequest";
    	Log.i(CommonConst.LOG_TAG, "[FUNCTION_CALL] {" + className + "} -> " + methodName);
    	LogManager.LogFunctionCall(className, methodName);
        
		if(phoneNumber == null || phoneNumber.isEmpty()){
        	String errMsg = "Add received join request failed - no phone number was provided";
        	Log.e(DBConst.LOG_TAG_DB, errMsg);
            LogManager.LogErrorMsg(className, "addReceivedJoinRequest", errMsg);
			return -1;
		}
		
		if(mutualId == null || mutualId.isEmpty()){
        	String errMsg = "Add received join request failed - no mutualId was provided";
        	Log.e(DBConst.LOG_TAG_DB, errMsg);
            LogManager.LogErrorMsg(className, "addReceivedJoinRequest", errMsg);
			return -1;
		}
		
		if(regId == null || regId.isEmpty()){
        	String errMsg = "Add received join request failed - no registrationId was provided";
        	Log.e(DBConst.LOG_TAG_DB, errMsg);
            LogManager.LogErrorMsg(className, "addReceivedJoinRequest", errMsg);
			return -1;
		}

		if(macAddress == null || macAddress.isEmpty()){
        	String errMsg = "Add received join request failed - no macAddress was provided";
        	Log.e(DBConst.LOG_TAG_DB, errMsg);
            LogManager.LogErrorMsg(className, "addReceivedJoinRequest", errMsg);
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
			
			if(!isPhoneInReceivedJoinRequestTable(phoneNumber, db)){
				logMessage = "INSERTING JOIN REQUEST from phone: " + phoneNumber + " TO DB";
				LogManager.LogInfoMsg(className, methodName, logMessage);
				Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
				return db.insert(DBConst.TABLE_RECEIVED_JOIN_REQUEST, null, cVal);
			} else {
				logMessage = "UPDAING JOIN REQUEST from phone: " + phoneNumber + " TO DB";
				LogManager.LogInfoMsg(className, methodName, logMessage);
				Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
				return db.update(DBConst.TABLE_RECEIVED_JOIN_REQUEST, cVal, DBConst.PHONE_NUMBER + " = ? ", new String[] { phoneNumber });
			}
		} catch (Throwable t) {
        	String errMsg = "Exception caught: " + t.getMessage();
        	Log.e(DBConst.LOG_TAG_DB, errMsg, t);
            LogManager.LogErrorMsg(className, "addReceivedJoinRequest", errMsg);
		} finally {
			if(db != null){
				DBManager.getDBManagerInstance().close();
			}
		}
		
		logMessage = "Failed - CAUSE UNKNOWN...";		
		LogManager.LogErrorMsg(className, methodName, logMessage);
		Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);

		return -1;
	}

	public  int deleteReceivedJoinRequest(String inMutualId){
		
		if(inMutualId == null || inMutualId.isEmpty()){
        	String errMsg = "Delete received join request failed - no mutualId was provided";
        	Log.e(DBConst.LOG_TAG_DB, errMsg);
            LogManager.LogErrorMsg(className, "deleteReceivedJoinRequest", errMsg);
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
            LogManager.LogErrorMsg(className, "deleteReceivedJoinRequest", errMsg);
		} finally {
			if(db != null){
				DBManager.getDBManagerInstance().close();
			}
		}
		return -1;
	}

	public  SentJoinRequestData getSentJoinRequestByPhone(String phoneNumberIn){
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
            LogManager.LogErrorMsg(className, "getSentJoinRequestByPhone", errMsg);
		} finally {
			if(db != null){
				DBManager.getDBManagerInstance().close();
			}
		}
    	return sentJoinRequestData;
    }

	public  ReceivedJoinRequestData getReceivedJoinRequest(String requestMutualId){
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
            LogManager.LogErrorMsg(className, "getReceivedJoinRequest", errMsg);
		} finally {
			if(db != null){
				DBManager.getDBManagerInstance().close();
			}
		}
    	return receivedJoinRequestData;
    }

	// Insert contact data
	private  long addContactDataInternal(ContactData contactData, SQLiteDatabase db, String whereClause, String[] whereArgs) 
	{
		boolean bNeedOpenDb  = db == null;

		if(contactData.getEmail() == null || contactData.getEmail().isEmpty()){
        	String errMsg = "Add contact data failed - no email account was provided";
        	Log.e(DBConst.LOG_TAG_DB, errMsg);
            LogManager.LogErrorMsg(className, "addContactData", errMsg);
			return -1;
		}
		
		try{
			if (bNeedOpenDb){
				db = DBManager.getDBManagerInstance().open();
			}
			
			ContentValues cVal = new ContentValues();
			cVal.put(DBConst.CONTACT_FIRST_NAME, contactData.getFirstName());
			cVal.put(DBConst.CONTACT_LAST_NAME, contactData.getLastName());
			cVal.put(DBConst.CONTACT_NICK, contactData.getNick());
			cVal.put(DBConst.CONTACT_EMAIL, contactData.getEmail());
			if (contactData.getContactPhoto() != null){
				cVal.put(DBConst.CONTACT_PHOTO, DbBitmapUtility.getBytes(contactData.getContactPhoto()));
			}
			        
			if (whereClause == null){
				return db.insert(DBConst.TABLE_CONTACT, null, cVal);
			}
			else{
				return db.update(DBConst.TABLE_CONTACT, cVal, whereClause, whereArgs);
			}
		} catch (Throwable t) {
			contactData = null;
        	String errMsg = "Exception caught: " + t.getMessage();
        	Log.e(DBConst.LOG_TAG_DB, errMsg, t);
            LogManager.LogErrorMsg(className, "addContactData", errMsg);
		} finally {
			if(db != null && bNeedOpenDb){
				DBManager.getDBManagerInstance().close();
			}
		}
		return -1;
	}
   
    // Insert device data
    private  long addDeviceDataInternal(DeviceData deviceData, SQLiteDatabase db, String whereClause, String[] whereArgs) 
     { 
    	boolean bNeedOpenDb  = db == null;

		if(deviceData.getDeviceMac() == null || deviceData.getDeviceMac().isEmpty()){
        	String errMsg = "Add device data failed - no … was provided";
        	Log.e(DBConst.LOG_TAG_DB, errMsg);
            LogManager.LogErrorMsg(className, "addDeviceData", errMsg);
			return -1;
		}
		
		if(deviceData.getDeviceTypeEnum() == null){
        	deviceData.setDeviceTypeEnum(DeviceTypeEnum.unknown) ;
		}

		try{
			if (bNeedOpenDb){
				db = DBManager.getDBManagerInstance().open();
			}
            
            ContentValues cVal = new ContentValues();
            cVal.put(DBConst.DEVICE_NAME, deviceData.getDeviceName());           
            cVal.put(DBConst.DEVICE_TYPE, deviceData.getDeviceTypeEnum().toString());
            cVal.put(DBConst.DEVICE_MAC, deviceData.getDeviceMac());
            if (whereClause == null){
            	return db.insert(DBConst.TABLE_DEVICE, null, cVal);
            }
            else{
            	return db.update(DBConst.TABLE_DEVICE, cVal, whereClause, whereArgs);
            }

        } catch (Throwable t) {
        	deviceData = null;
        	String errMsg = "Exception caught: " + t.getMessage();
        	Log.e(DBConst.LOG_TAG_DB, errMsg, t);
            LogManager.LogErrorMsg(className, "addDeviceData", errMsg);
		} finally {
			if(db != null && bNeedOpenDb){
				DBManager.getDBManagerInstance().close();
			}
		}
		return -1;
    }

    // Insert contact/device data
    private  ContactDeviceData addContactDeviceData(String phoneNumber, ContactData  contactData, 
    		DeviceData deviceData, String imei, String registartionId, String guid, SQLiteDatabase db)
     {
    	ContactDeviceData contactDeviceData = null;
    	boolean bNeedOpenDb  = db == null;
		try{
			if (bNeedOpenDb){
				db = DBManager.getDBManagerInstance().open();
			}
			
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
            cVal.put(DBConst.CONTACT_DEVICE_IS_FAVORITE, contactDeviceData.isFavorite() ? 1 : 0);
            
            db.insert(DBConst.TABLE_CONTACT_DEVICE, null, cVal);
        } catch (Throwable t) {
        	contactDeviceData = null;
            Log.e("Database", "Exception caught: " + t.getMessage(), t);
            LogManager.LogErrorMsg(className, "addContactDeviceData", t.getMessage());
		} finally {
			if(db != null && bNeedOpenDb){
				DBManager.getDBManagerInstance().close();
			}
		}
        return contactDeviceData;
    }
    
    // Insert contact/device data
    private  ContactDeviceData updateContactDeviceData(String phoneNumber, ContactData  contactData, 
    		DeviceData deviceData, String imei, String registartionId, String guid, SQLiteDatabase db)
     {
    	ContactDeviceData contactDeviceData = null;
    	boolean bNeedOpenDb  = db == null;
		try{
			if (bNeedOpenDb){
				db = DBManager.getDBManagerInstance().open();
			}
			
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
            cVal.put(DBConst.CONTACT_DEVICE_IS_FAVORITE, contactDeviceData.isFavorite() ? 1 : 0);
            
            db.update(DBConst.TABLE_CONTACT_DEVICE, cVal, 
            	DBConst.CONTACT_DEVICE_EMAIL + " = ? and " + 
            	DBConst.CONTACT_DEVICE_MAC + " = ? ", 
            	new String[] {contactDeviceData.getContactData().getEmail(),
            		contactDeviceData.getDeviceData().getDeviceMac()});
        } catch (Throwable t) {
        	contactDeviceData = null;
            Log.e("Database", "Exception caught: " + t.getMessage(), t);
            LogManager.LogErrorMsg(className, "addContactDeviceData", t.getMessage());
		} finally {
			if(db != null && bNeedOpenDb){
				DBManager.getDBManagerInstance().close();
			}
		}
        return contactDeviceData;
    }

    public  long updateRegistrationID(String email, String macAddress, String registrationID){
		String methodName = "updateRegistrationID";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_CALL] {" + className + "} -> " + methodName);

		if(email == null || email.isEmpty()){
        	String errMsg = "Update RegistrationID failed - no email account was provided";
        	Log.e(DBConst.LOG_TAG_DB, errMsg);
            LogManager.LogErrorMsg(className, methodName, errMsg);
			return -1;
		}
		
		if(macAddress == null || macAddress.isEmpty()){
        	String errMsg = "Update RegistrationID failed - no macAddress was provided";
        	Log.e(DBConst.LOG_TAG_DB, errMsg);
            LogManager.LogErrorMsg(className, methodName, errMsg);
			return -1;
		}

		if(registrationID == null || registrationID.isEmpty()){
        	String errMsg = "Update RegistrationID failed - no registrationID was provided";
        	Log.e(DBConst.LOG_TAG_DB, errMsg);
            LogManager.LogErrorMsg(className, methodName, errMsg);
			return -1;
		}

		SQLiteDatabase db = null;
		try{
			db = DBManager.getDBManagerInstance().open();
			 
			ContentValues cVal = new ContentValues();
			cVal.put(DBConst.CONTACT_DEVICE_REG_ID, registrationID);
			
			if(isEmailMacAddressInContactDeviceTable(email, macAddress,db)){
				int result = db.update(DBConst.TABLE_CONTACT_DEVICE, cVal, 
					DBConst.CONTACT_DEVICE_EMAIL + " = ? and " + DBConst.CONTACT_DEVICE_MAC + " = ?", 
					new String[] { email, macAddress });
				
				if(result == 0){
					String logMessage = "Update RegistrationID failed.";
					LogManager.LogErrorMsg(className, methodName, logMessage);
					Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
				} else {
					String logMessage = result + " columns updated.";
					LogManager.LogInfoMsg(className, methodName, logMessage);
					Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
				}

				LogManager.LogFunctionExit(className, methodName);
				Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
				return result;
			} else {
	        	String errMsg = "Update RegistrationID failed - no email and registrationID were found in ContactDeviceTable";
	        	Log.e(DBConst.LOG_TAG_DB, errMsg);
	            LogManager.LogErrorMsg(className, methodName, errMsg);
	            return -1;
			}
		} catch (Throwable t) {
        	String errMsg = "Exception caught: " + t.getMessage();
        	Log.e(DBConst.LOG_TAG_DB, errMsg, t);
            LogManager.LogErrorMsg(className, methodName, errMsg);
		} finally {
			if(db != null){
				DBManager.getDBManagerInstance().close();
			}
		}
		return -1;
	}

	public  long updateTableContactDevice(String email, String macAddress, Map<String, Object> mapKeyValue){
		String methodName = "updateTableContactDevice";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_CALL] {" + className + "} -> " + methodName);
	
		SQLiteDatabase db = null;
		try{
			db = DBManager.getDBManagerInstance().open();
				
			return updateTableContactDeviceInternal(email, macAddress, mapKeyValue, db);
		} catch (Throwable t) {
        	String errMsg = "Exception caught: " + t.getMessage();
        	Log.e(DBConst.LOG_TAG_DB, errMsg, t);
            LogManager.LogErrorMsg(className, methodName, errMsg);
		} finally {
			if(db != null){
				DBManager.getDBManagerInstance().close();
			}
		}		
		return -1;
	}
	
	private  long updateTableContactDeviceInternal(String email, String macAddress, Map<String, Object> mapKeyValue, SQLiteDatabase db){
		String methodName = "updateTableContactDeviceInternal";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_CALL] {" + className + "} -> " + methodName);
		
		if (db == null){
			String errMsg = methodName + " failed - The db is not opened";
        	Log.e(DBConst.LOG_TAG_DB, errMsg);
            LogManager.LogErrorMsg(className, methodName, errMsg);
			return -1;
		}

		if(email == null || email.isEmpty()){
        	String errMsg = methodName + " failed - no email account was provided";
        	Log.e(DBConst.LOG_TAG_DB, errMsg);
            LogManager.LogErrorMsg(className, methodName, errMsg);
			return -1;
		}
		
		if(macAddress == null || macAddress.isEmpty()){
        	String errMsg = methodName + " failed - no macAddress was provided";
        	Log.e(DBConst.LOG_TAG_DB, errMsg);
            LogManager.LogErrorMsg(className, methodName, errMsg);
			return -1;
		}

		// Loop - for each key,value
		for (Entry<String, Object> entry : mapKeyValue.entrySet()) {
			
			String key = entry.getKey();
			Object objValue = entry.getValue();
			ContentValues cVal = new ContentValues();
			if(objValue instanceof String){
				if((String)objValue == null || ((String)objValue).isEmpty()){
		        	String errMsg = "Update " + key + " failed - no " + key + " was provided";
		        	Log.e(DBConst.LOG_TAG_DB, errMsg);
		            LogManager.LogErrorMsg(className, methodName, errMsg);
					return -1;
				}
				cVal.put(key, (String)objValue);
			} else
			if(objValue instanceof Integer){
				cVal.put(key, (Integer)objValue);
			} else
			if(objValue instanceof Double){
				cVal.put(key, (Double)objValue);
			} else
			if(objValue instanceof Float){
				cVal.put(key, (Float)objValue);
			} else
			if(objValue instanceof Boolean){
				cVal.put(key, (Boolean)objValue);
			} else {
				String logMessage = "Unsupported type of " + key + "with value: " + objValue.toString();
				LogManager.LogErrorMsg(className, methodName, logMessage);
				Log.e(DBConst.LOG_TAG_DB, "[ERROR] {" + className + "} -> " + logMessage);
				return -1;
			}
			
			try{	
				if(isEmailMacAddressInContactDeviceTable(email, macAddress, db)){
					int result = db.update(DBConst.TABLE_CONTACT_DEVICE, cVal, 
						DBConst.CONTACT_DEVICE_EMAIL + " = ? and " + DBConst.CONTACT_DEVICE_MAC + " = ?", 
						new String[] { email, macAddress });
					
					if(result == 0){
						String logMessage = methodName + " failed.";
						LogManager.LogErrorMsg(className, methodName, logMessage);
						Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
					} else {
						String logMessage = result + " columns updated.";
						LogManager.LogInfoMsg(className, methodName, logMessage);
						Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
					}
	
					LogManager.LogFunctionExit(className, methodName);
					Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
					return result;
				} else {
		        	String errMsg = methodName + " failed - no email:[" + email + 
		        		"] and macAddress:[" + macAddress + "]" + 
		        		" were found in ContactDeviceTable";
		        	Log.e(DBConst.LOG_TAG_DB, errMsg);
		            LogManager.LogErrorMsg(className, methodName, errMsg);
		            return -1;
				}
			} catch (Throwable t) {
	        	String errMsg = "Exception caught: " + t.getMessage();
	        	Log.e(DBConst.LOG_TAG_DB, errMsg, t);
	            LogManager.LogErrorMsg(className, methodName, errMsg);
			}
		}
		return -1;
	}

	private  boolean isEmailMacAddressInContactDeviceTable(String email, String macAddress, SQLiteDatabase db){
		String selectQuery = "select " + DBConst.CONTACT_DEVICE_EMAIL + "," + DBConst.CONTACT_DEVICE_MAC + 
				" from " + DBConst.TABLE_CONTACT_DEVICE +
				" where " + DBConst.CONTACT_DEVICE_EMAIL + " = ? and " +
				DBConst.CONTACT_DEVICE_MAC + " = ?";
    	return isFieldExist(selectQuery, new String[] { email, macAddress }, db);
    }
    
	public  ContactDeviceDataList addContactDeviceDataList(ContactDeviceDataList contactDeviceDataList){
    	
		methodName = "addContactDeviceDataList";
		
    	ContactDeviceDataList contactDeviceDataListInserted = null;
    	
    	if(contactDeviceDataList != null){
    		SQLiteDatabase db = null;
			try{
				db = DBManager.getDBManagerInstance().open();
				db.beginTransaction();
				for (ContactDeviceData contactDeviceData : contactDeviceDataList) {
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
							LogManager.LogErrorMsg("DBLayer", methodName, errMsg);
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
							LogManager.LogErrorMsg("DBLayer", methodName, errMsg);
							continue;
						}
						
						if(registrationId == null || registrationId.isEmpty()){
							String errMsg = "Unable to add contactDeviceData to DB - no registrationId was provided";
							Log.e(CommonConst.LOG_TAG, errMsg);
							LogManager.LogErrorMsg("DBLayer", methodName, errMsg);
							continue;
						}
						
						long updateResult = 0;
						if(!isContactDeviceExist(email, macAddress, db)){
							updateResult = addContactDataInternal(contactData, db, null, null);
							if(updateResult < 1){
								logMessage = "Add Contact Data Internal failed for the following account: " + email;
								LogManager.LogErrorMsg(className, methodName, logMessage);
								Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + methodName + ": " + logMessage);
							}
							updateResult = addDeviceDataInternal(deviceData, db, null, null);
							if(updateResult < 1){
								logMessage = "Add Device Data Internal failed for the following mac address: " + macAddress;
								LogManager.LogErrorMsg(className, methodName, logMessage);
								Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + methodName + ": " + logMessage);
							}
							ContactDeviceData resultContactDeviceData = addContactDeviceData(phoneNumber, contactData, deviceData, imei, registrationId, guid, db);
							if(resultContactDeviceData == null){
								String errMsg = "Failed to add contactDeviceData to DB by addContactDeviceData()";
								Log.e(CommonConst.LOG_TAG, errMsg);
								LogManager.LogErrorMsg("DBLayer", methodName, errMsg);
							}
							if(addFullPermissions(email, db) < 0){
								String errMsg = "Failed to add FULL permissions for the following account: " + email;
								Log.e(CommonConst.LOG_TAG, errMsg);
								LogManager.LogErrorMsg("DBLayer", methodName, errMsg);
							}
							java.util.Map<String, Object> m = new HashMap<String, Object>();
							m.put(DBConst.CONTACT_DEVICE_LOCATION_SHARING, 1);
							updateResult = updateTableContactDeviceInternal(email, macAddress, m, db);
							if(updateResult < 1){
								logMessage = "Failed to add FULL permissions for the following account: " + email;
								LogManager.LogErrorMsg(className, methodName, logMessage);
								Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + methodName + ": " + logMessage);
							}
				    	} else {
							String warnMsg = "Account [" + email + "] with MacAddress [" + macAddress + "] " +
								"already exists in DB - updating it...";
							Log.i(CommonConst.LOG_TAG, warnMsg);
							LogManager.LogInfoMsg("DBLayer", methodName, warnMsg);
							
							// UPDATE CONTACT
				    		updateResult = addContactDataInternal(contactData, db, DBConst.CONTACT_EMAIL + " = ? ", new String[] { contactData.getEmail() });
							if(updateResult < 1){
								logMessage = "Add Contact Data Internal failed for the following account: " + email;
								LogManager.LogErrorMsg(className, methodName, logMessage);
								Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + methodName + ": " + logMessage);
							}
							updateResult = addDeviceDataInternal(deviceData, db, DBConst.DEVICE_MAC + " = ? ", new String[] { deviceData.getDeviceMac() });
							if(updateResult < 1){
								logMessage = "Add Device Data Internal failed for the following mac address: " + macAddress;
								LogManager.LogErrorMsg(className, methodName, logMessage);
								Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + methodName + ": " + logMessage);
							}
							ContactDeviceData resultContactDeviceData = updateContactDeviceData(phoneNumber, contactData, deviceData, imei, registrationId, guid, db);
							if(resultContactDeviceData == null){
								String errMsg = "Failed to update contactDeviceData to DB by addContactDeviceData()";
								Log.e(CommonConst.LOG_TAG, errMsg);
								LogManager.LogErrorMsg("DBLayer", methodName, errMsg);
							}
				    	}						
			    	}
					
				}
				contactDeviceDataListInserted = getContactDeviceDataListInternal(null, db);
				db.setTransactionSuccessful();
			} catch (Throwable t) {						
	        	String errMsg = "Exception caught: " + t.getMessage();
	        	Log.e(DBConst.LOG_TAG_DB, errMsg, t);
	            LogManager.LogErrorMsg(className, "getContactData", errMsg);
			} finally {
				if(db != null){
					db.endTransaction();
					DBManager.getDBManagerInstance().close();
				}
			}
    	}
			
		return contactDeviceDataListInserted;
    }
	
	public  ContactData getContactData(){
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
	            	byte[] contact_photo = cursor.getBlob(4);
	            	
	            	contactData.setEmail(contact_email);
	            	contactData.setFirstName(contact_first_name);
	            	contactData.setLastName(contact_last_name);
	            	contactData.setNick(contact_nick);
	            	contactData.setContactPhoto(DbBitmapUtility.getImage(contact_photo));
	            	
	            } while (cursor.moveToNext());
	        }
	        cursor.close();
        } catch (Throwable t) {
        	String errMsg = "Exception caught: " + t.getMessage();
        	Log.e(DBConst.LOG_TAG_DB, errMsg, t);
            LogManager.LogErrorMsg(className, "getContactData", errMsg);
		} finally {
			if(db != null){
				DBManager.getDBManagerInstance().close();
			}
		}
    	return contactData;
    }

    public  DeviceData getDeviceData(){
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
            LogManager.LogErrorMsg(className, "getDeviceData", errMsg);
		} finally {
			if(db != null){
				DBManager.getDBManagerInstance().close();
			}
		}
    	return deviceData;
    }
    
    public  ContactDeviceDataList getContactDeviceDataList(String email){
    	 
    	ContactDeviceDataList contactDeviceDataList = null;

    	SQLiteDatabase db = null;
		try{
			db = DBManager.getDBManagerInstance().open();
			
			contactDeviceDataList = getContactDeviceDataListInternal(email, db);
        
		} finally {
			if(db != null){
				DBManager.getDBManagerInstance().close();
			}
		}
		
    	return contactDeviceDataList;
    }
    
    public  ContactDeviceDataList getContactDeviceDataListInternal(String email, SQLiteDatabase db){
   	 
    	ContactDeviceDataList contactDeviceDataList = null;
    	ContactDeviceData contactDeviceData = null;
    	String selectQuery = null;
    	    	
		try{
	        String selectClause = "select " +		
	        DBConst.CONTACT_NICK + ", " + 
	        DBConst.CONTACT_EMAIL + ", " +
	        DBConst.CONTACT_PHOTO + ", " +
	        DBConst.DEVICE_MAC + ", " + 
	        DBConst.DEVICE_NAME + ", " + 
	        DBConst.DEVICE_TYPE + ", " + 
	        DBConst.CONTACT_DEVICE_IMEI + ", " + 
	        DBConst.CONTACT_DEVICE_PHONE_NUMBER + ", " + 
	        DBConst.CONTACT_DEVICE_REG_ID + ", " + 
	        DBConst.CONTACT_DEVICE_GUID + ", " + 
	        DBConst.CONTACT_DEVICE_LOCATION_SHARING + ", " + 
	        DBConst.CONTACT_DEVICE_TRACKING + ", " + 
	        DBConst.CONTACT_DEVICE_IS_FAVORITE;
	        
	        
	        String fromClause = " from " + DBConst.TABLE_CONTACT_DEVICE + " as CD " +
	        " join " + DBConst.TABLE_CONTACT + " as C " +
	        " on CD." + DBConst.CONTACT_DEVICE_EMAIL + " = C." + DBConst.CONTACT_EMAIL + 
	        " join " + DBConst.TABLE_DEVICE + " as D " +
	        " on CD." + DBConst.CONTACT_DEVICE_MAC + " = D." + DBConst.DEVICE_MAC;

	        // Select All Query
	        selectQuery = selectClause + fromClause;
	        
	        String[] val = null;
	        // where clause
	        if(email !=  null && !email.isEmpty()){
	        	selectQuery = selectQuery + " where CD.contact_device_email = ? ";
	        	val = new String[] { email };
	        }
	        
	        Cursor cursor = db.rawQuery(selectQuery, val);
	  
	        // looping through all rows and adding to list
	        if (cursor.moveToFirst()) {
	        	contactDeviceDataList = new ContactDeviceDataList();
	            do {
	            	contactDeviceData = new ContactDeviceData();
	            	
	            	String contact_nick = cursor.getString(0);
	            	String contact_email = cursor.getString(1);
	            	byte[] contact_photo = cursor.getBlob(2);
	            	String device_mac = cursor.getString(3);
	            	String device_name = cursor.getString(4);
	            	String device_type = cursor.getString(5);
	            	String contact_device_imei = cursor.getString(6);
	            	String contact_device_phone_number = cursor.getString(7);
	            	String registration_id = cursor.getString(8);
	            	String guid = cursor.getString(9);
	            	int locationSharing = cursor.getInt(10);
	    	        int tracking = cursor.getInt(11);
	    	        boolean is_favorite = cursor.getInt(12) == 1;  
	    	        
	            	ContactData contactData = new ContactData();
	            	contactData.setEmail(contact_email);
	            	contactData.setNick(contact_nick);
	            	contactData.setContactPhoto(contact_photo != null ? DbBitmapUtility.getImage(contact_photo) : null);
	            	
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
	            	contactDeviceData.setLocationSharing(locationSharing);
	    	        contactDeviceData.setTracking(tracking);
	    	        contactDeviceData.setFavorite(is_favorite);

	            	contactDeviceDataList.add(contactDeviceData);
	            	
	            } while (cursor.moveToNext());
	        }
	        cursor.close();
        } catch (Throwable t) {
        	String errMsg = "Exception caught: " + t.getMessage() + 
        		"\nSelect query:\n" + selectQuery;
        	Log.e(DBConst.LOG_TAG_DB, errMsg, t);
            LogManager.LogErrorMsg(className, "getContactDeviceDataListInternal", errMsg);
		}
		
    	return contactDeviceDataList;
    }

    private  boolean isContactDeviceExist(String email, String macAddress, SQLiteDatabase db){
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
			"and CD.contact_device_mac = ? ";
		// TODO: check that phoneNumber, email and macAddress are valid values to avoid SQL injection
		return isFieldExist(selectQuery, new String[] {email, macAddress}, db);
    }

    private  boolean isPhoneInJoinRequestTable(String phoneNumber, SQLiteDatabase db) throws Exception{
    	return isValueInTable(phoneNumber, new Object[]{ DBConst.PHONE_NUMBER, DBConst.TABLE_SEND_JOIN_REQUEST, DBConst.PHONE_NUMBER }, db);		
    }

    private  boolean isEmailInPermissionsTable(String email, SQLiteDatabase db) throws Exception{    	
    	return isValueInTable(email, new Object[]{ DBConst.EMAIL, DBConst.TABLE_PERMISSIONS, DBConst.EMAIL }, db);		
    }

    private  boolean isPhoneInReceivedJoinRequestTable(String phoneNumber, SQLiteDatabase db) throws Exception{   	
    	return isValueInTable(phoneNumber, new Object[]{ DBConst.PHONE_NUMBER, DBConst.TABLE_RECEIVED_JOIN_REQUEST, DBConst.PHONE_NUMBER }, db);		
    }
    
    private  boolean isValueInTable(String value, Object[] fields, SQLiteDatabase db) throws Exception{
    	if (fields.length == 3){
	    	String selectQuery = "select %s from %s where %s = ?";
			String selectQueryFormated = String.format(selectQuery, fields);				
			// TODO: check that value is valid value to avoid SQL injection
			return isFieldExist(selectQueryFormated, new String[] { value }, db);
    	}
    	else
    		throw new Exception("Number of query fields must be 3!");
    	
    }

    private  boolean isColumnExistsInTable(String tableName, String columnName, SQLiteDatabase db){
    	String pragmaQuery = "PRAGMA table_info('" + tableName + "')";
    	boolean isColumnNameExistsInTable = false;
    	boolean needOpenDb = db == null;
		try{
			if (needOpenDb){
				db = DBManager.getDBManagerInstance().open();
			}
			Cursor cursor = db.rawQuery(pragmaQuery, null);
			
	        // looping through all rows and adding to list
	        if (cursor.moveToFirst()) {
	            do {	            	
	            	// get second column - it is column name
	            	String tableColumnName = cursor.getString(1);
	            	if(tableColumnName.equals(columnName)){
	            		isColumnNameExistsInTable = true;
	            		break;
	            	}
	            } while (cursor.moveToNext());
	        }
	        cursor.close();
	    } catch (Throwable t) {
	    	String errMsg = "Exception caught: " + t.getMessage();
	    	Log.e(DBConst.LOG_TAG_DB, errMsg, t);
	        LogManager.LogErrorMsg(className, "isColumnExistsInTable", errMsg);
		} finally {
			if(db != null && needOpenDb){
				DBManager.getDBManagerInstance().close();
			}
		}
		return isColumnNameExistsInTable;
    }
    
    private  boolean isFieldExist(String selectQuery, String[] val, SQLiteDatabase db){
    	boolean result = false;
    	boolean needOpenDb = db == null;
	
		try{
			if (needOpenDb){
				db = DBManager.getDBManagerInstance().open();
			}
            
	        Cursor cursor = db.rawQuery(selectQuery, val);
	        int count = cursor.getCount();
	        cursor.close();
	        if(count > 0){
	        	result = true;
	        }        
	    } catch (Throwable t) {
        	String errMsg = "Exception caught: " + t.getMessage();
        	Log.e(DBConst.LOG_TAG_DB, errMsg, t);
            LogManager.LogErrorMsg(className, "isFieldExist", errMsg);
		} finally {
			if(db != null && needOpenDb){
				DBManager.getDBManagerInstance().close();
			}
		}
		return result;
    }

    public  ContactDeviceData getContactDeviceData(String email){
    	ContactDeviceDataList contactDeviceDataList = null;

    	SQLiteDatabase db = null;
		try{
			db = DBManager.getDBManagerInstance().open();
			
			contactDeviceDataList = getContactDeviceDataListInternal(email, db);
        
		} finally {
			if(db != null){
				DBManager.getDBManagerInstance().close();
			}
		}
		
    	return contactDeviceDataList != null && contactDeviceDataList.size() > 0 ? contactDeviceDataList.get(0) : null;    	
    } 
    
    // Escape string for single quotes (Insert,Update)
    private  String sqlEscapeString(String aString) {
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
    private  String sqlUnEscapeString(String aString) {
         
        String aReturn = "";
         
        if (null != aString) {
            aReturn = aString.replace("''", "'");
        }
         
        return aReturn;
    }
    
	public  int removeContactDataDeviceDetail(ContactDeviceData data) {
		int res = -1;		
		
		SQLiteDatabase db = DBManager.getDBManagerInstance().open();
		db.beginTransaction();
		try{					 		
			String whereClause = DBConst.EMAIL + " = ?";
			String[] whereArgs = new String[] { data.getContactData().getEmail() };			
			res = db.delete(DBConst.TABLE_PERMISSIONS, whereClause, whereArgs);
			
			whereClause = DBConst.CONTACT_DEVICE_EMAIL + " = ?";
			whereArgs = new String[] { data.getContactData().getEmail() };			
			res = db.delete(DBConst.TABLE_CONTACT_DEVICE, whereClause, whereArgs);
			
			whereClause = DBConst.DEVICE_MAC + " = ?"; 
			whereArgs = new String[] { data.getDeviceData().getDeviceMac() };			
			res = db.delete(DBConst.TABLE_DEVICE, whereClause, whereArgs);
			
			whereClause = DBConst.CONTACT_EMAIL + " = ?"; 
			whereArgs = new String[] { data.getContactData().getEmail() };			
			res = db.delete(DBConst.TABLE_CONTACT, whereClause, whereArgs);
			
			db.setTransactionSuccessful();
		}
		catch (Throwable t) {
        	String errMsg = "Exception caught: " + t.getMessage();
        	Log.e(DBConst.LOG_TAG_DB, errMsg, t);
            LogManager.LogErrorMsg(className, "removeContactDataDeviceDetail", errMsg);
		}
		finally{
			db.endTransaction();
			DBManager.getDBManagerInstance().close();
		}
		
		return res;
	}
	
	public long updateContactDeviceData(ContactDeviceData contactDeviceData){
		methodName = "updateContactDeviceData";
		long lRes = -1;
		SQLiteDatabase db = null;
		try{
			db = DBManager.getDBManagerInstance().open();
			db.beginTransaction();
			lRes = addContactDataInternal(contactDeviceData.getContactData(), db, DBConst.CONTACT_EMAIL + " = ? ", new String[] { contactDeviceData.getContactData().getEmail() });
			if (lRes == -1)
				return lRes;
			addDeviceDataInternal(contactDeviceData.getDeviceData(), db, DBConst.DEVICE_MAC + " = ? ", new String[] { contactDeviceData.getDeviceData().getDeviceMac()});
			if (lRes == -1)
				return lRes;
			//java.util.Map<String, Object> m = new HashMap<String, Object>();
			//m.put(DBConst.CONTACT_DEVICE_LOCATION_SHARING, 1);
			//iRes = updateTableContactDeviceInternal(contactDeviceData.getContactData().getEmail(), contactDeviceData.getDeviceData().getDeviceMac(), m, db);
			db.setTransactionSuccessful();
		} catch (Throwable t) {
        	String errMsg = "Exception caught: " + t.getMessage();
        	Log.e(DBConst.LOG_TAG_DB, errMsg, t);
            LogManager.LogErrorMsg(className, methodName, errMsg);
		} finally {
			if(db != null){
				db.endTransaction();
				DBManager.getDBManagerInstance().close();
			}
		}
		return lRes;
	}
	
}
