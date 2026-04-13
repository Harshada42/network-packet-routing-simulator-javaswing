package com.example.packettracer.model;

public class Link {
    private final Node from;
    private final Node to;

    public Link(Node from, Node to) {
        this.from = from;
        this.to = to;
    }

    public Node getFrom() {
        return from;
    }

    public Node getTo() {
        return to;
    }
}
