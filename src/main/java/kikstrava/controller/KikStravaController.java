package kikstrava.controller;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import javafx.application.HostServices;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.util.converter.FloatStringConverter;
import kikstrava.model.Config;
import kikstrava.model.KikourouActivity;
import kikstrava.model.GarminActivity;
import kikstrava.model.MovescountActivity;
import kikstrava.model.StravaActivity;
import kikstrava.service.ActivityFingerprint;
import kikstrava.service.KikourouService;
import kikstrava.service.LicenceEventListener;
import kikstrava.service.GarminService;
import kikstrava.service.MovescountService;
import kikstrava.service.ServiceException;
import kikstrava.service.StravaKeyLicenceServer;
import kikstrava.service.StravaV3Service;

public class KikStravaController implements Initializable, LicenceEventListener {
	
    @FXML
    private TableView<KikourouActivity> activitiesTableView;
    
    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;
    
    private ObservableList<KikourouActivity> activities = FXCollections.observableArrayList();
    
    private StravaV3Service stravaService;
    private MovescountService movescountService;
    private GarminService garminService;
    private KikourouService kikourouService;
    private HostServices hostServices;
    private StravaKeyLicenceServer stravaLicenceKeyServerListener;
    @FXML
    private Button importBtn;
    
    @FXML
    private Spinner<Integer> maxReturnSpinner;

    @FXML
    private RadioButton stravaRadioButton;

    @FXML
    private RadioButton garminRadioButton;


 	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		// init services
		//stravaService = new StravaService(Config.getConfig().getStravaToken());
		stravaService = new StravaV3Service(Config.getConfig().getStravaClientId(), Config.getConfig().getStravaSecret());
		kikourouService = new KikourouService(Config.getConfig().getKikUser(), Config.getConfig().getKikPassword());

		// Strava access server ...
		stravaLicenceKeyServerListener = new StravaKeyLicenceServer(stravaService, this);
		garminService = new GarminService(Config.getConfig().getGarminApiKey());

		// radio buttons source
		ToggleGroup toggleGroup = new ToggleGroup();
		stravaRadioButton.setToggleGroup(toggleGroup);
		garminRadioButton.setToggleGroup(toggleGroup);
		stravaRadioButton.setSelected(true);
		if ( !Config.getConfig().isGarminOK() ) {
			garminRadioButton.setDisable(true);
		}
		if ( !Config.getConfig().isStravaOK() ) {
			stravaRadioButton.setDisable(true);
			garminRadioButton.setSelected(true);
		}


		TableColumn<KikourouActivity, Boolean> exportCol = new TableColumn<>("");
		exportCol.setCellFactory(column -> new CheckBoxTableCell<>());
		exportCol.setCellValueFactory(cellData -> {
			KikourouActivity cellValue = cellData.getValue();
	        BooleanProperty property = cellValue.getIsTransfer();

	        // Add listener to handler change
	        property.addListener((observable, oldValue, newValue) -> cellValue.setIsTransfer(newValue));
	        return property;
		 });
		
		exportCol.setEditable(true);
		exportCol.setPrefWidth(30.);
		
		TableColumn<KikourouActivity, String> dateCol = new TableColumn<>("Date");
		dateCol.setCellValueFactory(new PropertyValueFactory<KikourouActivity, String>("startDateLocalRead"));
		dateCol.setEditable(false);
		dateCol.setPrefWidth(110.);
		
		TableColumn<KikourouActivity, String> labelCol = new TableColumn<>("Nom");
		labelCol.setCellValueFactory(new PropertyValueFactory<KikourouActivity, String>("name"));
		labelCol.setCellFactory(TextFieldTableCell.forTableColumn());
		labelCol.setOnEditCommit((CellEditEvent<KikourouActivity, String> t) -> {
	        ((KikourouActivity) t.getTableView().getItems().get(
	                t.getTablePosition().getRow())
	                ).setName(t.getNewValue());
	    });
		labelCol.setEditable(true);
		labelCol.setPrefWidth(150.);
		
		TableColumn<KikourouActivity, Float> distanceCol = new TableColumn<>("Distance");
		distanceCol.setCellValueFactory(new PropertyValueFactory<KikourouActivity, Float>("distance"));
		distanceCol.setCellFactory(TextFieldTableCell.forTableColumn(new FloatStringConverter()));
		distanceCol.setOnEditCommit((CellEditEvent<KikourouActivity, Float> t) -> {
	        ((KikourouActivity) t.getTableView().getItems().get(
	                t.getTablePosition().getRow())
	                ).setDistance(t.getNewValue());
	    });
		distanceCol.setEditable(true);
		distanceCol.setPrefWidth(70.);
		
