package com.example.packettracer.service;

import com.example.packettracer.model.Link;
import com.example.packettracer.model.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class RoutingService {

    public List<Node> findPath(Node source, Node destination, List<Link> links) {
        if (source == null || destination == null) {
            return Collections.emptyList();
        }

        Map<Node, Node> parentMap = new HashMap<>();
        Queue<Node> queue = new LinkedList<>();
        Set<Node> visited = new HashSet<>();

        queue.add(source);
        visited.add(source);

        while (!queue.isEmpty()) {
            Node current = queue.poll();

            if (current.equals(destination)) {
                break;
            }

            for (Node neighbor : getNeighbors(current, links)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parentMap.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        if (!visited.contains(destination)) {
            return Collections.emptyList();
        }

        List<Node> path = new ArrayList<>();
        Node step = destination;

        while (step != null) {
            path.add(step);
            step = parentMap.get(step);
        }

        Collections.reverse(path);
        return path;
    }

    public List<Node> getNeighbors(Node node, List<Link> links) {
        List<Node> neighbors = new ArrayList<>();

        for (Link link : links) {
            if (link.getFrom().equals(node)) {
                neighbors.add(link.getTo());
            } else if (link.getTo().equals(node)) {
                neighbors.add(link.getFrom());
            }
        }

        return neighbors;
    }

    public String pathToString(List<Node> path) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < path.size(); i++) {
            sb.append(path.get(i).getName());
            if (i < path.size() - 1) {
                sb.append(" -> ");
            }
        }

        return sb.toString();
    }
}
