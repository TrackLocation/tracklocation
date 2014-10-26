package com.dagrest.tracklocation.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import android.os.Environment;

import com.dagrest.tracklocation.datatype.CommandEnum;
import com.dagrest.tracklocation.datatype.ContactData;
import com.dagrest.tracklocation.datatype.ContactDeviceData;
import com.dagrest.tracklocation.datatype.ContactDeviceDataList;
import com.dagrest.tracklocation.datatype.DeviceData;
import com.dagrest.tracklocation.datatype.DeviceTypeEnum;
import com.dagrest.tracklocation.datatype.Message;
import com.dagrest.tracklocation.datatype.MessageData;
import com.dagrest.tracklocation.datatype.PushNotificationServiceStatusEnum;
import com.dagrest.tracklocation.datatype.TrackLocationServiceStatusEnum;
import com.dagrest.tracklocation.log.LogManager;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class Utils {
	
	private final static String COMMA = ",";

	public static List<String> splitLine(String line, String delimiter){
		String[] inputArray;
		List<String> paramsList;
		
		if (line.contains(delimiter)) {
			inputArray = line.split(delimiter);
			paramsList = new ArrayList<String>();
			for (int i = 0; i < inputArray.length; i++) {
				paramsList.add((inputArray[i] == null || inputArray[i].isEmpty()) 
					? null : inputArray[i].trim());
			}
			return paramsList;
		} else {
			if(line != null && !line.isEmpty()) {
				paramsList = new ArrayList<String>();
				paramsList.add(line);
				return paramsList;
			} else {
				return null;
			}
		}
	}
	
	public static List<List<String>> readCustomerDataFromFile(String fileName){
		BufferedReader br = null;
		
		ArrayList<List<String>> inputParamsList = null;
		
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(fileName));
 
			inputParamsList = new ArrayList<List<String>>();
			while ((sCurrentLine = br.readLine()) != null) {
				inputParamsList.add(splitLine(sCurrentLine, COMMA));
			}
		} catch (IOException e) {
			LogManager.LogErrorMsg("Utils", "readCustomerDataFromFile", "Unable to read file: " + 
				fileName + ". Error message: " + e.getMessage());
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				LogManager.LogErrorMsg("Utils", "readCustomerDataFromFile", "Unable to read file: " + 
					fileName + ". Error message: " + ex.getMessage());
			}
		}
		return inputParamsList;
	}
	
	/**
	 * Read file as on string 
	 * It should contain data in JASON and should be converted
	 * to java object by GSON
	 * @param fileName
	 * @return String
	 */
	public static String readInputFile(String fileName){
		String fileContent = null;
		try {
			Scanner sc = new Scanner(new FileReader(fileName));
			while (sc.hasNext()) {
				if (fileContent != null) {
					fileContent = fileContent + sc.next();
				} else {
					fileContent = sc.next();
				}
			}			
		} catch (FileNotFoundException e) {
			LogManager.LogErrorMsg("Utils", "readFile", "Unable to read file: " + 
				fileName + ". Error message: " + e.getMessage());
		}
		return fileContent;
		
//	    BufferedReader br = new BufferedReader(new FileReader("file.txt"));
//	    try {
//	        StringBuilder sb = new StringBuilder();
//	        String line = br.readLine();
//
//	        while (line != null) {
//	            sb.append(line);
//	            sb.append(System.lineSeparator());
//	            line = br.readLine();
//	        }
//	        String everything = sb.toString();
//	    } finally {
//	        br.close();
//	    }		
	}
	
	// TODO: Should be deleted - only as example
    public static void jsonTest(){
        Gson gson = new Gson();
    	
        //gson.fromJson(messageJson, QuickPayParkingLocations.class);
        Utils.CustomerDataFromFileJsonTest();
        
        MessageData messageData = new MessageData();
        messageData.setMessage("This is a message");
        messageData.setTime(new Date().toString());
        messageData.setCommand(CommandEnum.stop);
        
        Message message = new Message();
        message.setData(messageData); 
        
        List<String> listRegIDs = new ArrayList<String>(); 
        listRegIDs.add("registrationIDs");
        message.setRegistrationIDs(listRegIDs);
        
        Message messageTest = null;
        String gsonString = gson.toJson(message);
        if (gsonString != null) {
        	messageTest = gson.fromJson(gsonString, Message.class);
        }
    }
    
	public static ContactDeviceDataList CustomerDataFromFileJsonTest(){
        Gson gson = new Gson();
    	
        ContactData contactDataDavid = new ContactData();
        contactDataDavid.setEmail("dagrest@gmail.com");
        contactDataDavid.setNick("dagrest");
        
        DeviceData deviceDataDavid = new DeviceData();
        deviceDataDavid.setDeviceName("Samsung Galaxy S3");
        deviceDataDavid.setDeviceTypeEnum(DeviceTypeEnum.phone);
                        
        ContactDeviceData contactDeviceDataDavid = new ContactDeviceData();
        contactDeviceDataDavid.setContactData(contactDataDavid);
        contactDeviceDataDavid.setDeviceData(deviceDataDavid);
        contactDeviceDataDavid.setRegistration_id("registration_id");
        
        ContactData contactDataLarisa = new ContactData();
        contactDataLarisa.setEmail("agrest2000@gmail.com");
        contactDataLarisa.setNick("larisa");
        
        DeviceData deviceDataLarisa = new DeviceData();
        deviceDataLarisa.setDeviceName("LG NEXUS 4");
        deviceDataLarisa.setDeviceTypeEnum(DeviceTypeEnum.phone);
                        
        ContactDeviceData contactDeviceDataLarisa = new ContactDeviceData();
        contactDeviceDataLarisa.setContactData(contactDataLarisa);
        contactDeviceDataLarisa.setDeviceData(deviceDataLarisa);
        contactDeviceDataLarisa.setRegistration_id("registration_id");

        ContactDeviceDataList contactDeviceDataList = 
        	new ContactDeviceDataList();
        
        contactDeviceDataList.getContactDeviceDataList()
        	.add(contactDeviceDataDavid);
        contactDeviceDataList.getContactDeviceDataList()
    		.add(contactDeviceDataLarisa);
        
//        List<CustomerDataFromFile> customerDataList = customerDataFromFileList.getCustomerDataFromFileList();
//        customerDataList.add(customerDataFromFileDavid);
//        customerDataList.add(customerDataFromFileLarisa);
        
        String gsonString = gson.toJson(contactDeviceDataList);
        ContactDeviceDataList customerDataListNew = null;
        if (gsonString != null) {
        	customerDataListNew = 
        		gson.fromJson(gsonString, ContactDeviceDataList.class);
        	customerDataListNew.getContactDeviceDataList();
        }

		File contactDeviceDataListInputFileName = 
			new File(getStoragePath() + File.separator + CommonConst.TRACK_LOCATION_DIRECTORY_PATH + 
				File.separator + CommonConst.CONTACT_DTAT_INPUT_FILE);                          
        ContactDeviceDataList contactDeviceDataListFromFile = null;
        //String absPath = contactDeviceDataListInputFileName.getAbsolutePath();
        String gsonStringNew = readInputFile(contactDeviceDataListInputFileName.getAbsolutePath());
        contactDeviceDataListFromFile = 
        		gson.fromJson(gsonStringNew, ContactDeviceDataList.class);
        return contactDeviceDataListFromFile;
	}
	
	public static String getContactDeviceDataFromJsonFile(){
		File contactDeviceDataListInputFileName = 
				new File(getStoragePath() + File.separator + CommonConst.TRACK_LOCATION_DIRECTORY_PATH + 
					File.separator + CommonConst.CONTACT_DTAT_INPUT_FILE);                          
        return readInputFile(contactDeviceDataListInputFileName.getAbsolutePath());
	}
	
	public static ContactDeviceDataList fillContactDeviceDataListFromJSON(String jsonDataString){
		Gson gson = new Gson();
		try {
			ContactDeviceDataList contactDeviceDataList = gson.fromJson(jsonDataString, ContactDeviceDataList.class);
			return contactDeviceDataList;
		} catch (JsonSyntaxException e) {
    		LogManager.LogException(e, "Utils", "fillContactDeviceDataFromJSON");
			return null;
		}
	}
	
	public static ContactDeviceData getContactDeviceDataByUsername(ContactDeviceDataList contactDeviceDataCollection, String userName){

		List<ContactDeviceData> contactDeviceDataList = contactDeviceDataCollection.getContactDeviceDataList();
	    if(contactDeviceDataList == null){
	    	return null;
	    }
	    
	    for (ContactDeviceData contactDeviceData : contactDeviceDataList) {
	    	ContactData contactData = contactDeviceData.getContactData();
	    	if(contactData != null) {
	    		if(contactData.getNick() != null){
	    			if(contactData.getNick().equals(userName)){
	    				return contactDeviceData;
	    			}
	    		} 
	    	}
 		}
	    
	    return null;
	}

	public static String getStoragePath(){
		File extStore = Environment.getExternalStorageDirectory();
		return extStore.getAbsolutePath();
	}

    public static String getCurrentTime(){
        Calendar c = Calendar.getInstance();  
        int hours = c.get(Calendar.HOUR_OF_DAY);
        int minutes = c.get(Calendar.MINUTE);
        int seconds = c.get(Calendar.SECOND);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
