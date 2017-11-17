package com.ef.database;

/**
 * Utility class for storing sql queries
 *  
 * @author Wriju Bhattacharya
 *
 */
public class SqlQueries {
	
	/********************* Static Queries *********************/
	
	
	public static final String dorpAccessLogPk = 
			String.format("ALTER TABLE %s.%s DROP COLUMN %s", DatabaseCommons.DB, 
					DatabaseCommons.ACCESS_LOG_TABLE, DatabaseCommons.ACCESS_LOG_TABLE_PK);
	
	
	public static final String createAccessLogPk = 
			String.format("ALTER TABLE %s.%s ADD %s INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY", 
					DatabaseCommons.DB, DatabaseCommons.ACCESS_LOG_TABLE, 
					DatabaseCommons.ACCESS_LOG_TABLE_PK);
	
	
	public static final String clearAccessLog = 
			String.format("DELETE FROM %s.%s", DatabaseCommons.DB, DatabaseCommons.ACCESS_LOG_TABLE);
	
	
	public static final String clearBlockedIPs = 
			String.format("DELETE FROM %s.%s", DatabaseCommons.DB, DatabaseCommons.Blocked_IP_TABLE);
	
	
	
	
	/********************* Dynamic Queries *********************/
	
	
	public static final String insertAccessLog = 
			String.format("INSERT INTO %s.%s (date, ip, request, status, user_agent) \n" + 
			"VALUES (?, ?, ?, ?, ?)", DatabaseCommons.DB, DatabaseCommons.ACCESS_LOG_TABLE);
	
	
	public static final String loadAccessLogFile =
			String.format("LOAD DATA LOCAL INFILE ? \n" + 
			"	INTO TABLE %s.%s \n" + 
			"	COLUMNS TERMINATED BY '|'", DatabaseCommons.DB, DatabaseCommons.ACCESS_LOG_TABLE);
	
	
	public static final String insertBlockedIP = 
			String.format("INSERT INTO %s.%s \n" +
			"	(date, ip, duration, numRequests, message) \n" + 
			"VALUES \n" +
			"	(?, ?, ?, ?, ?)", DatabaseCommons.DB, DatabaseCommons.Blocked_IP_TABLE);
	
	
	public static final String generateBlockedIPs = 
			String.format(
			"SELECT * \n" + 
			"FROM \n" + 
			"	( \n" + 
			"		SELECT \n" + 
			"			COUNT(date) AS numRequests, \n" + 
			"			ip\n" + 
			"		FROM %s.%s \n" + 
			"		WHERE \n" + 
			"			date >= ? AND \n" + 
			"			date <= ? \n" + 
			"		GROUP BY \n" + 
			"			ip " +  
			"	) AS agg \n" + 
			"WHERE \n" + 
			"	agg.numRequests >= ? ", DatabaseCommons.DB, DatabaseCommons.ACCESS_LOG_TABLE);
	
	
}