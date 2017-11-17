package com.ef.time;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeService {
	
	public static final String datetimeFormat = "yyyy-MM-dd.HH:mm:ss";

	/**
	 * Increment the input datetime by 1 hour or 1 day
	 * 
	 * @param datetime
	 * @param duration
	 * @return a string representing the incremented datetime
	 */
	public static String getNextInterval(String datetime, String duration) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(datetimeFormat);
		LocalDateTime dateTime = LocalDateTime.parse(datetime, formatter);
		LocalDateTime nextInterval = null;
		
		if (duration.equalsIgnoreCase("hourly"))
			nextInterval = dateTime.plusHours(1);
		else if (duration.equalsIgnoreCase("daily"))
			nextInterval = dateTime.plusDays(1);
		
		return nextInterval.format(formatter);
	}
	
	
	/**
	 * Change the format of the input datetime to take out the period, needed for 
	 * testability.
	 * 
	 * @param datetime
	 * @return
	 */
	public static String convertFormat(String datetime) {
		String convertedFormat = "yyyy-MM-dd HH:mm:ss";
		DateTimeFormatter convertedFormatter = DateTimeFormatter.ofPattern(convertedFormat);
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(datetimeFormat);
		LocalDateTime convertedDatetime = LocalDateTime.parse(datetime, formatter);
		return convertedDatetime.format(convertedFormatter);
	}
	
}
