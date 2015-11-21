package com.doat.tracklocation.utils;

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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.doat.tracklocation.R;
import com.doat.tracklocation.datatype.CommandEnum;
import com.doat.tracklocation.datatype.ContactData;
import com.doat.tracklocation.datatype.ContactDeviceData;
import com.doat.tracklocation.datatype.ContactDeviceDataList;
import com.doat.tracklocation.datatype.DeviceData;
import com.doat.tracklocation.datatype.DeviceTypeEnum;
import com.doat.tracklocation.datatype.Message;
import com.doat.tracklocation.datatype.MessageData;
import com.doat.tracklocation.datatype.PushNotificationServiceStatusEnum;
import com.doat.tracklocation.datatype.TrackLocationServiceStatusEnum;
import com.doat.tracklocation.log.LogManager;
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
//			LogManager.LogErrorMsg("Utils", "readCustomerDataFromFile", "Unable to read file: " + 
//				fileName + ". Error message: " + e.getMessage());
			LogManager.LogException(e, "Utils", "readCustomerDataFromFile");
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
//				LogManager.LogErrorMsg("Utils", "readCustomerDataFromFile", "Unable to read file: " + 
//					fileName + ". Error message: " + ex.getMessage());
				LogManager.LogException(ex, "Utils", "readCustomerDataFromFile");
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
			LogManager.LogException(e, "Utils", "readInputFile");
			//LogErrorMsg("Utils", "readFile", "Unable to read file: " + 
			//	fileName + ". Error message: " + e.getMessage());
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
        
        contactDeviceDataList.add(contactDeviceDataDavid);
        contactDeviceDataList.add(contactDeviceDataLarisa);
        
//        List<CustomerDataFromFile> customerDataList = customerDataFromFileList.getCustomerDataFromFileList();
//        customerDataList.add(customerDataFromFileDavid);
//        customerDataList.add(customerDataFromFileLarisa);
        
        String gsonString = gson.toJson(contactDeviceDataList);
        ContactDeviceDataList customerDataListNew = null;
        if (gsonString != null) {
        	customerDataListNew = 
        		gson.fromJson(gsonString, ContactDeviceDataList.class);
        	//customerDataListNew.getContactDeviceDataList();
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
	    if(contactDeviceDataCollection == null){
	    	return null;
	    }
	    
	    for (ContactDeviceData contactDeviceData : contactDeviceDataCollection) {
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
    
	public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
		int width = bm.getWidth();
		int height = bm.getHeight();

		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		// CREATE A MATRIX FOR THE MANIPULATION
		Matrix matrix = new Matrix();

		// RESIZE THE BIT MAP
		matrix.postScale(scaleWidth, scaleHeight);

		// RECREATE THE NEW BITMAP
		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);

		return resizedBitmap;
	}
	
	public static Bitmap getRoundedCornerImage(Bitmap bitmap, Boolean bDefaultImage) {
		return getRoundedCornerImage(bitmap, bitmap.getWidth(), bitmap.getHeight(), bDefaultImage);
	}
	
	public static Bitmap getRoundedCornerImage(Bitmap bitmap, int iWeight, int iHeight, Boolean bDefaultImage) {
		if (bitmap == null){
			return bitmap;
		}		

		Bitmap output = Bitmap.createBitmap(iWeight, iHeight, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xffcfcfcf;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, iWeight, iHeight);
		final RectF rectF = new RectF(rect);
		final float roundPx = 100;

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		Rect rect1 = rect;
		if (bDefaultImage){		
			ColorFilter f=new LightingColorFilter(0x000000, 0xff999999);
			paint.setColorFilter(f);
	
			paint.setXfermode(new PorterDuffXfermode(Mode.SRC_OVER));
		}
		else{
			paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		}
		canvas.drawBitmap(bitmap, rect1, rect, paint);
		
		return output;
	}
	
	public static float convertPixelsToDp(float px, Resources resources){	    
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float dp = px / (metrics.densityDpi / 160f);
	    return dp;
	}
	
	public static float convertDpToPixels(float dp, Resources resources){		
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
	}

    public static Drawable covertBitmapToDrawable(Context context, Bitmap bitmap) {
        Drawable d = new BitmapDrawable(context.getResources(), bitmap);
        return d;
    }

    public static Bitmap convertDrawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), 
        drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
    
    public static Bitmap getDefaultContactBitmap(Resources resources){
    	Bitmap bmp = BitmapFactory.decodeResource(resources, R.drawable.ic_person_black_24dp);
    	return getRoundedCornerImage(bmp, true);    	
    }
}
