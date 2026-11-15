package kikstrava.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import kikstrava.model.Config;
import kikstrava.model.GarminActivity;
import kikstrava.model.KikourouActivity;
import kikstrava.model.StravaActivity;

public final class DailySync {

    private static final int LOOKBACK_DAYS = 30;
    private static final int MAX_ACTIVITIES_PER_PAGE = 100;
    private static final int MAX_GARMIN_ACTIVITIES = 500;

    private DailySync() {
    }

    public static void run() throws Exception {
        run(Source.GARMIN);
    }

    public static Source sourceFromArgs(String[] args) throws ServiceException {
        if (args.length == 1) {
            return Source.GARMIN;
        }
        if (args.length == 2 && args[1].startsWith("--source=")) {
            return Source.parse(args[1].substring("--source=".length()));
        }
        if (args.length == 3 && "--source".equals(args[1])) {
            return Source.parse(args[2]);
        }
        throw new ServiceException("Usage: --sync [--source=garmin|strava|both]");
    }

    public static void run(Source source) throws Exception {
        Objects.requireNonNull(source, "source");
        ConfigManager.init();
        ConfigManager.applyProxySettings();

        Config config = Config.getConfig();
        if (source == Source.GARMIN && !config.isGarminOK()) {
            throw new ServiceException("Garmin was selected but is not configured");
        }
        if (source == Source.STRAVA && !config.isStravaOK()) {
            throw new ServiceException("Strava was selected but is not configured");
        }

        System.out.println("Sync source: " + source.name().toLowerCase(Locale.ROOT));
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(LOOKBACK_DAYS - 1);
        KikourouService kikourouService = new KikourouService(
                config.getKikUser(), config.getKikPassword());
        List<KikourouActivity> existingActivities =
                kikourouService.searchActivitiesInRange(startDate, endDate);
        List<ActivityFingerprint> initialActivities = new ArrayList<>();
        for (KikourouActivity activity : existingActivities) {
            initialActivities.add(ActivityFingerprint.from(activity));
        }
        List<ActivityFingerprint> knownActivities = new ArrayList<>(initialActivities);
        int existingActivityCount = existingActivities.size();

        int submittedCount = 0;
        boolean failed = false;
        List<ActivityFingerprint> submittedActivities = new ArrayList<>();

        if (source != Source.GARMIN && config.isStravaOK()) {
            try {
                StravaV3Service stravaService =
                        new StravaV3Service(config.getStravaClientId(), config.getStravaSecret());
                if (!stravaService.isAuthenticated()) {
                    throw new ServiceException(
                            "Authorize Strava once in the desktop app before enabling daily sync");
                }
                int found = 0;
                int count = 0;
                for (int page = 1; ; page++) {
                    StravaActivity[] activities = stravaService.searchActivities(
                            startDate, endDate, page, MAX_ACTIVITIES_PER_PAGE);
                    if (activities == null) {
                        throw new ServiceException("Strava returned no activity list");
                    }
                    found += activities.length;
                    int pageImported = importNewActivities(
                            activities, knownActivities, submittedActivities,
                            startDate, endDate, kikourouService);
                    count += pageImported;
                    submittedCount += pageImported;
                    if (activities.length < MAX_ACTIVITIES_PER_PAGE) {
                        break;
                    }
                }
                System.out.printf("Strava: %d found, %d submitted.%n", found, count);
            } catch (Exception e) {
                failed = true;
                System.err.println("Strava sync failed: " + e.getMessage());
            }
        } else if (source == Source.GARMIN) {
            System.out.println("Strava was not selected; skipping.");
        } else {
            System.out.println("Strava is not configured; skipping.");
        }

        if (source != Source.STRAVA && config.isGarminOK()) {
            try {
                GarminService garminService = new GarminService(config.getGarminApiKey());
                GarminActivity[] activities = garminService.searchActivities(
                        startDate, endDate, MAX_GARMIN_ACTIVITIES);
                if (activities == null) {
                    throw new ServiceException("Garmin returned no activity list");
                }
                int count = importNewActivities(
                        activities, knownActivities, submittedActivities,
                        startDate, endDate, kikourouService);
                submittedCount += count;
                System.out.printf("Garmin: %d found, %d submitted.%n", activities.length, count);
            } catch (Exception e) {
                failed = true;
                System.err.println("Garmin sync failed: " + e.getMessage());
            }
        } else if (source == Source.STRAVA) {
            System.out.println("Garmin was not selected; skipping.");
        } else {
            System.out.println("Garmin is not configured; skipping.");
        }

        System.out.printf("Daily sync complete: %d submitted, %d existing Kikourou activities.%n",
                submittedCount, existingActivityCount);
        try {
            List<ActivityFingerprint> savedActivities = new ArrayList<>();
            for (KikourouActivity activity
                    : kikourouService.searchActivitiesInRange(startDate, endDate)) {
                savedActivities.add(ActivityFingerprint.from(activity));
            }
            int verified = countVerified(submittedActivities, initialActivities, savedActivities);
            System.out.printf("Kikourou verification: %d of %d submitted activities found.%n",
                    verified, submittedActivities.size());
            if (verified < submittedActivities.size()) {
                failed = true;
                System.err.println("Some submitted activities were not found in the Kikourou calendar.");
            }
        } catch (Exception e) {
            failed = true;
            System.err.println("Kikourou verification failed: " + e.getMessage());
        }
        if (failed) {
            throw new ServiceException("Daily sync was incomplete; see the source errors above");
        }
    }

