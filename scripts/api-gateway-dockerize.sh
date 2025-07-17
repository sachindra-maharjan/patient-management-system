# !/bin/bash

CURDIR=$(dirname "$0")
WORKDIR=$(cd "$CURDIR/..")

cd api-gateway
docker build -t api-gateway:latest .
if [ $? -ne 0 ]; then
    echo "Failed to build analytics service Docker image."
    exit 1
elif [ -z "$(docker images -q api-gateway:latest)" ]; then
    echo "api-gateway docker image is empty."
    exit 1
else
    echo "api-gateway docker image built successfully."
fi

# docker-compose -f $WORKDIR/docker-compose.yaml up -d