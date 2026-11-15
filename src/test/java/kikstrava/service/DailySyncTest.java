package kikstrava.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import kikstrava.model.KikourouActivityImpl;
import kikstrava.model.StravaTokenInfo;

public class DailySyncTest {

    @Test
    public void allowsDistinctActivitiesOnTheSameDateAndSkipsMatchingOnes() {
        List<ActivityFingerprint> knownActivities = new ArrayList<>();
        LocalDate date = LocalDate.of(2026, 11, 14);
        KikourouActivityImpl existing = new KikourouActivityImpl(date, "Run", 10.0f, 3600);
        KikourouActivityImpl duplicate = new KikourouActivityImpl(date, "Run", 10.04f, 3600);
        KikourouActivityImpl secondSession = new KikourouActivityImpl(date, "Run", 5.0f, 1800);
        ActivityFingerprint existingFingerprint = ActivityFingerprint.from(existing);

        assertTrue(DailySync.markFingerprintIfNew(existingFingerprint, knownActivities));
        knownActivities.add(existingFingerprint);
        assertFalse(DailySync.markFingerprintIfNew(
                ActivityFingerprint.from(duplicate), knownActivities));
        assertTrue(DailySync.markFingerprintIfNew(
                ActivityFingerprint.from(secondSession), knownActivities));
    }

    @Test
    public void countsOnlySubmissionsFoundInKikourouAfterSync() {
        ActivityFingerprint saved =
                new ActivityFingerprint(LocalDate.of(2026, 11, 14), 3600, 10000);
        ActivityFingerprint missing =
                new ActivityFingerprint(LocalDate.of(2026, 11, 13), 1800, 5000);

        assertEquals(1, DailySync.countVerified(
                List.of(saved, missing), List.of(saved), List.of(saved, missing)));
    }

    @Test
    public void selectsRequestedSyncSource() throws Exception {
        assertEquals(DailySync.Source.GARMIN,
                DailySync.sourceFromArgs(new String[] {"--sync", "--source=garmin"}));
        assertEquals(DailySync.Source.STRAVA,
                DailySync.sourceFromArgs(new String[] {"--sync", "--source", "strava"}));
        assertEquals(DailySync.Source.GARMIN,
                DailySync.sourceFromArgs(new String[] {"--sync"}));
    }

    @Test(expected = ServiceException.class)
    public void rejectsUnknownSyncSource() throws Exception {
        DailySync.sourceFromArgs(new String[] {"--sync", "--source=suunto"});
    }

    @Test
    public void stravaAuthorizationUrlDoesNotExposeClientSecret() {
        StravaV3Service service = new StravaV3Service("client-id", "secret-value");

        assertTrue(service.getAuthUrl().contains("client_id=client-id"));
        assertFalse(service.getAuthUrl().contains("secret-value"));
    }

    @Test
    public void tokenStringDoesNotExposeTokenValues() {
        StravaTokenInfo token = new StravaTokenInfo();
        token.setAccess_token("access-secret");
        token.setRefresh_token("refresh-secret");

        assertFalse(token.toString().contains("access-secret"));
        assertFalse(token.toString().contains("refresh-secret"));
    }
}