    public static void testImportLatestThreeGarmin() throws Exception {
        ConfigManager.init();
        ConfigManager.applyProxySettings();

        Config config = Config.getConfig();
        if (!config.isGarminOK()) {
            throw new ServiceException("Garmin is not configured");
        }

        KikourouService kikourouService = new KikourouService(
                config.getKikUser(), config.getKikPassword());
        GarminService garminService = new GarminService(config.getGarminApiKey());
        GarminActivity[] activities = garminService.searchActivities(null, LocalDate.now(),
                MAX_GARMIN_ACTIVITIES);
        if (activities == null || activities.length == 0) {
            throw new ServiceException("Garmin returned no activities for the test");
        }

        List<GarminActivity> latestActivities = Arrays.stream(activities)
                .filter(activity -> activity.getStartDateLocal() != null)
                .sorted(Comparator.comparing(GarminActivity::getStartDateLocal).reversed())
                .limit(3)
                .toList();
        List<KikourouActivity> existingActivities =
                searchKikourouForActivities(kikourouService, latestActivities);
        List<ActivityFingerprint> initialActivities = new ArrayList<>();
        for (KikourouActivity activity : existingActivities) {
            initialActivities.add(ActivityFingerprint.from(activity));
        }
        List<ActivityFingerprint> knownActivities = new ArrayList<>(initialActivities);

        List<ActivityFingerprint> submittedActivities = new ArrayList<>();
        int alreadyPresent = 0;
        String submissionFailure = null;
        for (GarminActivity activity : latestActivities) {
            ActivityFingerprint fingerprint = ActivityFingerprint.from(activity);
            if (!markFingerprintIfNew(fingerprint, knownActivities)) {
                alreadyPresent++;
                continue;
            }

            try {
                kikourouService.addActivity(activity);
                knownActivities.add(fingerprint);
                submittedActivities.add(fingerprint);
                System.out.printf("Test import submitted: %s.%n", fingerprint.date());
            } catch (Exception e) {
                submissionFailure = e.getMessage();
                System.err.printf("Test import failed for %s: %s%n",
                        fingerprint.date(), submissionFailure);
                break;
            }
        }

        List<ActivityFingerprint> savedActivities = new ArrayList<>();
        for (KikourouActivity activity
                : searchKikourouForActivities(kikourouService, latestActivities)) {
            savedActivities.add(ActivityFingerprint.from(activity));
        }
        int verified = countVerified(submittedActivities, initialActivities, savedActivities);
        System.out.printf(
                "Three-activity test: %d latest activities checked, %d already present, "
                        + "%d submitted, %d verified by Kikourou.%n",
                latestActivities.size(), alreadyPresent, submittedActivities.size(), verified);

        if (submissionFailure != null || verified != submittedActivities.size()) {
            throw new ServiceException("Test import was not fully verified; see the errors above");
        }
    }

