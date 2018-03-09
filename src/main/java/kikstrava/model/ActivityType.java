package kikstrava.model;

public enum ActivityType {
	Alpineski("alpineski", 36, 20),
	BackcountrySki("backcountryski", 36, 20), // FIXME
	Canoeing("canoe", 36, 20), // FIXME
	Crossfit("crossfit", 1, 3), // FIXME
	Ebikeride("ebikeride", 1, -1),
	Elliptical("eliptic", 1, -1), // FIXME
	Hike("hike", 1, 96),
	Iceskate("iceskate", 1, 70),
	Inlineskate("inlineskate", 1, 8),
	Kayaking("kayak", 1, -1), // FIXME 
	Kitesurf("kitesurf", 1, -1), // FIXME
	NordicSki("NordicSki", 13, 22),
	Ride("ride", 3, 4),
	RockClimbing("rockclimbing", 1,-1), // FIXME
	Rollerski("rollerski", 5, 88),
	Rowing("rameur", 1, -1),
    Run("run", 1, 3), 
    Snowboard("snowboard", 36, 21), 
    Snowshoe("raquette",  36, 85), 
    StairStepper("stairstepper", 1, -1),
    StandUpPaddling("standup", 1, -1),
    Surfing("surfing", 1, -1),
    Trail("trail", 25, 82),
    Swim("swim", 10, 6), 
    SwimExt("swim ext", 10, 83),
    VirtualRide("virtualride", 3, 4),
    Walk("walk", 28, 12), 
    WeightTraining("weight", 1, -1),
    Windsurf("windsurf", 1, 86),
    Workout("workout", 1, -1), 
    Yoga("yoga", 1, -1);
	

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