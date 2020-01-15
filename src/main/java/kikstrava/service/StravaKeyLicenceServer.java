package kikstrava.service;

import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

import kikstrava.model.StravaTokenInfo;

/**
 * Http minimal basic server to manage Oauth strava new key management/
 *
 */
public class StravaKeyLicenceServer {

	private static final int port = 5000;
	
	private HttpServer server;
	private StravaV3Service stravaService;
	private boolean isStart = false;
	private LicenceEventListener licenceEventListener;
	
	public StravaKeyLicenceServer(StravaV3Service stravaService, LicenceEventListener licenceEventListener) {
		this.stravaService = stravaService;
		this.licenceEventListener = licenceEventListener;
	}

	public void start() {
		try {
			server = HttpServer.create(new InetSocketAddress(port), 0);

			server.createContext("/", httpExchange -> {

				// A response coming from strava !!!!!
				// Let's extract the precious code 
				String query = httpExchange.getRequestURI().getQuery();
				System.out.println(query);
				
				String responseStr = "";
				
				if ( query != null && query.contains("code=")) {
					// Extract
					
					int idx1 = query.indexOf("code=");
					int idx2 = query.indexOf("&", idx1);
					String code =   query.substring(idx1 + 5, idx2);
					System.out.println("Code:" + code);
					stravaService.setCode(code);
					//stravaService.setTokenInfo(tokenInfo);
					
					// init the token
					StravaTokenInfo tokenInfo = stravaService.getToken();
					stravaService.setTokenInfo(tokenInfo);
					responseStr += "Strava authorisation OK !!!! \r\n";
					responseStr += "Je pense que tout devrait marcher maintenant \r\n";
					responseStr += tokenInfo.getAccess_token() + "\r\n";
					responseStr += tokenInfo.getAthlete().getFirstname()  + "\r\n";
					responseStr += tokenInfo.getAthlete().getLastname() + "\r\n";
					responseStr += tokenInfo.getAthlete().getUsername() + "\r\n";
					
					// save code for the next time 
					stravaService.writeCurrentCode();
					stravaService.writeCurrentToken(tokenInfo);
				}
				else {
					responseStr = "Problème avec l'autorisation Strava";
				}
				
				byte response[] = responseStr.getBytes("UTF-8");
				
				httpExchange.getResponseHeaders().add("Content-Type", "text/plain; charset=UTF-8");
				httpExchange.sendResponseHeaders(200, response.length);

				OutputStream out = httpExchange.getResponseBody();
				out.write(response);
				out.close();
				
				// Callback, license is now validated
				licenceEventListener.onLicenseValidated();
			});

			server.start();
			isStart = true;
			
		} catch (Throwable tr) {
			tr.printStackTrace();
		}
	}
	
	public void stop() {
		server.stop(0);
		isStart = false;
	}

	public boolean isStart() {
		return isStart;
	}
}
