package com.example.packettracer.ui;

import com.example.packettracer.model.Link;
import com.example.packettracer.model.Node;
import com.example.packettracer.model.Packet;
import com.example.packettracer.service.RoutingService;
import com.example.packettracer.util.PacketLogger;
import com.example.packettracer.util.PacketStatusListener;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

public class NetworkPanel extends JPanel {

    private final List<Node> nodes = new ArrayList<>();
    private final List<Link> links = new ArrayList<>();
    private final RoutingService routingService = new RoutingService();

    private List<Node> currentPath = new ArrayList<>();
    private int segmentIndex = 0;
    private double progress = 0.0;
    private javax.swing.Timer timer;
    private Packet currentPacket;

    public NetworkPanel() {
        setBackground(Color.WHITE);
        createSampleTopology();
    }

    public String[] getNodeNames() {
        return nodes.stream().map(Node::getName).toArray(String[]::new);
    }

    public Node getNodeByName(String name) {
        for (Node node : nodes) {
            if (node.getName().equals(name)) {
                return node;
            }
        }
        return null;
    }

    public List<Node> findPath(Node source, Node destination) {
        return routingService.findPath(source, destination, links);
    }

    public String pathToString(List<Node> path) {
        return routingService.pathToString(path);
    }

    public void resetSimulation() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }

        currentPath = new ArrayList<>();
        segmentIndex = 0;
        progress = 0.0;
        currentPacket = null;
        repaint();
    }

    public void animatePacket(Packet packet, List<Node> path,
                              PacketLogger logger,
                              PacketStatusListener statusListener) {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }

        currentPacket = packet;
        currentPath = path;
        segmentIndex = 0;
        progress = 0.0;

        int totalHops = path.size() - 1;

        currentPacket.setStatus("In Transit");
        logger.log("Transmission started...");
        statusListener.update(
                currentPacket.getStatus(),
                path.get(0).getName(),
                0,
                totalHops,
                currentPacket.getTtl()
        );

        timer = new javax.swing.Timer(30, e -> {
            if (segmentIndex >= currentPath.size() - 1) {
                ((javax.swing.Timer) e.getSource()).stop();
                currentPacket.setStatus("Delivered");
                logger.log("Packet delivered successfully.");
                statusListener.update(
                        currentPacket.getStatus(),
                        currentPath.get(currentPath.size() - 1).getName(),
                        totalHops,
                        totalHops,
                        currentPacket.getTtl()
                );
                repaint();
                return;
            }

            progress += 0.02;

            if (progress >= 1.0) {
                progress = 0.0;
                segmentIndex++;

                currentPacket.setTtl(currentPacket.getTtl() - 1);

                String reachedNode = currentPath.get(segmentIndex).getName();
                logger.log("Reached: " + reachedNode);
                logger.log("TTL remaining: " + currentPacket.getTtl());

                if (segmentIndex < currentPath.size() - 1 && currentPacket.getTtl() <= 0) {
                    ((javax.swing.Timer) e.getSource()).stop();
                    currentPacket.setStatus("Dropped (TTL Expired)");
                    logger.log("Packet dropped. TTL expired before reaching destination.");
                    statusListener.update(
                            currentPacket.getStatus(),
                            reachedNode,
                            segmentIndex,
                            totalHops,
                            currentPacket.getTtl()
                    );
                    repaint();
                    return;
                }

                if (segmentIndex == currentPath.size() - 1) {
                    currentPacket.setStatus("Delivered");
                    statusListener.update(
                            currentPacket.getStatus(),
                            reachedNode,
                            totalHops,
                            totalHops,
                            currentPacket.getTtl()
                    );
                } else {
                    currentPacket.setStatus("In Transit");
                    statusListener.update(
                            currentPacket.getStatus(),
                            reachedNode,
                            segmentIndex,
                            totalHops,
                            currentPacket.getTtl()
                    );
                }
            }

            repaint();
        });

        timer.start();
    }

    private void createSampleTopology() {
        Node pc1 = new Node("PC1", "PC", 100, 250);
        Node sw1 = new Node("SW1", "Switch", 280, 250);
        Node r1 = new Node("R1", "Router", 470, 250);
        Node server1 = new Node("Server1", "Server", 700, 150);
        Node pc2 = new Node("PC2", "PC", 700, 350);

        nodes.add(pc1);
        nodes.add(sw1);
        nodes.add(r1);
        nodes.add(server1);
        nodes.add(pc2);

        links.add(new Link(pc1, sw1));
        links.add(new Link(sw1, r1));
        links.add(new Link(r1, server1));
        links.add(new Link(r1, pc2));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(2));

        drawLinks(g2);
        drawNodes(g2);
        drawPacket(g2);
    }

    private void drawLinks(Graphics2D g2) {
        g2.setColor(Color.GRAY);

        for (Link link : links) {
            g2.drawLine(
                    link.getFrom().getX(), link.getFrom().getY(),
                    link.getTo().getX(), link.getTo().getY()
            );
        }
    }

    private void drawNodes(Graphics2D g2) {
        for (Node node : nodes) {
            switch (node.getType()) {
                case "PC" -> drawPC(g2, node);
                case "Switch" -> drawSwitch(g2, node);
                case "Router" -> drawRouter(g2, node);
                case "Server" -> drawServer(g2, node);
            }
        }
    }

    private void drawPacket(Graphics2D g2) {
        if (currentPacket == null || currentPath.size() < 2 || segmentIndex >= currentPath.size() - 1) {
            return;
        }

        if (!"In Transit".equals(currentPacket.getStatus())) {
            return;
        }

        Node from = currentPath.get(segmentIndex);
        Node to = currentPath.get(segmentIndex + 1);

        int x = (int) (from.getX() + (to.getX() - from.getX()) * progress);
        int y = (int) (from.getY() + (to.getY() - from.getY()) * progress);

        g2.setColor(getProtocolColor(currentPacket.getProtocol()));
        g2.fillOval(x - 8, y - 8, 16, 16);
        g2.setColor(Color.BLACK);
        g2.drawString(currentPacket.getProtocol(), x + 10, y - 10);
    }

    private Color getProtocolColor(String protocol) {
        return switch (protocol) {
            case "ICMP" -> Color.RED;
            case "TCP" -> Color.BLUE;
            case "UDP" -> new Color(0, 128, 0);
            default -> Color.RED;
        };
    }

    private void drawPC(Graphics2D g2, Node node) {
        g2.setColor(new Color(220, 235, 255));
        g2.fillRect(node.getX() - 25, node.getY() - 20, 50, 35);
        g2.setColor(Color.BLACK);
        g2.drawRect(node.getX() - 25, node.getY() - 20, 50, 35);
        g2.drawRect(node.getX() - 10, node.getY() + 15, 20, 8);
        g2.drawLine(node.getX(), node.getY() + 15, node.getX(), node.getY() + 23);
        g2.drawString(node.getName(), node.getX() - 15, node.getY() + 45);
    }

    private void drawSwitch(Graphics2D g2, Node node) {
        g2.setColor(new Color(255, 230, 180));
        g2.fillRect(node.getX() - 35, node.getY() - 20, 70, 40);
        g2.setColor(Color.BLACK);
        g2.drawRect(node.getX() - 35, node.getY() - 20, 70, 40);

        for (int i = 0; i < 4; i++) {
            g2.drawRect(node.getX() - 25 + (i * 12), node.getY() - 5, 8, 8);
        }

        g2.drawString(node.getName(), node.getX() - 18, node.getY() + 40);
    }

    private void drawRouter(Graphics2D g2, Node node) {
        g2.setColor(new Color(200, 255, 200));
        g2.fillOval(node.getX() - 30, node.getY() - 30, 60, 60);
        g2.setColor(Color.BLACK);
        g2.drawOval(node.getX() - 30, node.getY() - 30, 60, 60);
        g2.drawString("R", node.getX() - 4, node.getY() + 5);
        g2.drawString(node.getName(), node.getX() - 12, node.getY() + 45);
    }

    private void drawServer(Graphics2D g2, Node node) {
        g2.setColor(new Color(230, 230, 230));
        g2.fillRect(node.getX() - 22, node.getY() - 30, 45, 60);
        g2.setColor(Color.BLACK);
        g2.drawRect(node.getX() - 22, node.getY() - 30, 45, 60);
        g2.drawLine(node.getX() - 22, node.getY() - 10, node.getX() + 23, node.getY() - 10);
        g2.drawLine(node.getX() - 22, node.getY() + 10, node.getX() + 23, node.getY() + 10);
        g2.drawString(node.getName(), node.getX() - 22, node.getY() + 45);
    }
}