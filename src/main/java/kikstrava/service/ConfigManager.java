package kikstrava.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import kikstrava.model.Config;

public class ConfigManager {
	
	// Mandatory
	private final static String KIK_USER_KEY = "kik.user";
	private final static String KIK_PSW_KEY = "kik.password";
	private final static String STRAVA_TOKEN = "strava.token";
	private final static String STRAVA_CLIENTID = "strava.clientid";
	private final static String STRAVA_SECRET = "strava.secret";
	private final static String MOVESCOUNT_EMAIL = "movescount.email";
	private final static String MOVESCOUNT_USERKEY = "movescount.userkey";
	
	// Option
	private final static String USE_PROXY = "proxy";
	private final static String PROXY_URL = "proxy.url";
	private final static String PROXY_PORT = "proxy.port";

	public static void init() throws Exception {
		Properties properties = new Properties();
		InputStream input = null;

		try {
			Config config = Config.getConfig();

			input = ConfigManager.class.getClassLoader().getResourceAsStream("stravakik.conf");
			
			if ( input == null ) {
				throw new Exception("Impossible de trouver le fichier stravakik.conf");
			}
			// load a properties file
			//prop.load(input);
			InputStreamReader inputStreamReader = new InputStreamReader(input, "UTF-8");
			properties.load(inputStreamReader);

			// Read properties :
			//--------------
			// Kikourou
			//-------------
			String kikUser = properties.getProperty(KIK_USER_KEY);
			if ( kikUser != null ) {
				System.out.println("kikUser = " + kikUser);
				config.setKikUser(kikUser.trim());
			}
			
			String kikPsw = properties.getProperty(KIK_PSW_KEY);
			if ( kikPsw != null ) {
				System.out.println("kikPsw = " + kikPsw);
				config.setKikPassword(kikPsw.trim());
			}
			
			//--------------
			// Strava
			//-------------
			String stravaSecret = properties.getProperty(STRAVA_SECRET);
			if ( stravaSecret != null ) {
				System.out.println("Strava secret = " + stravaSecret);
				config.setStravaSecret(stravaSecret.trim());
			}
				
			String stravaClientId = properties.getProperty(STRAVA_CLIENTID);
			if ( stravaClientId != null ) {
				System.out.println("Strava client id = " + stravaClientId);
				config.setStravaClientId(stravaClientId.trim());
			}
			

			if ( ( kikUser == null ) || "".equals(kikUser) || 
					( kikPsw == null ) || "".equals(kikPsw) ) {
				throw new Exception("Info kikourou (user ou psw) non trouvé dans la conf, fichier conf/stravakik.conf");
			}
			
			if ( !config.isStravaOK() ) {
				throw new Exception("Il faut configurer un accès à Strava ou un accés à movescount, fichier conf/stravakik.conf");
			}
			
			String useProxy = properties.getProperty(USE_PROXY, "false");
			System.out.println("useProxy = " + useProxy);
			boolean isUseProxy = "true".equals(useProxy);   // Boolean.getBoolean(useProxy);
			config.setProxy(isUseProxy);
			if (isUseProxy) {
				String proxyUrl = properties.getProperty(PROXY_URL);
				System.out.println("Proxy url = " + proxyUrl);
				config.setProxyHost(proxyUrl);
				String proxyPort = properties.getProperty(PROXY_PORT);
				System.out.println("Proxy port = " + proxyPort);
				config.setProxyPort(proxyPort);
			}
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
