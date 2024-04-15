
package kikstrava.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kikstrava.model.StravaActivity;
import kikstrava.model.StravaTokenInfo;
import kikstrava.model.Stream;
import kikstrava.model.Utils;

public class StravaV3Service {
	
	private static final String AUTH_URL = "https://www.strava.com/oauth/authorize";
	private static final String TOKEN_URL = "https://www.strava.com/oauth/token";
	private static final String CALLBACK_URL = "http://127.0.0.1:5000/";
	
	
	private ObjectMapper objectMapper;
	private final String clientId;
	private final String clientSecret;
	private String code;
	
	//private String token;
	private StravaTokenInfo tokenInfo;

	public StravaV3Service(String clientId, String clientSecret) {
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		// read previous code/token in init ...
		readSavedInfos();
		
	}
	
	private void doAuthentication() throws ServiceException {
		// check token ...
		if ( code == null ) {
			throw new ServiceException("No strava code");
		}
		
		// token not initialized
		if ( tokenInfo == null ) {
			// need a token
			tokenInfo = getToken();
			
			// still no token
			if (tokenInfo == null) {
				throw new ServiceException("Pbm strava token init :-(");
			}

		}
		
		// Expired or less than one hour left
		System.out.println("[Strava] Now minus 1 hour " +  LocalDateTime.now().minusHours(2));
		System.out.println("[Strava token Expiration] " +  tokenInfo.getExpirationDate());

		LocalDateTime expirationDate = tokenInfo.getExpirationDate();

		System.out.println("[Strava token Expiration] " + expirationDate.getYear()  + "|" + expirationDate.getMonthValue() + "|" + expirationDate.getDayOfMonth() + "|" + expirationDate.getHour() + "|" + expirationDate.getMinute() + "|" + expirationDate.getSecond()  );
		System.out.println("Now:" + LocalDateTime.now().getYear()  + "|" + LocalDateTime.now().getMonthValue() + "|" + LocalDateTime.now().getDayOfMonth() + "|" + LocalDateTime.now().getHour() + "|" + LocalDateTime.now().getMinute() + "|" + LocalDateTime.now().getSecond()  );

		if ( expirationDate.isBefore(LocalDateTime.now() )) {
		// check if token is expired or is about to expire


		//if ( LocalDateTime.now().minusHours(2).isAfter(tokenInfo.getExpirationDate()) )  {
			// should update
			System.out.println("[Strava] Token expired!!! " + tokenInfo.getExpirationDate());
			
			tokenInfo = updateToken();
			
			if (tokenInfo == null) {
				throw new ServiceException("Pbm strava token refresh :-(");
			}
			writeCurrentToken(tokenInfo);

			
		}
		
	}

	
	private StravaTokenInfo updateToken() {
		// TODO Auto-generated method stub
		/*curl -X POST https://www.strava.com/api/v3/oauth/token \
			  -d client_id=ReplaceWithClientID \
			  -d client_secret=ReplaceWithClientSecret \
			  -d grant_type=refresh_token \
			  -d refresh_token=ReplaceWithRefreshToken*/

		System.out.println("[Strava] Update token");
		StringBuilder json = new StringBuilder();

		try {
			
			URL url = new URL(TOKEN_URL);
		    Map<String,Object> params = new LinkedHashMap<>();
	        params.put("client_id", this.clientId);
	        params.put("client_secret", this.clientSecret);
	        params.put("refresh_token", this.tokenInfo.getRefresh_token());
	        params.put("grant_type", "refresh_token");

	        StringBuilder postData = new StringBuilder();
	        for (Map.Entry<String,Object> param : params.entrySet()) {
	            if (postData.length() != 0) postData.append('&');
	            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
	            postData.append('=');
	            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
	        }
	        
	        byte[] postDataBytes = postData.toString().getBytes("UTF-8");

	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setRequestMethod("POST");
	        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
	        conn.setDoOutput(true);
	        conn.getOutputStream().write(postDataBytes);
	        
	        int statusCode = conn.getResponseCode(); 
	        
	        if ( statusCode >= 200 && statusCode < 400 ) {
	        
		    	BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
	
				String line;
				while ((line = br.readLine()) != null) {
					json.append(line);
				}
				
				conn.disconnect();
				StravaTokenInfo tokenInfo = null;
				if ( json != null ) {
				
					// json to Token info :
					System.out.println(json);
					tokenInfo =  objectMapper.readValue(json.toString(), StravaTokenInfo.class);
					//token = tokenInfo.getAccess_token();
					
					System.out.println("[Stava] New refresh Token: " + tokenInfo);
				}
				
				return tokenInfo;
	        }
	        else {
	        	// pbm 
	        	BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
	        	
	        	String responseBody = br.lines().collect(Collectors.joining());
				System.out.println("Error during auth body http [" + statusCode + "]:" + responseBody);
	        	return null;
	        }

		} catch (IOException e) {

			e.printStackTrace();
			return null;
		}			  
	}

