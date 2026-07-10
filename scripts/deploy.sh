#!/bin/bash

set -e

IMAGE_TAG=$1
TARGET_COLOR=$2

echo "BE 배포 시작"
echo "Image Tag: $IMAGE_TAG"
echo "Target Color: $TARGET_COLOR"

if [ "$TARGET_COLOR" = "blue" ]; then
  COMPOSE_FILE="compose.blue.yml"
  TARGET_PORT=8080
elif [ "$TARGET_COLOR" = "green" ]; then
  COMPOSE_FILE="compose.green.yml"
  TARGET_PORT=8081
else
  echo "잘못된 Target Color: $TARGET_COLOR"
  exit 1
fi

PROJECT_NAME="community-be-${TARGET_COLOR}"

echo "Compose File: $COMPOSE_FILE"
echo "Target Port: $TARGET_PORT"
echo "Project Name: $PROJECT_NAME"

BE_IMAGE_TAG="$IMAGE_TAG" docker compose \
  -p "$PROJECT_NAME" \
  -f "$COMPOSE_FILE" \
  up -d --pull always

echo "Health Check 시작"

for i in {1..10}; do
  if curl -fsS "http://localhost:${TARGET_PORT}/posts" > /dev/null; then
    echo "Health Check 성공"
    echo "BE $TARGET_COLOR 배포 완료"
    exit 0
  fi

  echo "Health Check 재시도: $i/10"
  sleep 3
done

echo "Health Check 실패"
exit 1