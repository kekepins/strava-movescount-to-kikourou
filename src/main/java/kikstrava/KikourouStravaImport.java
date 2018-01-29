package kikstrava;

import java.time.LocalDate;

import kikstrava.model.Config;
import kikstrava.model.StravaActivity;
import kikstrava.service.KikourouService;
import kikstrava.service.StravaService;

public class KikourouStravaImport {
	
	
	private StravaService stravaService;
	private KikourouService kikourouService;
	
	public KikourouStravaImport() {
		stravaService = new StravaService(Config.getConfig().getStravaToken());
		kikourouService = new KikourouService(Config.getConfig().getKikUser(), Config.getConfig().getKikPassword());
	}
	
	public void doImport(LocalDate startDate, LocalDate endDate) throws Exception {
		StravaActivity[] activities = stravaService.searchActivities(startDate, endDate, 1, 100);
		kikourouService.addStravaActivities(activities);
	}
	
	public static void main(String[] args) throws Exception {
		
		
		
		// Set proxy
		if ( Config.getConfig().isProxy() ) {
			System.setProperty("http.proxyHost", Config.getConfig().getProxyHost());
		    System.setProperty("http.proxyPort",  Config.getConfig().getProxyPort());
			System.setProperty("https.proxyHost", Config.getConfig().getProxyHost());
		    System.setProperty("https.proxyPort", Config.getConfig().getProxyPort());
		}

		KikourouStravaImport kikImport = new KikourouStravaImport();
		
		///LocalDate startDate = LocalDate.of(2018, 1, 1);
		LocalDate startDate = LocalDate.of(2019, 1, 1);
		//LocalDate endDate = LocalDate.of(2017, 11, 31);
		LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
		kikImport.doImport(startDate, endDate);
		
		
	}
}
