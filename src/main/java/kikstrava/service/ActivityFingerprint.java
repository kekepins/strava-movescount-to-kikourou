package kikstrava.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import kikstrava.model.KikourouActivity;

public record ActivityFingerprint(LocalDate date, int elapsedSeconds, long distanceMeters) {

    public static ActivityFingerprint from(KikourouActivity activity) {
        LocalDateTime start = activity.getStartDateLocal();
        if (start == null) {
            throw new IllegalArgumentException("Activity start date is missing");
        }
        double distanceMeters = "kikourou".equals(activity.getSource())
                ? activity.getDistance() * 1000.0
                : activity.getDistance();
        return new ActivityFingerprint(
                start.toLocalDate(), activity.getElapsedTime(), Math.round(distanceMeters));
    }

    public boolean matches(ActivityFingerprint other) {
        return date.equals(other.date)
                && Math.abs((long) elapsedSeconds - other.elapsedSeconds) <= 60
                && Math.abs(distanceMeters - other.distanceMeters) <= 100;
    }

}
