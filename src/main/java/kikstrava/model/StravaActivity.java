package kikstrava.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

public class StravaActivity {
    private int id;
    private int resource_state;
    private String external_id;
    private int upload_id;
    private String name;
    private float distance;
    private int moving_time;
    private int elapsed_time;
    private float total_elevation_gain;
    private StravaActivityType type;
    private String start_date;
    private String start_date_local;
    private String timezone;
    private String[] start_latlng;
    private String[] end_latlng;
    private String location_city;
    private String location_state;
    private int achievement_count;
    private int kudos_count;
    private int comment_count;
    private int athlete_count;
    private int photo_count;
    private boolean trainer;
    private boolean commute;
    private boolean manual;
    private boolean flagged;
    private String gear_id;
    private float average_speed;
    private float max_speed;
    private float average_cadence;
    private int average_temp;
    private float average_watts;
    private float kilojoules;
    private float average_heartrate;
    private float max_heartrate;
    private float calories;
    private int truncated;
    private boolean has_kudoed;
    
    //private boolean isTransfer = true;
    private final BooleanProperty isTransfer = new SimpleBooleanProperty(this, "isTransfer", true);
    
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getResource_state() {
		return resource_state;
	}
	public void setResource_state(int resource_state) {
		this.resource_state = resource_state;
	}
	public String getExternal_id() {
		return external_id;
	}
	public void setExternal_id(String external_id) {
		this.external_id = external_id;
	}
	public int getUpload_id() {
		return upload_id;
	}
	public void setUpload_id(int upload_id) {
		this.upload_id = upload_id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public float getDistance() {
		return distance;
	}
	public void setDistance(float distance) {
		this.distance = distance;
	}
	public int getMoving_time() {
		return moving_time;
	}
	public void setMoving_time(int moving_time) {
		this.moving_time = moving_time;
	}
	public int getElapsed_time() {
		return elapsed_time;
	}
	public void setElapsed_time(int elapsed_time) {
		this.elapsed_time = elapsed_time;
	}
	public float getTotal_elevation_gain() {
		return total_elevation_gain;
	}
	public void setTotal_elevation_gain(float total_elevation_gain) {
		this.total_elevation_gain = total_elevation_gain;
	}
	public StravaActivityType getType() {
		return type;
	}
	public void setType(StravaActivityType type) {
		this.type = type;
	}
	public String getStart_date() {
		return start_date;
	}
	public void setStart_date(String start_date) {
		this.start_date = start_date;
	}
	
	public String getStartDateLocalRead() {
		LocalDateTime ld = Utils.stringToLocalDateTime(this.start_date);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		return formatter.format(ld);
	}
	
	
	public String getStart_date_local() {
		return start_date_local;
	}
	public void setStart_date_local(String start_date_local) {
		this.start_date_local = start_date_local;
	}
	public String getTimezone() {
		return timezone;
	}
	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}
	public String[] getStart_latlng() {
		return start_latlng;
	}
	public void setStart_latlng(String[] start_latlng) {
		this.start_latlng = start_latlng;
	}
	public String[] getEnd_latlng() {
		return end_latlng;
	}
	public void setEnd_latlng(String[] end_latlng) {
		this.end_latlng = end_latlng;
	}
	public String getLocation_city() {
		return location_city;
	}
	public void setLocation_city(String location_city) {
		this.location_city = location_city;
	}
	public String getLocation_state() {
		return location_state;
	}
	public void setLocation_state(String location_state) {
		this.location_state = location_state;
	}
	public int getAchievement_count() {
		return achievement_count;
	}
	public void setAchievement_count(int achievement_count) {
		this.achievement_count = achievement_count;
	}
	public int getKudos_count() {
		return kudos_count;
	}
	public void setKudos_count(int kudos_count) {
		this.kudos_count = kudos_count;
	}
	public int getComment_count() {
		return comment_count;
	}
	public void setComment_count(int comment_count) {
		this.comment_count = comment_count;
	}
	public int getAthlete_count() {
		return athlete_count;
	}
	public void setAthlete_count(int athlete_count) {
		this.athlete_count = athlete_count;
	}
	public int getPhoto_count() {
		return photo_count;
	}
	public void setPhoto_count(int photo_count) {
		this.photo_count = photo_count;
	}
	public boolean isTrainer() {
		return trainer;
	}
	public void setTrainer(boolean trainer) {
		this.trainer = trainer;
	}
	public boolean isCommute() {
		return commute;
	}
	public void setCommute(boolean commute) {
		this.commute = commute;
	}
	public boolean isManual() {
		return manual;
	}
	public void setManual(boolean manual) {
		this.manual = manual;
	}
	public boolean isFlagged() {
		return flagged;
	}
	public void setFlagged(boolean flagged) {
		this.flagged = flagged;
	}
	public String getGear_id() {
		return gear_id;
	}
	public void setGear_id(String gear_id) {
		this.gear_id = gear_id;
	}
	public float getAverage_speed() {
		return average_speed;
	}
	public void setAverage_speed(float average_speed) {
		this.average_speed = average_speed;
	}
	public float getMax_speed() {
		return max_speed;
	}
	public void setMax_speed(float max_speed) {
		this.max_speed = max_speed;
	}
	public float getAverage_cadence() {
		return average_cadence;
	}
	public void setAverage_cadence(float average_cadence) {
		this.average_cadence = average_cadence;
	}
	public int getAverage_temp() {
		return average_temp;
	}
	public void setAverage_temp(int average_temp) {
		this.average_temp = average_temp;
	}
	public float getAverage_watts() {
		return average_watts;
	}
	public void setAverage_watts(float average_watts) {
		this.average_watts = average_watts;
	}
	public float getKilojoules() {
		return kilojoules;
	}
	public void setKilojoules(float kilojoules) {
		this.kilojoules = kilojoules;
	}
	public float getAverage_heartrate() {
		return average_heartrate;
	}
	public void setAverage_heartrate(float average_heartrate) {
		this.average_heartrate = average_heartrate;
	}
	public float getMax_heartrate() {
		return max_heartrate;
	}
	public void setMax_heartrate(float max_heartrate) {
		this.max_heartrate = max_heartrate;
	}
	public float getCalories() {
		return calories;
	}
	public void setCalories(float calories) {
		this.calories = calories;
	}
	public int getTruncated() {
		return truncated;
	}
	public void setTruncated(int truncated) {
		this.truncated = truncated;
	}
	public boolean isHas_kudoed() {
		return has_kudoed;
	}
	public void setHas_kudoed(boolean has_kudoed) {
		this.has_kudoed = has_kudoed;
	}
	public BooleanProperty getIsTransfer() {
		return isTransfer;
	}
	
	public void setIsTransfer(boolean isStransfer) {
		isTransfer.set(isStransfer);
	}

	public String getElapseStr() {
		int[] el = Utils.getSecondsToHMS(elapsed_time);
		
		return "" + el[0] + ":" +  el[1] + ":" + el[2];
	}
	
	public void setElapseStr(String elapseStr) {
		int second = Utils.getSecondsFromElapseStr(elapseStr);
		elapsed_time = second;
		//return "" + el[0] + ":" +  el[1] + ":" + el[2];
	}


}
