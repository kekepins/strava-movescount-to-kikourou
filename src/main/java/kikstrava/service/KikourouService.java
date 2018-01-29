package kikstrava.service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import kikstrava.model.Config;
import kikstrava.model.StravaActivity;
import kikstrava.model.Utils;

public class KikourouService {
	private final static String USER_AGENT = "Mozilla/5.0";
	
	private Map<String, HttpCookie> mapCookies = new HashMap<>();
	
	private final String user;
	private final String password;
	private boolean isConnect = false;
	
	public KikourouService(String user, String password) {
		this.user = user;
		this.password = password;
	}
	
	private void connect() throws Exception {
		
		// Get sid 
		String sid = getSid();
		
		// Init sessions cookies
		String url = "http://www.kikourou.net/forum/ucp.php?mode=login";
		String postParams = "username="+ user+ "&password=" + password + "&sid=" + sid + "&login=connection";
		sendPost(url, postParams);
		
		isConnect = true;
		
		//
	}
	
	public void addStravaActivities(StravaActivity[] activities) throws Exception {
		
		if ( activities != null && activities.length > 0 ) {
			
			// connect to kik is necessary
			/*if ( !isConnect ) {
				connect();
			}*/
			
			for (StravaActivity stravaActivity : activities ) {
				/*
				LocalDateTime startDate = Utils.stringToLocalDateTime(stravaActivity.getStart_date_local());
				int[] elapsed = Utils.getSecondsToHMS(stravaActivity.getElapsed_time());
				
				addSeance(
					startDate.getYear(), 
					startDate.getMonthValue(), 
					startDate.getDayOfMonth(),
					elapsed[0], 
					elapsed[1],
					elapsed[2], 
					stravaActivity.getTotal_elevation_gain() * Config.getConfig().getElevationCorrection(), 
					stravaActivity.getDistance() / 1000., // kik is in km
					stravaActivity.getType().getKikCode(),
					stravaActivity.getName(),
					"Auto included by StravatoKik " + new Date());
				*/
				
				addStravaActivity(stravaActivity);
			}
			
		}
		// Connect to kik
	}
	
	public void addStravaActivity(StravaActivity stravaActivity) throws Exception {
		// connect to kik is necessary
		if ( !isConnect ) {
			connect();
		}
		
		LocalDateTime startDate = Utils.stringToLocalDateTime(stravaActivity.getStart_date_local());
		int[] elapsed = Utils.getSecondsToHMS(stravaActivity.getElapsed_time());
		
		addSeance(
			startDate.getYear(), 
			startDate.getMonthValue(), 
			startDate.getDayOfMonth(),
			elapsed[0], 
			elapsed[1],
			elapsed[2], 
			stravaActivity.getTotal_elevation_gain() * Config.getConfig().getElevationCorrection(), 
			stravaActivity.getDistance() / 1000., // kik is in km
			stravaActivity.getType().getKikCode(),
			stravaActivity.getName(),
			"Auto included by StravatoKik " + new Date());
		
	}

	

	private void sendPost(String url, String postParams) throws Exception {

		URL obj = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) obj.openConnection();

		// Acts like a browser
		conn.setUseCaches(false);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Host", "www.kikourou.net");
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		conn.setRequestProperty("Accept-Language", "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3");
		conn.setRequestProperty("Upgrade-Insecure-Requests", "1");
		conn.setRequestProperty("Connection", "keep-alive");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("Content-Length", Integer.toString(postParams.length()));
		if ( isCookie()) {
			conn.setRequestProperty("Cookie", getCookieString());
		}
		System.out.println("content-length:" + Integer.toString(postParams.length()));

		conn.setDoOutput(true);
		conn.setDoInput(true);

		// Send post request
		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
		wr.writeBytes(postParams);
		wr.flush();
		wr.close();

		int responseCode = conn.getResponseCode();
		
