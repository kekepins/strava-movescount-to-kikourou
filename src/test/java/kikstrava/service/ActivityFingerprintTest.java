package kikstrava.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;

import org.junit.Test;

import kikstrava.model.GarminActivity;
import kikstrava.model.KikourouActivityImpl;
import kikstrava.model.StravaActivity;

public class ActivityFingerprintTest {

    @Test
    public void matchesSameActivityAcrossKikourouAndGarminUnits() {
        LocalDate date = LocalDate.of(2026, 11, 14);
        KikourouActivityImpl saved = new KikourouActivityImpl(date, "Run", 10.0f, 3600);

        GarminActivity source = new GarminActivity();
        source.setStartTimeLocal("2026-11-14 08:00:00");
        source.setDistance(10050);
        source.setDuration(3630);

        assertTrue(ActivityFingerprint.from(saved).matches(ActivityFingerprint.from(source)));
    }

    @Test
    public void doesNotMatchDifferentWorkoutOnTheSameDate() {
        LocalDate date = LocalDate.of(2026, 11, 14);
        KikourouActivityImpl saved = new KikourouActivityImpl(date, "Run", 10.0f, 3600);
        KikourouActivityImpl otherWorkout = new KikourouActivityImpl(date, "Run", 5.0f, 1800);

        assertFalse(ActivityFingerprint.from(saved).matches(
                ActivityFingerprint.from(otherWorkout)));
    }

    @Test
    public void matchesSameStravaWorkoutWithKikourouValues() {
        LocalDate date = LocalDate.of(2026, 11, 14);
        KikourouActivityImpl saved = new KikourouActivityImpl(date, "Run", 10.0f, 3600);

        StravaActivity source = new StravaActivity();
        source.setStart_date_local("2026-11-14T08:00:00Z");
        source.setDistance(10050);
        source.setElapsed_time(3600);

        assertTrue(ActivityFingerprint.from(saved).matches(ActivityFingerprint.from(source)));
    }
}
