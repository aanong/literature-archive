#!/bin/bash

# Check Java version
java -version

# Function to start a service
start_service() {
    local service_name=$1
    local jar_path=$(find $service_name/target -name "$service_name-*.jar" | head -n 1)

    if [ -z "$jar_path" ]; then
        echo "Error: Jar file for $service_name not found. Did you run '# mvn clean install -DskipTests'?"
        return
    fi

    echo "Starting $service_name from $jar_path..."
    nohup java -jar $jar_path > $service_name/startup.log 2>&1 &
    echo "$service_name started with PID $!"
}

# Start services
start_service "api-gateway"
start_service "user-service"
start_service "content-service"
start_service "knowledge-service"
start_service "chat-service"
start_service "asset-service"

echo "All services started. Check <service_dir>/startup.log for details."
