package kikstrava.controller;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
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
import kikstrava.model.StravaActivity;
import kikstrava.service.KikourouService;
import kikstrava.service.StravaService;

public class KikStravaController implements Initializable {
	
    @FXML
    private TableView<StravaActivity> activitiesTableView;
    
    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;
    
    private ObservableList<StravaActivity> activities = FXCollections.observableArrayList();
    
    private StravaService stravaService;
    private KikourouService kikourouService;
    
    @FXML
    private Button importBtn;
    
    @FXML
    private Spinner<Integer> maxReturnSpinner;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		TableColumn<StravaActivity, Boolean> exportCol = new TableColumn<>("");
		exportCol.setCellFactory(column -> new CheckBoxTableCell<>());
		exportCol.setCellValueFactory(cellData -> {
			StravaActivity cellValue = cellData.getValue();
	        BooleanProperty property = cellValue.getIsTransfer();

	        // Add listener to handler change
	        property.addListener((observable, oldValue, newValue) -> cellValue.setIsTransfer(newValue));
	        return property;
		 });
		
		exportCol.setEditable(true);
		exportCol.setPrefWidth(30.);
		
		TableColumn<StravaActivity, String> dateCol = new TableColumn<>("Date");
		dateCol.setCellValueFactory(new PropertyValueFactory<StravaActivity, String>("startDateLocalRead"));
		dateCol.setEditable(false);
		dateCol.setPrefWidth(110.);
		
		TableColumn<StravaActivity, String> labelCol = new TableColumn<>("Nom");
		labelCol.setCellValueFactory(new PropertyValueFactory<StravaActivity, String>("name"));
		labelCol.setCellFactory(TextFieldTableCell.forTableColumn());
		labelCol.setOnEditCommit((CellEditEvent<StravaActivity, String> t) -> {
	        ((StravaActivity) t.getTableView().getItems().get(
	                t.getTablePosition().getRow())
	                ).setName(t.getNewValue());
	    });
		labelCol.setEditable(true);
		labelCol.setPrefWidth(150.);
		
		TableColumn<StravaActivity, Float> distanceCol = new TableColumn<>("Distance");
		distanceCol.setCellValueFactory(new PropertyValueFactory<StravaActivity, Float>("distance"));
		distanceCol.setCellFactory(TextFieldTableCell.forTableColumn(new FloatStringConverter()));
		distanceCol.setOnEditCommit((CellEditEvent<StravaActivity, Float> t) -> {
	        ((StravaActivity) t.getTableView().getItems().get(
	                t.getTablePosition().getRow())
	                ).setDistance(t.getNewValue());
	    });
		distanceCol.setEditable(true);
		distanceCol.setPrefWidth(70.);
		
		TableColumn<StravaActivity, String> tpsCol = new TableColumn<>("Tps");
		tpsCol.setCellValueFactory(new PropertyValueFactory<StravaActivity, String>("elapseStr"));
		tpsCol.setCellFactory(TextFieldTableCell.forTableColumn());
		tpsCol.setOnEditCommit((CellEditEvent<StravaActivity, String> t) -> {
	        ((StravaActivity) t.getTableView().getItems().get(
	                t.getTablePosition().getRow())
	                ).setElapseStr(t.getNewValue());
	    });
		tpsCol.setEditable(true);
		tpsCol.setPrefWidth(70.);
		
		TableColumn<StravaActivity, Float> dplusCol = new TableColumn<>("D+");
		dplusCol.setCellValueFactory(new PropertyValueFactory<StravaActivity, Float>("total_elevation_gain"));
		dplusCol.setCellFactory(TextFieldTableCell.forTableColumn(new FloatStringConverter()));
		dplusCol.setOnEditCommit((CellEditEvent<StravaActivity, Float> t) -> {
	        ((StravaActivity) t.getTableView().getItems().get(
	                t.getTablePosition().getRow())
	                ).setTotal_elevation_gain(t.getNewValue());
	    });
		dplusCol.setEditable(true);
		dplusCol.setPrefWidth(70.);
		

		
		TableColumn<StravaActivity, String> sportCol = new TableColumn<>("Sport");
		sportCol.setCellValueFactory(new PropertyValueFactory<StravaActivity, String>("type"));
		sportCol.setEditable(false);
		sportCol.setPrefWidth(70.);
			            
		activitiesTableView.getColumns().addAll(exportCol, dateCol, labelCol, tpsCol, distanceCol, dplusCol, sportCol);
		
		stravaService = new StravaService(Config.getConfig().getStravaToken());
		kikourouService = new KikourouService(Config.getConfig().getKikUser(), Config.getConfig().getKikPassword());
		
		activitiesTableView.setItems(activities);
		activitiesTableView.setEditable(true);
		
		maxReturnSpinner.getValueFactory().setValue(5);
		
	}
	
    @FXML
    void onViewClicked(MouseEvent event) {
    	
    	// Clear previous search
    	activities.clear();
    	
    	try {
    		LocalDate startDate = startDatePicker.getValue();
    		LocalDate endDate = endDatePicker.getValue();
			StravaActivity[] resActivities = stravaService.searchActivities(startDate, endDate, 1, maxReturnSpinner.getValueFactory().getValue());
			
			if ( resActivities != null && resActivities.length > 0) {
				for ( StravaActivity stravaActivity : resActivities ) {
					activities.add(stravaActivity);
				}
				
				importBtn.setDisable(false);
			}
			else {
				importBtn.setDisable(true);
			}
			
			
		} 
    	catch (Exception e) {
			e.printStackTrace();
		} 
    }
    
    /**
     * Import checked items in kikourou 
     */
    @FXML
    void onImportClicked(MouseEvent event) {
    	int countOk = 0;
    	for (StravaActivity activity : activities) {
    		if ( activity.getIsTransfer().get()) {
    			// Checked ..
    			System.out.println("Transfering " + activity.getName() + " to kikourou d+ " + activity.getTotal_elevation_gain());
    			try {
					kikourouService.addStravaActivity(activity);
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
	    	alert.setContentText(countOk + " entrainements ont été correctement importés");
	    	alert.showAndWait();
    	}
    }
}
