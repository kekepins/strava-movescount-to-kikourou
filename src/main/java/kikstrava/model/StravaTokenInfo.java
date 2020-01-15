package kikstrava.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Token info received form strava 
 */
public class StravaTokenInfo {
	private String token_type;
	private String access_token;
	private StravaAthlete athlete;
	private long expires_at;
	private LocalDateTime expiresDate;
	private long expires_in;
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
		
		expiresDate = LocalDateTime.ofInstant(Instant.ofEpochSecond(expires_at), ZoneId.systemDefault());
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
	public long getExpires_in() {
		return expires_in;
	}
	public void setExpires_in(long expires_in) {
		this.expires_in = expires_in;
	}
	
	public String toString() {
		
		LocalDateTime expireDate =
			    LocalDateTime.ofInstant(Instant.ofEpochSecond(expires_at), ZoneId.systemDefault());
		
		return "type:" + token_type + "/ac:" + access_token  + "/ea:" + expires_at + "/ed:" + expireDate + "/st:" + state +"/rt:" + refresh_token;
	}
	
	public LocalDateTime getExpirationDate() {
		return expiresDate;
	}
}
