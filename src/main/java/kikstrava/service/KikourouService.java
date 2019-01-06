package kikstrava.service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import kikstrava.model.Config;
import kikstrava.model.KikourouActivity;
import kikstrava.model.KikourouActivityImpl;
import kikstrava.model.Utils;

public class KikourouService {
	private final static String USER_AGENT = "Mozilla/5.0";
	
	private Map<String, HttpCookie> mapCookies = new HashMap<>();
	
	private final String user;
	private final String password;
	private boolean isConnect = false;
	private String kikoureurId;
	
	private final static String FROM_LAST_YEAR = "http://www.kikourou.net/entrainement/navigation.php?nav1an=1&kikoureur=";
	
	//navannee=2016&navmois=12
	
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
	
	private  void findKiroureurId() throws Exception {
		String html = sendGet("http://www.kikourou.net");
		Document document = Jsoup.parse(html);
		Elements liElts = document.select("ul#nav > li");
		Element espacePerso = liElts.get(4);
		Elements espacePersoUl =espacePerso.select("ul > li");
		Element a = espacePersoUl.get(2).child(0);
		
		String relHref = a.attr("href"); // == "/"

		int index =  relHref.indexOf("kikoureur=");
		
		this.kikoureurId = relHref.substring(index + 10);
	}
	
	public void addActivities(KikourouActivity[] activities) throws Exception {
		
		if ( activities != null && activities.length > 0 ) {
			
			for (KikourouActivity stravaActivity : activities ) {
				addActivity(stravaActivity);
			}
		}
		// Connect to kik
	}
	
	public void addActivity(KikourouActivity activity) throws Exception {
		// connect to kik is necessary
		if ( !isConnect ) {
			connect();
		}
		
		LocalDateTime startDate = activity.getStartDateLocal();
		
		int[] elapsed = Utils.getSecondsToHMS(activity.getElapsedTime());
		
		addSeance(
			startDate.getYear(), 
			startDate.getMonthValue(), 
			startDate.getDayOfMonth(),
			elapsed[0], 
			elapsed[1],
			elapsed[2], 
			activity.getDPlus(), 
			activity.getDistance() / 1000., // kik is in km
			activity.getCode(),
			activity.getName(),
			"Importé de " + activity.getSource() + " le " + new Date() + " par kikstrava",
			activity.getUrl());
		
	}
	
	public Map<LocalDate, KikourouActivity> searchActivities(LocalDate start, LocalDate end ) throws Exception {
		
		if ( !isConnect ) {
			connect();
		}
		
		if ( kikoureurId == null ) {
			findKiroureurId();
		}
		
		Map<LocalDate, KikourouActivity> result = new HashMap<>();
		
		try {
			String url = FROM_LAST_YEAR  + kikoureurId;
			String html = sendGet(url);
			
			Document document = Jsoup.parse(html);
			
			Element table = document.getElementsByClass("calendrier").first();
			
			Elements rows = table.select("tr");
			
			for (int i = 1; i < rows.size()-6; i++) { //first row is the col names so skip it.
			    Element row = rows.get(i);
			    Elements cols = row.select("td");
			    String strDate = cols.get(0).text();
			    LocalDate ldt = parseDate(strDate);
			    
			    //System.out.println(cols.get(0).text());
			    String desc = cols.get(1).text();
			    //System.out.println(desc); // desc
			    //System.out.println(cols.get(4).text()); // elapse
			    int parse = parseElapse(cols.get(4).text());
			    float distance = Float.parseFloat(cols.get(5).text());
			    //System.out.println("dist " + distance); // distance
			    
			    KikourouActivityImpl kikActivity  = new KikourouActivityImpl(ldt, desc, distance, parse);
			    result.put(ldt, kikActivity);
			    
			}
		}
		catch( Throwable t ) {
			t.printStackTrace();
		}


		return result;
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
		//System.out.println("content-length:" + Integer.toString(postParams.length()));

		conn.setDoOutput(true);
		conn.setDoInput(true);

		// Send post request
		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
		wr.writeBytes(postParams);
		wr.flush();
		wr.close();

		int responseCode = conn.getResponseCode();
		
		setCookies(conn.getHeaderFields().get("Set-Cookie"));
		/*System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + postParams);
		System.out.println("Response Code : " + responseCode);*/

		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		System.out.println(response.toString());

	}
	
