# !/bin/bash

CURDIR=$(dirname "$0")
WORKDIR=$(cd "$CURDIR/..")

cd notification-service
docker build -t notification-service:latest .
if [ $? -ne 0 ]; then
    echo "Failed to build notification service Docker image."
    exit 1
elif [ -z "$(docker images -q notification-service:latest)" ]; then
    echo "notification service Docker image is empty."
    exit 1
else
    echo "analnotificationytics service Docker image built successfully."
fi

# docker-compose -f $WORKDIR/docker-compose.yaml up -d