package org.example.Gossip.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Component
public class DistributedGossipNode {
    // Multicast settings
    private static final String MULTICAST_ADDRESS = "230.0.0.0";
    private static final int PORT = 4446;

    private final String serverName;
    private final String ipAddress;

    // Synchronized map to hold live nodes (serverName -> ipAddress)
    private final Map<String, String> clusterNodes = Collections.synchronizedMap(new HashMap<>());

    // Only serverName is injected; ipAddress is determined at runtime.
    public DistributedGossipNode(@Value("${spring.application.name:Gossip-java}") String serverName) {
        this.serverName = serverName;
        this.ipAddress = determineLocalIpAddress();
        // Initialize our own entry in the hash table
        clusterNodes.put(serverName, ipAddress);
    }

    /**
     * Determines the local IP address at runtime.
     * Iterates through network interfaces and selects a non-loopback IPv4 address.
     */
    private String determineLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while(interfaces.hasMoreElements()){
                NetworkInterface iface = interfaces.nextElement();
                // Skip loopback and inactive interfaces
                if(iface.isLoopback() || !iface.isUp())
                    continue;
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while(addresses.hasMoreElements()){
                    InetAddress addr = addresses.nextElement();
                    if(addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException("Failed to determine local IP address", e);
        }
        // Fallback: use localhost
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException("Failed to determine local IP address", e);
        }
    }

    /**
     * Automatically start the gossip node after bean construction.
     */
    @PostConstruct
    public void init() throws IOException {
        start();
    }

    public void start() throws IOException {
        InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
        try (MulticastSocket socket = new MulticastSocket(PORT)) {
            // Join the multicast group
            socket.joinGroup(group);

            // Start the listener thread to receive heartbeats
            new Thread(() -> {
                byte[] buf = new byte[256];
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    try {
                        socket.receive(packet);
                        String received = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                        // Expected format: serverName:ipAddress
                        String[] parts = received.split(":");
                        if (parts.length == 2) {
                            String receivedServerName = parts[0].trim();
                            String receivedIp = parts[1].trim();
                            // Update the hash table with the received heartbeat
                            clusterNodes.put(receivedServerName, receivedIp);
                            System.out.println("[" + serverName + "] Updated cluster nodes: " + clusterNodes);
                        }
                    } catch (IOException e) {
                        System.err.println("[" + serverName + "] Error receiving packet: " + e.getMessage());
                        break;
                    }
                }
            }).start();

            // Main thread: periodically send heartbeat messages
            while (true) {
                String heartbeatMessage = serverName + ":" + ipAddress;
                byte[] data = heartbeatMessage.getBytes(StandardCharsets.UTF_8);
                DatagramPacket packet = new DatagramPacket(data, data.length, group, PORT);
                socket.send(packet);
                System.out.println("[" + serverName + "] Sent heartbeat: " + heartbeatMessage);
                try {
                    Thread.sleep(5000); // Wait 5 seconds before sending the next heartbeat
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}
