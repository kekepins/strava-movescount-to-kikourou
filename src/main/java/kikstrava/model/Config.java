package kikstrava.model;

public class Config {
	private static Config config  = new Config();
	
	private String kikUser = "xxxxxxxxxxxx";
	private String kikPassword = "yyyyyyyyyyyyyy";

	private String stravaToken = "zzzzzzzzzzzzzzzzzz";
	
	private boolean isProxy = false;
	private String proxyHost = "ggggggggggggg";
	private String proxyPort = "eee";
	
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

	public String getStravaToken() {
		return stravaToken;
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

	public void setStravaToken(String stravaToken) {
		this.stravaToken = stravaToken;
	}



}
