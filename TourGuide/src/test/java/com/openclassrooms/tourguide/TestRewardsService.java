package com.openclassrooms.tourguide;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
        final CompletableFuture<VisitedLocation> completableFuture = tourGuideService.tracker.trackUserLocation(user);

        completableFuture.thenAccept(res -> {
            final List<UserReward> userRewards = user.getUserRewards();
            then(userRewards).hasSize(1);
        });
    }

    @Test
    public void isWithinAttractionProximity() {
        final GpsUtil gpsUtil = new GpsUtil();
        final RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        final Attraction attraction = gpsUtil.getAttractions().get(0);
        then(rewardsService.isWithinAttractionProximity(attraction, attraction)).isTrue();
    }

    @Test
    public void nearAllAttractions() {
        final GpsUtil gpsUtil = new GpsUtil();

        final RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        rewardsService.setProximityBuffer(Integer.MAX_VALUE);

        InternalTestHelper.setInternalUserNumber(1);
        final TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        final CompletableFuture<Void> completableFuture = rewardsService
                .calculateRewards(tourGuideService.getAllUsers().get(0));
        completableFuture.thenAccept(res -> {
            final List<UserReward> userRewards = tourGuideService.getUserRewards(tourGuideService.getAllUsers().get(0));
            then(gpsUtil.getAttractions()).hasSameSizeAs(userRewards);
        });
    }

}
