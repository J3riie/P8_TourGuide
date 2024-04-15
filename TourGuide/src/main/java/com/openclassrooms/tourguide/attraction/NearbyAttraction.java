package com.openclassrooms.tourguide.attraction;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;

public class NearbyAttraction {

    public String attractionName;

    public double attractionLatitude;

    public double attractionLongitude;

    public double userLatitude;

    public double userLongitude;

    public double distance;

    public int rewardPoints;

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
