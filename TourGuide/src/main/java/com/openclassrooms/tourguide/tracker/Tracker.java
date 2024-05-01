package com.openclassrooms.tourguide.tracker;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;

public class Tracker {
    private final Logger logger = LoggerFactory.getLogger(Tracker.class);

    private final TourGuideService tourGuideService;

    public Tracker(TourGuideService tourGuideService) {
        this.tourGuideService = tourGuideService;
    }

    public void trackUsers() {
        final List<User> users = tourGuideService.getAllUsers();
        logger.debug("Begin Tracker. Tracking {} users.", users.size());
        users.parallelStream().forEach(tourGuideService::trackUserLocation);
    }
}
