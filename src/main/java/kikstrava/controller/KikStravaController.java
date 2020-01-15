package kikstrava.controller;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Map;
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
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.util.converter.FloatStringConverter;
import kikstrava.model.Config;
import kikstrava.model.KikourouActivity;
import kikstrava.model.MovescountActivity;
import kikstrava.model.StravaActivity;
import kikstrava.service.KikourouService;
import kikstrava.service.LicenceEventListener;
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
    private RadioButton movescountRadioButton;
    
 	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		// init services
		//stravaService = new StravaService(Config.getConfig().getStravaToken());
		stravaService = new StravaV3Service(Config.getConfig().getStravaClientId(), Config.getConfig().getStravaSecret());
		kikourouService = new KikourouService(Config.getConfig().getKikUser(), Config.getConfig().getKikPassword());
		movescountService = new MovescountService(Config.getConfig().getMovescountEmail(), Config.getConfig().getMovescountUserKey());
		
		// Strava access server ...
		stravaLicenceKeyServerListener = new StravaKeyLicenceServer(stravaService, this);
		
		// radio buttons
		ToggleGroup toggleGroup = new ToggleGroup();

		stravaRadioButton.setToggleGroup(toggleGroup);
		movescountRadioButton.setToggleGroup(toggleGroup);
		stravaRadioButton.setSelected(true);

		
		if ( !Config.getConfig().isMovescountOK() ) {
			movescountRadioButton.setDisable(true);
		}
		
		if ( !Config.getConfig().isStravaOK() ) {
			stravaRadioButton.setDisable(true);
			movescountRadioButton.setSelected(true);
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
    	for (KikourouActivity activity : activities) {
    		if ( activity.getIsTransfer().get()) {
    			// Checked ..
    			//System.out.println("Transfering " + activity.getName() + " to kikourou d+ " + activity.getDPlus());
    			try {
					kikourouService.addActivity(activity);
					countOk++;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			
    		}
    	}
    	
    	if ( countOk > 0 ) {
	    	Alert alert = new Alert(AlertType.INFORMATION);
	    	alert.setTitle("Import");
	    	alert.setHeaderText("Import dans kikourou");
	    	
	    	if ( countOk == 1 ) {
	    		alert.setContentText(countOk + " entrainement a été correctement importé");
	    		
	    	}
	    	else {
	    		alert.setContentText(countOk + " entrainements ont été correctement importés");
	    	}
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
    		
    		// Get kikourou data from the same period
    		Map<LocalDate, KikourouActivity> kikActivities = kikourouService.searchActivities(startDate, endDate);
    		
    		if ( stravaRadioButton.isSelected() ) {
    			
    			if  ( !stravaService.isLicenceCodeInit() ) {
    				// Get new strava authorisation
    				verifyStravaLicence();
    			}
    			else {
    				readStravaActivities(kikActivities, startDate, endDate);
    			}
    		}
    		else {
    			readMovescountActivities(kikActivities, startDate, endDate);
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
	
	private void readStravaActivities(Map<LocalDate, KikourouActivity> kikActivities, LocalDate startDate, LocalDate endDate) 
		throws JsonParseException, JsonMappingException, IOException, ServiceException {
		StravaActivity[] resActivities = stravaService.searchActivities(startDate, endDate, 1, maxReturnSpinner.getValueFactory().getValue());
		
		if ( resActivities != null && resActivities.length > 0) {
			for ( StravaActivity stravaActivity : resActivities ) {
				activities.add(stravaActivity);
				
				LocalDate localDate = stravaActivity.getStartDateLocal().toLocalDate();
				
				if ( kikActivities.containsKey(localDate) ) {
					stravaActivity.setIsTransfer(false);
				}
			}
			// si entrainement meme date => on decoche
			importBtn.setDisable(false);
		}
		else {
			importBtn.setDisable(true);
		}
	}
	
	private void readMovescountActivities(Map<LocalDate, KikourouActivity> kikActivities, LocalDate startDate, LocalDate endDate) 
		throws JsonParseException, JsonMappingException, IOException {
		MovescountActivity[] movescountActivities = movescountService.searchActivities(startDate, endDate, maxReturnSpinner.getValueFactory().getValue());
		
		if ( movescountActivities != null && movescountActivities.length > 0) {
			for ( MovescountActivity movescountActivity : movescountActivities ) {
				activities.add(movescountActivity);
				
				LocalDate localDate = movescountActivity.getStartDateLocal().toLocalDate();
				
				if ( kikActivities.containsKey(localDate) ) {
					movescountActivity.setIsTransfer(false);
				}

			}
			
			importBtn.setDisable(false);
		}
		else {
			importBtn.setDisable(true);
		}
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
