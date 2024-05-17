package com.openclassrooms.tourguide;

import static org.assertj.core.api.BDDAssertions.then;

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
        final VisitedLocation visitedLocation = tourGuideService.tracker.trackUserLocation(user).join();
        then(visitedLocation.userId).isEqualTo(user.getUserId());
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

        then(user).isEqualTo(retrivedUser);
        then(user2).isEqualTo(retrivedUser2);
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

        then(allUsers).contains(user);
        then(allUsers).contains(user2);
    }

    @Test
    public void trackUser() {
        final GpsUtil gpsUtil = new GpsUtil();
        final RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        final TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        final User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        final VisitedLocation visitedLocation = tourGuideService.tracker.trackUserLocation(user).join();

        then(user.getUserId()).isEqualTo(visitedLocation.userId);
    }

    @Test
    public void getNearbyAttractions() {
        final GpsUtil gpsUtil = new GpsUtil();
        final RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        final TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        final User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        final VisitedLocation visitedLocation = tourGuideService.tracker.trackUserLocation(user).join();

        final List<NearbyAttraction> attractions = tourGuideService.getNearByAttractions(visitedLocation);

        then(attractions).hasSize(5);
    }

    public void getTripDeals() {
        final GpsUtil gpsUtil = new GpsUtil();
        final RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        final TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        final User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

        final List<Provider> providers = tourGuideService.getTripDeals(user);

        then(providers).hasSize(10);
    }

}
