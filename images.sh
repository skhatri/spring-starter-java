#!/usr/bin/env bash
set -o pipefail
set -e

cmd=$1

case $cmd in

spring)
  ./gradlew app:clean app:build
  docker build --no-cache -t spring-starter app -f app/docker/Dockerfile.jvm
  ;;

*)
  echo not known
  exit 1;
;;

esac