		setCookies(conn.getHeaderFields().get("Set-Cookie"));
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + postParams);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		System.out.println(response.toString());

	}

	
	private String getCookieString() {
		String cookieString = "";
		for ( HttpCookie httpCookie : mapCookies.values() ) {
			cookieString += httpCookie.toString() + ";"; 
		}
		
		return cookieString;
	}


	private boolean isCookie() {
		return !mapCookies.isEmpty();
	}


	public String getSid() throws Exception {
		String url = "http://www.kikourou.net/forum/ucp.php?mode=login";
		Response response = 
                 Jsoup.connect(url)
                 .userAgent("Mozilla/5.0")
                 .method(Method.GET)
                 .followRedirects(true)
                 .execute();
		
		Document document = response.parse();
		Elements elements = document.select("input[type=hidden]");
		Element elt = elements.first();
		return elt.attr("value");
	}
	


	private void addSeance(int annee, int mois, int jour, int heure, int minute, int seconde, double deniv, double distance, int sport, String name, String descriptionPublique) throws Exception {
		
		// sport 24 : trail
		// 
		String url = "http://www.kikourou.net/entrainement/ajout.php";
		
		//String postParams = "annee="+ annee+ "&mois=" + mois+ "&jour=" + jour  + "&min=" + minute + "&sec=" + seconde + "&denivele=" + deniv + "&distance=" + distance + "&sport=" + sport + "&nom=" + name;
		String postParams = 
				encode("annee", "" + annee ) +
				encode("mois", "" + mois ) +
				encode("jour", "" + jour ) +
				encode("heure", "" + heure ) +
				encode("min", "" + minute ) +
				encode("sec", "" + seconde ) +
				encode("denivele", "" + deniv ) +
				encode("distance", "" + distance ) +
				encode("sport", "" + sport ) +
				encode("type", "1" ) +
				encode("difficulte", "4" ) +
				encode("nom", "" + name ) + 
				encode("submit", "Enregistrer") +
				encode("details", "1") + 
				encode("difficulte", "4") +
				encode("dureesommeil", "0") +
				encode("etape", "3") +
				encode("etatarrivee", "1") +
				encode("etirements", "0") +
				encode("FCmaxj", "180") +
				encode("FCR", "45") +
				encode("forme", "1") +
				encode("meteo", "1") +
				encode("pct1", "100") +
				encode("phase", "0") +
				encode("sommeil", "1") +
				encode("typeterrain1", "1") +
				encode("typeterrain2", "1") +
				encode("typeterrain3", "1") +
				encode("zone1", "122") +
				encode("zone2", "150") +
				encode("zone3", "157") +
				encode("zone4", "166") +
				encode("zone5", "173") +
				encode("zone5sup", "178") +
				encode("descriptionpublique", descriptionPublique);
				
		
		sendPost(url, postParams);
		//annee=2018&mois=1&jour=24&heure=1&min=3&sec=12&denivele=120&distance=1.4&sport=24&type=1&difficulte=4&nom=test+creation+auto&submit=Enregistrer&
		/*
		 altimax	
		altimin	
		altimoy	
		annee	2018
		cadmoy	
		denivele	150
		deniveleneg	
		description	
		descriptionpublique	
		details	1
		difficulte	4
		distance	1.5
		dureesommeil	0
		dureezone0	
		dureezone1	
		dureezone2	
		dureezone3	
		dureezone4	
		dureezone5	
		dureezone5sup	
		etape	3
		etatarrivee	1
		etirements	0
		fcmax	
		FCmaxj	180
		fcmoy	
		FCR	45
		forme	1
		heure	1
		heuredepart	
		idevenement	
		idparcours	
		intensite	0
		jour	24
		kcal	
		lieu	
		meteo	1
		min	12
		mindepart	
		mois	1
		nbequipiers	
		nom	test+creation+seance+auto
		pct1	100
		pct2	
		pct3	
		phase	0
		poidsapres	
		poidsavant	
		sec	12
		sommeil	1
		sport	24
		submit	Enregistrer
		tmax	
		tmin	
		txtblessure	
		type	1
		typeterrain1	1
		typeterrain2	1
		typeterrain3	1
		vmax	
		vmoy	
		zone1	122
		zone2	150
		zone3	157
		zone4	166
		zone5	173
		zone5sup	178
		 */
		
	}
	
	 public void setCookies(List<String> cookies) {
		 
		 if (cookies != null ) {
			 for ( String cookie: cookies ) {
				 List<HttpCookie> pcookies = HttpCookie.parse(cookie);
				 for ( HttpCookie httpCookie : pcookies) {
					 System.out.println("Put Cookie " + httpCookie.toString());
					 mapCookies.put(httpCookie.getName(), httpCookie);
				 }
			 }
		 }

	 }
	 
	 private String encode(String key, String value) throws UnsupportedEncodingException {
		 return key + "=" + URLEncoder.encode(value, "ISO-8859-1") + "&";
	 }

}
