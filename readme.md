# Gossip Protocol Implementation with Heartbeat and Hash Table Updates

## Overview

This project implements a **Gossip Protocol** between a **Java server** and a **Go server**, where both servers send heartbeat messages to each other. They also update their respective hash tables with new IP addresses each time a heartbeat is received.

Additionally, the project integrates **Jenkins** for continuous integration, running code quality checks with **SonarQube**, performing image vulnerability scans with **Trivy**, and uploading the images to **Docker Hub**.

## Features

- **Gossip Protocol**: The Java and Go servers send periodic heartbeat messages to each other.
- **Hash Table Updates**: Both servers maintain a hash table of IPs, which is updated whenever a heartbeat is received.
- **Jenkins Integration**: Jenkins is used to automatically trigger builds, perform code quality checks with SonarQube, and run security scans using Trivy.
- **Docker Hub**: The Docker images are built, scanned for vulnerabilities, and then uploaded to Docker Hub.

## Project Structure

- **Java Server**: Implements the gossip protocol, handles heartbeats, and updates the hash table.
- **Go Server**: Implements the gossip protocol, handles heartbeats, and updates the hash table.
- **Jenkins Pipeline**: Automates building, testing, scanning, and pushing Docker images to Docker Hub.

## Prerequisites

1. **Docker**: For running the Java and Go server images.
2. **Jenkins**: Set up for continuous integration with SonarQube and Trivy integration.
3. **SonarQube**: Set up to perform code quality checks.
4. **Trivy**: Set up to perform vulnerability scanning on Docker images.
5. **Docker Hub Account**: To push and pull images.

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/dhruv21patel/Gossip.git

### 2. Building the Docker Images Locally

The Docker images for both Java and Go servers are already configured in the Dockerfile. To build the images, use the following commands:

#### Build the Java Server Image

```bash
docker build -t Java_Server ./Gossip
```

#### Build the Go Server Image

```bash
docker build -t go-server ./Gossip_Go
```

### 3. Running the Docker Images Locally

Once the images are built, you can run the containers locally:

#### Run Java Server Container

```bash
docker run -d -p 8081:8080 --name java-server java-server
```

#### Run Go Server Container

```bash
docker run -d -p 8082:8080 --name go-server go-server
```

### 4. Jenkins Integration

To ensure your code quality and security, Jenkins is set up to:

- Run SonarQube Analysis on the code.
- Run Trivy Scans to check for vulnerabilities in the Docker images.
- Push Docker Images to Docker Hub after successful builds.

You can configure Jenkins by following these steps:

1. Set up Jenkins with the required plugins (SonarQube Scanner, Docker Pipeline, Trivy).
2. Use the provided Jenkinsfile to define the pipeline steps.
3. Configure Jenkins to listen for pushes to your GitHub repository (for dev branch pushes).

### 5. Docker Hub Image Management

Once the images are built and scanned successfully, Jenkins will automatically push them to Docker Hub. To download and run the images locally, use the following commands:

#### Pull the Java Server Image

```bash
docker pull your-dockerhub-username/java-server
```

#### Pull the Go Server Image

```bash
docker pull your-dockerhub-username/go-server
```

#### Run the Java Server Container

```bash
docker run -d -p 8081:8080 --name java-server your-dockerhub-username/java-server
```

#### Run the Go Server Container

```bash
docker run -d -p 8082:8080 --name go-server your-dockerhub-username/go-server
```

### How It Works

#### Heartbeat Communication

- The Java and Go servers continuously send heartbeat messages to each other on their respective ports (8080 for both by default).
- When a heartbeat is received, the server updates its hash table with the IP address of the sender.

#### Hash Table Update

- Both the Java and Go servers maintain a hash table with IPs they have received during heartbeats.

#### Jenkins CI Pipeline

- The Jenkins pipeline automates the build, SonarQube analysis, Trivy scanning, and image upload to Docker Hub when a push is made to the dev branch.

#### Trivy Scan

- The Trivy scan checks the built Docker images for known vulnerabilities (e.g., high-risk or critical vulnerabilities).

### Conclusion

This project demonstrates a simple implementation of the Gossip Protocol with periodic heartbeat exchanges between a Java and a Go server. It also highlights the use of Jenkins to automate Docker image builds, vulnerability scans, and deployment to Docker Hub.