	private String sendGet(String url) throws Exception {

		URL obj = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) obj.openConnection();

		// Acts like a browser
		conn.setUseCaches(false);
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Host", "www.kikourou.net");
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		conn.setRequestProperty("Accept-Language", "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3");
		conn.setRequestProperty("Upgrade-Insecure-Requests", "1");
		conn.setRequestProperty("Connection", "keep-alive");
		conn.setDoOutput(true);
		conn.setDoInput(true);
		if ( isCookie()) {
			conn.setRequestProperty("Cookie", getCookieString());
		}

		int responseCode = conn.getResponseCode();
		//System.out.println("\nSending 'GET' request to URL : " + url);
		//System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		//System.out.println(response.toString());
		
		return response.toString();

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
	


	private void addSeance(int annee, int mois, int jour, int heure, int minute, int seconde, double deniv, double distance, int sport, String name, String descriptionPublique, String descriptionPrivee) throws Exception {
		
		// sport 24 : trail
		// 
		String url = "http://www.kikourou.net/entrainement/ajout.php";
		
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
				encode("descriptionpublique", descriptionPublique) +
				encode("description", descriptionPrivee);
				
		
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
					 //System.out.println("Put Cookie " + httpCookie.toString());
					 mapCookies.put(httpCookie.getName(), httpCookie);
				 }
			 }
		 }

	 }
	 
	 private String encode(String key, String value) throws UnsupportedEncodingException {
		 return key + "=" + URLEncoder.encode(value, "ISO-8859-1") + "&";
	 }
	 
	 public static void main(String[] args) throws Exception {
		ConfigManager.init();
		
		// Set proxy
		if ( Config.getConfig().isProxy() ) {
			System.setProperty("http.proxyHost", Config.getConfig().getProxyHost());
		    System.setProperty("http.proxyPort",  Config.getConfig().getProxyPort());
			System.setProperty("https.proxyHost", Config.getConfig().getProxyHost());
		    System.setProperty("https.proxyPort", Config.getConfig().getProxyPort());
		}
		
		KikourouService kikService = new KikourouService(Config.getConfig().getKikUser(), Config.getConfig().getKikPassword());
		kikService.searchActivities(null, null);
		//kikService.connect();
		//kikService.getKiroureurId();
		 
	 }
	 
	 private LocalDate parseDate(String date) {
		 
		 date = date.replace("jan", "01");
		 date = date.replace("fév", "02");
		 date = date.replace("mar", "03");
		 date = date.replace("avr", "04");
		 date = date.replace("mai", "05");
		 date = date.replace("jun", "06");
		 date = date.replace("jui", "07");
		 date = date.replace("aoû", "08");
		 date = date.replace("sep", "09");
		 date = date.replace("oct", "10");
		 date = date.replace("nov", "11");
		 date = date.replace("déc", "12");
		 DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MM yy").withLocale(Locale.FRANCE);
		 LocalDate dt = LocalDate.parse(date, dtf);

		 return dt;
	 }
	 
	 // 01h03'29''
	 public int parseElapse(String elapse) {
		 
		 if ( elapse == null || "".equals(elapse )) {
			 return 0;
		 }
		 elapse = elapse.replace("''", "");
		 elapse = elapse.replace("h", ":");
		 elapse = elapse.replace("'", ":");
		 String[] splitted = elapse.split(":");
		 
		 int total = 0;
		 int mult = 1;
		 
		 for ( int idx = splitted.length; idx > 0; idx--) {
			 total += Integer.parseInt(splitted[idx-1]) * mult;
			 mult *=60;
		 }
		 
		 return total; 
		 
	 }

}
