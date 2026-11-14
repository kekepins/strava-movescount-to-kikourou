package kikstrava.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Activite provenant de l'API personnelle Garmin Connect
 * (endpoint /activitylist-service/activities/search/activities).
 *
 * Mappe les champs JSON Garmin vers l'interface commune KikourouActivity,
 * sur le meme principe que StravaActivity.
 *
 * Exemple de champs renvoyes par Garmin :
 *   activityId, activityName, startTimeLocal ("yyyy-MM-dd HH:mm:ss"),
 *   distance (metres), duration (secondes, double), elevationGain (metres),
 *   activityType.typeKey ("running", "cycling", "trail_running", ...).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GarminActivity implements KikourouActivity {

    /** Format des dates Garmin : heure locale, sans fuseau. */
    private static final DateTimeFormatter GARMIN_DATE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private long activityId;
    private String activityName;
    private String startTimeLocal;
    private double distance;        // metres
    private double duration;        // secondes
    private double elapsedDuration; // secondes
    private double elevationGain;   // metres
    private GarminActivityType activityType;

    private final BooleanProperty isTransfer = new SimpleBooleanProperty(this, "isTransfer", true);

    /** Sous-objet activityType de la reponse Garmin. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GarminActivityType {
        private int typeId;
        private String typeKey;
        private int parentTypeId;

        public int getTypeId() {
            return typeId;
        }
        public void setTypeId(int typeId) {
            this.typeId = typeId;
        }
        public String getTypeKey() {
            return typeKey;
        }
        public void setTypeKey(String typeKey) {
            this.typeKey = typeKey;
        }
        public int getParentTypeId() {
            return parentTypeId;
        }
        public void setParentTypeId(int parentTypeId) {
            this.parentTypeId = parentTypeId;
        }
    }

    // --- champs bruts (desserialisation Jackson) ---

    public long getActivityId() {
        return activityId;
    }
    public void setActivityId(long activityId) {
        this.activityId = activityId;
    }
    public String getActivityName() {
        return activityName;
    }
    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }
    public String getStartTimeLocal() {
        return startTimeLocal;
    }
    public void setStartTimeLocal(String startTimeLocal) {
        this.startTimeLocal = startTimeLocal;
    }
    public double getDuration() {
        return duration;
    }
    public void setDuration(double duration) {
        this.duration = duration;
    }
    public double getElapsedDuration() {
        return elapsedDuration;
    }
    public void setElapsedDuration(double elapsedDuration) {
        this.elapsedDuration = elapsedDuration;
    }
    public double getElevationGain() {
        return elevationGain;
    }
    public void setElevationGain(double elevationGain) {
        this.elevationGain = elevationGain;
    }
    public GarminActivityType getActivityType() {
        return activityType;
    }
    public void setActivityType(GarminActivityType activityType) {
        this.activityType = activityType;
    }

    // --- implementation de KikourouActivity ---

    @Override
    public String getStartDateLocalRead() {
        LocalDateTime ld = getStartDateLocal();
        if (ld == null) {
            return "";
        }
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(ld);
    }

    @Override
    public LocalDateTime getStartDateLocal() {
        if (startTimeLocal == null || startTimeLocal.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(startTimeLocal, GARMIN_DATE);
    }

    @Override
    public int getElapsedTime() {
        // duration (temps total) est l'equivalent de l'elapsed_time de Strava.
        return (int) Math.round(duration);
    }

    @Override
    public float getDPlus() {
        return (float) elevationGain;
    }

    @Override
    public void setDPlus(float dplus) {
        this.elevationGain = dplus;
    }

    @Override
    public float getDistance() {
        return (float) distance;
    }

    @Override
    public void setDistance(float distance) {
        this.distance = distance;
    }

    @Override
    public int getCode() {
        return getType().getKikCode();
    }

    @Override
    public String getActivity() {
        return getType().getCode();
    }

    @Override
    public String getName() {
        return activityName;
    }

    @Override
    public void setName(String name) {
        this.activityName = name;
    }

    @Override
    public BooleanProperty getIsTransfer() {
        return isTransfer;
    }

    @Override
    public void setIsTransfer(boolean isStransfer) {
        isTransfer.set(isStransfer);
    }

    public String getElapseStr() {
        int[] el = Utils.getSecondsToHMS(getElapsedTime());
        return "" + el[0] + ":" + el[1] + ":" + el[2];
    }

    @Override
    public void setElapseStr(String newValue) {
        this.duration = Utils.getSecondsFromElapseStr(newValue);
    }

    @Override
    public String getUrl() {
        return "https://connect.garmin.com/modern/activity/" + activityId;
    }

    @Override
    public String getSource() {
        return "Garmin";
    }

    /** Type d'activite kikourou derive du typeKey Garmin. */
    private ActivityType getType() {
        String typeKey = activityType != null ? activityType.getTypeKey() : null;
        return ActivityType.fromGarminTypeKey(typeKey);
    }
}
