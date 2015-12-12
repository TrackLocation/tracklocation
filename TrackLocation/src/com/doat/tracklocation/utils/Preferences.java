package com.doat.tracklocation.utils;

import java.util.HashMap;
import java.util.Map;

import com.doat.tracklocation.log.LogManager;
import com.google.gson.Gson;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

//http://developer.android.com/guide/topics/data/data-storage.html#pref
public class Preferences {
      
//	private SharedPreferences sharedPreferences;
      
	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	public static SharedPreferences getGCMPreferences(Context context) {
		String className = CommonConst.TRACK_LOCATION_PROJECT_PREFIX + ".Preferences";
		String methodName = "getGCMPreferences";
		if(context != null){
		return context.getSharedPreferences(CommonConst.SHARED_PREFERENCES_NAME,
		        Context.MODE_PRIVATE);
		} else {
			String logMessage = "Unable to get GCMPreferences. Context parameter is null.";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
			return null;
		}
	}
  		
//  	public Preferences(SharedPreferences sharedPreferences){
//  		this.sharedPreferences = sharedPreferences;
//  	}
//      
	public static int getPreferencesInt(Context context, String valueName){
		String className = CommonConst.TRACK_LOCATION_PROJECT_PREFIX + ".Preferences";
		String methodName = "getPreferencesInt";
		int value = -1;
		// Restore preferences
		if(context != null){
			value = getGCMPreferences(context).getInt(valueName, 0);
		} else {
			String logMessage = "Unable to get AppInfo. Context parameter is null.";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
		}
		//setSilent(silent);
		return value;
	}
	  
