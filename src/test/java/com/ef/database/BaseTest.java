package com.ef.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.h2.tools.Server;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

public class BaseTest {
	
	protected static String h2_conn_string;
	protected static String h2_conn_with_schema;
	
	private static Server tcpServer;
	
	private Connection rootConn;
	private PreparedStatement ps;
	private ResultSet rs;
	
	@BeforeClass
	public static void startH2TcpServer() {
		try {
			// set up a tcp server for debugging via web
			tcpServer = Server.createTcpServer().start();
			h2_conn_string = String.format("jdbc:h2:%s/mem:%s", tcpServer.getURL(), 
					DatabaseCommons.DB);
			System.out.println("Server started and connection is open.");
			System.out.println("URL: " + h2_conn_string);
		}
		catch (Exception e) {
			h2_conn_string = "jdbc:h2:mem:" + DatabaseCommons.DB;
			e.printStackTrace();
		}
		
		h2_conn_with_schema = h2_conn_string + ";SCHEMA=server_log"; //;MV_STORE=FALSE;MVCC=FALSE";
	}
	
	protected Connection getRootConnection() throws SQLException {
		Connection conn = DriverManager.getConnection(h2_conn_string, 
				"root", "");
		conn.setAutoCommit(true);
		return conn;
	}
	
	protected Connection getConnection() throws SQLException {
		Connection conn = DriverManager.getConnection(BaseTest.h2_conn_with_schema, 
				DatabaseCommons.USER, DatabaseCommons.PASS);
		conn.setAutoCommit(true);
		return conn;
	}
	
	@Before
	public void initSchema() throws SQLException {
		this.rootConn = this.getRootConnection();
		
		String sql = "RUNSCRIPT FROM 'classpath:create_schema_h2.sql'";
		ps = this.rootConn.prepareStatement(sql);
		ps.execute();
	}
	
	@After
	public void tearDown() throws SQLException {
		String sql = "DROP ALL OBJECTS DELETE FILES";
		ps = this.rootConn.prepareStatement(sql);
		ps.execute();
		
		if (rs != null && !rs.isClosed())
			rs.close();
		if (ps != null && !ps.isClosed())
			ps.close();
		if (rootConn != null && !rootConn.isClosed())
			rootConn.close();
	}
	
	@AfterClass
	public static void stopH2TcpServer() {
		try {
			tcpServer.stop();
			tcpServer.shutdown();
			System.out.println("Server stopped and connection is closed.");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
