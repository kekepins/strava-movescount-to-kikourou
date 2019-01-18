package kikstrava;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import kikstrava.controller.KikStravaController;
import kikstrava.model.Config;
import kikstrava.service.ConfigManager;

public class KikStravaGui extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			
			ConfigManager.init();
			
			// Set proxy
			if ( Config.getConfig().isProxy() ) {
				System.setProperty("http.proxyHost", Config.getConfig().getProxyHost());
			    System.setProperty("http.proxyPort",  Config.getConfig().getProxyPort());
				System.setProperty("https.proxyHost", Config.getConfig().getProxyHost());
			    System.setProperty("https.proxyPort", Config.getConfig().getProxyPort());
			}
			
			primaryStage.initStyle(StageStyle.UTILITY);
			
			FXMLLoader loader = new FXMLLoader();
			
			loader.setLocation(KikStravaGui.class.getResource("view/kikstrava.fxml"));
			Parent root =  loader.load();
			
			KikStravaController controller = loader.getController();
			controller.setHostServices(getHostServices());
	       
	        //Scene scene = new Scene(root, 640, 540);
			Scene scene = new Scene(root);
	        //scene.getStylesheets().add(NeuranetGui.class.getResource("application.css").toExternalForm());
	    
	        primaryStage.setTitle("Strava to kikourou");
	        primaryStage.setScene(scene);
	        primaryStage.setResizable(false);
	        primaryStage.show();
			

		} catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}