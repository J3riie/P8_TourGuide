package com.openclassrooms.tourguide.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;

@Service
public class RewardsService {

    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

    private static final int DEFAULT_PROXIMITY_BUFFER = 10;

    private int proximityBuffer = DEFAULT_PROXIMITY_BUFFER;

    private static final int ATTRACTION_PROXIMITY_RANGE = 200;

    private final GpsUtil gpsUtil;

    private final RewardCentral rewardsCentral;

    public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
        this.gpsUtil = gpsUtil;
        this.rewardsCentral = rewardCentral;
    }

    public void setProximityBuffer(int proximityBuffer) {
        this.proximityBuffer = proximityBuffer;
    }

    public void setDefaultProximityBuffer() {
        proximityBuffer = DEFAULT_PROXIMITY_BUFFER;
    }

    public synchronized void calculateRewards(User user) {
        final List<VisitedLocation> userLocations = new ArrayList<>(user.getVisitedLocations());
        final List<Attraction> attractions = gpsUtil.getAttractions();

        for (final VisitedLocation visitedLocation : userLocations) {
            for (final Attraction attraction : attractions) {
                if (user.getUserRewards().stream()
                        .filter(r -> r.attraction.attractionName.equals(attraction.attractionName)).count() == 0
                        && (isNearAttraction(visitedLocation, attraction))) {
                    user.addUserReward(new UserReward(visitedLocation, attraction,
                            getRewardPoints(attraction.attractionId, user.getUserId())));
                }
            }
        }
    }

    public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
        return (getDistance(attraction, location) <= ATTRACTION_PROXIMITY_RANGE);
    }

    private boolean isNearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
        return (getDistance(attraction, visitedLocation.location) <= proximityBuffer);
    }

    public int getRewardPoints(UUID attractionId, UUID userId) {
        return rewardsCentral.getAttractionRewardPoints(attractionId, userId);
    }

    public double getDistance(Location loc1, Location loc2) {
        final double lat1 = Math.toRadians(loc1.latitude);
        final double lon1 = Math.toRadians(loc1.longitude);
        final double lat2 = Math.toRadians(loc2.latitude);
        final double lon2 = Math.toRadians(loc2.longitude);

        final double angle = Math
                .acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        final double nauticalMiles = 60 * Math.toDegrees(angle);
        return STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
    }

}
