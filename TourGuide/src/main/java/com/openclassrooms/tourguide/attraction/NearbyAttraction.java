package com.openclassrooms.tourguide.attraction;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;

public class NearbyAttraction {

    public final String attractionName;

    public final double attractionLatitude;

    public final double attractionLongitude;

    public final double userLatitude;

    public final double userLongitude;

    public final double distance;

    public final int rewardPoints;

    public NearbyAttraction(Attraction attraction, VisitedLocation visitedLocation, double distance, int rewardPoints) {
        attractionName = attraction.attractionName;
        attractionLatitude = attraction.latitude;
        attractionLongitude = attraction.longitude;
        userLatitude = visitedLocation.location.latitude;
        userLongitude = visitedLocation.location.longitude;
        this.distance = distance;
        this.rewardPoints = rewardPoints;
    }

}
