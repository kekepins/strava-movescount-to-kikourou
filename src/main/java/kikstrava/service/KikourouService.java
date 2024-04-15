package kikstrava.service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import kikstrava.model.Config;
import kikstrava.model.KikourouActivity;
import kikstrava.model.KikourouActivityImpl;
import kikstrava.model.Utils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.*;

public class KikourouService {

	static private SSLSocketFactory socketFactory() {
		TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}

			@Override
			public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
			}

			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		}};

		try {
			SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
			SSLSocketFactory result = sslContext.getSocketFactory();

			return result;
		} catch (Exception e) {
			throw new RuntimeException("Failed to create a SSL socket factory", e);
		}
	}

	private final static String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36";
	
	private Map<String, HttpCookie> mapCookies = new HashMap<>();
	
	private final String user;
	private final String password;
	private boolean isConnect = false;
	private String kikoureurId;
	
	private final static String FROM_LAST_YEAR = "https://www.kikourou.net/entrainement/navigation.php?nav1an=1&kikoureur=";
	
	//navannee=2016&navmois=12
	
	public KikourouService(String user, String password) {
		this.user = user;
		this.password = password;
	}
	
	private void connect() throws Exception {

		// Get sid 
		String sid = getSid();

		System.out.println("Connect kikourou with sid [" + sid + "]");

		if ( sid == null ) {
			throw new Exception("Impossible de récupérer le sid");
		}
		
		// Init sessions cookies
		String url =  "https://www.kikourou.net/forum/ucp.php?mode=login";

		System.out.println("User " + user + " password " + password );
		// url encode
		String postParams = "username=" + URLEncoder.encode(user, "UTF-8") + "&password=" + URLEncoder.encode(password, "UTF-8") + "&login=Connexion&sid=" + sid;

		System.out.println("Post params " + postParams);

		String result = sendPost(url, postParams);
		//System.out.println("Connect return");
		//System.out.println(result);

		isConnect = true;
	}
	
	private  void findKiroureurId() throws Exception {
		String html = sendGet("https://www.kikourou.net");
		Document document = Jsoup.parse(html);
		Elements liElts = document.select("ul#nav > li");

		Element espacePerso = liElts.get(4);
		Elements espacePersoUl =espacePerso.select("ul > li");

		Element a = espacePersoUl.get(2).child(0);
		String relHref = a.attr("href"); // == "/"
		int index =  relHref.indexOf("kikoureur=");
		this.kikoureurId = relHref.substring(index + 10);

		System.out.println("Kikoureur id found [" + kikoureurId + "]");
	}
	
	/*public void addActivities(KikourouActivity[] activities) throws Exception {
		
		if ( activities != null && activities.length > 0 ) {
			
			for (KikourouActivity stravaActivity : activities ) {
				addActivity(stravaActivity);
			}
		}
		// Connect to kik
	}*/
	
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

		System.out.println("Search activities " + start + " " + end);
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

			//System.println("html " + html);

			Document document = Jsoup.parse(html);

			Element table = document.getElementsByClass("calendrier").first();

			if ( table != null ) {

				Elements rows = table.select("tr");

				for (int i = 1; i < rows.size()-6; i++) { //first row is the col names so skip it.
					Element row = rows.get(i);
					Elements cols = row.select("td");
					String strDate = cols.get(0).text();
					LocalDate ldt = parseDate(strDate);

					//System.out.println(cols.get(0).text());
					String desc = cols.get(1).text();
					int parse = parseElapse(cols.get(4).text());
					float distance = Float.parseFloat(cols.get(5).text());
					//System.out.println("dist " + distance); // distance

					KikourouActivityImpl kikActivity  = new KikourouActivityImpl(ldt, desc, distance, parse);
					result.put(ldt, kikActivity);

				}
			}
		}
		catch( Throwable t ) {
			t.printStackTrace();
		}


		return result;
	}

	

	private String sendPost(String url, String postParams) throws Exception {

		SSLContext ctx = SSLContext.getInstance("TLS");
		ctx.init(new KeyManager[0], new TrustManager[]{new TrustAnyTrustManager()}, new java.security.SecureRandom());
		//SSLContext.setDefault(ctx);

		//System.out.println("Post request to " + url);
		//System.out.println("Post parameters : " + postParams);

		HttpsURLConnection.setFollowRedirects(true);

		URL obj = new URL(url);
		HttpsURLConnection conn = (HttpsURLConnection) obj.openConnection();

		HostnameVerifier allHostsValid = new HostnameVerifier() {

			public boolean verify(String hostname, SSLSession session) {

				//System.out.println("verify");
				return true;
			}
		};
		conn.setHostnameVerifier(allHostsValid);
		conn.setSSLSocketFactory(ctx.getSocketFactory());
		conn.setInstanceFollowRedirects(false);
		conn.setFollowRedirects(true);

		// Acts like a browser
		conn.setUseCaches(false);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("User-Agent", USER_AGENT);
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

		//System.out.println("Post Response code " + responseCode );
		
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

		// get http status code


		//System.out.println(response.toString());

		return response.toString();

	}
	
	private String sendGet(String url) throws Exception {

		System.out.println("Send GET request to " + url);

		SSLContext ctx = SSLContext.getInstance("TLS");
		ctx.init(new KeyManager[0], new TrustManager[]{new TrustAnyTrustManager()}, new java.security.SecureRandom());
		SSLContext.setDefault(ctx);
		HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
			@Override
			public boolean verify(String arg0, SSLSession arg1) {
				return true;
			}
		});

		URL obj = new URL(url);

		HttpURLConnection conn = (HttpURLConnection) obj.openConnection();

		// Acts like a browser
		conn.setFollowRedirects(true);
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

		setCookies(conn.getHeaderFields().get("Set-Cookie"));

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

		//System.out.println("==>Cookie string " + cookieString);
		
		return cookieString;
	}


	private boolean isCookie() {
		return !mapCookies.isEmpty();
	}


	public String getSid() throws Exception {

		String getRet = sendGet("https://www.kikourou.net/forum/ucp.php?mode=login");
		//System.out.println("Get ret " + getRet);

		String url = "https://www.kikourou.net/forum/ucp.php?mode=login";
		Connection.Response response =
                 Jsoup.connect(url)
				 //.proxy(Config.getConfig().getProxyHost(), Integer.parseInt(Config.getConfig().getProxyPort()))
				 .sslSocketFactory(socketFactory())
                 .userAgent("Mozilla/5.0")
                 .method(Connection.Method.GET)
                 .followRedirects(true)
                 .execute();

		// dump response
		//System.out.println(response.body());

		Document document = response.parse();
		Elements elements = document.select("input[type=hidden]");
		for ( Element elt : elements ) {
			if ( "sid".equals(elt.attr("name"))) {
				// return elt.attr("value");
				//System.out.println("sid found " + elt.attr("value"));

				return elt.attr("value");
			}
		}

		return null;
	}
	


	private void addSeance(int annee, int mois, int jour, int heure, int minute, int seconde, double deniv, double distance, int sport, String name, String descriptionPublique, String descriptionPrivee) throws Exception {
		
		// sport 24 : trail
		// 
		String url = "https://www.kikourou.net/entrainement/ajout.php";

		String postParams =
			encode("jour", "" + jour ) +
			encode("mois", "" + mois ) +
			encode("annee", "" + annee ) +
			encode("type", "1" ) +
			encode("nom", "" + name ) +
			encode("difficulte", "4" ) +
			encode("lieu", "" ) +
			encode("intensite", "0" ) +
			encode("sport", "" + sport ) +
			encode("phase", "0" ) +
			encode("distance", "" + distance ) +
			encode("denivele", "" + deniv ) +
		  	encode("fcmoy", "" ) +
			encode("fcmax", "" ) +
			encode("heuredepart", "" ) +
			encode("mindepart", "" ) +
			encode("heure", "" + heure ) +
			encode("min", "" + minute ) +
			encode("sec", "" + seconde ) +
			encode("descriptionpublique", descriptionPublique) +
			encode("description", descriptionPrivee) +
			encode("submit", "Enregistrer") +
			encode("details", "1") +
			encode("vmax", "" ) +
			encode("vmoy", "" ) +
			encode("cadmoy", "" ) +
			encode("deniveleneg", "" ) +
			encode("altimin", "" ) +
			encode("altimoy", "" ) +
			encode("altimax", "" ) +
			encode("nbequipiers", "" ) +
			encode("kcal", "" ) +
			encode("typeterrain1", "1") +
			encode("pct1", "100") +
			encode("typeterrain2", "1") +
			encode("pct2", "100") +
			encode("typeterrain3", "1") +
			encode("pct3", "100") +
			encode("meteo", "1") +
			encode("tmin", "" ) +
			encode("tmax", "" ) +
			encode("forme", "1") +
			encode("etatarrivee", "1") +
			encode("poidsavant", "" ) +
			encode("poidsapres", "" ) +
			encode("txtblessure", "" ) +
			encode("zone1", "122") +
			encode("zone2", "150") +
			encode("zone3", "157") +
			encode("zone4", "166") +
			encode("zone5", "173") +
			encode("zone5sup", "178") +
			encode("dureezone0", "" ) +
			encode("dureezone1", "" ) +
			encode("dureezone2", "" ) +
			encode("dureezone3", "" ) +
			encode("dureezone4", "" ) +
			encode("dureezone5", "" ) +
			encode("dureezone5sup", "" ) +
			encode("etirements", "0") +
			encode("sommeil", "1") +
			encode("dureesommeil", "0") +
			encode("FCmaxj", "180") +
			encode("FCR", "45") +
			encode("idevenement", "" ) +
			encode("idmodif", "0") +
			encode("idparcours", "" ) +
			encode("etape", "3") ;

		sendPost(url, postParams);

		
	}
	
	 public void setCookies(List<String> cookies) {
		 
		 if (cookies != null ) {
			 for ( String cookie: cookies ) {
				 //System.out.println("Parsing cookies " + cookie);
				 List<HttpCookie> pcookies = HttpCookie.parse(cookie);
				 for ( HttpCookie httpCookie : pcookies) {
					 //System.out.println("Put Cookie " + httpCookie.toString());
					// httpCookie.setDomain(".kikourou.net");
					 mapCookies.put(httpCookie.getName(), httpCookie);
				 }
			 }
		 }

	 }
	 
	 private String encode(String key, String value) throws UnsupportedEncodingException {
		//return key + "=" + URLEncoder.encode(value, "ISO-8859-1") + "&";
		 return key + "=" + URLEncoder.encode(value, "UTF-8") + "&";
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
		List<String> formatStrings = Arrays.asList("dd MM yy", "dd MMM yy");
		List<Locale> locales = Arrays.asList(Locale.FRANCE, Locale.ENGLISH);

		for ( String formatString : formatStrings ) {
			for ( Locale locale : locales ) {
				try {
					DateTimeFormatter dtf = DateTimeFormatter.ofPattern(formatString, locale);
					LocalDate dt = LocalDate.parse(date, dtf);
					//System.out.println("Date parsed" + dt);
					return dt;
				}
				catch( Throwable t ) {
					//System.out.println("Error " + t.getMessage());
					//System.out.println("Go next");
				}
			}
		}

		//System.out.println("Date not parsed " + date);
		return null;



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

