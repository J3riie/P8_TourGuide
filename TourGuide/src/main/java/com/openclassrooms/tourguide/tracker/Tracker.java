package com.openclassrooms.tourguide.tracker;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;

import gpsUtil.GpsUtil;
import gpsUtil.location.VisitedLocation;

public class Tracker {

    private final Logger logger = LoggerFactory.getLogger(Tracker.class);

    private final TourGuideService tourGuideService;

    private final GpsUtil gpsUtil;

    private final RewardsService rewardsService;

    public Tracker(TourGuideService tourGuideService, GpsUtil gpsUtil, RewardsService rewardsService) {
        this.tourGuideService = tourGuideService;
        this.gpsUtil = gpsUtil;
        this.rewardsService = rewardsService;
    }

    public void trackUsers() {
        final List<User> users = tourGuideService.getAllUsers();
        logger.debug("Begin Tracker. Tracking {} users.", users.size());
        users.parallelStream().forEach(this::trackUserLocation);
    }

    public VisitedLocation getUserLocation(User user) {
        return (!user.getVisitedLocations().isEmpty()) ? user.getLastVisitedLocation() : trackUserLocation(user).join();
    }

    public CompletableFuture<VisitedLocation> trackUserLocation(User user) {
        return CompletableFuture.supplyAsync(() -> {
            final VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
            user.addToVisitedLocations(visitedLocation);
            rewardsService.calculateRewards(user);
            return visitedLocation;
        });
    }
}
