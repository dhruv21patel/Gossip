package main

import (
	"fmt"
	"net"
	"os"
	"strings"
	"sync"
	"time"
)

// GossipNode represents a node in the gossip protocol.
type GossipNode struct {
	serverName    string
	ipAddress     string
	clusterNodes  map[string]string // Map: serverName -> ipAddress
	mutex         sync.Mutex
	multicastAddr *net.UDPAddr
}

// NewGossipNode initializes a new GossipNode.
func NewGossipNode(serverName, ipAddress string, multicastAddr *net.UDPAddr) *GossipNode {
	// Initialize the cluster with our own node.
	nodes := make(map[string]string)
	nodes[serverName] = ipAddress
	return &GossipNode{
		serverName:    serverName,
		ipAddress:     ipAddress,
		clusterNodes:  nodes,
		multicastAddr: multicastAddr,
	}
}

// Start begins the gossip protocol:
// - It listens for incoming heartbeats.
// - It periodically sends out its own heartbeat.
func (g *GossipNode) Start() error {
	// Create a UDP multicast listener.
	conn, err := net.ListenMulticastUDP("udp", nil, g.multicastAddr)
	if err != nil {
		return fmt.Errorf("error joining multicast group: %v", err)
	}
	conn.SetReadBuffer(1024)

	// Start a goroutine to listen for heartbeats.
	go g.listenForHeartbeats(conn)

	// Create a connection for sending heartbeats.
	sendConn, err := net.DialUDP("udp", nil, g.multicastAddr)
	if err != nil {
		return fmt.Errorf("error creating UDP sender: %v", err)
	}
	defer sendConn.Close()

	// Periodically send heartbeat messages.
	ticker := time.NewTicker(5 * time.Second)
	defer ticker.Stop()
	for {
		<-ticker.C
		heartbeat := fmt.Sprintf("%s:%s", g.serverName, g.ipAddress)
		_, err := sendConn.Write([]byte(heartbeat))
		if err != nil {
			fmt.Printf("[%s] Error sending heartbeat: %v\n", g.serverName, err)
		} else {
			fmt.Printf("[%s] Sent heartbeat: %s\n", g.serverName, heartbeat)
		}
	}
}

// listenForHeartbeats listens on the multicast UDP connection for incoming heartbeats.
func (g *GossipNode) listenForHeartbeats(conn *net.UDPConn) {
	buf := make([]byte, 1024)
	for {
		n, src, err := conn.ReadFromUDP(buf)
		if err != nil {
			fmt.Printf("[%s] Error receiving heartbeat: %v\n", g.serverName, err)
			continue
		}
		message := strings.TrimSpace(string(buf[:n]))
		// Expect message format: "serverName:ipAddress"
		parts := strings.Split(message, ":")
		if len(parts) != 2 {
			fmt.Printf("[%s] Received malformed message from %v: %s\n", g.serverName, src, message)
			continue
		}
		receivedServerName := strings.TrimSpace(parts[0])
		receivedIP := strings.TrimSpace(parts[1])
		// Update the cluster map.
		g.mutex.Lock()
		g.clusterNodes[receivedServerName] = receivedIP
		// Print the updated cluster view.
		fmt.Printf("[%s] Updated cluster nodes: %v\n", g.serverName, g.clusterNodes)
		g.mutex.Unlock()
	}
}

func main() {
	// Read command-line arguments (or use defaults).
	serverName := "GoNode1"
	ipAddress := "127.0.0.1"
	if len(os.Args) > 1 {
		serverName = os.Args[1]
	}
	if len(os.Args) > 2 {
		ipAddress = os.Args[2]
	}

	// Resolve the multicast address.
	multicastAddrStr := "230.0.0.0:4446"
	multicastAddr, err := net.ResolveUDPAddr("udp", multicastAddrStr)
	if err != nil {
		fmt.Printf("Error resolving multicast address: %v\n", err)
		os.Exit(1)
	}

	// Create and start the gossip node.
	node := NewGossipNode(serverName, ipAddress, multicastAddr)
	if err := node.Start(); err != nil {
		fmt.Printf("Error starting gossip node: %v\n", err)
	}
}
