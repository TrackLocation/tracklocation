package com.dagrest.tracklocation.datatype;

import java.io.File;
import java.io.IOException;

import android.util.Log;

import com.dagrest.tracklocation.crypto.CryptoUtils;
import com.dagrest.tracklocation.db.DBLayer;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.FileOperations;
import com.dagrest.tracklocation.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class BackupDataOperations {
	
	private ContactDeviceDataList contactDeviceDataList;
	private BackupSettings settings;
	private String jsonBackupData;
    private String logMessage;
    private String methodName;
    private String className;
    private String storagePath;
    private File TrackLocationDirectory;
    private File backUpFile;
    private Gson gson;
    private boolean enforceRestore = false;
	
    public BackupDataOperations(){
		className = this.getClass().getName();
		storagePath = Utils.getStoragePath();
		backUpFile = new File(getTrackLocationDirectory().getAbsolutePath() + 
			File.separator + CommonConst.TRACK_LOCATION_BACKUP_FILE_NAME);  
		gson = new Gson();
    }
    		
	public BackupDataOperations(ContactDeviceDataList contactDeviceDataList,
			BackupSettings settings) {
		this(); // call default constructor
		this.contactDeviceDataList = contactDeviceDataList;
		this.settings = settings;
	}
	
	private String getDataForBackup(){
		BackupData backupData = null;
		methodName = "getDataForBackup";
		if(contactDeviceDataList == null){
			contactDeviceDataList = DBLayer.getInstance().getContactDeviceDataList(null);
		}
		if(contactDeviceDataList == null){
			logMessage = "Contact Device Data list is empty - nothing to backup.";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
			return null;
		}
		
		if(settings == null){
			// Get settings
			// TODO: ...
		}
		if(settings == null){
			// TODO: Open commented code:
//			logMessage = "Backup failed. Settings list is empty - nothing to backup.";
//			LogManager.LogErrorMsg(className, methodName, logMessage);
//			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
//			//return null;
		}
		
		backupData = new BackupData(contactDeviceDataList, settings);
		jsonBackupData = gson.toJson(backupData);
		if(jsonBackupData == null || jsonBackupData.isEmpty()){
			logMessage = "There is no any data to backup.";
			LogManager.LogInfoMsg(className, methodName, logMessage);
			Log.i(CommonConst.LOG_TAG, "[WARNING] {" + className + "} -> " + logMessage);
			return null;
		}
		return jsonBackupData;
	}
	
	// Retrieve ContactDeviceDataList from DB and collect BackedUpSettings
	// and save them to backup file according to path, if exists ask to rewrite it.
	public boolean backUp() {
		methodName = "backUp";
		jsonBackupData = getDataForBackup();
		if(jsonBackupData == null || jsonBackupData.isEmpty()){
			// There is no any data to backup...
			// Error message is logged out in getBackupData() function
			return false;
		}
		
		String md5HashFromDB = null;
		if(jsonBackupData != null && !jsonBackupData.isEmpty()){
			md5HashFromDB = CryptoUtils.md5HexHash(jsonBackupData);
		}
		String md5HashFromBackUpFile = null;
		String content = null;
		try {
			if(backUpFile.exists() == true){
				content = FileOperations.readFile(backUpFile.getAbsolutePath());
			}
		} catch (IOException e) {
			logMessage = "Backup process failed.";
			LogManager.LogException(e, className, methodName);
			Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + logMessage, e);
			return false;
		}
		if(content != null && !content.isEmpty()){
			md5HashFromBackUpFile = CryptoUtils.md5HexHash(content);
		}
		if( md5HashFromDB != null && !md5HashFromDB.isEmpty() && 
			!md5HashFromDB.equals(md5HashFromBackUpFile)){
			try {
				FileOperations.writeFile(jsonBackupData, backUpFile.getAbsolutePath());
			} catch (IOException e) {
				logMessage = "Backup process failed.";
				LogManager.LogException(e, className, methodName);
				Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + logMessage, e);
				return false;
			}
		}
		
		return true;
	}
		
	// Read backup file from path and retrieve from it:
	// - ContactDeviceDataList - insert to DB
	// - BackedUpSettings - set settings according to retrieved data
	public boolean restore() {
		methodName = "restore";
		if(backUpFile.exists() == false){
			// There is no backup 
			logMessage = "There is no backup file exists: [" + backUpFile + "]";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
			return false;
		} 

		String jsonBackupDataFromDB = getDataForBackup();
		String jsonBackupDataFromFile = null;
		try {
			jsonBackupDataFromFile = FileOperations.readFile(backUpFile.getAbsolutePath());
		} catch (IOException e) {
			logMessage = "Restore process failed.";
			LogManager.LogException(e, className, methodName);
			Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + logMessage, e);
			return false;
		}
		String md5HashBackupFromDB = null;
		String md5HashBackupDataFromFile = null;
		
		if(jsonBackupDataFromDB != null && !jsonBackupDataFromDB.isEmpty()){
			md5HashBackupFromDB = CryptoUtils.md5HexHash(jsonBackupDataFromDB);
		}
		if(jsonBackupDataFromFile != null && !jsonBackupDataFromFile.isEmpty()){
			md5HashBackupDataFromFile = CryptoUtils.md5HexHash(jsonBackupDataFromFile);
		}
		if( (md5HashBackupFromDB == null && md5HashBackupDataFromFile != null) ||
			(enforceRestore == true && md5HashBackupDataFromFile != null)
			){
			
			// Insert from BackUp file to DB on device

			// Insert ContactDeviceDataList data to DB
			BackupData backupData = null;
			try {
				backupData = gson.fromJson(jsonBackupDataFromFile, BackupData.class);
				if(backupData == null){
					logMessage = "Failed to restore backup from file: [" + backUpFile + "]";
					LogManager.LogErrorMsg(className, methodName, logMessage);
					Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
					return false;
				}
				contactDeviceDataList = backupData.getContactDeviceDataList();
				settings = backupData.getSettings();
			} catch (JsonSyntaxException e) {
				LogManager.LogException(e, className, methodName);
				Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + e.getMessage());
				return false;
			}
			
			if(contactDeviceDataList != null){
				// Set ContactDeviceDataList according to retrieved data
				ContactDeviceDataList insertedContactDeviceDataList = 
						DBLayer.getInstance().addContactDeviceDataList(contactDeviceDataList);
				if(insertedContactDeviceDataList == null){
					logMessage = "Backup restore failed.";
					LogManager.LogErrorMsg(className, methodName, logMessage);
					Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
					return false;
				}
			}
			
			if(settings != null){
				// Set Settings according to retrieved data
			}
		} else {
			logMessage = "Automatic restore was not started.";
			LogManager.LogInfoMsg(className, methodName, logMessage);
			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
		}
		return true;
	}
	
	public File getTrackLocationDirectory(){
		TrackLocationDirectory = new File(storagePath + File.separator + CommonConst.TRACK_LOCATION_DIRECTORY_PATH);                          
		if(TrackLocationDirectory.exists() == false ){                                  
			// create log directory if needed                                  
			TrackLocationDirectory.mkdirs();                          
		} if (TrackLocationDirectory.isDirectory() == false) {
			TrackLocationDirectory.mkdirs();     
		}
		return TrackLocationDirectory;
	}
	
	public boolean isValidExisitingPath(String path){
		File backUpFile = new File(path);
		String canonicalPathBackUpFile = null;
		try {
			canonicalPathBackUpFile = backUpFile.getCanonicalPath();
		} catch (IOException e) {
			logMessage = e.getMessage();
			LogManager.LogException(e, className, methodName);
			Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + logMessage);
			return false;
		}
		if(canonicalPathBackUpFile == null || canonicalPathBackUpFile.isEmpty()){
			logMessage = "Invalid path: [" + path +"]: null or empty after checking for canonical path.";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
			return false;
		}
		backUpFile = new File(canonicalPathBackUpFile);
		boolean isBackUpFileExist = backUpFile.exists();
		if(isBackUpFileExist == false){
			logMessage = "TrackLocation backup file doesn't exist: [" + canonicalPathBackUpFile +"]";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
			return false;
		}
		boolean isBackUpFileIsFileType = new File(path).isFile();
		if(isBackUpFileIsFileType == false){
			logMessage = "TrackLocation backup path is not pointing to a file: [" + canonicalPathBackUpFile +"]";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
			return false;
		}
		return true;
	}
	
	public ContactDeviceDataList getContactDeviceDataList() {
		return contactDeviceDataList;
	}

	public void setContactDeviceDataList(ContactDeviceDataList contactDeviceDataList) {
		this.contactDeviceDataList = contactDeviceDataList;
	}

	public BackupSettings getSettings() {
		return settings;
	}

	public void setSettings(BackupSettings settings) {
		this.settings = settings;
	}

	public boolean isEnforceRestore() {
		return enforceRestore;
	}

	public void setEnforceRestore(boolean enforceRestore) {
		this.enforceRestore = enforceRestore;
	}
}
