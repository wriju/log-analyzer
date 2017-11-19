-- Get the contents from AccessLog table
SELECT * FROM server_log.AccessLog


-- Get the contents from BlockedIP table
SELECT * FROM server_log.BlockedIP


-- Get the AccessLog entries for the IPs in the BlockedIPs table
SELECT 
	bip.numRequests,
	al.date,
	al.ip,
	al.request,
	al.status,
	al.user_agent
FROM
	server_log.AccessLog al
	INNER JOIN server_log.BlockedIP AS bip
		ON bip.ip = al.ip
ORDER BY
	bip.numRequests DESC,
	bip.ip ASC,
	al.date ASC;


-- Get IPs that made equal to or greater than the threshold requests 
-- between the start and end datetimes.
SET @startDate:='2017-01-01.13:00:00';
SET @endDate:='2017-01-02 13:00:00';
SET @threshold:='250';
SELECT * 
FROM 
	( 
		SELECT 
			COUNT(date) AS requests, 
			ip
		FROM server_log.AccessLog 
		WHERE 
			date >= @startDate AND 
			date <= @endDate 
		GROUP BY 
			ip
	) AS agg 
WHERE 
	agg.requests >= @threshold
ORDER BY 
	agg.requests DESC, 
	agg.ip;



-- Get the AccessLog entries for the IPs that made equal to or greater 
-- than the threshold requests between the start and end datetimes.
-- (same as third query, without dependency on BlockedIP table)
SET @startDate:='2017-01-01.13:00:00';
SET @endDate:='2017-01-02 13:00:00';
SET @threshold:='250';
SELECT 
	al.date,
	al.ip,
	al.request,
	al.status,
	al.user_agent
FROM
	server_log.AccessLog al
	INNER JOIN 
	(
		SELECT * 
		FROM 
			( 
				SELECT 
					COUNT(date) AS numRequests, 
					ip
				FROM server_log.AccessLog 
				WHERE 
					date >= @startDate AND 
					date <= @endDate 
				GROUP BY 
					ip
			) AS agg 
		WHERE 
			agg.numRequests >= @threshold
	) AS bip
		ON bip.ip = al.ip
ORDER BY
	bip.numRequests DESC,
	bip.ip ASC,
	al.date ASC;