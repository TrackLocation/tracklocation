package com.doat.tracklocation.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import com.doat.tracklocation.Controller;
import com.doat.tracklocation.R;
import com.doat.tracklocation.datatype.CommandEnum;
import com.doat.tracklocation.datatype.ContactData;
import com.doat.tracklocation.datatype.ContactDeviceData;
import com.doat.tracklocation.datatype.ContactDeviceDataList;
import com.doat.tracklocation.datatype.DeviceData;
import com.doat.tracklocation.datatype.DeviceTypeEnum;
import com.doat.tracklocation.datatype.Message;
import com.doat.tracklocation.datatype.MessageData;
import com.doat.tracklocation.datatype.MessageDataContactDetails;
import com.doat.tracklocation.log.LogManager;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Utils {
	
	private final static String COMMA = ",";
	private final static String className = "Utils";
	private final static Gson gson = new Gson();
    private static List<Integer> mColors;

    static {
        mColors = Arrays.asList(
                0xffe57373,
                0xfff06292,
                0xffba68c8,
                0xff9575cd,
                0xff7986cb,
                0xff64b5f6,
                0xff4fc3f7,
                0xff4dd0e1,
                0xff4db6ac,
                0xff81c784,
                0xffaed581,
                0xffff8a65,
                0xffd4e157,
                0xffffd54f,
                0xffffb74d,
                0xffa1887f,
                0xff90a4ae
        );
    }

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
//			LogManager.LogErrorMsg(className, "readCustomerDataFromFile", "Unable to read file: " + 
//				fileName + ". Error message: " + e.getMessage());
			LogManager.LogException(e, className, "readCustomerDataFromFile");
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
//				LogManager.LogErrorMsg(className, "readCustomerDataFromFile", "Unable to read file: " + 
//					fileName + ". Error message: " + ex.getMessage());
				LogManager.LogException(ex, className, "readCustomerDataFromFile");
			}
		}
		return inputParamsList;
	}
	
	/**
	 * Read file as on string 
	 * It should contain data in JSON and should be converted
	 * to java object by GSON
	 * @param fileName
	 * @return String
	 */
	public static String readInputFile(String fileName){
		String methodName = "readInputFile";
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
			String logMessage = "Unable to read file:\n" + fileName;
			LogManager.LogException(e, className, methodName);
			Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + logMessage);
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
		try {
			ContactDeviceDataList contactDeviceDataList = gson.fromJson(jsonDataString, ContactDeviceDataList.class);
			return contactDeviceDataList;
		} catch (JsonSyntaxException e) {
    		LogManager.LogException(e, className, "fillContactDeviceDataFromJSON");
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
		if(bitmap == null){
			return null;
		}
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
	
	public static Bitmap changeBitmapColor(Bitmap sourceBitmap) {
	    Bitmap resultBitmap = Bitmap.createBitmap(sourceBitmap.getWidth(), sourceBitmap.getHeight(), Config.ARGB_8888);
	    final Rect rect = new Rect(0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight());

	    Paint paint = new Paint();
	    paint.setAntiAlias(true);
	    ColorFilter filter = new LightingColorFilter(0x000000, 0xff999999);
	    paint.setColorFilter(filter);	  
	    Canvas canvas = new Canvas(resultBitmap);
	    canvas.drawBitmap(sourceBitmap, rect, rect, paint);
	    return resultBitmap;
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

    /**
     * Uses static final constants to detect if the device's platform version is Gingerbread or
     * later.
     */
    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    public static boolean hasICS() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }
    
    public static Bitmap getDefaultContactBitmap(Resources resources){
    	Bitmap bmp = BitmapFactory.decodeResource(resources, R.drawable.ic_person_black_24dp);
    	return getRoundedCornerImage(bmp, true);    	
    }
    
    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    
	public static MessageDataContactDetails initLocalRecipientData(Context context){
		String account = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
		String macAddress = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_MAC_ADDRESS);
		String phoneNumber = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_NUMBER);
		String regId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
		int batteryLevel = Controller.getBatteryLevel(context);
		return new MessageDataContactDetails(account, macAddress, phoneNumber, regId, batteryLevel); 
	}

	public static Bitmap textAsBitmap(String text, Bitmap bmp, int iColor) {
		Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        paint.setFakeBoldText(false);
        paint.setStyle(Paint.Style.FILL);
        paint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setStrokeWidth(0);

		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(image);

        canvas.drawColor(iColor);
        paint.setTextSize(Math.min( width, height) / 2);
        canvas.drawText(text, width / 2, height / 2 - ((paint.descent() + paint.ascent()) / 2), paint);
		return image;
	}

    public static int getRandomColor() {
        Random rnd = new Random();
        return mColors.get(rnd.nextInt(mColors.size()));
    }

}
