package com.example.packettracer.model;

public class Node {
    private final String name;
    private final String type;
    private final int x;
    private final int y;

    public Node(String name, String type, int x, int y) {
        this.name = name;
        this.type = type;
        this.x = x;
        this.y = y;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}