    public static void verifyLatestThreeGarmin() throws Exception {
        ConfigManager.init();
        ConfigManager.applyProxySettings();

        Config config = Config.getConfig();
        if (!config.isGarminOK()) {
            throw new ServiceException("Garmin is not configured");
        }

        GarminService garminService = new GarminService(config.getGarminApiKey());
        GarminActivity[] activities = garminService.searchActivities(null, LocalDate.now(),
                MAX_GARMIN_ACTIVITIES);
        if (activities == null || activities.length == 0) {
            throw new ServiceException("Garmin returned no activities for verification");
        }
        List<GarminActivity> latestActivities = Arrays.stream(activities)
                .filter(activity -> activity.getStartDateLocal() != null)
                .sorted(Comparator.comparing(GarminActivity::getStartDateLocal).reversed())
                .limit(3)
                .toList();

        KikourouService kikourouService = new KikourouService(
                config.getKikUser(), config.getKikPassword());
        List<ActivityFingerprint> savedActivities = new ArrayList<>();
        for (KikourouActivity activity
                : searchKikourouForActivities(kikourouService, latestActivities)) {
            savedActivities.add(ActivityFingerprint.from(activity));
        }
        List<ActivityFingerprint> unmatchedSaved = new ArrayList<>(savedActivities);

        int verified = 0;
        for (GarminActivity activity : latestActivities) {
            ActivityFingerprint fingerprint = ActivityFingerprint.from(activity);
            int matchIndex = findMatch(unmatchedSaved, fingerprint);
            if (matchIndex >= 0) {
                unmatchedSaved.remove(matchIndex);
                verified++;
                System.out.printf("Verified in Kikourou: %s.%n", fingerprint.date());
            } else {
                List<ActivityFingerprint> sameDateActivities = savedActivities.stream()
                        .filter(saved -> saved.date().equals(fingerprint.date()))
                        .toList();
                System.out.printf(
                        "Not found in Kikourou: %s (%d same-date records; Garmin %ds/%dm).%n",
                        fingerprint.date(), sameDateActivities.size(),
                        fingerprint.elapsedSeconds(), fingerprint.distanceMeters());
                sameDateActivities.stream().limit(5).forEach(saved ->
                        System.out.printf("  Kikourou record: %ds/%dm.%n",
                                saved.elapsedSeconds(), saved.distanceMeters()));
            }
        }
        System.out.printf("Read-only verification: %d of %d latest activities found.%n",
                verified, latestActivities.size());
        if (verified != latestActivities.size()) {
            throw new ServiceException("Some latest Garmin activities are still missing from Kikourou");
        }
    }

    public static void verifyLatestThreeStrava() throws Exception {
        verifyLatestStrava(3);
    }

    public static void verifyLatestStrava(int activityCount) throws Exception {
        if (activityCount < 1 || activityCount > MAX_ACTIVITIES_PER_PAGE) {
            throw new ServiceException("Verification count must be between 1 and 100");
        }
        ConfigManager.init();
        ConfigManager.applyProxySettings();

        Config config = Config.getConfig();
        if (!config.isStravaOK()) {
            throw new ServiceException("Strava is not configured");
        }

        StravaV3Service stravaService =
                new StravaV3Service(config.getStravaClientId(), config.getStravaSecret());
        if (!stravaService.isAuthenticated()) {
            throw new ServiceException("Strava is not authenticated in this installation");
        }

        StravaActivity[] activities = stravaService.searchActivities(
                null, LocalDate.now(), 1, MAX_ACTIVITIES_PER_PAGE);
        if (activities == null || activities.length == 0) {
            throw new ServiceException("Strava returned no activities for verification");
        }
        List<StravaActivity> latestActivities = Arrays.stream(activities)
                .filter(activity -> activity.getStartDateLocal() != null)
                .sorted(Comparator.comparing(StravaActivity::getStartDateLocal).reversed())
                .limit(activityCount)
                .toList();
        if (latestActivities.size() != activityCount) {
            throw new ServiceException(
                    "Strava returned fewer dated activities than requested");
        }

        KikourouService kikourouService = new KikourouService(
                config.getKikUser(), config.getKikPassword());
        List<KikourouActivity> existingActivities =
                searchKikourouForActivities(kikourouService, latestActivities);
        List<ActivityFingerprint> unmatched = new ArrayList<>();
        for (KikourouActivity activity : existingActivities) {
            unmatched.add(ActivityFingerprint.from(activity));
        }

        int found = 0;
        for (StravaActivity activity : latestActivities) {
            ActivityFingerprint fingerprint = ActivityFingerprint.from(activity);
            int matchIndex = findMatch(unmatched, fingerprint);
            if (matchIndex >= 0) {
                unmatched.remove(matchIndex);
                found++;
                System.out.printf("Found in Kikourou: %s %ds/%dm.%n",
                        fingerprint.date(), fingerprint.elapsedSeconds(),
                        fingerprint.distanceMeters());
            } else {
                List<ActivityFingerprint> sameDate = existingActivities.stream()
                        .map(ActivityFingerprint::from)
                        .filter(saved -> saved.date().equals(fingerprint.date()))
                        .toList();
                System.out.printf("Not found in Kikourou: %s %ds/%dm; %d same-date records.%n",
                        fingerprint.date(), fingerprint.elapsedSeconds(),
                        fingerprint.distanceMeters(), sameDate.size());
                sameDate.stream().limit(5).forEach(saved ->
                        System.out.printf("  Kikourou record: %ds/%dm.%n",
                                saved.elapsedSeconds(), saved.distanceMeters()));
            }
        }
        System.out.printf("Read-only Strava verification: %d of %d latest activities found.%n",
                found, activityCount);
        if (found != activityCount) {
            throw new ServiceException("Some latest Strava activities are missing from Kikourou");
        }
    }

