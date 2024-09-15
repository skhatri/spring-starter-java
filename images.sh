#!/usr/bin/env bash
set -o pipefail
set -e

./gradlew app:clean app:build
docker build --no-cache -t app . -f app/docker/Dockerfile.jvm
if [[ $? -eq 0 ]];
then
  echo " created container image app:latest. Run this image using:
  docker run --rm -p 8080:8080 -d app
  "
fi;
