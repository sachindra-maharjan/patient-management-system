# !/bin/bash

CURDIR=$(dirname "$0")
WORKDIR=$(cd "$CURDIR/..")

cd analytics-service
docker build -t analytics-service:latest .
if [ $? -ne 0 ]; then
    echo "Failed to build analytics service Docker image."
    exit 1
elif [ -z "$(docker images -q analytics-service:latest)" ]; then
    echo "analytics service Docker image is empty."
    exit 1
else
    echo "analytics service Docker image built successfully."
fi

# docker-compose -f $WORKDIR/docker-compose.yaml up -d