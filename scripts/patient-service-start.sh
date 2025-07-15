# !/bin/bash

WORKDIR=$(dirname "$0")
cd "$WORKDIR/.."
cd patient-service
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.jvmArguments="-Xmx512m -Xms256m" &
PID=$!
echo "Patient service started with PID $PID"
# Wait for the service to start
sleep 30
# Check if the service is running  
if ps -p $PID > /dev/null; then
    echo "Patient service is running."
else
    echo "Failed to start patient service."
    exit 1
fi