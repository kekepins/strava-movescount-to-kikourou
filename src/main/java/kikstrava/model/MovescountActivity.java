package kikstrava.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonProperty;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class MovescountActivity implements KikourouActivity {
	
	@JsonProperty("ActivityID")
	private int activityId;

	@JsonProperty("AscentAltitude")
	private float dPlus;
	
	@JsonProperty("Distance")
	private float distance;
	
	
	//"LocalStartTime":"2018-01-30T12:04:23.000"
	@JsonProperty("LocalStartTime")
	private String startDate;
	

	@JsonProperty("Duration")
	private int elapseTime;
	
	@JsonProperty("MoveID")
	private long moveId;
	
	@JsonProperty("SelfURI")
	private String uri;
	
	private final BooleanProperty isTransfer = new SimpleBooleanProperty(this, "isTransfer", true);
	
	private String name;
	   
	
	public long getMoveId() {
		return moveId;
	}

	public void setMoveId(long moveId) {
		this.moveId = moveId;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	private ActivityType activityType;

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDateLocal) {
		this.startDate = startDateLocal;
	}

	@Override
	public int getElapsedTime() {
		return elapseTime;
	}

	public void setElapseTime(int elapseTime) {
		this.elapseTime = elapseTime;
	}

	@Override
	public float getDPlus() {
		return dPlus;
	}

	public int getActivityId() {
		return activityId;
	}

	public void setActivityId(int activityId) {
		this.activityId = activityId;
		setActivityType(ActivityType.fromMovescountCode(activityId));
	}



	@Override
	public float getDistance() {
		return distance;
	}

	public void setDPlus(float dPlus) {
		this.dPlus = dPlus;
	}

	public void setDistance(float distance) {
		this.distance = distance;
	}

	@Override
	public int getCode() {
		
		return activityType.getKikCode();
	}

	@Override
	public String getActivity() {
		return activityType.getCode();
	}

	@Override
	public String getName() {
		if ( this.name == null ) {
			return getActivity() + "_" + getMoveId();		
		}
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public ActivityType getActivityType() {
		return activityType;
	}

	public void setActivityType(ActivityType activityType) {
		this.activityType = activityType;
	}
	
	public BooleanProperty getIsTransfer() {
		return isTransfer;
	}
	
	public void setIsTransfer(boolean isStransfer) {
		isTransfer.set(isStransfer);
	}

	
	public String getElapseStr() {
		int[] el = Utils.getSecondsToHMS(elapseTime);
		
		return "" + el[0] + ":" +  el[1] + ":" + el[2];
	}
	
	public void setElapseStr(String elapseStr) {
		int second = Utils.getSecondsFromElapseStr(elapseStr);
		elapseTime = second;
	}

	@Override
	public String getStartDateLocalRead() {
		// 2018-01-05T12:01:52.000
		LocalDateTime ld = Utils.stringDateWithMilliToLocalDateTime(this.startDate);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		return formatter.format(ld);
	}

	@Override
	public LocalDateTime getStartDateLocal() {
		return Utils.stringDateWithMilliToLocalDateTime(this.startDate);
	}

	@Override
	public String getUrl() {
		return "http://www.movescount.com/fr/moves/move" + moveId; 
	}

	@Override
	public String getSource() {
		return "Movescount";
	}

}
