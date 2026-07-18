#!/bin/bash

# 명령 실패, 정의되지 않은 변수 사용, 파이프라인 오류 시 즉시 종료
set -euo pipefail

# 전달받은 이미지 태그 확인
IMAGE_TAG=${1:?IMAGE_TAG가 필요합니다}

ACTIVE_FILE="/home/ssm-user/community-app/be-active-color"

NGINX_DIR="/home/ssm-user/community-app/nginx"
BE_UPSTREAM_FILE="$NGINX_DIR/upstreams-be.conf"

echo "BE 배포 시작"
echo "Image Tag: $IMAGE_TAG"


# 현재 활성 환경 확인
# 최초 배포는 Blue 활성으로 가정
if [ -f "$ACTIVE_FILE" ]; then
  ACTIVE_COLOR=$(cat "$ACTIVE_FILE")
else
  ACTIVE_COLOR="blue"
fi

echo "Active Color: $ACTIVE_COLOR"


# 현재 활성의 반대편에 배포
if [ "$ACTIVE_COLOR" = "blue" ]; then

  TARGET_COLOR="green"
  COMPOSE_FILE="compose.green.yml"
  TARGET_PORT=8081

elif [ "$ACTIVE_COLOR" = "green" ]; then

  TARGET_COLOR="blue"
  COMPOSE_FILE="compose.blue.yml"
  TARGET_PORT=8080

else
  echo "잘못된 Active Color: $ACTIVE_COLOR"
  exit 1
fi


PROJECT_NAME="community-be-${TARGET_COLOR}"

echo "Target Color: $TARGET_COLOR"
echo "Compose File: $COMPOSE_FILE"
echo "Target Port: $TARGET_PORT"
echo "Project Name: $PROJECT_NAME"


# 대상 환경 실행
BE_IMAGE_TAG="$IMAGE_TAG" docker compose \
  -p "$PROJECT_NAME" \
  -f "$COMPOSE_FILE" \
  up -d --pull always


echo "Health Check 시작"


# 새 컨테이너 Health Check
for i in {1..20}; do

  if curl -fsS "http://localhost:${TARGET_PORT}/posts" > /dev/null; then

    echo "Health Check 성공"


    # BE Nginx upstream 변경
    cat > "$BE_UPSTREAM_FILE" <<EOF
upstream backend {
    server 127.0.0.1:${TARGET_PORT};
}
EOF


    echo "BE Nginx 전환"


    # nginx 검증 후 reload
    docker exec community-app-nginx nginx -t
    docker exec community-app-nginx nginx -s reload


    # 활성 색상 기록
    echo "$TARGET_COLOR" > "$ACTIVE_FILE"


    echo "BE $TARGET_COLOR 배포 완료"
    exit 0

  fi


  echo "Health Check 재시도: $i/20"
  sleep 3

done


echo "Health Check 실패"
exit 1