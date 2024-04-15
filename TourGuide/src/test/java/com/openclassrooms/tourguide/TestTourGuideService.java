package com.openclassrooms.tourguide;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.openclassrooms.tourguide.attraction.NearbyAttraction;
import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;

import gpsUtil.GpsUtil;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tripPricer.Provider;

public class TestTourGuideService {

    @Test
    public void getUserLocation() {
        final GpsUtil gpsUtil = new GpsUtil();
        final RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        final TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        final User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        final VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);
        assertTrue(visitedLocation.userId.equals(user.getUserId()));
    }

    @Test
    public void addUser() {
        final GpsUtil gpsUtil = new GpsUtil();
        final RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        final TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        final User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        final User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

        tourGuideService.addUser(user);
        tourGuideService.addUser(user2);

        final User retrivedUser = tourGuideService.getUser(user.getUserName());
        final User retrivedUser2 = tourGuideService.getUser(user2.getUserName());

        assertEquals(user, retrivedUser);
        assertEquals(user2, retrivedUser2);
    }

    @Test
    public void getAllUsers() {
        final GpsUtil gpsUtil = new GpsUtil();
        final RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        final TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        final User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        final User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

        tourGuideService.addUser(user);
        tourGuideService.addUser(user2);

        final List<User> allUsers = tourGuideService.getAllUsers();

        assertTrue(allUsers.contains(user));
        assertTrue(allUsers.contains(user2));
    }

    @Test
    public void trackUser() {
        final GpsUtil gpsUtil = new GpsUtil();
        final RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        final TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        final User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        final VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);

        assertEquals(user.getUserId(), visitedLocation.userId);
    }

    @Test
    public void getNearbyAttractions() {
        final GpsUtil gpsUtil = new GpsUtil();
        final RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        final TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        final User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        final VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);

        final List<NearbyAttraction> attractions = tourGuideService.getNearByAttractions(visitedLocation);

        assertEquals(5, attractions.size());
    }

    public void getTripDeals() {
        final GpsUtil gpsUtil = new GpsUtil();
        final RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        final TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        final User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

        final List<Provider> providers = tourGuideService.getTripDeals(user);

        assertEquals(10, providers.size());
    }

}
