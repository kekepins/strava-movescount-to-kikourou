package kikstrava.model;

import java.time.LocalDateTime;

import javafx.beans.property.BooleanProperty;

public interface KikourouActivity {

	String getStartDateLocalRead();
	LocalDateTime getStartDateLocal();

	int getElapsedTime();

	float getDPlus();
	void setDPlus(float dplus);

	float getDistance();
	void setDistance(float distance);

	int getCode();

	String getActivity();

	String getName();
	void setName(String name);

	BooleanProperty getIsTransfer();
	void setIsTransfer(boolean isStransfer);

	void setElapseStr(String newValue);
	
	String getUrl();
	
	String getSource();
	

}
