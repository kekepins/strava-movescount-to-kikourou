package kikstrava.model;

/**
 * Token info received form strava 
 */
public class StravaTokenInfo {
	private String token_type;
	private String access_token;
	private StravaAthlete athlete;
	private long expires_at;
	private String state;
	private String refresh_token;
	
	public String getToken_type() {
		return token_type;
	}
	public void setToken_type(String token_type) {
		this.token_type = token_type;
	}
	public String getAccess_token() {
		return access_token;
	}
	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}
	public StravaAthlete getAthlete() {
		return athlete;
	}
	public void setAthlete(StravaAthlete athlete) {
		this.athlete = athlete;
	}
	public long getExpires_at() {
		return expires_at;
	}
	public void setExpires_at(long expires_at) {
		this.expires_at = expires_at;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getRefresh_token() {
		return refresh_token;
	}
	public void setRefresh_token(String refresh_token) {
		this.refresh_token = refresh_token;
	}
	
}
