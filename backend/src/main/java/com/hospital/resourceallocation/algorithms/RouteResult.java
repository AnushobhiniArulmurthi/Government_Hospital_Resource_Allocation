package com.hospital.resourceallocation.algorithms;

import java.util.ArrayList;
import java.util.List;

/**
 * Result DTO containing the shortest path route details calculated by Dijkstra's algorithm.
 */
public class RouteResult {

    private String origin;
    private String destination;
    private int totalTravelTimeMinutes;
    private double totalDistanceKm;
    private List<String> pathNodes;
    private String formattedPath;

    public RouteResult() {
        this.pathNodes = new ArrayList<>();
    }

    public RouteResult(String origin, String destination, int totalTravelTimeMinutes,
                       double totalDistanceKm, List<String> pathNodes) {
        this.origin = origin;
        this.destination = destination;
        this.totalTravelTimeMinutes = totalTravelTimeMinutes;
        this.totalDistanceKm = totalDistanceKm;
        this.pathNodes = pathNodes != null ? pathNodes : new ArrayList<>();
        this.formattedPath = String.join(" -> ", this.pathNodes);
    }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public int getTotalTravelTimeMinutes() { return totalTravelTimeMinutes; }
    public void setTotalTravelTimeMinutes(int totalTravelTimeMinutes) { this.totalTravelTimeMinutes = totalTravelTimeMinutes; }

    public double getTotalDistanceKm() { return totalDistanceKm; }
    public void setTotalDistanceKm(double totalDistanceKm) { this.totalDistanceKm = totalDistanceKm; }

    public List<String> getPathNodes() { return pathNodes; }
    public void setPathNodes(List<String> pathNodes) {
        this.pathNodes = pathNodes;
        this.formattedPath = String.join(" -> ", pathNodes);
    }

    public String getFormattedPath() { return formattedPath; }
    public void setFormattedPath(String formattedPath) { this.formattedPath = formattedPath; }
}
