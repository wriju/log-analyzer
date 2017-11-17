package com.ef.database;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


/**
 * Utility class for common database functionality and constants
 * 
 * @author vagrant
 *
 */
public class DatabaseCommons {
	
	public static final String CONNECTION_STRING = "jdbc:mysql://localhost:3306/";
	public static final String USER = "admin_user";
	public static final String PASS = "admin_pass";
	
	public static final String DB = "server_log";
	public static final String ACCESS_LOG_TABLE = "AccessLog";
	public static final String ACCESS_LOG_TABLE_PK = "id";
	public static final String Blocked_IP_TABLE = "BlockedIP";
	
	
	/**
	 * Returns a connection to the database
	 * 
	 * @return connection to the db
	 */
	public static Connection getConnection() {
		Connection conn = null;
		
		try {
			conn = DriverManager.getConnection(DatabaseCommons.CONNECTION_STRING + DatabaseCommons.DB, DatabaseCommons.USER, DatabaseCommons.PASS);
			conn.setAutoCommit(true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return conn;
	}
	
	
	/**
	 * Closes database resources (helper method to clean up code)
	 * 
	 * NOTE: close ResultSet, then Statement, then Connection objects in that order
	 * 
	 * @param resources - first ResultSets, then Statements, then Connections
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void closeDatabaseResources(Object... resources) {
		ArrayList<Class> validInterfaces = new ArrayList<Class>();
		validInterfaces.add(Connection.class);
		validInterfaces.add(Statement.class);
		validInterfaces.add(ResultSet.class);
		
		for (Object resource : resources) {
			if (resource == null) {
				continue;
			}
			
			Class resourceClass = resource.getClass();
			boolean validResource = false;
			for (Class validInterface : validInterfaces) {
				if (validInterface.isAssignableFrom(resourceClass)) {
					validResource = true;
					try {
						Method method = resourceClass.getMethod("close");
						method.invoke(resource);
						break;
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
			if (!validResource) {
				String errorMessage = String.format("closeResources() called on "
						+ "object of incompatible class '%s'", resourceClass);
				new Exception(errorMessage).printStackTrace();
			}
		}
	}
	
	
	/**
	 * Run a create, update, or delete sql query that doesn't require any inputs and 
	 * doesn't return any data
	 * 
	 * @param staticSql
	 * @return 0 or higher if success, -1 if fails
	 */
	public static int runStaticUpdate(String staticSql) {
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {
			conn = DatabaseCommons.getConnection();
			ps = conn.prepareStatement(staticSql);
			int result = ps.executeUpdate();
			return result;
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
	 * Print the column names and values in the given result set, then set the 
	 * cursor to before the first row. Used for debugging
	 * 
	 * @param rs
	 * @throws SQLException
	 */
	public static void printResultSet(ResultSet rs) throws SQLException {
		if (rs == null || rs.isClosed()) {
			return;
		}
		
		try {
			ResultSetMetaData rsmd = rs.getMetaData();
			int numCols = rsmd.getColumnCount();
			
			// print the column names
			for (int i = 1; i <= numCols; i++) {
				System.out.print(rsmd.getColumnName(i));
				if (i < numCols) {
					System.out.print(", ");
				}
			}
			
			// print the column data
			while (rs.next()) {
				System.out.println();
				for (int i = 1; i <= numCols; i++) {
					System.out.print(rs.getString(i));
					if (i < numCols) {
						System.out.print(", ");
					}
				}
			}
			
			rs.beforeFirst();
		}
		catch (Exception e) {
			System.out.println();
			//e.printStackTrace();
		}
	}
}
