
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
		
		//System.setProperty("https.protocols", "TLSv1.2,TLSv1.3");
		//System.out.println("Set TLSv1.2");
		//System.setProperty("https.protocols", "TLSv1.2");
		
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
		LocalDateTime expirationDate = tokenInfo.getExpirationDate();

		if ( expirationDate.isBefore(LocalDateTime.now() )) {
		// check if token is expired or is about to expire


		//if ( LocalDateTime.now().minusHours(2).isAfter(tokenInfo.getExpirationDate()) )  {
			// should update
			tokenInfo = updateToken();
			
			if (tokenInfo == null) {
				throw new ServiceException("Pbm strava token refresh :-(");
			}
			writeCurrentToken(tokenInfo);

			
		}
		
	}

	
	private StravaTokenInfo updateToken() throws ServiceException {
		/*curl -X POST https://www.strava.com/api/v3/oauth/token \
			  -d client_id=ReplaceWithClientID \
			  -d client_secret=ReplaceWithClientSecret \
			  -d grant_type=refresh_token \
			  -d refresh_token=ReplaceWithRefreshToken*/

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
				
					tokenInfo =  objectMapper.readValue(json.toString(), StravaTokenInfo.class);
					//token = tokenInfo.getAccess_token();
					
				}
				
				return tokenInfo;
	        }
	        else {
	        	// pbm 
	        	BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
	        	
		br.close();
				throw new ServiceException("Strava token refresh failed with HTTP " + statusCode);
	        }

		} catch (IOException e) {
			throw new ServiceException("Strava token refresh failed: " + e.getMessage());
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
				throw new ServiceException("Strava activity request failed with HTTP " + conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));

			String output;
			while ((output = br.readLine()) != null) {
				sb.append(output);
			}

			conn.disconnect();

		} catch (IOException e) {
			throw new ServiceException("Strava activity request failed: " + e.getMessage());
		}

		return sb.toString();

	}


	 /**
	  * Get strava url providing authorization ... 
	  * 
	  */
	public String getAuthUrl() {
		return AUTH_URL + "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
				+ "&response_type=code&scope=activity:read&redirect_uri="
				+ URLEncoder.encode(CALLBACK_URL, StandardCharsets.UTF_8)
				+ "&approval_prompt=force";
	}
	 

	/**
	 * Get a valid token from strava !!!!
	 */
	public StravaTokenInfo getToken() {
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
				
					tokenInfo =  objectMapper.readValue(json.toString(), StravaTokenInfo.class);
					//token = tokenInfo.getAccess_token();
					
				}
				
				return tokenInfo;
	        }
	        else {
	        	// pbm 
	        	BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
	        	
		br.close();
				System.err.println("Strava authorization failed with HTTP " + statusCode);
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
		try {
			Path codeFile = stateFile(".stravacode");
			if (Files.exists(codeFile)) {
				List<String> lines = Files.readAllLines(codeFile, StandardCharsets.UTF_8);
				if (!lines.isEmpty()) {
					code = lines.get(0);
				}
			}

			Path tokenFile = stateFile(".stravatoken");
			if (Files.exists(tokenFile)) {
				List<String> lines = Files.readAllLines(tokenFile, StandardCharsets.UTF_8);
				if (lines.size() < 3) {
					throw new IOException("Saved Strava token file is incomplete");
				}
				tokenInfo = new StravaTokenInfo();
				tokenInfo.setAccess_token(lines.get(0));
				tokenInfo.setRefresh_token(lines.get(1));
				tokenInfo.setExpires_at(Long.parseLong(lines.get(2)));
			}
		} catch (IOException | NumberFormatException e) {
			throw new IllegalStateException("Unable to read saved Strava authorization", e);
		}
	}
	
	public void writeCurrentCode() {
		try {
			Files.writeString(stateFile(".stravacode"), code, StandardCharsets.UTF_8);
		} 
		catch (IOException e) {
			throw new IllegalStateException("Unable to save Strava authorization", e);
		}
	}
	
	public void writeCurrentToken(StravaTokenInfo tokenInfo) {
		try {
			Iterable<String> iterable = Arrays.asList(tokenInfo.getAccess_token(), tokenInfo.getRefresh_token(), "" + tokenInfo.getExpires_at());
		    Files.write(stateFile(".stravatoken"), iterable, StandardCharsets.UTF_8);
		} 
		catch (IOException e) {
			throw new IllegalStateException("Unable to save Strava token", e);
		}
	}

	private Path stateFile(String fileName) throws IOException {
		String localAppData = System.getenv("LOCALAPPDATA");
		Path stateDirectory = localAppData == null || localAppData.isBlank()
				? Paths.get(System.getProperty("user.home"), ".kikstrava")
				: Paths.get(localAppData, "KikStrava");
		Files.createDirectories(stateDirectory);

		Path stateFile = stateDirectory.resolve(fileName);
		Path legacyFile = Paths.get(System.getProperty("user.dir"), fileName);
		if (!Files.exists(stateFile) && Files.exists(legacyFile)) {
			Files.copy(legacyFile, stateFile);
		}
		return stateFile;
	}

	
	public boolean isLicenceCodeInit() {
		return code != null;
	}

	public boolean isAuthenticated() {
		return code != null && tokenInfo != null
				&& tokenInfo.getRefresh_token() != null
				&& !tokenInfo.getRefresh_token().isBlank();
	}


	public StravaTokenInfo getTokenInfo() {
		return tokenInfo;
	}


	public void setTokenInfo(StravaTokenInfo tokenInfo) {
		this.tokenInfo = tokenInfo;
	}
	


}
