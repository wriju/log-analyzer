package com.ef.database;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.ef.filesystem.FileUploadService;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DatabaseCommons.class)
public class DatabaseServiceTest extends BaseTest {
	
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
	public void shoulGenerateBlockedIpsHourlyTest() throws SQLException {
		// given an access log table with data
		FileUploadService.batchLoadData("src/test/resources/access.log.hourlyTest");
		
		// when generateBlockedIPs() is called
		String startDate = "2017-01-01.15:00:00";
		String duration = "hourly";
		String threshold = "200";
		
		this.conn = this.getConnection();
		PowerMockito.stub(PowerMockito.method(DatabaseCommons.class, "getConnection")
				).toReturn(this.conn);
		DatabaseService.generateBlockedIPs(startDate, duration, threshold);
		
		// then the BlockedIPs table should have the appropriate entries
		this.conn = this.getConnection();
		String blockedIpSql = "SELECT IP FROM server_log.BlockedIP";
		try {
			ps = this.conn.prepareStatement(blockedIpSql);
			ps.executeQuery();
			rs = ps.getResultSet();
			
			ArrayList<String> blockedIPs = new ArrayList<>();
			while(rs.next()) {
				blockedIPs.add(rs.getString(1));
			}
			
			assertTrue(blockedIPs.contains("192.168.11.231"));
		}
		catch (Exception e) {
			e.printStackTrace();
			fail("error inserting or reading from table");
		}
	}
	
	
	@Test
	public void shoulGenerateBlockedIpsDailyTest() throws SQLException {
		// given an access log table with data
		FileUploadService.batchLoadData("src/test/resources/access.log.dailyTest");
		
		// when generateBlockedIPs() is called
		String startDate = "2017-01-01.00:00:00";
		String duration = "daily";
		String threshold = "500";
		
		this.conn = this.getConnection();
		PowerMockito.stub(PowerMockito.method(DatabaseCommons.class, "getConnection")
				).toReturn(this.conn);
		DatabaseService.generateBlockedIPs(startDate, duration, threshold);
		
		// then the BlockedIPs table should have the appropriate entries
		this.conn = this.getConnection();
		String blockedIpSql = "SELECT IP FROM server_log.BlockedIP";
		try {
			ps = this.conn.prepareStatement(blockedIpSql);
			ps.executeQuery();
			rs = ps.getResultSet();
			
			ArrayList<String> blockedIPs = new ArrayList<>();
			while(rs.next()) {
				blockedIPs.add(rs.getString(1));
			}
			
			assertTrue(blockedIPs.contains("192.168.102.136"));
		}
		catch (Exception e) {
			e.printStackTrace();
			fail("error inserting or reading from table");
		}
	}
	
	
	@After
	public void after() throws SQLException {
		if (rs != null && !rs.isClosed())
			rs.close();
		if (ps != null && !ps.isClosed())
			ps.close();
		if (conn != null && !conn.isClosed())
			conn.close();
	}
}
