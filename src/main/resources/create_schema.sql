DROP USER IF EXISTS 'admin_user'@'localhost';
CREATE USER 'admin_user'@'localhost' IDENTIFIED BY 'admin_pass';
GRANT ALL PRIVILEGES ON *.* TO 'admin_user'@'localhost'	WITH GRANT OPTION;


DROP DATABASE IF EXISTS server_log;
CREATE DATABASE server_log;


-- AccessLog table to store server info. Index created on ip column to speed up blocked ip 
-- query.
--
-- NOTE: LOAD DATA query for populating this table runs very fast but PK id column must be
--  	 dropped and added in later
--
DROP TABLE IF EXISTS server_log.AccessLog;
CREATE TABLE server_log.AccessLog (
	id 			INTEGER,
	date 		DATETIME,	
	ip 			VARCHAR(32), 
	request 	VARCHAR(32), 
	status 		SMALLINT,
	user_agent 	VARCHAR(256)
);
ALTER TABLE server_log.AccessLog
	MODIFY COLUMN id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY;
CREATE INDEX ip_index ON server_log.AccessLog (ip);


-- BlockedIP table stores blocked ips with request statistics. Index created on ip column 
-- to speed up verification queries.
DROP TABLE IF EXISTS server_log.BlockedIP;
CREATE TABLE server_log.BlockedIP (
	id 			INTEGER,
	date 		DATETIME,	
	ip 			VARCHAR(32),
	duration 	VARCHAR(16),
	numRequests VARCHAR(32), 
	message 	VARCHAR(256)
);
ALTER TABLE server_log.BlockedIP
	MODIFY COLUMN id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY;
CREATE INDEX ip_index ON server_log.BlockedIP (ip);