	public static void setPreferencesInt(Context context, String valueName, int value){
		String className = CommonConst.TRACK_LOCATION_PROJECT_PREFIX + ".Preferences";
		String methodName = "setPreferencesInt";
		// We need an Editor object to make preference changes.
		// All objects are from android.context.Context
		// SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		if(context != null){
			SharedPreferences.Editor editor = getGCMPreferences(context).edit();
			editor.putInt(valueName, value);
		
			// Commit the edits!
			editor.commit();
		} else {
			String logMessage = "Unable to set PreferencesInt. Context parameter is null.";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
		}
	}

	public static boolean getPreferencesBoolean(Context context, String valueName){
		String className = CommonConst.TRACK_LOCATION_PROJECT_PREFIX + ".Preferences";
		String methodName = "getPreferencesBoolean";
		boolean value = false;
		// Restore preferences
		if(context != null){
			value = getGCMPreferences(context).getBoolean(valueName, false);
		} else {
			String logMessage = "Unable to get PreferencesBoolean. Context parameter is null.";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
		}
		//setSilent(silent);
		return value;
	}
	  
	public static void setPreferencesBoolean(Context context,
			String valueName, boolean value) {
		String className = CommonConst.TRACK_LOCATION_PROJECT_PREFIX
				+ ".Preferences";
		String methodName = "setPreferencesBooolean";
		// We need an Editor object to make preference changes.
		// All objects are from android.context.Context
		// SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		if (context != null) {
			SharedPreferences.Editor editor = getGCMPreferences(context).edit();
			editor.putBoolean(valueName, value);

			// Commit the edits!
			editor.commit();
		} else {
			String logMessage = "Unable to set PreferencesBooolean. Context parameter is null.";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> "
					+ logMessage);
		}
	}

	public static String getPreferencesString(Context context, String valueName) {
		String className = CommonConst.TRACK_LOCATION_PROJECT_PREFIX
				+ ".Preferences";
		String methodName = "getPreferencesString";
		// Restore preferences
		if (context != null) {
			return getGCMPreferences(context).getString(valueName, "");
		} else {
			String logMessage = "Unable to get PreferencesString. Context parameter is null.";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> "
					+ logMessage);
		}
		return null;
	}
	  
	public static void setPreferencesString(Context context, String valueName,
			String value) {
		String className = CommonConst.TRACK_LOCATION_PROJECT_PREFIX
				+ ".Preferences";
		String methodName = "getPreferencesString";
		// We need an Editor object to make preference changes.
		// All objects are from android.context.Context
		// SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		if (context != null) {
			SharedPreferences.Editor editor = getGCMPreferences(context).edit();
			editor.putString(valueName, value);

			// Commit the edits!
			editor.commit();
		} else {
			String logMessage = "Unable to set PreferencesString. Context parameter is null.";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> "
					+ logMessage);
		}
	}

	@SuppressWarnings("unchecked")
	public static Map<String, String> getPreferencesReturnToContactMap(
			Context context) {
		String className = CommonConst.TRACK_LOCATION_PROJECT_PREFIX
				+ ".Preferences";
		String methodName = "getPreferencesReturnToContactMap";
		Map<String, String> returnToContactMap = null;
		Gson gson = new Gson();
		if (context != null) {
			String jsonReturnToContactMap = Preferences.getPreferencesString(
					context, CommonConst.PREFERENCES_LOCATION_REQUESTER_MAP__ACCOUNT_AND_REG_ID);
			if (jsonReturnToContactMap != null
					&& !jsonReturnToContactMap.isEmpty()) {
				returnToContactMap = gson.fromJson(jsonReturnToContactMap,
						HashMap.class);
				return returnToContactMap;
			}
		} else {
			String logMessage = "Unable to get PreferencesReturnToContactMap. Context parameter is null.";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> "
					+ logMessage);
		}
		return returnToContactMap;
	}

	public static Map<String, String> setPreferencesReturnToContactMap(
			Context context, String account, String regId) {
		String className = CommonConst.TRACK_LOCATION_PROJECT_PREFIX
				+ ".Preferences";
		String methodName = "setPreferencesReturnToContactMap";
		Map<String, String> returnToContactMap = getPreferencesReturnToContactMap(context);
		Gson gson = new Gson();

		if (returnToContactMap == null) {
			returnToContactMap = new HashMap<String, String>();
		}
		if (context != null) {
			returnToContactMap.put(account, regId);
			String jsonReturnToContactMap = gson.toJson(returnToContactMap);
			Preferences.setPreferencesString(context,
					CommonConst.PREFERENCES_LOCATION_REQUESTER_MAP__ACCOUNT_AND_REG_ID,
					jsonReturnToContactMap);
		} else {
			String logMessage = "Unable to set PreferencesReturnToContactMap. Context parameter is null.";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> "
					+ logMessage);
		}

		return returnToContactMap;
	}

	public static void clearPreferencesReturnToContactMap(Context context,
			String account) {
		String className = CommonConst.TRACK_LOCATION_PROJECT_PREFIX
				+ ".Preferences";
		String methodName = "clearPreferencesReturnToContactMap";
		Map<String, String> returnToContactMap = getPreferencesReturnToContactMap(context);
		Gson gson = new Gson();

		if (context != null) {
			if (returnToContactMap != null) {
				returnToContactMap.remove(account);
				String jsonReturnToContactMap = gson.toJson(returnToContactMap);
				Preferences.setPreferencesString(context,
						CommonConst.PREFERENCES_LOCATION_REQUESTER_MAP__ACCOUNT_AND_REG_ID,
						jsonReturnToContactMap);
			}
		} else {
			String logMessage = "Unable to clear PreferencesReturnToContactMap. Context parameter is null.";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> "
					+ logMessage);
		}
	}

	public static void clearPreferencesReturnToContactMap(Context context) {
		String className = CommonConst.TRACK_LOCATION_PROJECT_PREFIX
				+ ".Preferences";
		String methodName = "clearPreferencesReturnToContactMap";
		Map<String, String> returnToContactMap = getPreferencesReturnToContactMap(context);
		Gson gson = new Gson();

		if (context != null) {
			if (returnToContactMap != null) {
				returnToContactMap.clear();
				String jsonReturnToContactMap = gson.toJson(returnToContactMap);
				Preferences.setPreferencesString(context,
						CommonConst.PREFERENCES_LOCATION_REQUESTER_MAP__ACCOUNT_AND_REG_ID,
						jsonReturnToContactMap);
			}
		} else {
			String logMessage = "Unable to clear PreferencesReturnToContactMap. Context parameter is null.";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> "
					+ logMessage);
		}
	}

//	  //CommonConst.PREFERENCES_RETURN_TO_REG_ID_LIST // regIDToReturnMessageTo
//		public static List<String> getPreferencesReturnToRegIDList(Context context){
//			String returnToRegIdList = Preferences.getPreferencesString(context, 
//				CommonConst.PREFERENCES_RETURN_TO_REG_ID_LIST);
//			if (returnToRegIdList != null && !returnToRegIdList.isEmpty()) {
//				return Utils.splitLine(returnToRegIdList, CommonConst.DELIMITER_STRING);
//			} else {
//				String tempRegId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
//				if(tempRegId != null){
//					ArrayList<String> tempRegIdList = new ArrayList<String>();
//					tempRegIdList.add(tempRegId);
//					return tempRegIdList;
//				} else {
//					return null;
//				}
//			}
//		}
//		
//		public static List<String> setPreferencesReturnToRegIDList(Context context, String valueName, String value){
//			String returnToRegIdList = Preferences.getPreferencesString(context, valueName);
//			if (returnToRegIdList != null && !returnToRegIdList.isEmpty()) {
//				
//				CharSequence cs = value;
//				if(returnToRegIdList.contains(cs) == false){
//					returnToRegIdList = returnToRegIdList + CommonConst.DELIMITER_STRING + value;
//				}
//				
//			} else {
//				returnToRegIdList = value;
//			}
//			Preferences.setPreferencesString(context, valueName, returnToRegIdList);
//			return Utils.splitLine(returnToRegIdList, CommonConst.DELIMITER_STRING);
//		}

}
	