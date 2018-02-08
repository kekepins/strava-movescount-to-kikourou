package kikstrava.model;

public enum ActivityType {
    Ride("ride", 3, 4), 
    Run("run", 1, 3), 
    Trail("trail", 25, 82),
    Swim("swim", 10, 6), 
    SwimExt("swim ext", 10, 83),
    Workout("workout", 1, -1), 
    Hike("hike", 1, 96), 
    Walk("walk", 28, 12), 
    NordicSki("NordicSki", 13, 22), 
    Alpineski("alpineski", 36, 20), 
    Iceskate("iceskate", 1, 70), 
    Inlineskate("inlineskate", 1, 8), 
    Rollerski("rollerski", 5, 88), 
    Windsurf("windsurf", 1, 86), 
    Snowboard("snowboard", 36, 21), 
    Snowshoe("snowshoe", 36, 85), 
    Ebikeride("ebikeride", 1, -1), 
    Virtualride("virtualride", 1, -1);

    private String code;
    private int kikCode;
    private int movescountCode;

    private ActivityType(String value, int kikCode, int movescountCode) {
    	this.code = value;
    	this.kikCode = kikCode;
    	this.movescountCode = movescountCode;
    }

    public String getCode() {
    	return this.code;
    }
    
    public int getKikCode() {
    	return this.kikCode;
    }

	public int getMovescountCode() {
		return movescountCode;
	}

	public void setMovescountCode(int movescountCode) {
		this.movescountCode = movescountCode;
	}
	
	public static ActivityType fromMovescountCode(int code) {
		for ( ActivityType activityType : values()) {
			if ( code == activityType.getMovescountCode()) {
				return activityType;
			}
		}
		
		return Run;
	}
}