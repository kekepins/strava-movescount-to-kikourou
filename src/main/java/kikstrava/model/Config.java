package kikstrava.model;

public class Config {
	private static Config config  = new Config();
	
	private String kikUser; 
	private String kikPassword;

	// strava
	// old ...
	private String stravaClientId;
	private String stravaSecret;
	
	private String movescountEmail;
	private String movescountUserKey;
	
	private boolean isProxy = false;
	private String proxyHost;
	private String proxyPort;
	
	private double elevationCorrection = 1.0;
	
	private int maxSearch = 20;
	
	public static Config getConfig() {
		return config;
	}

	public String getKikUser() {
		return kikUser;
	}

	public String getKikPassword() {
		return kikPassword;
	}


	public boolean isProxy() {
		return isProxy;
	}

	public void setProxy(boolean isProxy) {
		this.isProxy = isProxy;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public String getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(String proxyPort) {
		this.proxyPort = proxyPort;
	}

	public double getElevationCorrection() {
		return elevationCorrection;
	}

	public void setElevationCorrection(double elevationCorrection) {
		this.elevationCorrection = elevationCorrection;
	}

	public int getMaxSearch() {
		return maxSearch;
	}

	public void setMaxSearch(int maxSearch) {
		this.maxSearch = maxSearch;
	}
	
	public void setKikUser(String kikUser) {
		this.kikUser = kikUser;
	}

	public void setKikPassword(String kikPassword) {
		this.kikPassword = kikPassword;
	}

	public String getMovescountEmail() {
		return movescountEmail;
	}

	public void setMovescountEmail(String movescountEmail) {
		this.movescountEmail = movescountEmail;
	}

	public String getMovescountUserKey() {
		return movescountUserKey;
	}

	public void setMovescountUserKey(String movescountUserKey) {
		this.movescountUserKey = movescountUserKey;
	}
	
	public boolean isMovescountOK() {
		return (movescountEmail != null && !"".equals(movescountEmail) && movescountUserKey != null && !"".equals(movescountUserKey));
	}
	
	public boolean isStravaOK() {
		//return (stravaToken != null && !"".equals(stravaToken));
		
		return (stravaClientId != null && !"".equals(stravaClientId)) && 
			 (stravaSecret != null && !"".equals(stravaSecret))	;
	}

	public String getStravaClientId() {
		return stravaClientId;
	}

	public void setStravaClientId(String stravaClientId) {
		this.stravaClientId = stravaClientId;
	}

	public String getStravaSecret() {
		return stravaSecret;
	}

	public void setStravaSecret(String stravaSecret) {
		this.stravaSecret = stravaSecret;
	}


}
