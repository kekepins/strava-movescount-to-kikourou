package kikstrava.model;

public enum ActivityType {
	AlpineSki("alpineski", 36, 20),
	BackcountrySki("backcountryski", 31, 20), // FIXME
	Canoeing("canoe", 36, 20), // FIXME
	Crossfit("crossfit", 1, 90), // FIXME
	EBikeRide("ebikeride", 39, -1),
	Elliptical("eliptic", 1, -1), // FIXME
	Hike("hike", 1, 96),
	IceSkate("iceskate", 1, 70),
	InlineSkate("inlineskate", 1, 8),
	Kayaking("kayak", 1, -1), // FIXME 
	Kitesurf("kitesurf", 1, -1), // FIXME
	NordicSki("nordicski", 13, 22),
	Ride("ride", 3, 4),
	RockClimbing("rockclimbing", 1,-1), // FIXME
	RollerSki("rollerski", 5, 88),
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
    VTT("vtt", 4, 5),
    VirtualRide("virtualride", 3, 4),
    Walk("walk", 28, 12), 
    WeightTraining("weight", 33, -1),
    Windsurf("windsurf", 11, 86),
    Workout("workout", 33, -1), 
    Yoga("yoga", 30, -1);

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
	
	/**
	 * Mappe un typeKey Garmin (ex: "running", "trail_running", "cycling")
	 * vers un ActivityType. Retourne Run par defaut si inconnu.
	 */
	public static ActivityType fromGarminTypeKey(String typeKey) {
		if ( typeKey == null ) {
			return Run;
		}
		switch ( typeKey.toLowerCase() ) {
			case "running":
			case "street_running":
			case "track_running":
			case "treadmill_running":
			case "indoor_running":
				return Run;
			case "trail_running":
				return Trail;
			case "hiking":
			case "mountaineering":
				return Hike;
			case "walking":
			case "casual_walking":
			case "speed_walking":
				return Walk;
			case "cycling":
			case "road_biking":
			case "indoor_cycling":
			case "virtual_ride":
			case "commuting":
				return Ride;
			case "mountain_biking":
			case "cyclocross":
			case "gravel_cycling":
				return VTT;
			case "e_bike_fitness":
			case "e_bike_mountain":
				return EBikeRide;
			case "lap_swimming":
			case "swimming":
				return Swim;
			case "open_water_swimming":
				return SwimExt;
			case "resort_skiing_snowboarding":
			case "resort_skiing":
				return AlpineSki;
			case "backcountry_skiing_snowboarding":
			case "backcountry_skiing":
				return BackcountrySki;
			case "cross_country_skiing":
			case "skate_skiing":
				return NordicSki;
			case "snowboarding":
				return Snowboard;
			case "snowshoeing":
				return Snowshoe;
			case "rowing":
			case "indoor_rowing":
				return Rowing;
			case "stand_up_paddleboarding":
				return StandUpPaddling;
			case "kayaking":
				return Kayaking;
			case "strength_training":
			case "indoor_cardio":
				return WeightTraining;
			case "yoga":
				return Yoga;
			case "elliptical":
				return Elliptical;
			default:
				return Run;
		}
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