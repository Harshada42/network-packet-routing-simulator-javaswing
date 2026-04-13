package com.example.packettracer.model;

public class Packet {
    private final String source;
    private final String destination;
    private final String protocol;
    private int ttl;
    private String status;

    public Packet(String source, String destination, String protocol, int ttl, String status) {
        this.source = source;
        this.destination = destination;
        this.protocol = protocol;
        this.ttl = ttl;
        this.status = status;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public String getProtocol() {
        return protocol;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}