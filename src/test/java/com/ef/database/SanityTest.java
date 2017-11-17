package com.ef.database;

import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DatabaseCommons.class)
public class SanityTest extends BaseTest {
	
	private Connection conn;
	private PreparedStatement ps;
	private ResultSet rs;
	
	@Before
	public void before() throws SQLException {
		this.conn = this.getConnection();
		   
		// getConnection() is mysql specific, needs to be stubbed
		PowerMockito.stub(PowerMockito.method(DatabaseCommons.class, "getConnection")
		).toReturn(this.conn);
	}
	
	@Test
	public void shouldVerifySchemaCreated() throws SQLException {
		String sql = String.format("SELECT COUNT(*) FROM INFORMATION_SCHEMA.SCHEMATA "
				+ "WHERE SCHEMA_NAME='%s'", DatabaseCommons.DB.toUpperCase());
		ps = this.conn.prepareStatement(sql);
		ps.execute();

		// then only one user is found
		rs = ps.getResultSet();
		rs.next();
		int count = rs.getInt(1);
		assertEquals(1, count);
	}
	
	@Test
	public void shouldInsertAccessLog() throws SQLException {
		// given an insert query for the access log table
		String date = "2017-01-01 00:00:11";
		String ip = "192.168.234.82";
		String request = "GET / HTTP/1.1";
		String status = "200";
		String user_agent = "swcd (unknown version) CFNetwork/808.2.16 Darwin/15.6.0";
		
		// when the insert query runs
		String sql = String.format("INSERT INTO server_log.AccessLog (date, ip, request, status, user_agent) \n" + 
				"VALUES ('%s','%s','%s','%s','%s')", date, ip, request, status, user_agent);
		ps = conn.prepareStatement(sql);
		ps.executeUpdate();
		
		// then the data is persisted
		String verifySql = "SELECT * FROM server_log.AccessLog";
		ps = conn.prepareStatement(verifySql);
		rs = ps.executeQuery();
		rs.next();
		String returnedIp = rs.getString(3);
		assertEquals(ip, returnedIp);
		conn.close();
	}
	
	@Test
	public void shouldInsertAccessLogBatch() throws SQLException, FileNotFoundException {
		Connection conn = getConnection();
		conn.setAutoCommit(false);
		ps = conn.prepareStatement(SqlQueries.insertAccessLog);
		
		Scanner sc = new Scanner(new File("src/test/resources/access.log.hourlyTest"));
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
		sc.close();
		
		ps.executeBatch();
		conn.commit();
		
		String verifySql = "SELECT COUNT(*) FROM server_log.AccessLog";
		ps = conn.prepareStatement(verifySql);
		rs = ps.executeQuery();
		rs.next();
		int count = rs.getInt(1);
		assertEquals(453, count);
		
		conn.close();
	}
	
	@Test
	public void shouldVerifyUserCreated() throws SQLException {
		// given a database with the user account
		
		// when a query is executed searching for that use
		String sql = String.format("SELECT COUNT(*) FROM INFORMATION_SCHEMA.USERS "
				+ "WHERE NAME='%s'", DatabaseCommons.USER.toUpperCase());
		ps = this.conn.prepareStatement(sql);
		ps.execute();

		// then only one user is found
		rs = ps.getResultSet();
		rs.next();
		int count = rs.getInt(1);
		assertEquals(1, count);
	}
	
	@Test
	public void shouldVerifyUserCreated2() throws SQLException {
		// given a database with the user account
		
		// when a query is executed searching for that user
		String sql = String.format("SELECT * FROM INFORMATION_SCHEMA.USERS "
				+ "WHERE NAME='%s'", DatabaseCommons.USER.toUpperCase());
		ps = this.conn.prepareStatement(sql);
		ps.execute();

		// then that user name is found
		rs = ps.getResultSet();
		rs.next();
		String userName = rs.getString(1);
		assertThat("admin_user", equalToIgnoringCase(userName));
	}
	
	@Test
	public void shouldReadFromDb_sanity() throws SQLException {
		// Given a sql command to get time 
		String sql = "VALUES (CURRENT_TIMESTAMP)";
		
		// When the command is executed
		ps = this.conn.prepareStatement(sql);
		ps.executeQuery();
		
		// Then the result is a time  
		ResultSet rs = ps.getResultSet();
		rs.next();
		String datetime = rs.getString(1).substring(0, 19);
		
		String format = "yyyy-MM-dd HH:mm:ss";
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
		try {
	    	LocalDateTime.parse(datetime, formatter);
	    } catch (DateTimeParseException ex) {
			String message = String.format("'%s' not valid with format '%s'", datetime,
					format);
			fail(message);
	    }
	}
	
	@Test
	public void shouldWriteToDb_sanity() throws SQLException {
		// given a create table query
		String sql = "CREATE TABLE aaa (id INTEGER)";
		
		// when the query is run
		ps = this.conn.prepareStatement(sql);
		ps.executeUpdate();
		
		// then the table should be created and have 0 records
		String sqlVerify = "SELECT COUNT(*) FROM aaa";
		ps = this.conn.prepareStatement(sqlVerify);
		ps.executeQuery();
		rs = ps.getResultSet();
		assertNotNull(rs);
		rs.next();
		int count = rs.getInt(1);
		assertEquals(0, count);
	}

}
