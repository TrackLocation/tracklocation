package com.doat.tracklocation.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
	
	// public static final TimeZone 	utc = TimeZone.getTimeZone("Etc/UTC");
	public static final String 		dateFormatPattern = "yyyy-MM-dd'T'HH:mm:ss.SSS";
	public static final String 		dateFormatPatternNoMillis = "yyyy-MM-dd'T'HH:mm:ss";
	public static final String 		dateFormatPatternSimple = "yyyy-MM-dd'T'HH:mm:ss";

	/*
	 * Convert Calendar to timestmap string: "yyyy-MM-dd'T'HH:mm:ss.SSSX"
	 */
	public static String calendarToTimestampString(Calendar cal) {
		return calendarToTimestampString(cal, false);
	}

	/*
	 * Convert Calendar to timestmap string without milliseconds in output: "yyyy-MM-dd'T'HH:mm:ss"
	 */
	public static String calendarToTimestampString(Calendar cal, boolean hideMillis) {
		if(cal == null){
			throw new IllegalArgumentException("Input argument is null: calendar");
		}
		// TimeZone.setDefault(DateUtils.utc);
		SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatPattern);
		dateFormat.setLenient(false);
		if(hideMillis){
			dateFormat = new SimpleDateFormat(dateFormatPatternNoMillis);
			dateFormat.setLenient(false);
		}
		return dateFormat.format(cal.getTime());
	}
	
	public static String getCurrentTimestampAsString(){
		SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatPatternSimple);
		return dateFormat.format(new Date());
	}

}
