#!/bin/bash

CURDIR=$(dirname "$0")
WORKDIR=$(cd "$CURDIR/..")

docker-compose -f "docker-compose.yaml" up -d