	/**
	 * Execute a get and return the content
	 * 
	 * @param getUrl
	 * @return
	 * @throws ServiceException 
	 */
	private String doGet(String getUrl) throws ServiceException {
		
		doAuthentication();
		
		StringBuilder sb = new StringBuilder();

		try {
			URL url = new URL(getUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			
			conn.setRequestProperty("Authorization", "Bearer " + this.tokenInfo.getAccess_token());

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


	 /**
	  * Get strava url providing authorization ... 
	  * 
	  */
	public String getAuthUrl() {
		return String.format(AUTH_URL + "?client_id=%s&response_type=code&scope=activity:read&redirect_uri=%s&client_secret=%s&approval_prompt=force", clientId, CALLBACK_URL, clientSecret);
	}
	 

	/**
	 * Get a valid token from strava !!!!
	 */
	public StravaTokenInfo getToken() {
		System.out.println("Get new token from strava");
		StringBuilder json = new StringBuilder();

		try {
			
			URL url = new URL(TOKEN_URL);
		    Map<String,Object> params = new LinkedHashMap<>();
	        params.put("client_id", this.clientId);
	        params.put("client_secret", this.clientSecret);
	        params.put("code", this.code);
	        params.put("grant_type", "authorization_code");
	        
	        //
	        //params.put("scope", "read_all");
	        params.put("scope", "activity:read");
	        //scope=read_all&scope=activity:read_all

	        StringBuilder postData = new StringBuilder();
	        for (Map.Entry<String,Object> param : params.entrySet()) {
	            if (postData.length() != 0) postData.append('&');
	            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
	            postData.append('=');
	            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
	        }
	        byte[] postDataBytes = postData.toString().getBytes("UTF-8");

	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setRequestMethod("POST");
	        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
	        conn.setDoOutput(true);
	        conn.getOutputStream().write(postDataBytes);
	        
	        int statusCode = conn.getResponseCode(); 
	        
	        if ( statusCode >= 200 && statusCode < 400 ) {
	        
		    	BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
	
				String line;
				while ((line = br.readLine()) != null) {
					json.append(line);
				}
				
				conn.disconnect();
				StravaTokenInfo tokenInfo = null;
				if ( json != null ) {
				
					// json to Token info :
					System.out.println(json);
					tokenInfo =  objectMapper.readValue(json.toString(), StravaTokenInfo.class);
					//token = tokenInfo.getAccess_token();
					
					System.out.println("Token: " + tokenInfo);
				}
				
				return tokenInfo;
	        }
	        else {
	        	// pbm 
	        	BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
	        	
	        	String responseBody = br.lines().collect(Collectors.joining());
				System.out.println("Error during auth body http [" + statusCode + "]:" + responseBody);
	        	return null;
	        }

		} catch (IOException e) {

			e.printStackTrace();
			return null;
		}
	}

	
    public String getCurrentAthlete() throws ServiceException {
        String url = "https://www.strava.com/api/v3/athlete";
        String result= doGet(url);
        return result;	
    }
    
    public String getAthleteKoms() throws ServiceException {
    	String url = "https://www.strava.com/api/v3/athletes/6102935/koms?page=1&per_page=5";
        return doGet(url);
    }
    
    
    private StravaActivity[] getActivities(String url) throws JsonParseException, JsonMappingException, IOException, ServiceException {
       
        String json = doGet(url);
        StravaActivity[] activities = objectMapper.readValue(json, StravaActivity[].class);
        for ( StravaActivity activity : activities ) {
        	LocalDateTime startDate = Utils.stringInstantToLocalDateTime(activity.getStart_date_local());
        	int[] elapsed = Utils.getSecondsToHMS(activity.getElapsed_time());
        }
        
        return activities;
 
    }
    
    public StravaActivity[] searchActivities(LocalDate startDate, LocalDate endDate, int page, int countPerPage) throws JsonParseException, JsonMappingException, IOException, ServiceException {
    	String url = getSearchActivitiesUrl(startDate, endDate, page, countPerPage);
    	return getActivities(url);
    }
    
    public void getGpxFile(String activityId) throws JsonParseException, JsonMappingException, IOException, ServiceException {
    	 String url = "https://www.strava.com/api/v3/activities/"+activityId+"/streams/"+"time,latlng" + "?resolution=high";
    	 System.out.println(url);
    	 
    	 String json = doGet(url);
    	 
    	 Stream[] activities = objectMapper.readValue(json, Stream[].class);
   	 
    	
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
  

	public void setCode(String code) {
		this.code = code;
	}
	
	private void readSavedInfos() {
		// look
		final String dir = System.getProperty("user.dir");
		
		// strava code
		Path pathCode = Paths.get(dir, ".stravacode");
		
		if (Files.exists(pathCode)) {

			try {
				List<String> lines = Files.readAllLines(pathCode);
				code = lines.get(0);
				
				System.out.println("Code found " + code);
			} 
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// strava token
		Path pathToken = Paths.get(dir, ".stravatoken");
		if (Files.exists(pathToken)) {
			try {
				List<String> lines = Files.readAllLines(pathToken);
				String accessToken = lines.get(0);
				String refreshToken = lines.get(1);
				String expiration = lines.get(2);
				
				System.out.println("Token " + accessToken  + "|" + refreshToken + "|" + expiration);
				tokenInfo = new StravaTokenInfo();
				tokenInfo.setAccess_token(accessToken);
				tokenInfo.setRefresh_token(refreshToken);
				tokenInfo.setExpires_at(Long.parseLong(expiration));
			} 
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
		
	}
	
	public void writeCurrentCode() {
		final String dir = System.getProperty("user.dir");
		Path path = Paths.get(dir, ".stravacode");
		try {
			if (Files.exists(path)) {
			    Files.delete(path);
			}
			Files.write(path, code.getBytes());
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeCurrentToken(StravaTokenInfo tokenInfo) {
		final String dir = System.getProperty("user.dir");
		Path path = Paths.get(dir, ".stravatoken");
		try {
			if (Files.exists(path)) {
			    Files.delete(path);
			}
			
			Iterable<String> iterable = Arrays.asList(tokenInfo.getAccess_token(), tokenInfo.getRefresh_token(), "" + tokenInfo.getExpires_at());
		    Files.write(path, iterable);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	public boolean isLicenceCodeInit() {
		return code != null;
	}


	public StravaTokenInfo getTokenInfo() {
		return tokenInfo;
	}


	public void setTokenInfo(StravaTokenInfo tokenInfo) {
		this.tokenInfo = tokenInfo;
	}
	


}
