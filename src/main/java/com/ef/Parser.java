package com.ef;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;

import com.ef.database.DatabaseCommons;
import com.ef.database.DatabaseService;
import com.ef.filesystem.FileUploadService;
import com.ef.time.TimeService;

/**
 * Main class which parses user input and controls rest of application 
 * 
 * @author Wriju Bhattacharya
 *
 */
public class Parser {
	
	public static final String LOAD_DATA_FLAG = "--accesslog";
	public static final String STARTDATE_FLAG = "--startDate";
	public static final String DURATION_FLAG = "--duration";
	public static final String THRESHOLD_FLAG = "--threshold";
	
	public static void main(String args[]) {
		
		// parse the input flags
		HashMap<String, String> flags = new HashMap<String, String>();
		for (String arg : args) {
			String[] tokens = arg.split("=");
			String flag = tokens[0];
			String value = tokens[1];
			flags.put(flag, value);
		}
		
		// check if data needs to be reloaded
		if (flags.containsKey(LOAD_DATA_FLAG) && flags.get(LOAD_DATA_FLAG).length() > 0) {
			String pathToFile = flags.get(LOAD_DATA_FLAG);
			loadData(pathToFile);
		}
		
		// check if blocked ip's need to be generated
		if (hasValidParams(flags)) {
			String startDate = flags.get(STARTDATE_FLAG);
			String duration = flags.get(DURATION_FLAG);
			String threshold = flags.get(THRESHOLD_FLAG);
			
			DatabaseService.deleteBlockedIPData();
			DatabaseService.generateBlockedIPs(startDate, duration, threshold);
		}
	}
	
	
	/**
	 * Clear any previous data in the access log table and load the data in the file
	 * provided.
	 *  
	 * @param pathToFile
	 */
	public static void loadData(String pathToFile) {
		long startTime, endTime, msDuration;
		String message = String.format("Deleting and reloading data into %s.%s...", 
				DatabaseCommons.DB, DatabaseCommons.ACCESS_LOG_TABLE);
		System.out.println(message);
		
		startTime = System.nanoTime();
		DatabaseService.deleteAccessLogData();
		endTime = System.nanoTime();
		msDuration = (endTime - startTime) / 1000000;
		message = String.format("All data deleted from %s.%s in %s ms", 
				DatabaseCommons.DB, DatabaseCommons.ACCESS_LOG_TABLE, msDuration);
		System.out.println(message);
		
		startTime = System.nanoTime();
//		FileUploadService.batchLoadData(pathToFile);
		DatabaseService.loadAccessLogFile(pathToFile);
		endTime = System.nanoTime();
		msDuration = (endTime - startTime) / 1000000;
		message = String.format("Data file loaded to %s.%s in %s ms", DatabaseCommons.DB, 
				DatabaseCommons.ACCESS_LOG_TABLE, msDuration);
		System.out.println(message);
	}
	
	
	/**
	 * Perform validation on the input parameters
	 * 
	 * @param flags
	 * @return
	 */
	public static boolean hasValidParams(HashMap<String, String> flags) {
		String invalidParameter = null;
		
		// check startDate is a valid datetime format
		if (!flags.containsKey(STARTDATE_FLAG) || 
				flags.get(STARTDATE_FLAG).length() == 0)
		{
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TimeService.datetimeFormat);
			try {
				LocalDateTime.parse(flags.get(STARTDATE_FLAG), formatter);
			}
			catch (DateTimeParseException e) {
				String errorMessage = String.format("%s parameter value was '%s' but "
						+ "must be in format '%s'", STARTDATE_FLAG, 
						flags.get(STARTDATE_FLAG), TimeService.datetimeFormat);
				new Exception(errorMessage).printStackTrace();
				invalidParameter = STARTDATE_FLAG;
			}
		}
		
		// check duration is "hourly" or "daily"
		if (!flags.containsKey(DURATION_FLAG) || 
				!(flags.get(DURATION_FLAG).equalsIgnoreCase("hourly") ||
				flags.get(DURATION_FLAG).equalsIgnoreCase("daily")))
		{
			String errorMessage = String.format("% parameter value was '%s' but must be "
					+ "either 'hourly' or 'daily'", DURATION_FLAG, 
					flags.get(DURATION_FLAG));
			new Exception(errorMessage).printStackTrace();
			invalidParameter = DURATION_FLAG;
		}
		
		// check threshold is an integer
		if (!flags.containsKey(THRESHOLD_FLAG) || 
				flags.get(THRESHOLD_FLAG).length() == 0)
		{
			try {
				Integer.parseInt(flags.get(THRESHOLD_FLAG));
			}
			catch(NumberFormatException e) {
				e.printStackTrace();
				invalidParameter = THRESHOLD_FLAG;
			}
		}
		
		// if there is an invalid parameter, then print an error message and return false
		if (invalidParameter != null) {
			String message = String.format("ERROR: invalid or missing flag '%s' with value '%s'", 
					invalidParameter, flags.get(invalidParameter));
			System.err.println(message);
			return false;
		}
		// otherwise return true
		else {
			return true;
		}
	}
	
}
