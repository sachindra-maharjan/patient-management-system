# !/bin/bash

CURDIR=$(dirname "$0")
WORKDIR=$(cd "$CURDIR/..")

cd patient-service
docker build -t patient-service:latest .
if [ $? -ne 0 ]; then
    echo "Failed to build patient service Docker image."
    exit 1
elif [ -z "$(docker images -q patient-service:latest)" ]; then
    echo "Patient service Docker image is empty."
    exit 1
else
    echo "Patient service Docker image built successfully."
    docker-compose -f $WORKDIR/docker-compose.yaml up -d
fi