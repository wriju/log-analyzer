package com.ef.filesystem;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Scanner;

import com.ef.database.DatabaseCommons;
import com.ef.database.SqlQueries;

/**
 * This class handles everything file upload related. This ended up being unused due to
 * the LOAD DATA query in MySQL which performs faster.
 *
 * LOAD DATA performed ~3x faster with auto commit turned on than off. Batch inserts 
 * with prepared statements performed ~10x faster with auto commit turned off than 
 * on. Comparing the faster of both configurations, LOAD DATA performed ~7-10x faster 
 * than parsing the log file in java and using batch inserts.
 * 
 * @author Wriju Bhattacharya
 *
 */
public class FileUploadService {
	
	/**
	 * Load the access.log file into the database using batch inserts. The db 
	 * connection is kept open in between inserts to speed up performance.
	 * 
	 * NOTE: DatabaseConnection.loadAccessLogFile is much faster
	 * 
	 */
	public static void batchLoadData(String pathToFile) {
		Scanner sc = null;
		Connection conn = null;
		PreparedStatement ps = null;
		
		try 
        {
			// set up the resources
			conn = DatabaseCommons.getConnection();
			conn.setAutoCommit(false);
			ps = conn.prepareStatement(SqlQueries.insertAccessLog);
			sc = new Scanner(new File(pathToFile));
			
			// read the log file line by line and add a batch insert to the prepared
			// statement for each line
			while(sc.hasNextLine()) {
				String line = sc.nextLine();
				if (line != null && line.length() > 0) {
					String[] tokens = line.split("\\|");
					String date = tokens[0];
					String ip = tokens[1];
					String request = tokens[2];
					String status = tokens[3];
					String user_agent = tokens[4];
					
					ps.setString(1, date);
					ps.setString(2, ip);
					ps.setString(3, request);
					ps.setString(4, status);
					ps.setString(5, user_agent);
					ps.addBatch();
				}
			}
			
			// execute the batch and check if successful
			int[] results = ps.executeBatch();
			conn.commit();
			
			// print the total records inserted
			String message = String.format("%s records added to %s.%s", results.length, 
					DatabaseCommons.DB, DatabaseCommons.ACCESS_LOG_TABLE);
			System.out.println(message);
        } 
        catch (Exception e) 
        {
        	e.printStackTrace();
        }
		finally {
			if (sc != null) {
				sc.close();
			}
			DatabaseCommons.closeDatabaseResources(ps, conn);
		}
	}
	
}
