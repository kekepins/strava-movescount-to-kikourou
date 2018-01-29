package kikstrava.model;

public enum StravaActivityType {
    Ride("ride", 3), 
    Run("run", 1), 
    Swim("swim", 10), 
    Workout("workout", 1), 
    Hike("hike", 1), 
    Walk("walk", 1), 
    NordicSki("NordicSki", 13), 
    Alpineski("alpineski", 36), 
    Iceskate("iceskate", 1), 
    Inlineskate("inlineskate", 1), 
    Rollerski("rollerski", 1), 
    Windsurf("windsurf", 1), 
    Snowboard("snowboard", 1), 
    Snowshoe("snowshoe", 1), 
    Ebikeride("ebikeride", 1), 
    Virtualride("virtualride", 1);

    private String code;
    private int kikCode;

    private StravaActivityType(String value, int kikCode) {
    	this.code = value;
    	this.kikCode = kikCode;
    }

    public String getCode() {
    	return this.code;
    }
    
    public int getKikCode() {
    	return this.kikCode;
    }
}