		TableColumn<KikourouActivity, String> tpsCol = new TableColumn<>("Tps");
		tpsCol.setCellValueFactory(new PropertyValueFactory<KikourouActivity, String>("elapseStr"));
		tpsCol.setCellFactory(TextFieldTableCell.forTableColumn());
		tpsCol.setOnEditCommit((CellEditEvent<KikourouActivity, String> t) -> {
	        ((KikourouActivity) t.getTableView().getItems().get(
	                t.getTablePosition().getRow())
	                ).setElapseStr(t.getNewValue());
	    });
		tpsCol.setEditable(true);
		tpsCol.setPrefWidth(70.);
		
		TableColumn<KikourouActivity, Float> dplusCol = new TableColumn<>("D+");
		dplusCol.setCellValueFactory(new PropertyValueFactory<KikourouActivity, Float>("dPlus"));
		dplusCol.setCellFactory(TextFieldTableCell.forTableColumn(new FloatStringConverter()));
		dplusCol.setOnEditCommit((CellEditEvent<KikourouActivity, Float> t) -> {
	        ((KikourouActivity) t.getTableView().getItems().get(
	                t.getTablePosition().getRow())
	                ).setDPlus(t.getNewValue());
	    });
		dplusCol.setEditable(true);
		dplusCol.setPrefWidth(70.);
		

		
		TableColumn<KikourouActivity, String> sportCol = new TableColumn<>("Sport");
		sportCol.setCellValueFactory(new PropertyValueFactory<KikourouActivity, String>("activity"));
		sportCol.setEditable(false);
		sportCol.setPrefWidth(70.);
			            
		activitiesTableView.getColumns().addAll(exportCol, dateCol, labelCol, tpsCol, distanceCol, dplusCol, sportCol);
		
		activitiesTableView.setItems(activities);
		activitiesTableView.setEditable(true);
		