    private static int importNewActivities(
            KikourouActivity[] activities,
            List<ActivityFingerprint> knownActivities,
            List<ActivityFingerprint> submittedActivities,
            LocalDate startDate,
            LocalDate endDate,
            KikourouService kikourouService) throws Exception {
        int imported = 0;
        if (activities == null) {
            throw new ServiceException("Activity source returned no activity list");
        }

        for (KikourouActivity activity : activities) {
            LocalDateTime start = activity.getStartDateLocal();
            if (start == null) {
                throw new ServiceException("Activity has no start date");
            }

            LocalDate date = start.toLocalDate();
            if (date.isBefore(startDate) || date.isAfter(endDate)) {
                continue;
            }
            ActivityFingerprint fingerprint = ActivityFingerprint.from(activity);
            if (!markFingerprintIfNew(fingerprint, knownActivities)) {
                continue;
            }

            knownActivities.add(fingerprint);
            kikourouService.addActivity(activity);
            submittedActivities.add(fingerprint);
            imported++;
        }
        return imported;
    }

    private static List<KikourouActivity> searchKikourouForActivities(
            KikourouService kikourouService, List<? extends KikourouActivity> activities)
            throws Exception {
        LocalDate startDate = activities.stream()
                .map(KikourouActivity::getStartDateLocal)
                .filter(date -> date != null)
                .map(date -> date.toLocalDate())
                .min(LocalDate::compareTo)
                .orElseThrow(() -> new ServiceException("No activity dates are available"));
        LocalDate endDate = activities.stream()
                .map(KikourouActivity::getStartDateLocal)
                .filter(date -> date != null)
                .map(date -> date.toLocalDate())
                .max(LocalDate::compareTo)
                .orElseThrow(() -> new ServiceException("No activity dates are available"));
        return kikourouService.searchActivitiesInRange(startDate, endDate);
    }

    static boolean markFingerprintIfNew(
            ActivityFingerprint candidateFingerprint, List<ActivityFingerprint> knownActivities) {
        return knownActivities.stream().noneMatch(
                existing -> existing.matches(candidateFingerprint));
    }

    static int countVerified(
            List<ActivityFingerprint> submitted,
            List<ActivityFingerprint> initial,
            List<ActivityFingerprint> saved) {
        List<ActivityFingerprint> newActivities = new ArrayList<>(saved);
        removeMatches(newActivities, initial);

        int verified = 0;
        for (ActivityFingerprint activity : submitted) {
            int matchingIndex = findMatch(newActivities, activity);
            if (matchingIndex >= 0) {
                newActivities.remove(matchingIndex);
                verified++;
            }
        }
        return verified;
    }

    private static void removeMatches(
            List<ActivityFingerprint> remaining, List<ActivityFingerprint> existing) {
        for (ActivityFingerprint activity : existing) {
            int matchingIndex = findMatch(remaining, activity);
            if (matchingIndex >= 0) {
                remaining.remove(matchingIndex);
            }
        }
    }

    private static int findMatch(
            List<ActivityFingerprint> activities, ActivityFingerprint candidate) {
        for (int i = 0; i < activities.size(); i++) {
            if (activities.get(i).matches(candidate)) {
                return i;
            }
        }
        return -1;
    }

    public enum Source {
        BOTH,
        STRAVA,
        GARMIN;

        private static Source parse(String value) throws ServiceException {
            try {
                return Source.valueOf(value.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw new ServiceException("Source must be garmin, strava, or both");
            }
        }
    }
}
