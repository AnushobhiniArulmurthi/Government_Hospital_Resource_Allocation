package com.hospital.resourceallocation.model;

import jakarta.persistence.*;

/**
 * Hospital Route Entity
 * Represents weighted graph edge connecting geographic locations.
 */
@Entity
@Table(name = "hospital_routes")
public class HospitalRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "route_id")
    private Integer routeId;

    @Column(name = "from_location", nullable = false, length = 100)
    private String fromLocation;

    @Column(name = "to_location", nullable = false, length = 100)
    private String toLocation;

    @Column(name = "distance_km", nullable = false)
    private Double distanceKm;

    @Column(name = "travel_time_minutes", nullable = false)
    private Integer travelTimeMinutes;

    @Column(name = "road_condition", length = 50)
    private String roadCondition = "GOOD";

    public HospitalRoute() {}

    public HospitalRoute(String fromLocation, String toLocation, Double distanceKm,
                         Integer travelTimeMinutes, String roadCondition) {
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.distanceKm = distanceKm;
        this.travelTimeMinutes = travelTimeMinutes;
        this.roadCondition = roadCondition != null ? roadCondition : "GOOD";
    }

    // Getters and Setters
    public Integer getRouteId() { return routeId; }
    public void setRouteId(Integer routeId) { this.routeId = routeId; }

    public String getFromLocation() { return fromLocation; }
    public void setFromLocation(String fromLocation) { this.fromLocation = fromLocation; }

    public String getToLocation() { return toLocation; }
    public void setToLocation(String toLocation) { this.toLocation = toLocation; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

    public Integer getTravelTimeMinutes() { return travelTimeMinutes; }
    public void setTravelTimeMinutes(Integer travelTimeMinutes) { this.travelTimeMinutes = travelTimeMinutes; }

    public String getRoadCondition() { return roadCondition; }
    public void setRoadCondition(String roadCondition) { this.roadCondition = roadCondition; }
}
