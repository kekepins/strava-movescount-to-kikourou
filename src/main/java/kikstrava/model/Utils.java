package kikstrava.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class Utils {
	public static int[] getSecondsToHMS(int seconds ) {
		
		int[] res = new int[3];
		
		res[0] = (seconds % 86400 ) / 3600 ; // hours
		res[1] = ((seconds % 86400 ) % 3600 ) / 60; // mns
		res[2] = ((seconds % 86400 ) % 3600 ) % 60; // seconds
		
		return res;
		
	}
	public static LocalDateTime stringDateWithMilliToLocalDateTime(String strDate) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
		return LocalDateTime.parse(strDate, formatter);
	}
	
	// 2018-01-01T10:47:13Z
	public static LocalDateTime stringInstantToLocalDateTime(String strDate) {
		Instant instant = Instant.parse(strDate);
		return LocalDateTime.ofInstant(instant, ZoneId.of(ZoneOffset.UTC.getId()));
	}

	public static int getSecondsFromElapseStr(String elapseStr) {
		
		if ( elapseStr != null) {
			String[] parts = elapseStr.split(":");
			
			if (parts != null && parts.length == 3 ) {
				int[] partsInt = new int[3];
				for ( int i = 0; i < 3; i++) {
					try {
						partsInt[i] = Integer.parseInt(parts[i]);
					}
					catch(NumberFormatException nfe) {
						return 0;
					}
					
				}
				
				return partsInt[0]*3600 + partsInt[1]*60 + partsInt[2];
			}
			
		}
			
		
		return 0;
	}
}
