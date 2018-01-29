package kikstrava.service;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.ClasspathLocationStrategy;
import org.apache.commons.configuration2.io.CombinedLocationStrategy;
import org.apache.commons.configuration2.io.FileLocationStrategy;
import org.apache.commons.configuration2.io.FileSystemLocationStrategy;
import org.apache.commons.configuration2.io.ProvidedURLLocationStrategy;

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
	
	
	public static void init() throws ConfigurationException {
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
		
		
	}
}
