package kikstrava.service;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

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
				
				String responseStr;
				boolean authorizationSucceeded = false;
				
				String code = getQueryParameter(query, "code");
				if ( code != null && !code.isBlank()) {
					stravaService.setCode(code);
					
					StravaTokenInfo tokenInfo = stravaService.getToken();
					if (tokenInfo == null || tokenInfo.getRefresh_token() == null) {
						responseStr = "Strava authorization failed. Return to the application and try again.";
					} else {
						stravaService.setTokenInfo(tokenInfo);
						stravaService.writeCurrentCode();
						stravaService.writeCurrentToken(tokenInfo);
						responseStr = "Strava authorization successful. You can close this page.";
						authorizationSucceeded = true;
					}
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
				
				if (authorizationSucceeded) {
					licenceEventListener.onLicenseValidated();
				}
			});

			server.start();
			isStart = true;
			
		} catch (Throwable tr) {
			tr.printStackTrace();
		}
	}

	private String getQueryParameter(String query, String name) {
		if (query == null) {
			return null;
		}
		for (String parameter : query.split("&")) {
			String[] pair = parameter.split("=", 2);
			if (pair.length == 2 && name.equals(pair[0])) {
				return URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
			}
		}
		return null;
	}
	
	public void stop() {
		server.stop(0);
		isStart = false;
	}

	public boolean isStart() {
		return isStart;
	}
}
