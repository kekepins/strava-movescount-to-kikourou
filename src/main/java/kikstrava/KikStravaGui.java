package kikstrava;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import kikstrava.controller.KikStravaController;
import kikstrava.service.ConfigManager;
import kikstrava.service.DailySync;

public class KikStravaGui extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			
			ConfigManager.init();
			ConfigManager.applyProxySettings();
			
			FXMLLoader loader = new FXMLLoader();
			
			loader.setLocation(KikStravaGui.class.getResource("view/kikstrava.fxml"));
			Parent root =  loader.load();
			
			KikStravaController controller = loader.getController();
			controller.setHostServices(getHostServices());
	       
	        //Scene scene = new Scene(root, 640, 540);
			Scene scene = new Scene(root);
	        //scene.getStylesheets().add(NeuranetGui.class.getResource("application.css").toExternalForm());
	    
	        primaryStage.setTitle("Strava to kikourou");
	        primaryStage.getIcons().add(new Image(
					KikStravaGui.class.getResource("view/kikourou-icon.png").toExternalForm()));
	        primaryStage.setScene(scene);
	        primaryStage.setResizable(false);
	        primaryStage.show();
			

		} catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static void main(String[] args) {
		if (args.length == 1 && "--test-import=3".equals(args[0])) {
			try {
				DailySync.testImportLatestThreeGarmin();
				System.exit(0);
			} catch (Exception e) {
				System.err.println("Three-activity test failed: " + e.getMessage());
				e.printStackTrace(System.err);
				System.exit(1);
			}
			return;
		}
		if (args.length == 1 && "--verify-import=3".equals(args[0])) {
			try {
				DailySync.verifyLatestThreeGarmin();
				System.exit(0);
			} catch (Exception e) {
				System.err.println("Three-activity verification failed: " + e.getMessage());
				e.printStackTrace(System.err);
				System.exit(1);
			}
			return;
		}
		if (args.length == 1 && args[0].startsWith("--verify-strava=")) {
			try {
				int count = Integer.parseInt(args[0].substring("--verify-strava=".length()));
				DailySync.verifyLatestStrava(count);
				System.exit(0);
			} catch (Exception e) {
				System.err.println("Read-only Strava verification failed: " + e.getMessage());
				e.printStackTrace(System.err);
				System.exit(1);
			}
			return;
		}
		if (args.length > 0 && "--sync".equals(args[0])) {
			try {
				DailySync.run(DailySync.sourceFromArgs(args));
				System.exit(0);
			} catch (Exception e) {
				System.err.println("Daily synchronization failed: " + e.getMessage());
				e.printStackTrace(System.err);
				System.exit(1);
			}
			return;
		}
		launch(args);
	}
}