package com.hospital.resourceallocation.algorithms;

import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Custom Dijkstra's Shortest Path Algorithm Implementation.
 * Uses custom MinHeap and WeightedGraph to compute fastest emergency travel times between hospitals.
 */
@Component
public class DijkstraRouter implements RoutePlanner {

    private final WeightedGraph graph;

    public DijkstraRouter() {
        this.graph = new WeightedGraph();
        initializeDefaultRoadNetwork();
    }

    public DijkstraRouter(WeightedGraph graph) {
        this.graph = graph;
    }

    public WeightedGraph getGraph() {
        return graph;
    }

    /**
     * Initializes default 10 connected locations in Nalam District.
     */
    public final void initializeDefaultRoadNetwork() {
        graph.addBidirectionalEdge("Nalam Central", "South Hub", 8, 4.5, "EXCELLENT");
        graph.addBidirectionalEdge("Nalam Central", "Medical Square", 12, 6.0, "GOOD");
        graph.addBidirectionalEdge("Nalam Central", "West Ward", 6, 3.0, "GOOD");
        graph.addBidirectionalEdge("West Ward", "North Bypass", 9, 5.0, "GOOD");
        graph.addBidirectionalEdge("North Bypass", "Green Valley", 7, 4.2, "EXCELLENT");
        graph.addBidirectionalEdge("Green Valley", "Medical Square", 6, 3.5, "GOOD");
        graph.addBidirectionalEdge("Medical Square", "East Junction", 14, 8.0, "HIGHWAY");
        graph.addBidirectionalEdge("South Hub", "Port Road", 10, 5.5, "AVERAGE");
        graph.addBidirectionalEdge("Port Road", "Valley Station", 7, 4.0, "GOOD");
        graph.addBidirectionalEdge("Valley Station", "Cross Junction", 6, 3.8, "EXCELLENT");
        graph.addBidirectionalEdge("Cross Junction", "East Junction", 11, 6.5, "GOOD");
        graph.addBidirectionalEdge("South Hub", "Cross Junction", 13, 7.0, "GOOD");
        graph.addBidirectionalEdge("Nalam Central", "Green Valley", 15, 7.5, "CONGESTED");
    }

    @Override
    public RouteResult findShortestRoute(String origin, String destination) {
        if (origin == null || destination == null) {
            return new RouteResult(origin, destination, Integer.MAX_VALUE, 0.0, Collections.emptyList());
        }

        if (origin.equalsIgnoreCase(destination)) {
            return new RouteResult(origin, destination, 0, 0.0, List.of(origin));
        }

        // Distances map (Travel time in minutes)
        Map<String, Integer> distances = new HashMap<>();
        Map<String, Double> distancesKm = new HashMap<>();
        Map<String, String> predecessors = new HashMap<>();
        MinHeap<String> minHeap = new MinHeap<>();
        Set<String> visited = new HashSet<>();

        // Initialize distances
        for (String vertex : graph.getAllVertices()) {
            distances.put(vertex, Integer.MAX_VALUE);
            distancesKm.put(vertex, Double.MAX_VALUE);
        }

        distances.put(origin, 0);
        distancesKm.put(origin, 0.0);
        minHeap.insert(origin, 0);

        while (!minHeap.isEmpty()) {
            MinHeap.HeapNode<String> current = minHeap.extractMin();
            String u = current.element;

            if (visited.contains(u)) continue;
            visited.add(u);

            if (u.equalsIgnoreCase(destination)) {
                break; // Shortest path to target found
            }

            for (WeightedGraph.Edge edge : graph.getNeighbors(u)) {
                String v = edge.getTarget();
                if (visited.contains(v)) continue;

                int weight = edge.getTravelTimeMinutes();
                int newDist = distances.get(u) + weight;

                if (newDist < distances.getOrDefault(v, Integer.MAX_VALUE)) {
                    distances.put(v, newDist);
                    distancesKm.put(v, distancesKm.get(u) + edge.getDistanceKm());
                    predecessors.put(v, u);
                    minHeap.decreaseKey(v, newDist);
                }
            }
        }

        // Reconstruct path
        if (!predecessors.containsKey(destination) && !origin.equalsIgnoreCase(destination)) {
            return new RouteResult(origin, destination, Integer.MAX_VALUE, 0.0, Collections.emptyList());
        }

        LinkedList<String> path = new LinkedList<>();
        String step = destination;
        path.addFirst(step);

        while (predecessors.containsKey(step)) {
            step = predecessors.get(step);
            path.addFirst(step);
        }

        int totalTime = distances.getOrDefault(destination, Integer.MAX_VALUE);
        double totalDistance = distancesKm.getOrDefault(destination, 0.0);

        return new RouteResult(origin, destination, totalTime, totalDistance, path);
    }

    @Override
    public List<String> getAllReachableLocations(String origin) {
        return new ArrayList<>(graph.getAllVertices());
    }
}