		maxReturnSpinner.getValueFactory().setValue(5);
		
	}
	
    @FXML
    void onViewClicked(MouseEvent event) {
    	readActivities();
    }
    
    
    /**
     * Import checked items in kikourou 
     */
    @FXML
    void onImportClicked(MouseEvent event) {
    	int countOk = 0;
		int countFailed = 0;
		String firstFailure = null;
    	for (KikourouActivity activity : activities) {
    		if ( activity.getIsTransfer().get()) {
    			// Checked ..
    			//System.out.println("Transfering " + activity.getName() + " to kikourou d+ " + activity.getDPlus());
    			try {
					kikourouService.addActivity(activity);
					countOk++;
				} catch (Exception e) {
					e.printStackTrace();
					countFailed++;
					if (firstFailure == null) {
						firstFailure = e.getMessage();
					}
				}
    			
    		}
    	}
    	
		if (countOk > 0 || countFailed > 0) {
			Alert alert = new Alert(countFailed > 0 ? AlertType.WARNING : AlertType.INFORMATION);
			alert.setTitle("Import");
			alert.setHeaderText("Import dans kikourou");
			String content = countOk + " entrainement(s) envoyé(s) sans erreur.";
			if (countFailed > 0) {
				content += "\n" + countFailed + " échec(s).";
				if (firstFailure != null && !firstFailure.isBlank()) {
					content += "\n" + firstFailure;
				}
			}
			alert.setContentText(content);
			alert.showAndWait();
		}
    }
    
    private void readActivities() {
    	
    	System.out.println("readActivities");
    	// Clear previous search
    	activities.clear();
    	
    	try {
    		LocalDate startDate = startDatePicker.getValue();
    		LocalDate endDate = endDatePicker.getValue();
    		
			List<KikourouActivity> kikActivities =
					kikourouService.searchActivitiesInRange(startDate, endDate);
    		
    		if ( stravaRadioButton.isSelected() ) {
			if  ( !stravaService.isAuthenticated() ) {
				// Get new strava authorisation
				verifyStravaLicence();
			}
			else {
				readStravaActivities(kikActivities, startDate, endDate);
			}
    		}
    		else {
    			readGarminActivities(kikActivities, startDate, endDate);
    		}
		} 
    	catch (Exception e) {
			e.printStackTrace();
		} 
    }
 

	public void setHostServices(HostServices hostServices) {
		this.hostServices = hostServices;
		
	}
	
	private void verifyStravaLicence() {
		System.out.println("verifyStravaLicence");

		if ( !stravaLicenceKeyServerListener.isStart()) {
    		stravaLicenceKeyServerListener.start();
    	}
		
    	// Launch authorization in a web page
    	// response in stravaLicenceKeyServerListener (web server)
    	this.hostServices.showDocument(stravaService.getAuthUrl());
	}
	
	private void readStravaActivities(List<KikourouActivity> kikActivities, LocalDate startDate, LocalDate endDate)
		throws JsonParseException, JsonMappingException, IOException, ServiceException {
		StravaActivity[] resActivities = stravaService.searchActivities(startDate, endDate, 1, maxReturnSpinner.getValueFactory().getValue());
		addActivities(resActivities, kikActivities);
	}
	
	private void readMovescountActivities(List<KikourouActivity> kikActivities, LocalDate startDate, LocalDate endDate)
		throws JsonParseException, JsonMappingException, IOException {
		MovescountActivity[] movescountActivities = movescountService.searchActivities(startDate, endDate, maxReturnSpinner.getValueFactory().getValue());
		addActivities(movescountActivities, kikActivities);
	}

	private void readGarminActivities(List<KikourouActivity> kikActivities, LocalDate startDate, LocalDate endDate)
		throws JsonParseException, JsonMappingException, IOException, ServiceException {
		GarminActivity[] garminActivities = garminService.searchActivities(startDate, endDate, maxReturnSpinner.getValueFactory().getValue());
		addActivities(garminActivities, kikActivities);
	}

	private void addActivities(
			KikourouActivity[] sourceActivities, List<KikourouActivity> kikourouActivities) {
		if (sourceActivities == null || sourceActivities.length == 0) {
			importBtn.setDisable(true);
			return;
		}

		List<ActivityFingerprint> existingActivities = new ArrayList<>();
		for (KikourouActivity activity : kikourouActivities) {
			existingActivities.add(ActivityFingerprint.from(activity));
		}
		List<ActivityFingerprint> unmatchedKikourouActivities = new ArrayList<>(existingActivities);
		List<ActivityFingerprint> sourceActivitiesSeen = new ArrayList<>();
		int notToTransferCount = 0;
		int transferCount = 0;
		for (KikourouActivity activity : sourceActivities) {
			ActivityFingerprint fingerprint = ActivityFingerprint.from(activity);
			int matchIndex = findKikourouMatch(unmatchedKikourouActivities, fingerprint);
			boolean alreadyInKikourou = matchIndex >= 0;
			if (alreadyInKikourou) {
				unmatchedKikourouActivities.remove(matchIndex);
			}
			boolean duplicateSourceActivity = sourceActivitiesSeen.stream()
					.anyMatch(existing -> existing.matches(fingerprint));
			boolean isNew = !alreadyInKikourou && !duplicateSourceActivity;
			activity.setIsTransfer(isNew);
			activities.add(activity);
			sourceActivitiesSeen.add(fingerprint);
			if (isNew) {
				transferCount++;
			} else {
				notToTransferCount++;
			}

			if (isNew) {
				logPreselectionMismatch(activity, fingerprint, existingActivities);
			}
		}
		System.out.printf(
				"Preselection %s: %d source activities, %d Kikourou activities, "
						+ "%d not selected, %d to transfer.%n",
				sourceActivities[0].getSource(), sourceActivities.length, existingActivities.size(),
				notToTransferCount, transferCount);
		importBtn.setDisable(false);
	}

	private int findKikourouMatch(
			List<ActivityFingerprint> kikourouActivities, ActivityFingerprint sourceActivity) {
		for (int i = 0; i < kikourouActivities.size(); i++) {
			ActivityFingerprint existing = kikourouActivities.get(i);
			if (existing.matches(sourceActivity)) {
				return i;
			}
		}
		return -1;
	}

	private void logPreselectionMismatch(
			KikourouActivity source,
			ActivityFingerprint fingerprint,
			List<ActivityFingerprint> existingActivities) {
		List<ActivityFingerprint> sameDate = existingActivities.stream()
				.filter(existing -> existing.date().equals(fingerprint.date()))
				.sorted((first, second) -> {
					long firstDistanceDifference = Math.abs(
							first.distanceMeters() - fingerprint.distanceMeters());
					long secondDistanceDifference = Math.abs(
							second.distanceMeters() - fingerprint.distanceMeters());
					int distanceComparison = Long.compare(
							firstDistanceDifference, secondDistanceDifference);
					if (distanceComparison != 0) {
						return distanceComparison;
					}
					long firstDurationDifference = Math.abs(
							(long) first.elapsedSeconds() - fingerprint.elapsedSeconds());
					long secondDurationDifference = Math.abs(
							(long) second.elapsedSeconds() - fingerprint.elapsedSeconds());
					return Long.compare(firstDurationDifference, secondDurationDifference);
				})
				.toList();
		System.out.printf("Preselection mismatch %s: %s %ds/%dm; %d Kikourou records on date.%n",
				source.getSource(), fingerprint.date(), fingerprint.elapsedSeconds(),
				fingerprint.distanceMeters(), sameDate.size());
		sameDate.stream().limit(3).forEach(existing -> System.out.printf(
				"  Kikourou candidate: %ds/%dm.%n",
				existing.elapsedSeconds(), existing.distanceMeters()));
	}

	@Override
	public void onLicenseValidated() {
		System.out.println("onLicenseValidated");
		// stop licence server
		if ( stravaLicenceKeyServerListener.isStart()) {
    		stravaLicenceKeyServerListener.stop();
    	}
		
		// Ok license is now validated, we should try to read activites 
		readActivities();
	}

 
}
