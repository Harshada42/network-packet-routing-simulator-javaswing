package com.example.packettracer;

import com.example.packettracer.model.Node;
import com.example.packettracer.model.Packet;
import com.example.packettracer.ui.NetworkPanel;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

public class PacketTracerApp extends JFrame {

    private final NetworkPanel networkPanel;
    private final JComboBox<String> sourceBox;
    private final JComboBox<String> destinationBox;
    private final JComboBox<String> protocolBox;
    private final JComboBox<Integer> ttlBox;
    private final JTextArea logArea;

    private final JLabel sourceValue;
    private final JLabel destinationValue;
    private final JLabel protocolValue;
    private final JLabel statusValue;
    private final JLabel currentNodeValue;
    private final JLabel hopCountValue;
    private final JLabel ttlValue;

    public PacketTracerApp() {
        setTitle("Network Packet Routing Simulator");
        setSize(1200, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        networkPanel = new NetworkPanel();

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        sourceBox = new JComboBox<>(networkPanel.getNodeNames());
        destinationBox = new JComboBox<>(networkPanel.getNodeNames());
        protocolBox = new JComboBox<>(new String[]{"ICMP", "TCP", "UDP"});
        ttlBox = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 6});

        JButton sendButton = new JButton("Send Packet");
        JButton resetButton = new JButton("Reset Simulation");
        JButton clearLogButton = new JButton("Clear Log");

        logArea = new JTextArea(8, 20);
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Packet Log"));

        sourceValue = new JLabel("-");
        destinationValue = new JLabel("-");
        protocolValue = new JLabel("-");
        statusValue = new JLabel("Idle");
        currentNodeValue = new JLabel("-");
        hopCountValue = new JLabel("0 / 0");
        ttlValue = new JLabel("-");

        sendButton.addActionListener(e -> sendPacket());
        resetButton.addActionListener(e -> resetSimulation());
        clearLogButton.addActionListener(e -> logArea.setText(""));

        topPanel.add(new JLabel("Source:"));
        topPanel.add(sourceBox);
        topPanel.add(new JLabel("Destination:"));
        topPanel.add(destinationBox);
        topPanel.add(new JLabel("Protocol:"));
        topPanel.add(protocolBox);
        topPanel.add(new JLabel("TTL:"));
        topPanel.add(ttlBox);
        topPanel.add(sendButton);
        topPanel.add(resetButton);
        topPanel.add(clearLogButton);

        add(topPanel, BorderLayout.NORTH);
        add(networkPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);
        add(createDetailsPanel(), BorderLayout.EAST);

        setVisible(true);
    }

    private JPanel createDetailsPanel() {
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Packet Details"));
        detailsPanel.setPreferredSize(new Dimension(260, 0));

        JPanel gridPanel = new JPanel(new GridLayout(7, 2, 10, 12));

        gridPanel.add(new JLabel("Source:"));
        gridPanel.add(sourceValue);

        gridPanel.add(new JLabel("Destination:"));
        gridPanel.add(destinationValue);

        gridPanel.add(new JLabel("Protocol:"));
        gridPanel.add(protocolValue);

        gridPanel.add(new JLabel("Status:"));
        gridPanel.add(statusValue);

        gridPanel.add(new JLabel("Current Node:"));
        gridPanel.add(currentNodeValue);

        gridPanel.add(new JLabel("Hop Count:"));
        gridPanel.add(hopCountValue);

        gridPanel.add(new JLabel("TTL:"));
        gridPanel.add(ttlValue);

        detailsPanel.add(gridPanel);
        return detailsPanel;
    }

    private void sendPacket() {
        String sourceName = (String) sourceBox.getSelectedItem();
        String destinationName = (String) destinationBox.getSelectedItem();
        String protocol = (String) protocolBox.getSelectedItem();
        Integer ttl = (Integer) ttlBox.getSelectedItem();

        if (sourceName == null || destinationName == null || protocol == null || ttl == null) {
            return;
        }

        if (sourceName.equals(destinationName)) {
            JOptionPane.showMessageDialog(this, "Source and destination cannot be the same.");
            return;
        }

        Node source = networkPanel.getNodeByName(sourceName);
        Node destination = networkPanel.getNodeByName(destinationName);

        List<Node> path = networkPanel.findPath(source, destination);

        if (path.isEmpty()) {
            log("No route found from " + sourceName + " to " + destinationName);
            updatePacketDetails(sourceName, destinationName, protocol, "No Route", "-", 0, 0, ttl);
            return;
        }

        Packet packet = new Packet(sourceName, destinationName, protocol, ttl, "Created");
        int totalHops = path.size() - 1;

        log("Packet created");
        log("Protocol: " + protocol);
        log("Source: " + sourceName);
        log("Destination: " + destinationName);
        log("TTL: " + ttl);
        log("Path: " + networkPanel.pathToString(path));

        updatePacketDetails(
                sourceName,
                destinationName,
                protocol,
                "Created",
                sourceName,
                0,
                totalHops,
                ttl
        );

        networkPanel.animatePacket(
                packet,
                path,
                this::log,
                (status, currentNode, completedHops, hopsTotal, remainingTtl) ->
                        updatePacketDetails(
                                sourceName,
                                destinationName,
                                protocol,
                                status,
                                currentNode,
                                completedHops,
                                hopsTotal,
                                remainingTtl
                        )
        );
    }

    private void resetSimulation() {
        networkPanel.resetSimulation();
        sourceValue.setText("-");
        destinationValue.setText("-");
        protocolValue.setText("-");
        statusValue.setText("Idle");
        currentNodeValue.setText("-");
        hopCountValue.setText("0 / 0");
        ttlValue.setText("-");
        log("Simulation reset.");
    }

    private void updatePacketDetails(String source,
                                     String destination,
                                     String protocol,
                                     String status,
                                     String currentNode,
                                     int currentHop,
                                     int totalHops,
                                     int ttl) {
        sourceValue.setText(source);
        destinationValue.setText(destination);
        protocolValue.setText(protocol);
        statusValue.setText(status);
        currentNodeValue.setText(currentNode);
        hopCountValue.setText(currentHop + " / " + totalHops);
        ttlValue.setText(String.valueOf(ttl));
    }

    private void log(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PacketTracerApp::new);
    }
}