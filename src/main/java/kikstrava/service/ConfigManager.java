package kikstrava.service;

import java.io.IOException;
import java.io.InputStream;
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
		Properties prop = new Properties();
		InputStream input = null;

		try {
			Config config = Config.getConfig();

			input = ConfigManager.class.getClassLoader().getResourceAsStream("stravakik.conf");
			
			if ( input == null ) {
				throw new Exception("Impossible de trouver le fichier stravakik.conf");
			}
			// load a properties file
			prop.load(input);

			// Read properties :
			String kikUser = prop.getProperty(KIK_USER_KEY);
			config.setKikUser(kikUser);
			String kikPsw = prop.getProperty(KIK_PSW_KEY);
			config.setKikPassword(kikPsw);
			//String stravaToken = prop.getProperty(STRAVA_TOKEN);
			//config.setStravaToken(stravaToken);
			String stravaSecret = prop.getProperty(STRAVA_SECRET);
			config.setStravaSecret(stravaSecret);
			String stravaClientId = prop.getProperty(STRAVA_CLIENTID);
			config.setStravaClientId(stravaClientId);

			
			String movescountEmail = prop.getProperty(MOVESCOUNT_EMAIL);
			config.setMovescountEmail(movescountEmail);
			String movescountUserKey = prop.getProperty(MOVESCOUNT_USERKEY);
			config.setMovescountUserKey(movescountUserKey);

			
			if ( ( kikUser == null ) || "".equals(kikUser) || 
					( kikPsw == null ) || "".equals(kikPsw) ) {
				throw new Exception("Info kikourou (user ou psw) non trouvé dans la conf, fichier conf/stravakik.conf");
			}
			
			if ( !config.isMovescountOK() && !config.isStravaOK() ) {
				throw new Exception("Il faut configurer un accès à Strava ou un accès à movescount, fichier conf/stravakik.conf");
			}
			
			String useProxy = prop.getProperty(USE_PROXY, "false");
			boolean isUseProxy = "true".equals(useProxy);   // Boolean.getBoolean(useProxy);
			config.setProxy(isUseProxy);
			if (isUseProxy) {
				String proxyUrl = prop.getProperty(PROXY_URL);
				config.setProxyHost(proxyUrl);
				String proxyPort = prop.getProperty(PROXY_PORT);
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
