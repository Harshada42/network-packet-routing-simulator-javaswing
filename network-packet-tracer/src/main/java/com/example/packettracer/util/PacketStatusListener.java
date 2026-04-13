package com.example.packettracer.util;

public interface PacketStatusListener {
    void update(String status, String currentNode, int completedHops, int totalHops, int ttl);
}
