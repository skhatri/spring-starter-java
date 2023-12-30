#!/usr/bin/env bash
set -o pipefail
set -e

cmd=$1

case $cmd in

spring)
  ./gradlew spring-starter:clean spring-starter:build
  docker build --no-cache -t spring-starter spring-starter -f spring-starter/docker/Dockerfile.jvm
  ;;

quarkus)
  ./gradlew quarkus-starter:clean quarkus-starter:build
  docker build --no-cache -t quarkus-starter quarkus-starter -f quarkus-starter/docker/Dockerfile.jvm
  ;;


quarkus-native-distroless)
  ./gradlew quarkus-starter:clean quarkus-starter:build \
  -Pquarkus.package.type=native \
  -Pquarkus.container-image.build=true \
  -Pquarkus.native.container-runtime=docker
  docker build --no-cache -t quarkus-starter-native-distroless quarkus-starter -f quarkus-starter/docker/Dockerfile.native-distroless
  ;;

quarkus-scratch)
  docker build --no-cache -t quarkus-starter-scratch . -f quarkus-starter/docker/Dockerfile.scratch
;;

quarkus-native-alpine)
  docker build --no-cache -t quarkus-starter-alpine . -f quarkus-starter/docker/Dockerfile.native-alpine
  ;;

quarkus-native)
  ./gradlew quarkus-starter:clean quarkus-starter:build -Pquarkus.package.type=native
  docker build --no-cache -t quarkus-starter-native quarkus-starter -f quarkus-starter/docker/Dockerfile.native
;;

quarkus-native-micro)
  ./gradlew quarkus-starter:clean quarkus-starter:build -Pquarkus.package.type=native
  docker build --no-cache -t quarkus-starter-native-micro quarkus-starter -f quarkus-starter/docker/Dockerfile.native-micro
;;


*)
  echo not known
  exit 1;
;;

esac

