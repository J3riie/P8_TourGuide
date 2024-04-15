package com.openclassrooms.tourguide.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.openclassrooms.tourguide.attraction.NearbyAttraction;
import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.tracker.Tracker;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tripPricer.Provider;
import tripPricer.TripPricer;

@Service
public class TourGuideService {
    private final Logger logger = LoggerFactory.getLogger(TourGuideService.class);

    private final GpsUtil gpsUtil;

    private final RewardsService rewardsService;

    private final TripPricer tripPricer = new TripPricer();

    public final Tracker tracker;

    boolean testMode = true;

    public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService) {
        this.gpsUtil = gpsUtil;
        this.rewardsService = rewardsService;

        Locale.setDefault(Locale.US);

        if (testMode) {
            logger.info("TestMode enabled");
            logger.debug("Initializing users");
            initializeInternalUsers();
            logger.debug("Finished initializing users");
        }
        tracker = new Tracker(this);
        addShutDownHook();
    }

    public List<UserReward> getUserRewards(User user) {
        return user.getUserRewards();
    }

    public VisitedLocation getUserLocation(User user) {
        return (!user.getVisitedLocations().isEmpty()) ? user.getLastVisitedLocation() : trackUserLocation(user);
    }

    public User getUser(String userName) {
        return internalUserMap.get(userName);
    }

    public List<User> getAllUsers() {
        return internalUserMap.values().stream().toList();
    }

    public void addUser(User user) {
        if (!internalUserMap.containsKey(user.getUserName())) {
            internalUserMap.put(user.getUserName(), user);
        }
    }

    public List<Provider> getTripDeals(User user) {
        final int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
        final List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(),
                user.getUserPreferences().getNumberOfAdults(), user.getUserPreferences().getNumberOfChildren(),
                user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
        user.setTripDeals(providers);
        return providers;
    }

    public VisitedLocation trackUserLocation(User user) {
        final VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
        user.addToVisitedLocations(visitedLocation);
        rewardsService.calculateRewards(user);
        return visitedLocation;
    }

    public List<NearbyAttraction> getNearByAttractions(VisitedLocation visitedLocation) {
        final Map<Attraction, Double> attractionDistances = calculateDistanceOfEachAttraction(visitedLocation);
        final List<Attraction> sortedAttractions = sortAttractionsByDistance(attractionDistances);
        final List<Attraction> closestAttractions = selectFiveClosestAttractions(sortedAttractions);
        return getNearbyAttractionsDTO(visitedLocation, closestAttractions);
    }

    private Map<Attraction, Double> calculateDistanceOfEachAttraction(VisitedLocation visitedLocation) {
        final Map<Attraction, Double> attractionDistances = new HashMap<>();
        for (final Attraction attraction : gpsUtil.getAttractions()) {
            final double distance = rewardsService.getDistance(visitedLocation.location, attraction);
            attractionDistances.put(attraction, distance);
        }
        return attractionDistances;
    }

    private List<Attraction> sortAttractionsByDistance(Map<Attraction, Double> attractionDistances) {
        final List<Attraction> sortedAttractions = new ArrayList<>(attractionDistances.keySet());
        Collections.sort(sortedAttractions,
                (a1, a2) -> Double.compare(attractionDistances.get(a1), attractionDistances.get(a2)));
        return sortedAttractions;
    }

    private List<Attraction> selectFiveClosestAttractions(List<Attraction> sortedAttractions) {
        final List<Attraction> closestAttractions = new ArrayList<>();
        for (int i = 0; i < Math.min(5, sortedAttractions.size()); i++) {
            closestAttractions.add(sortedAttractions.get(i));
        }
        return closestAttractions;
    }

    private List<NearbyAttraction> getNearbyAttractionsDTO(VisitedLocation visitedLocation,
            final List<Attraction> closestAttractions) {
        final List<NearbyAttraction> nearbyAttractions = new ArrayList<>();
        for (final Attraction attraction : closestAttractions) {
            final double distance = rewardsService.getDistance(new Location(attraction.latitude, attraction.longitude),
                    visitedLocation.location);
            final int rewardPoints = rewardsService.getRewardPoints(attraction.attractionId, visitedLocation.userId);
            final NearbyAttraction nearbyAttraction = new NearbyAttraction(attraction, visitedLocation, distance,
                    rewardPoints);
            nearbyAttractions.add(nearbyAttraction);
        }
        return nearbyAttractions;
    }

    private void addShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                tracker.stopTracking();
            }
        });
    }

    /**********************************************************************************
     * 
     * Methods Below: For Internal Testing
     * 
     **********************************************************************************/
    private static final String tripPricerApiKey = "test-server-api-key";

    // Database connection will be used for external users, but for testing purposes
    // internal users are provided and stored in memory
    private final Map<String, User> internalUserMap = new HashMap<>();

    private void initializeInternalUsers() {
        IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
            final String userName = "internalUser" + i;
            final String phone = "000";
            final String email = userName + "@tourGuide.com";
            final User user = new User(UUID.randomUUID(), userName, phone, email);
            generateUserLocationHistory(user);

            internalUserMap.put(userName, user);
        });
        logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
    }

    private void generateUserLocationHistory(User user) {
        IntStream.range(0, 3).forEach(i -> {
            user.addToVisitedLocations(new VisitedLocation(user.getUserId(),
                    new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
        });
    }

    private double generateRandomLongitude() {
        final double leftLimit = -180;
        final double rightLimit = 180;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    private double generateRandomLatitude() {
        final double leftLimit = -85.05112878;
        final double rightLimit = 85.05112878;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    private Date getRandomTime() {
        final LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
        return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
    }

}
