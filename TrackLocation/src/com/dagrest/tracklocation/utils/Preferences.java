package com.dagrest.tracklocation.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import android.content.Context;
import android.content.SharedPreferences;

//http://developer.android.com/guide/topics/data/data-storage.html#pref
public class Preferences {
      
//	private SharedPreferences sharedPreferences;
      
	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	public static SharedPreferences getGCMPreferences(Context context) {
	    return context.getSharedPreferences(CommonConst.SHARED_PREFERENCES_NAME,
	            Context.MODE_PRIVATE);
	}
  		
//  	public Preferences(SharedPreferences sharedPreferences){
//  		this.sharedPreferences = sharedPreferences;
//  	}
//      
	public static int getPreferencesInt(Context context, String valueName){
		// Restore preferences
		int value = getGCMPreferences(context).getInt(valueName, 0);
		//setSilent(silent);
		return value;
	}
	  
	public static void setPreferencesInt(Context context, String valueName, int value){
		// We need an Editor object to make preference changes.
		// All objects are from android.context.Context
		// SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = getGCMPreferences(context).edit();
		editor.putInt(valueName, value);
		
		// Commit the edits!
		editor.commit();
	}

	public static boolean getPreferencesBoolean(Context context, String valueName){
		// Restore preferences
		boolean value = getGCMPreferences(context).getBoolean(valueName, false);
		//setSilent(silent);
		return value;
	}
	  
	  public static void setPreferencesBooolean(Context context, String valueName, boolean value){
          // We need an Editor object to make preference changes.
          // All objects are from android.context.Context
          // SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
          SharedPreferences.Editor editor = getGCMPreferences(context).edit();
          editor.putBoolean(valueName, value);
          
          // Commit the edits!
          editor.commit();
	  }

	  public static String getPreferencesString(Context context, String valueName){
	      // Restore preferences
	      return getGCMPreferences(context).getString(valueName, "");
	  }
	  
	  public static void setPreferencesString(Context context, String valueName, String value){
	      // We need an Editor object to make preference changes.
	      // All objects are from android.context.Context
	      // SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	      SharedPreferences.Editor editor = getGCMPreferences(context).edit();
	      editor.putString(valueName, value);
	      
	      // Commit the edits!
	      editor.commit();
	  }

		@SuppressWarnings("unchecked")
		public static Map<String, String> getPreferencesReturnToContactMap(Context context){
			Map<String, String> returnToContactMap = null;
			Gson gson = new Gson();
			String jsonReturnToContactMap = Preferences.getPreferencesString(context, 
				CommonConst.PREFERENCES_RETURN_TO_CONTACT_MAP);
			if (jsonReturnToContactMap != null && !jsonReturnToContactMap.isEmpty()) {
				returnToContactMap = gson.fromJson(jsonReturnToContactMap, HashMap.class);
				return returnToContactMap;
			}
			return returnToContactMap;
		}

		public static Map<String, String> setPreferencesReturnToContactMap(Context context, String account, String regId){
			Map<String, String> returnToContactMap = getPreferencesReturnToContactMap(context);
			Gson gson = new Gson();
			
			if(returnToContactMap == null){
				returnToContactMap = new HashMap<String, String>();
			}
			returnToContactMap.put(account, regId);
			String jsonReturnToContactMap = gson.toJson(returnToContactMap);
			Preferences.setPreferencesString(context, CommonConst.PREFERENCES_RETURN_TO_CONTACT_MAP, jsonReturnToContactMap);
			
			return returnToContactMap;
		}

		public static void clearPreferencesReturnToContactMap(Context context, String account){
			Map<String, String> returnToContactMap = getPreferencesReturnToContactMap(context);
			Gson gson = new Gson();
			
			if(returnToContactMap != null){
				returnToContactMap.remove(account);
				String jsonReturnToContactMap = gson.toJson(returnToContactMap);
				Preferences.setPreferencesString(context, CommonConst.PREFERENCES_RETURN_TO_CONTACT_MAP, jsonReturnToContactMap);
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
	