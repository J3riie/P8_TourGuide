package com.openclassrooms.tourguide.performance;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;

public class TestPerformance {

    /*
     * These tests are disabled by default so first tests run on a newly pulled project don't last 10 minutes.
     * They should be enabled when wanting to check the performance of the app, and disabled again afterwards.
     */

    @Disabled
    @Test
    public void highVolumeTrackLocation() {
        final GpsUtil gpsUtil = new GpsUtil();
        final RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        // Users should be incremented up to 100,000, and test finishes within 15
        // minutes
        InternalTestHelper.setInternalUserNumber(100000);
        final TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        List<User> allUsers = new ArrayList<>();
        allUsers = tourGuideService.getAllUsers();

        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        allUsers.parallelStream().forEach(tourGuideService::trackUserLocation);
        stopWatch.stop();
        System.out.println("highVolumeTrackLocation: Time Elapsed: "
                + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
        assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
    }

    @Disabled
    @Test
    public void highVolumeGetRewards() {
        final GpsUtil gpsUtil = new GpsUtil();
        final RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());

        // Users should be incremented up to 100,000, and test finishes within 20
        // minutes
        InternalTestHelper.setInternalUserNumber(100000);
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        final Attraction attraction = gpsUtil.getAttractions().get(0);
        List<User> allUsers = new ArrayList<>();
        allUsers = tourGuideService.getAllUsers();
        allUsers.parallelStream()
                .forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));

        allUsers.parallelStream().forEach(u -> {
            final CompletableFuture<Void> completableFuture = rewardsService.calculateRewards(u);
            completableFuture.thenAccept(res -> assertTrue(u.getUserRewards().size() > 0));
        });
        stopWatch.stop();

        System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime())
                + " seconds.");
        assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
    }

}
