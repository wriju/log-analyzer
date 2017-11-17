package com.ef.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Use H2 to unit test the db layer 
 * @author vagrant
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(DatabaseCommons.class)
public class DatabaseCommonsTest extends BaseTest {
	
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
	public void shouldCloseConnections() throws SQLException {
		// given a connection, prepared statement, and result set
		String sql = "VALUES (CURRENT_TIMESTAMP)";
		ps = this.conn.prepareStatement(sql);
		ps.execute();
		rs = ps.getResultSet();
		
		// when closeDatabaseResources() is called
		DatabaseCommons.closeDatabaseResources(rs, ps, conn);
		
		// then resources are closed
		assertTrue(rs.isClosed());
		assertTrue(ps.isClosed());
		assertTrue(conn.isClosed());
	}
	
	@Test
	public void shouldRunStaticUpdateForCreate() throws SQLException {
		// given some static sql to create a table
		String createSql = "CREATE TABLE server_log.aaa (id INTEGER)";
		
		// when createSql is run
		DatabaseCommons.runStaticUpdate(createSql);
		
		// then the table is created and has 0 entries
		this.conn = this.getConnection();
		String sqlVerify = "SELECT * FROM server_log.aaa";
		try {
			ps = this.conn.prepareStatement(sqlVerify);
			ps.executeQuery();
			rs = ps.getResultSet();
			
			int numRows = 0;
			while (rs.next()) {
				numRows++;
			}
			assertEquals(0, numRows);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail("error creating or reading from table");
		}
	}
	
	@Test
	public void shouldRunStaticUpdateForInsert() throws SQLException {
		// given some static sql to create a table and insert a record
		String createSql = "CREATE TABLE server_log.aaa (id INTEGER)";
		String insertSql = "INSERT INTO server_log.aaa (id) VALUES (99)";
		
		// when createSql and insertSql are run
		DatabaseCommons.runStaticUpdate(createSql);
		this.conn = this.getConnection();
		PowerMockito.stub(PowerMockito.method(DatabaseCommons.class, 
				"getConnection")).toReturn(this.conn);
		DatabaseCommons.runStaticUpdate(insertSql);
		
		// then the table is created and has 1 entry with the inserted value
		this.conn = this.getConnection();
		String sqlVerify = "SELECT * FROM server_log.aaa";
		try {
			ps = this.conn.prepareStatement(sqlVerify);
			ps.executeQuery();
			rs = ps.getResultSet();
			
			int numRows = 0;
			int firstRecordId = -1;
			while (rs.next()) {
				numRows++;
				if (numRows == 1) {
					firstRecordId = rs.getInt(numRows);
				}
			}
			assertEquals(1, numRows);
			assertEquals(99, firstRecordId);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail("error inserting or reading from table");
		}
	}
	
	@Test
	public void shouldRunStaticUpdateForUpdate() throws SQLException {
		// given some static sql to create a table, insert a record, and 
		// update that record
		String createSql = "CREATE TABLE server_log.aaa (id INTEGER)";
		String insertSql = "INSERT INTO server_log.aaa (id) VALUES (99)";
		String updateSql = "UPDATE server_log.aaa SET id=100 WHERE id=99";
		
		// when createSql, then insertSql, then updateSql are run
		DatabaseCommons.runStaticUpdate(createSql);
		this.conn = this.getConnection();
		PowerMockito.stub(PowerMockito.method(DatabaseCommons.class, 
				"getConnection")).toReturn(this.conn);
		DatabaseCommons.runStaticUpdate(insertSql);
		this.conn = this.getConnection();
		PowerMockito.stub(PowerMockito.method(DatabaseCommons.class, 
				"getConnection")).toReturn(this.conn);
		DatabaseCommons.runStaticUpdate(updateSql);
		
		// then the table is created and has 1 entry with the updated value
		this.conn = this.getConnection();
		String sqlVerify = "SELECT * FROM server_log.aaa";
		try {
			ps = this.conn.prepareStatement(sqlVerify);
			ps.executeQuery();
			rs = ps.getResultSet();
			
			int numRows = 0;
			int firstRecordId = -1;
			while (rs.next()) {
				numRows++;
				if (numRows == 1) {
					firstRecordId = rs.getInt(numRows);
				}
			}
			assertEquals(1, numRows);
			assertEquals(100, firstRecordId);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail("error inserting or reading from table");
		}
	}
	
	@Test
	public void shouldRunStaticUpdateForDelete() throws SQLException {
		// given some static sql to create a table, insert a record, and 
		// delete that record
		String createSql = "CREATE TABLE server_log.aaa (id INTEGER)";
		String insertSql = "INSERT INTO server_log.aaa (id) VALUES (99)";
		String deleteSql = "DELETE FROM server_log.aaa WHERE id=99";
		
		// when createSql, then insertSql, then deleteSql are run
		DatabaseCommons.runStaticUpdate(createSql);
		this.conn = this.getConnection();
		PowerMockito.stub(PowerMockito.method(DatabaseCommons.class, 
				"getConnection")).toReturn(this.conn);
		DatabaseCommons.runStaticUpdate(insertSql);
		this.conn = this.getConnection();
		PowerMockito.stub(PowerMockito.method(DatabaseCommons.class, 
				"getConnection")).toReturn(this.conn);
		DatabaseCommons.runStaticUpdate(deleteSql);
		
		// then the table is created and has 1 entry with the updated value
		this.conn = this.getConnection();
		String sqlVerify = "SELECT * FROM server_log.aaa";
		try {
			ps = this.conn.prepareStatement(sqlVerify);
			ps.executeQuery();
			rs = ps.getResultSet();
			
			int numRows = 0;
			while (rs.next()) {
				numRows++;
			}
			assertEquals(0, numRows);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail("error inserting or reading from table");
		}
	}
	
//	@Test
//	public void shouldNotRunStaticUpdateForSelect() throws SQLException {
//		// given some static sql select a row count
//		String selectSql = "SELECT COUNT(*) FROM server_log.AccessLog";
//		
//		// when runStaticSql() is called
//		int result = DatabaseCommons.runStaticUpdate(selectSql);
//		
//		// then -1 is returned
//		assertEquals(-1, result);
//	}
	
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
