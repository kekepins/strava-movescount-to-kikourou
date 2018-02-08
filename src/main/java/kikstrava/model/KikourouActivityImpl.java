package kikstrava.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javafx.beans.property.BooleanProperty;

public class KikourouActivityImpl implements KikourouActivity {
	
	private float distance;
	private LocalDateTime startDateTime;
	private String desc;
	private int elapse;
	
	public KikourouActivityImpl(LocalDate startDate, String desc, float distance, int elapse) {
		
		this.distance = distance;
		this.startDateTime = startDate.atStartOfDay();
		this.elapse = elapse;
		this.desc = desc;
	}
	

	@Override
	public String getStartDateLocalRead() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LocalDateTime getStartDateLocal() {
		return startDateTime;
	}

	@Override
	public int getElapsedTime() {
		return elapse;
	}

	@Override
	public float getDPlus() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setDPlus(float dplus) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public float getDistance() {
		return this.distance;
	}

	@Override
	public void setDistance(float distance) {
		this.distance = distance;
		
	}

	@Override
	public int getCode() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getActivity() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return desc;
	}

	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public BooleanProperty getIsTransfer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setIsTransfer(boolean isStransfer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setElapseStr(String newValue) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public String getUrl() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String getSource() {
		return "kikourou";
	}

}
