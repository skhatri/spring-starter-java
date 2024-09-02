#!/usr/bin/env bash
set -o pipefail
set -e

cmd=$1

case $cmd in

spring)
  ./gradlew app:clean app:build -x app:test
  docker build --no-cache -t spring-starter . -f app/docker/Dockerfile.jvm
  ;;

*)
  echo not known
  exit 1;
;;

esac

