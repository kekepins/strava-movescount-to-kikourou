package kikstrava.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kikstrava.model.StravaActivity;
import kikstrava.model.Utils;

public class StravaService {
	
	
	private ObjectMapper objectMapper;
	private final String token;

	public StravaService(String token) {
		this.token = token;
		objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
	}

	
	/**
	 * Execute a get and return the content
	 * 
	 * @param getUrl
	 * @return
	 */
	private String doGet(String getUrl) {
		StringBuilder sb = new StringBuilder();

		try {
			URL url = new URL(getUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("Authorization", "Bearer " + this.token);

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException(
						"Failed : HTTP error code : " + conn.getResponseCode() + " - " + conn.getResponseMessage());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));

			String output;
			while ((output = br.readLine()) != null) {
				sb.append(output);
			}

			conn.disconnect();

		} catch (IOException e) {

			e.printStackTrace();
			return null;
		}

		return sb.toString();

	}

	
    public String getCurrentAthlete() {
        String url = "https://www.strava.com/api/v3/athlete";
        String result= doGet(url);
        return result;	
    }
    
    public String getAthleteKoms() {
    	String url = "https://www.strava.com/api/v3/athletes/6102935/koms?page=1&per_page=5";
        return doGet(url);
    }
    
    
    private StravaActivity[] getActivities(String url) throws JsonParseException, JsonMappingException, IOException {
       
        String json = doGet(url);
        
        System.out.println(json);
        StravaActivity[] activities = objectMapper.readValue(json, StravaActivity[].class);
        for ( StravaActivity activity : activities ) {
        	System.out.println("Name "  +  activity.getName());
        	System.out.println("Distance "  +  activity.getDistance());
        	System.out.println("Start date "  + activity.getStart_date_local());
        	
        	LocalDateTime startDate = Utils.stringToLocalDateTime(activity.getStart_date_local());
        	
        	System.out.println("Start date decode "  + startDate);
        	
        	System.out.println("Elapse time "  + activity.getElapsed_time());
        	int[] elapsed = Utils.getSecondsToHMS(activity.getElapsed_time());
        	System.out.println("Elapse hour "  + elapsed[0]);
        	System.out.println("Elapse min "  + elapsed[1]);
        	System.out.println("Elapse s "  + elapsed[2]);
        	
        	System.out.println("Moving time (s) "  + activity.getMoving_time());
        	System.out.println("Elevation"  + activity.getTotal_elevation_gain());
        	System.out.println("Type "  + activity.getType());
        }
        
        return activities;
 
    }
    
    public StravaActivity[] searchActivities(LocalDate startDate, LocalDate endDate, int page, int countPerPage) throws JsonParseException, JsonMappingException, IOException {
    	String url = getSearchActivitiesUrl(startDate, endDate, page, countPerPage);
    	return getActivities(url);
    }
    
    private String getSearchActivitiesUrl(LocalDate startDate, LocalDate endDate, int page, int countPerPage) {
    	String url = "https://www.strava.com/api/v3/athlete/activities?page=" + page + "&per_page=" + countPerPage;
    	
    	if ( startDate != null ) {
        	long epoch = startDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
    		url += "&after=" + epoch;
    	}
    
    	if ( endDate != null ) {
        	long epoch = endDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() + 86399;
    		url += "&before=" + epoch;
    	}
    	
    	return url;
    }

    


}
