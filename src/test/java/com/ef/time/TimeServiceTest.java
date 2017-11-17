package com.ef.time;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TimeServiceTest {
	
	@Test
	public void shouldGetNextInterval1() {
		// given a datetime and a duration of hourly
		String datetime = "2017-01-01.00:00:12";
		String duration = "hourly";
		
		// when getNextInterval() is called
		String result = TimeService.getNextInterval(datetime, duration);
		
		// then the datetime is incremented by 1 hour
		assertEquals("2017-01-01.01:00:12", result);
	}
	
	@Test
	public void shouldGetNextInterval2() {
		// given a datetime and a duration of daily
		String datetime = "2017-01-01.00:00:12";
		String duration = "daily";
		
		// when getNextInterval() is called
		String result = TimeService.getNextInterval(datetime, duration);
		
		// then the datetime is incremented by 1 day
		assertEquals("2017-01-02.00:00:12", result);
	}

}
