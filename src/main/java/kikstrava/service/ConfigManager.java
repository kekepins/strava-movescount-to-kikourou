package kikstrava.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import kikstrava.model.Config;

public class ConfigManager {
	
	// Mandatory
	private final static String KIK_USER_KEY = "kik.user";
	private final static String KIK_PSW_KEY = "kik.password";
	private final static String STRAVA_TOKEN = "strava.token";
	
	// Option
	private final static String USE_PROXY = "proxy";
	private final static String PROXY_URL = "proxy.url";
	private final static String PROXY_PORT = "proxy.port";
	
	
	/*public static void init() throws ConfigurationException {
		List<FileLocationStrategy> subs = Arrays.asList(
				  new ProvidedURLLocationStrategy(),
				  new FileSystemLocationStrategy(),
				  new ClasspathLocationStrategy());
		FileLocationStrategy strategy = new CombinedLocationStrategy(subs);
		
		FileBasedConfigurationBuilder<PropertiesConfiguration> builder = new FileBasedConfigurationBuilder<PropertiesConfiguration>(
				PropertiesConfiguration.class).configure(
						new Parameters().fileBased().setLocationStrategy(strategy).setFileName("stravakik.conf"));
		
		PropertiesConfiguration propConfig = builder.getConfiguration();
		
		
		Config config = Config.getConfig();
		
		// Read properties :
		String kikUser = propConfig.getString(KIK_USER_KEY);
		config.setKikUser(kikUser);
		String kikPsw = propConfig.getString(KIK_PSW_KEY);
		config.setKikPassword(kikPsw);
		String stravaToken = propConfig.getString(STRAVA_TOKEN);
		config.setStravaToken(stravaToken);
		
		boolean useProxy = propConfig.getBoolean(USE_PROXY);
		config.setProxy(useProxy);
		if (useProxy) {
			String proxyUrl = propConfig.getString(PROXY_URL);
			config.setProxyHost(proxyUrl);
			String proxyPort = propConfig.getString(PROXY_PORT);
			config.setProxyPort(proxyPort);
		}
		
		
	}*/
	
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
			String stravaToken = prop.getProperty(STRAVA_TOKEN);
			config.setStravaToken(stravaToken);
			
			if ( ( kikUser == null ) || "".equals(kikUser) || 
				( kikPsw == null ) || "".equals(kikPsw) || 	
				( stravaToken == null ) || "".equals(stravaToken) 
				) {
				throw new Exception("Une info indispensable non-trouvée dans stravakik.conf (kik.user, kik.password ou strava.token)");
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
