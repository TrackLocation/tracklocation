package com.dagrest.tracklocation.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import android.os.Environment;

import com.dagrest.tracklocation.datatype.CommandEnum;
import com.dagrest.tracklocation.datatype.CustomerData;
import com.dagrest.tracklocation.datatype.CustomerDataFromFile;
import com.dagrest.tracklocation.datatype.CustomerDataFromFileList;
import com.dagrest.tracklocation.datatype.DeviceData;
import com.dagrest.tracklocation.datatype.DeviceTypeEnum;
import com.dagrest.tracklocation.datatype.Message;
import com.dagrest.tracklocation.datatype.MessageData;
import com.dagrest.tracklocation.log.LogManager;
import com.google.gson.Gson;

public class Utils {
	
	private final static String COMMA = ",";

	public static List<String> splitLine(String line){
		String[] inputArray;
		List<String> paramsList;
		
		if (line.contains(COMMA)) {
			inputArray = line.split(COMMA);
			paramsList = new ArrayList<String>();
			for (int i = 0; i < inputArray.length; i++) {
				paramsList.add((inputArray[i] == null || inputArray[i].isEmpty()) 
					? null : inputArray[i].trim());
			}
			return paramsList;
		} else {
		    return null;
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
				inputParamsList.add(splitLine(sCurrentLine));
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
	
	public static void CustomerDataFromFileJsonTest(){
        Gson gson = new Gson();
    	
        CustomerData customerDataDavid = new CustomerData();
        customerDataDavid.setEmail("dagrest@gmail.com");
        customerDataDavid.setUsername("dagrest");
        customerDataDavid.setRegistration_id("registration_id");
        
        DeviceData deviceDataDavid = new DeviceData();
        deviceDataDavid.setDeviceName("Samsung Galaxy S3");
        deviceDataDavid.setDeviceTypeEnum(DeviceTypeEnum.phone);
        deviceDataDavid.setImei("imei");
                        
        CustomerDataFromFile customerDataFromFileDavid = new CustomerDataFromFile();
        customerDataFromFileDavid.setCustomerData(customerDataDavid);
        customerDataFromFileDavid.setDeviceData(deviceDataDavid);
        
        CustomerData customerDataLarisa = new CustomerData();
        customerDataLarisa.setEmail("agrest2000@gmail.com");
        customerDataLarisa.setUsername("larisa");
        customerDataLarisa.setRegistration_id("registration_id");
        
        DeviceData deviceDataLarisa = new DeviceData();
        deviceDataLarisa.setDeviceName("LG NEXUS 4");
        deviceDataLarisa.setDeviceTypeEnum(DeviceTypeEnum.phone);
        deviceDataLarisa.setImei("imei");
                        
        CustomerDataFromFile customerDataFromFileLarisa = new CustomerDataFromFile();
        customerDataFromFileLarisa.setCustomerData(customerDataLarisa);
        customerDataFromFileLarisa.setDeviceData(deviceDataLarisa);

        CustomerDataFromFileList customerDataFromFileList = 
        	new CustomerDataFromFileList();
        
        customerDataFromFileList.getCustomerDataFromFileList()
        	.add(customerDataFromFileDavid);
        customerDataFromFileList.getCustomerDataFromFileList()
    		.add(customerDataFromFileLarisa);
        
//        List<CustomerDataFromFile> customerDataList = customerDataFromFileList.getCustomerDataFromFileList();
//        customerDataList.add(customerDataFromFileDavid);
//        customerDataList.add(customerDataFromFileLarisa);
        
        String gsonString = gson.toJson(customerDataFromFileList);
        CustomerDataFromFileList customerDataListNew = null;
        if (gsonString != null) {
        	customerDataListNew = 
        		gson.fromJson(gsonString, CustomerDataFromFileList.class);
        	customerDataListNew.getCustomerDataFromFileList();
        	int i = 0;
        }

		File customerDataInputFileName = 
			new File(getStoragePath() + File.separator + CommonConstants.LOG_DIRECTORY_PATH + 
				File.separator + "CustomerData.dat");                          
        CustomerDataFromFileList customerDataListFromFile = null;
        String absPath = customerDataInputFileName.getAbsolutePath();
        String gsonStringNew = readInputFile(customerDataInputFileName.getAbsolutePath());
        customerDataListFromFile = 
        		gson.fromJson(gsonStringNew, CustomerDataFromFileList.class);
	}
	
	public static String getStoragePath(){
		File extStore = Environment.getExternalStorageDirectory();
		return extStore.getAbsolutePath();
	}

}
