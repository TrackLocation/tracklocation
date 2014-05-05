package com.dagrest.tracklocation.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.dagrest.tracklocation.log.LogManager;

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
	
	public static String readFile(String fileName){
//		Scanner in;
		try {
			return new Scanner(new FileReader(fileName)).toString();
		} catch (FileNotFoundException e) {
			LogManager.LogErrorMsg("Utils", "readFile", "Unable to read file: " + 
				fileName + ". Error message: " + e.getMessage());
		}
		return null;
		
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
}
