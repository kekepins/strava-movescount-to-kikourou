package kikstrava.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import kikstrava.model.Config;

public class ConfigManager {
	
	// Mandatory
	private final static String KIK_USER_KEY = "kik.user";
	private final static String KIK_PSW_KEY = "kik.password";
	private final static String STRAVA_TOKEN = "strava.token";
	private final static String STRAVA_CLIENTID = "strava.clientid";
	private final static String STRAVA_SECRET = "strava.secret";
	private final static String GARMIN_APIKEY = "garmin.apikey";
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

			input = openConfig();
			
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
				config.setKikUser(kikUser.trim());
			}
			
			String kikPsw = properties.getProperty(KIK_PSW_KEY);
			if ( kikPsw != null ) {
				config.setKikPassword(kikPsw.trim());
			}
			
			//--------------
			// Strava
			//-------------
			String stravaSecret = properties.getProperty(STRAVA_SECRET);
			if ( stravaSecret != null ) {
				config.setStravaSecret(stravaSecret.trim());
			}
				
			String stravaClientId = properties.getProperty(STRAVA_CLIENTID);
			if ( stravaClientId != null ) {
				config.setStravaClientId(stravaClientId.trim());
			}
			

			//--------------
			// Garmin
			//-------------
			String garminApiKey = properties.getProperty(GARMIN_APIKEY);
			if ( garminApiKey != null ) {
				config.setGarminApiKey(garminApiKey.trim());
			}

			if (kikUser == null || kikUser.isBlank()
					|| kikPsw == null || kikPsw.isBlank()) {
				throw new Exception("Info kikourou (user ou psw) non trouvé dans la conf, fichier conf/stravakik.conf");
			}
			
			if ( !config.isStravaOK() && !config.isGarminOK() ) {
				throw new Exception("Il faut configurer un accès à Strava ou à Garmin, fichier conf/stravakik.conf");
			}
			
			String useProxy = properties.getProperty(USE_PROXY, "false");
			boolean isUseProxy = "true".equals(useProxy);   // Boolean.getBoolean(useProxy);
			config.setProxy(isUseProxy);
			if (isUseProxy) {
				String proxyUrl = properties.getProperty(PROXY_URL);
				config.setProxyHost(proxyUrl);
				String proxyPort = properties.getProperty(PROXY_PORT);
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

	private static InputStream openConfig() throws Exception {
		Path workingDirectory = Paths.get("").toAbsolutePath().normalize();
		CodeSource codeSource = ConfigManager.class.getProtectionDomain().getCodeSource();
		Path codeLocation = codeSource == null
				? null
				: Paths.get(codeSource.getLocation().toURI()).toAbsolutePath().normalize();
		Optional<Path> configFile = findConfigFile(workingDirectory, codeLocation);
		if (configFile.isPresent()) {
			return Files.newInputStream(configFile.get());
		}
		return ConfigManager.class.getClassLoader().getResourceAsStream("stravakik.conf");
	}

	static Optional<Path> findConfigFile(Path workingDirectory, Path codeLocation) {
		List<Path> candidates = new ArrayList<>();
		candidates.add(workingDirectory.resolve(Paths.get("conf", "stravakik.conf")).normalize());

		if (codeLocation != null) {
			Path appDirectory = Files.isDirectory(codeLocation) ? codeLocation : codeLocation.getParent();
			if (appDirectory != null) {
				Path executableDirectory = appDirectory.getParent();
				if (executableDirectory != null) {
					candidates.add(executableDirectory.resolve(Paths.get("conf", "stravakik.conf")));
				}
				candidates.add(appDirectory.resolve(Paths.get("conf", "stravakik.conf")));
			}
		}
		candidates.add(workingDirectory.resolve(Paths.get("app", "conf", "stravakik.conf")).normalize());

		for (Path candidate : candidates) {
			if (Files.isRegularFile(candidate)) {
				return Optional.of(candidate);
			}
		}
		return Optional.empty();
	}

	public static void applyProxySettings() {
		Config config = Config.getConfig();
		if (config.isProxy()) {
			System.setProperty("http.proxyHost", config.getProxyHost());
			System.setProperty("http.proxyPort", config.getProxyPort());
			System.setProperty("https.proxyHost", config.getProxyHost());
			System.setProperty("https.proxyPort", config.getProxyPort());
		}
	}

}
