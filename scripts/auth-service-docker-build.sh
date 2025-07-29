# !/bin/bash

CURDIR=$(dirname "$0")
WORKDIR=$(cd "$CURDIR/..")

cd auth-service
docker build -t auth-service:latest .
if [ $? -ne 0 ]; then
    echo "Failed to build auth-service Docker image."
    exit 1
elif [ -z "$(docker images -q auth-service:latest)" ]; then
    echo "auth-service Docker image is empty."
    exit 1
else
    echo "auth-service Docker image built successfully."  
fi

docker-compose up auth-service