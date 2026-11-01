package com.hospital.resourceallocation.algorithms;

import java.util.List;

/**
 * Abstraction Interface: Route planning contract implemented via custom Dijkstra's algorithm.
 */
public interface RoutePlanner {

    RouteResult findShortestRoute(String originLocation, String destinationLocation);

    List<String> getAllReachableLocations(String originLocation);
}
