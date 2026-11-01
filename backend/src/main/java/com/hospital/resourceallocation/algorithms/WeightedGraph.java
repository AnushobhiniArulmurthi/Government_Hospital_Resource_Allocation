package com.hospital.resourceallocation.algorithms;

import java.util.*;

/**
 * Weighted Graph Data Structure for Hospital and Regional Road Network.
 * Uses HashMap<String, List<Edge>> for adjacency representation and constant-time vertex lookup.
 */
public class WeightedGraph {

    public static class Edge {
        private final String target;
        private final int travelTimeMinutes;
        private final double distanceKm;
        private final String roadCondition;

        public Edge(String target, int travelTimeMinutes, double distanceKm, String roadCondition) {
            this.target = target;
            this.travelTimeMinutes = travelTimeMinutes;
            this.distanceKm = distanceKm;
            this.roadCondition = roadCondition;
        }

        public String getTarget() { return target; }
        public int getTravelTimeMinutes() { return travelTimeMinutes; }
        public double getDistanceKm() { return distanceKm; }
        public String getRoadCondition() { return roadCondition; }
    }

    private final Map<String, List<Edge>> adjacencyList;

    public WeightedGraph() {
        this.adjacencyList = new HashMap<>();
    }

    public void addVertex(String location) {
        adjacencyList.putIfAbsent(location, new ArrayList<>());
    }

    public void addEdge(String from, String to, int travelTimeMinutes, double distanceKm, String roadCondition) {
        addVertex(from);
        addVertex(to);
        adjacencyList.get(from).add(new Edge(to, travelTimeMinutes, distanceKm, roadCondition));
    }

    public void addBidirectionalEdge(String locA, String locB, int travelTimeMinutes, double distanceKm, String roadCondition) {
        addEdge(locA, locB, travelTimeMinutes, distanceKm, roadCondition);
        addEdge(locB, locA, travelTimeMinutes, distanceKm, roadCondition);
    }

    public List<Edge> getNeighbors(String location) {
        return adjacencyList.getOrDefault(location, Collections.emptyList());
    }

    public Set<String> getAllVertices() {
        return adjacencyList.keySet();
    }

    public boolean containsVertex(String location) {
        return adjacencyList.containsKey(location);
    }
}
