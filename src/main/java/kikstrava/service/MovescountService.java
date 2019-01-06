package kikstrava.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kikstrava.model.Config;
import kikstrava.model.MovescountActivity;

public class MovescountService {
	
	private static final String APP_KEY = "HpF9f1qV5qrDJ1hY1QK1diThyPsX10Mh4JvCw9xVQSglJNLdcwr3540zFyLzIC3e";

	private final static String MEMBERS_URL = "https://uiservices.movescount.com/members/private?";
	private static final String MOVESCOUNT_URL = "https://uiservices.movescount.com/";
	private static final String PRIVATE_URI = "moves/private";
	private static final String PRIVATE_PARAM = "?appkey=%s&userkey=%s&email=%s&startDate=%s&endDate=%s&maxcount=%d";
	
	private final String email;
	private final String userKey;
	private final ObjectMapper objectMapper;
	
	// https://uiservices.movescount.com/
	
	//  val request: HttpRequest = Http("
	//https://uiservices.movescount.com/moves/private?appkey=HpF9f1qV5qrDJ1hY1QK1diThyPsX10Mh4JvCw9xVQSglJNLdcwr3540zFyLzIC3e&userkey=604a51e9-9622-4905-af04-db99283a1d02&email=steffen%40ripke-home.de&startdate=2014-01-01")
	
	public MovescountService(String email, String userKey) {
		this.email	 = email;
		this.userKey = userKey;
		
		objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	}
	
	public void login() throws UnsupportedEncodingException {
		
		String url =  MEMBERS_URL;
		url += "appkey=" + APP_KEY;
		url += "&userkey=" + userKey;
		url += "&email=" + email;
		//System.out.println(url);
		
		doGet(url); 
		

	}
	
	public MovescountActivity[] searchActivities(LocalDate startDate, LocalDate endDate, int maxCount) throws JsonParseException, JsonMappingException, IOException {
		
		List<MovescountActivity> result = new ArrayList<>();
		boolean isFinish = false;
		LocalDate end = null;
		LocalDate start = null;
		int countIter = 0;
		
		// set start and end
		if ( endDate == null ) {
			end = LocalDate.now();
		}
		else {
			end = endDate;
		}
		
		start = end.minusDays(3);
		if ( startDate != null ) {
			if ( startDate.isAfter(start)) {
				start = startDate;
			}
		}
		
		while (!isFinish) {
			// Movescount can't return more than 5 training, iteration is needed to get more data 
			String url = String.format(MOVESCOUNT_URL + PRIVATE_URI + PRIVATE_PARAM , APP_KEY, userKey, email, localDateToString(start), localDateToString(end), 5 );
			String json = doGet(url);
			MovescountActivity[] activities = objectMapper.readValue(json, MovescountActivity[].class);
			
			if ( activities != null ) {
				for (MovescountActivity activity : activities ) {
					// Bad should use a queue
					result.add(0, activity);
				}
			}
			
			countIter ++;
			// New period
			end = start.minusDays(1);
			start = end.minusDays(3);
			
			if ( (countIter > 30 ) ||	//  max queries
				 (result.size() >= maxCount) ||// enough result
				 (startDate != null && end.isBefore(startDate))
				) {
				isFinish = true;
			}
			  
		}
		
		result.sort(new Comparator<MovescountActivity>() {
			@Override
			public int compare(MovescountActivity activity1, MovescountActivity activity2) {
				return activity2.getStartDateLocal().compareTo(activity1.getStartDateLocal());
			}}
		);
		
		if ( result.size() > maxCount ) {
			result = result.subList(0, maxCount );
		}
		
		return result.toArray(new MovescountActivity[result.size()]);
	}
		
	
	private String doGet(String getUrl) {
		StringBuilder sb = new StringBuilder();

		try {
			URL url = new URL(getUrl);
			System.out.println(url);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "*/*");
			conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
	
			if (conn.getResponseCode() != 200) {
				throw new RuntimeException(
						"Failed : HTTP error code : " + conn.getResponseCode() + " - " + conn.getResponseMessage());
			}
			else {
				//System.out.println("Code " + conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));

			String output;
			while ((output = br.readLine()) != null) {
				sb.append(output);
			}
			
			/*Map<String, List<String>> map = conn.getHeaderFields();
			for (Map.Entry<String, List<String>> entry : map.entrySet()) {
				System.out.println("Key : " + entry.getKey() +
			                 " ,Value : " + entry.getValue());
			}
			
			System.out.println("--------------");
			System.out.println("Content");
			System.out.println("--------------");
			
			System.out.println(sb);*/

			conn.disconnect();

		} catch (IOException e) {

			e.printStackTrace();
			return null;
		}

		return sb.toString();

	}
	public static void main( String[] args) throws Exception {
		
		ConfigManager.init();
		
		// Set proxy
		if ( Config.getConfig().isProxy() ) {
			System.setProperty("http.proxyHost", Config.getConfig().getProxyHost());
		    System.setProperty("http.proxyPort",  Config.getConfig().getProxyPort());
			System.setProperty("https.proxyHost", Config.getConfig().getProxyHost());
		    System.setProperty("https.proxyPort", Config.getConfig().getProxyPort());
		}
		
		MovescountService movescountService = new MovescountService(Config.getConfig().getMovescountEmail(), Config.getConfig().getMovescountEmail());
		//movescountService.login();
		movescountService.searchActivities(null, null, 1);

	}
	
	private String localDateToString(LocalDate localDate) {
		
		if ( localDate == null ) {
			return "";
		}
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		return  localDate.format(formatter);
	}

}
