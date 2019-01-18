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
import java.util.LinkedHashMap;
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
	private String token;

	public StravaV3Service(String clientId, String clientSecret) {
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		// read previous code in init ...
		readPreviousCode();
		
	}

	
	/**
	 * Execute a get and return the content
	 * 
	 * @param getUrl
	 * @return
	 * @throws ServiceException 
	 */
	private String doGet(String getUrl) throws ServiceException {
		
		// check token ...
		if ( code == null ) {
			throw new ServiceException("Il faut valider la licence en cliquant sur l'icone");
		}
		
		if ( token == null ) {
			// need a token
			getToken();
		}
		
		
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
	
	 public String getTokenUrl(String clientId,  String clientSecret) {
        return String.format(TOKEN_URL + "?client_id=%s&response_type=code&redirect_uri=%s&client_secret=%s", clientId, CALLBACK_URL, clientSecret);
	}
	

	 /**
	  * Get strava url providing authorization ... 
	  * 
	  */
	public String getAuthUrl() {
		return String.format(AUTH_URL + "?client_id=%s&response_type=code&redirect_uri=%s&client_secret=%s&approval_prompt=force", clientId, CALLBACK_URL, clientSecret);
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
				token = tokenInfo.getAccess_token();
			}
			
			return tokenInfo;

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
    	 //String url = "https://www.strava.com/api/v3/activities/"+activityId+"/streams/"+"latlng,distance,altitude,time" + "?resolution=high";
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
    
    
   	public static void main( String[] args) throws Exception {
    		
		/*ConfigManager.init();
		
		// Set proxy
		if ( Config.getConfig().isProxy() ) {
			System.setProperty("http.proxyHost", Config.getConfig().getProxyHost());
		    System.setProperty("http.proxyPort",  Config.getConfig().getProxyPort());
			System.setProperty("https.proxyHost", Config.getConfig().getProxyHost());
		    System.setProperty("https.proxyPort", Config.getConfig().getProxyPort());
		}
		
		StravaKeyLicenceServer licenseServer = new StravaKeyLicenceServer();
		licenseServer.start();
		
		StravaV3Service stravaService = new StravaV3Service("22837", "0bc902e46f8aabca2ed92f4869b2c6c3f9ac727c");
		stravaService.getToken();
		//StravaV3Service stravaService = new StravaV3Service(Config.getConfig().getStravaToken());
		//StravaActivity[] activities = stravaService.searchActivities(null, null, 1, 1);
		//int id = activities[0].getId();
		
		//stravaService.getGpxFile(""+id);
		
		Thread.sleep(300000);
		licenseServer.stop();*/
   	}


	public void setCode(String code) {
		this.code = code;
	}
	
	public void readPreviousCode() {
		// look
		final String dir = System.getProperty("user.dir");
		Path path = Paths.get(dir, ".stravacode");
		
		if (Files.exists(path)) {
			// read code
			try {
				String content = new String(Files.readAllBytes(path));
				code = content;
				System.out.println("Code found "  +code);
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
	
	public boolean isLicenceInit() {
		return code != null;
	}
	


}
