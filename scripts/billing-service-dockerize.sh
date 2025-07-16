# !/bin/bash

CURDIR=$(dirname "$0")
WORKDIR=$(cd "$CURDIR/..")

cd billing-service
docker build -t billing-service:latest .
if [ $? -ne 0 ]; then
    echo "Failed to build billing service Docker image."
    exit 1
elif [ -z "$(docker images -q billing-service:latest)" ]; then
    echo "Billing service Docker image is empty."
    exit 1
else
    echo "Billing service Docker image built successfully."
fi

# docker-compose -f $WORKDIR/docker-compose.yaml up -d