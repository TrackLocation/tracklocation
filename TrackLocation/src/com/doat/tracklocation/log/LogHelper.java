package com.doat.tracklocation.log;

import java.io.File;  
import java.io.FileWriter;  
import java.io.IOException;  
import java.io.PrintWriter;  
import java.util.Calendar;  

import com.doat.tracklocation.TrackLocationApplication;
import com.doat.tracklocation.utils.CommonConst;
import com.doat.tracklocation.utils.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log; 

public class LogHelper {

	private final static int MAX_LOG_FILE_SIZE_IN_MB = 1;
	private static LogHelper instance;          
	private Object lockMe = null;  
	private String storagePath;
	private Context context;
    	
    	private LogHelper(){                  
    		lockMe = new Object();
    		
    		storagePath = Utils.getStoragePath();
    		
    		context = TrackLocationApplication.getContext();

    		if(isLogEnabled() == true){  
    			// create a File object for the log directory                          
    			File logDirectory = new File(storagePath + File.separator + CommonConst.TRACK_LOCATION_DIRECTORY_PATH);                          
    			if(logDirectory.exists() == false){                                  
    				// create log directory if needed                                  
    				logDirectory.mkdirs();                          
   				}
    			
    			createNewLogFileIfGreaterThanSize(MAX_LOG_FILE_SIZE_IN_MB);
   			}          
  		}                    

    	private void createNewLogFileIfGreaterThanSize(int fileSizeInMb){
    		String logFileName = getLogFileName();
			File logFile = new File(logFileName);                          
			if(logFile.exists()){    
				int fileSize = new Double(logFile.length() / (1024 * 1024)).intValue();
				if(fileSize >= fileSizeInMb){
					
					logFile.renameTo(new File(getLogFileName(Long.toString(System.currentTimeMillis()))));
				}
			}                                            
    	}
    	
    	
    	private String getLogFileName(){
    		return storagePath + File.separator + CommonConst.TRACK_LOCATION_DIRECTORY_PATH + 
    			File.separator + CommonConst.TRACK_LOCATION_LOG_FILE_NAME + CommonConst.TRACK_LOCATION_LOG_FILE_EXT;
    	}
   	
    	private String getLogFileNameNoExt(){
    		return storagePath + File.separator + CommonConst.TRACK_LOCATION_DIRECTORY_PATH + 
    			File.separator + CommonConst.TRACK_LOCATION_LOG_FILE_NAME;
    	}
    	
     	private String getLogFileName(String logFileNameExtension){
    		if(logFileNameExtension != null && !logFileNameExtension.isEmpty()){
    			return getLogFileNameNoExt() + "_" + logFileNameExtension + CommonConst.TRACK_LOCATION_LOG_FILE_EXT;
    		} else {
    			return getLogFileName();
    		}
    	}
    	
    	public static LogHelper getLog(){                  
    		if(instance == null){                          
    			instance = new LogHelper();                  
    		}                  
    		return instance;          
    	}
    	
    	public void toLog(final MessageType msgType, final String logMessage){                  
    		String buildMsg = "";
    		
    		//buildMsg = getTimestamp() + DELIMITER;
    		
    		// Message Type                  
    		buildMsg += Wrap(msgType.toString()) + CommonConst.DELIMITER;                                    
    		
    		// Thread ID                  
    		final long threadId = Thread.currentThread().getId();                  
    		buildMsg += Wrap(String.valueOf(threadId)) + CommonConst.DELIMITER;                                    
    		
    		// Message                  
    		buildMsg += logMessage;                                    
    		
    		toLogWrite(msgType, buildMsg);          
    	}   
 
		private static String Wrap(String string)          
		{                  
			return "["+string+"]";          
		}

		public void toLogWrite(final MessageType msgType, final String logMessage){      
			if(isLogEnabled() == true)                  
			{                          
				createNewLogFileIfGreaterThanSize(MAX_LOG_FILE_SIZE_IN_MB);
				final String timeStamp = getTimestamp();     
				String activityCreatePrefix = "";
				String activityDestroyPostfix = "";
	    		if(msgType.equals(MessageType.ACTIVITY_CREATE)){
	    			activityCreatePrefix += "\n===== ACTIVITY_CREATE =====\n";
	    		}
	    		if(msgType.equals(MessageType.ACTIVITY_DESTROY)){
	    			activityDestroyPostfix += "\n===== ACTIVITY_DESTROY =====\n";
	    		}
	    		final String prefix = activityCreatePrefix;
	    		final String postfix = activityDestroyPostfix;
	    		
				Thread t = new Thread(new Runnable() {

					//@Override                          
					public void run()                           
					{                                  
						synchronized(lockMe)                                  
						{                                          
							try {                                                  
								PrintWriter pw = new PrintWriter(                                                  
								new FileWriter(getLogFileName(), true));                                          
								//ex.printStackTrace(pw);  
								
								pw.println(prefix+timeStamp+CommonConst.DELIMITER+logMessage+postfix);                                          
								pw.flush();                                          
								pw.close();                                      
							} catch (IOException e) {                                          
								Log.e(timeStamp+" WiFiConnect: LogHelper->toLog", e.toString());                                          
							}                                  
						} // of syncrhonized                          
					} // of run()                          
				}); // of new Runnable()                                                            
				t.start();                  
				}          
			}
		
		public boolean isLogEnabled(){       
     		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
     		return sharedPref.getBoolean("pref_log_enable", false);
		}       
    	
		private String getTimestamp()          
		{                  
			Calendar now = Calendar.getInstance();                  
			int day = now.get(Calendar.DAY_OF_MONTH);                  
			int month = now.get(Calendar.MONTH) - Calendar.JANUARY +1;                  
			int year = now.get(Calendar.YEAR);                  
			int hour = now.get(Calendar.HOUR_OF_DAY);                  
			int minute = now.get(Calendar.MINUTE);                  
			int second = now.get(Calendar.SECOND);   
			
			return Wrap(year+"."+month+"."+day+" "+hour+":"+minute+":"+second);          
		}                    
}

