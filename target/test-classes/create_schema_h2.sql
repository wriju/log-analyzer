DROP ALL OBJECTS DELETE FILES;

CREATE USER IF NOT EXISTS admin_user PASSWORD 'admin_pass' ADMIN;
GRANT ALTER ANY SCHEMA TO admin_user;

DROP SCHEMA IF EXISTS server_log;
CREATE SCHEMA server_log;
GRANT ALL ON SCHEMA server_log TO admin_user;

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
	ALTER COLUMN id INTEGER NOT NULL AUTO_INCREMENT;
CREATE PRIMARY KEY ON server_log.AccessLog (id);
CREATE INDEX ip_index ON server_log.AccessLog (ip);


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
	ALTER COLUMN id INTEGER NOT NULL AUTO_INCREMENT;
CREATE PRIMARY KEY ON server_log.BlockedIP (id);