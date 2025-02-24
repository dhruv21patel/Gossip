# Build Stage
FROM golang:1.19-alpine AS builder
# Install git to download dependencies
RUN apk add --no-cache git

WORKDIR /app
# Copy go.mod (and go.sum if available) to cache dependencies
COPY go.mod ./
# Uncomment the next line if you have a go.sum file:
# COPY go.sum ./
RUN go mod download
# Copy the source code
COPY Main.go ./
# Build the binary; name the output binary "gossipapp"
RUN CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -o gossipapp Main.go

# Final Stage
FROM alpine:latest
# Install CA certificates (if your app makes HTTPS requests)
RUN apk --no-cache add ca-certificates
WORKDIR /root/
# Copy the binary from the builder stage
COPY --from=builder /app/gossipapp .
# Expose any required port (e.g., 4446 for multicast; adjust as needed)
EXPOSE 4446
# Set the entrypoint to run the binary
ENTRYPOINT ["./gossipapp"]
