#!/bin/bash

CURDIR="$(pwd)"
WORKDIR="$(cd "$CURDIR" && pwd)"

echo "Working directory: $WORKDIR"

echo "Building patient service Docker image..."
cd $WORKDIR/patient-service
docker build -t patient-service:latest .
if [ $? -ne 0 ]; then
    echo "Failed to build patient service Docker image."
    exit 1
fi

echo "Building analytics service Docker image..."
cd $WORKDIR/analytics-service   
docker build -t analytics-service:latest .
if [ $? -ne 0 ]; then
    echo "Failed to build analytics service Docker image."
    exit 1
fi

echo "Building billing service Docker image..."
cd $WORKDIR/billing-service
docker build -t billing-service:latest .
if [ $? -ne 0 ]; then
    echo "Failed to build billing service Docker image."
    exit 1
fi

echo "Building notification service Docker image..."
cd $WORKDIR/notification-service
docker build -t notification-service:latest .
if [ $? -ne 0 ]; then
    echo "Failed to build notification service Docker image."
    exit 1
fi

echo "Building auth service Docker image..."
cd $WORKDIR/auth-service
docker build -t auth-service:latest .
if [ $? -ne 0 ]; then
    echo "Failed to build auth service Docker image."
    exit 1
fi

echo "Building api gateway Docker image..."
cd $WORKDIR/api-gateway
docker build -t api-gateway:latest .
if [ $? -ne 0 ]; then
    echo "Failed to build api gateway Docker image."
    exit 1
fi

echo "Building docker compose..."
cd $WORKDIR
docker-compose up -d




