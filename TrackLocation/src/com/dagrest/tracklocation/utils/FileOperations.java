package com.dagrest.tracklocation.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.provider.MediaStore.Files;
import android.util.Log;

import com.dagrest.tracklocation.log.LogManager;

public class FileOperations {

	public final static String className = "com.dagrest.tracklocation.utils.FileOperations";

	public static void writeFile(String content, String filePath) throws IOException{
		FileOutputStream fileOutputStream = null;
		File file;
		String logMessage;
		String methodName = "writeFile";
		
		try {

			file = new File(filePath);
			fileOutputStream = new FileOutputStream(file);

			// get the content in bytes
			byte[] contentInBytes = content.getBytes();

			fileOutputStream.write(contentInBytes);
			fileOutputStream.flush();
			fileOutputStream.close();

			logMessage = "File successfully created: " + file.getAbsolutePath();
			LogManager.LogInfoMsg(className, methodName, logMessage);
			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

		} catch (IOException e) {
			LogManager.LogException(e, className, methodName);
			Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + e.getMessage());
			throw new IOException(methodName + " failed.", e);
		} finally {
			try {
				if (fileOutputStream != null) {
					fileOutputStream.close();
				}
			} catch (IOException e) {
				LogManager.LogException(e, className, methodName);
				Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + e.getMessage());
				throw new IOException(methodName + " failed.", e);
			}
		}
	}
	
	public static String readFile(String filePath) throws IOException{
		String fileContent = null;
		FileInputStream fileInputStream = null;
		String logMessage;
		String methodName = "readFile";
		byte[] fileContentByteArray;
		try {
			fileInputStream = new FileInputStream(filePath);

			logMessage = "Total file size to read (in bytes) : " +
				fileInputStream.available();
			LogManager.LogInfoMsg(className, methodName, logMessage);
			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

			int content;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(baos);

			while ((content = fileInputStream.read()) != -1) {
				// convert to char and display it
				out.write(content);
//				System.out.print((char) content);
			}
			fileContentByteArray = baos.toByteArray();
			fileContent = new String(fileContentByteArray, "UTF-8");
			
			logMessage = "File successfully read: " + filePath;
			LogManager.LogInfoMsg(className, methodName, logMessage);
			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

		} catch (IOException e) {
			LogManager.LogException(e, className, methodName);
			Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + e.getMessage());
			throw new IOException(methodName + " failed.", e);
		} finally {
			try {
				if (fileInputStream != null)
					fileInputStream.close();
			} catch (IOException e) {
				LogManager.LogException(e, className, methodName);
				Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + e.getMessage());
				throw new IOException(methodName + " failed.", e);
			}
		}
		return fileContent;
	}

}
