package com.ef.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.ef.time.TimeService;

/**
 * This class performs all database transactions
 * 
 * @author Wriju Bhattacharya
 *
 */
public class DatabaseService {
	
	/**
	 * Clear all values from the access log table
	 * 
	 * @return 0 or higher if success, -1 if fails
	 */
	public static int deleteAccessLogData() {
		return DatabaseCommons.runStaticUpdate(SqlQueries.clearAccessLog);
	}
	
	
	/**
	 * Drop the primary key for the access log table. This should be used before 
	 * first loading the empty table with LOAD DATA query.
	 * 
	 * @return 0 or higher if success, -1 if fails
	 */
	public static int dropAccessLogPk() {
		return DatabaseCommons.runStaticUpdate(SqlQueries.dorpAccessLogPk);
	}
	
	
	/**
	 * Create the primary key for the access log table. This should be used after 
	 * first loading the empty table with LOAD DATA query.
	 * 
	 * @return 0 or higher if success, -1 if fails
	 */
	public static int createAccessLogPk() {
		return DatabaseCommons.runStaticUpdate(SqlQueries.createAccessLogPk);
	}

	
	/**
	 * Load the given file to the access log table. The LOAD DATA query is used and 
	 * performs much faster than batched INSERT queries. It performs faster when 
	 * auto commit is turned on than when turned off.
	 * 
	 * NOTE: Need to drop the PK before LOAD DATA and recreate it after to make this
	 * work with the file format.
	 * 
	 * @param pathToFile
	 * @return
	 */
	public static int loadAccessLogFile(String pathToFile) {
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {
			dropAccessLogPk();
			conn = DatabaseCommons.getConnection();
			conn.setAutoCommit(true);
			
			ps = conn.prepareStatement(SqlQueries.loadAccessLogFile);
			ps.setString(1, pathToFile);
			createAccessLogPk();
			return ps.executeUpdate();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			DatabaseCommons.closeDatabaseResources(conn, ps);
		}
		
		return -1;
	}
	
	
	/**
	 * Gather the blocked ip's and load them to the blocked ip table
	 * 
	 * @param startDate
	 * @param duration
	 * @param threshold
	 */
	public static void generateBlockedIPs(String startDate, String duration, 
			String threshold) 
	{
		Connection conn = null;
		PreparedStatement psGenerateBlockedIPs = null;
		PreparedStatement psInsertBlockedIPs = null;
		ResultSet rs = null;
		
		// calculate the end datetime
		String endDate = TimeService.getNextInterval(startDate, duration);
		String convertedStartDate = TimeService.convertFormat(startDate);
		String convertedEndDate = TimeService.convertFormat(endDate);
				
		try {
			// set up the resources
			conn = DatabaseCommons.getConnection();
			psGenerateBlockedIPs = conn.prepareStatement(SqlQueries.generateBlockedIPs);
			psInsertBlockedIPs = conn.prepareStatement(SqlQueries.insertBlockedIP);
			
			// generate the blocked ip's
			psGenerateBlockedIPs.setString(1, convertedStartDate);
			psGenerateBlockedIPs.setString(2, convertedEndDate);
			psGenerateBlockedIPs.setString(3, threshold);
			rs = psGenerateBlockedIPs.executeQuery();
			
			// parse the blocked ip's and load them into a batch insert query
			System.out.println();
			while (rs.next()) {
				String numRequests = rs.getString(1);
				String ip = rs.getString(2);
				String message = String.format("IP %s blocked because %s "
						+ "requests made %s on %s which exceeds threshold of %s", 
						ip, numRequests, duration, startDate, threshold);
				
				psInsertBlockedIPs.setString(1, convertedStartDate);
				psInsertBlockedIPs.setString(2, ip);
				psInsertBlockedIPs.setString(3, duration);
				psInsertBlockedIPs.setString(4, numRequests);
				psInsertBlockedIPs.setString(5, message);
				psInsertBlockedIPs.addBatch();
				
				System.out.println(message);
			}
			System.out.println();
			
			// execute the batch insert
			int[] results = psInsertBlockedIPs.executeBatch();
			String message = String.format("%s records found and added to %s.%s", 
					results.length, DatabaseCommons.DB, 
					DatabaseCommons.Blocked_IP_TABLE);
			System.out.println(message);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			// close the resources
			DatabaseCommons.closeDatabaseResources(conn, psGenerateBlockedIPs, 
					psInsertBlockedIPs, rs);
		}
	}
	
	
	/**
	 * Clear all values from the blocked ip's table
	 * 
	 * @return returns 0 or higher if success, -1 if fails
	 */
	public static int deleteBlockedIPData() {
		return DatabaseCommons.runStaticUpdate(SqlQueries.clearBlockedIPs);
	}
	
}
