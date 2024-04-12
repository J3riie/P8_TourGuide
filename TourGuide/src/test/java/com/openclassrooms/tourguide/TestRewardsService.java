package com.openclassrooms.tourguide;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;

public class TestRewardsService {

    @Test
    public void userGetRewards() {
        final GpsUtil gpsUtil = new GpsUtil();
        final RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());

        InternalTestHelper.setInternalUserNumber(0);
        final TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        final User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        final Attraction attraction = gpsUtil.getAttractions().get(0);
        user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
        tourGuideService.trackUserLocation(user);
        final List<UserReward> userRewards = user.getUserRewards();
        tourGuideService.tracker.stopTracking();
        assertThat(userRewards).hasSize(1);
    }

    @Test
    public void isWithinAttractionProximity() {
        final GpsUtil gpsUtil = new GpsUtil();
        final RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        final Attraction attraction = gpsUtil.getAttractions().get(0);
        assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
    }

    @Test
    public void nearAllAttractions() {
        final GpsUtil gpsUtil = new GpsUtil();

        final RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        rewardsService.setProximityBuffer(Integer.MAX_VALUE);

        InternalTestHelper.setInternalUserNumber(1);
        final TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        rewardsService.calculateRewards(tourGuideService.getAllUsers().get(0));
        final List<UserReward> userRewards = tourGuideService.getUserRewards(tourGuideService.getAllUsers().get(0));
        tourGuideService.tracker.stopTracking();

        assertEquals(gpsUtil.getAttractions().size(), userRewards.size());
    }

}
