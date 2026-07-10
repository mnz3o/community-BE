#!/bin/bash

# 명령 실패, 정의되지 않은 변수 사용, 파이프라인 오류 시 즉시 종료
set -euo pipefail

# 워크플로우에서 배포 환경과 BE Rule ARN을 전달받음
TARGET_COLOR=${1:?TARGET_COLOR가 필요합니다}
RULE_ARN=${2:?RULE_ARN이 필요합니다}

echo "BE ALB 전환 시작"
echo "Target Color: $TARGET_COLOR"

# 배포 환경에 따라 전환할 Target Group 결정
if [ "$TARGET_COLOR" = "blue" ]; then
  TARGET_TG_NAME="community-be-tg"
elif [ "$TARGET_COLOR" = "green" ]; then
  TARGET_TG_NAME="community-be-green-tg"
else
  echo "잘못된 Target Color: $TARGET_COLOR"
  exit 1
fi

# Target Group 이름으로 ARN 조회
TARGET_TG_ARN=$(aws elbv2 describe-target-groups \
  --names "$TARGET_TG_NAME" \
  --query 'TargetGroups[0].TargetGroupArn' \
  --output text)

echo "Target Group: $TARGET_TG_NAME"
echo "Target Group Health Check 대기"

# 새 Target Group이 Healthy 상태인지 확인
if ! aws elbv2 wait target-in-service \
  --target-group-arn "$TARGET_TG_ARN"; then

  echo "Target Group Health Check 실패"
  echo "ALB 전환 중단"
  exit 1
fi

echo "Target Group Healthy 확인"

# Health Check가 성공한 경우에만 BE Rule의 트래픽 대상 변경
aws elbv2 modify-rule \
  --rule-arn "$RULE_ARN" \
  --actions Type=forward,TargetGroupArn="$TARGET_TG_ARN"

echo "BE ALB 전환 완료"
echo "Active Color: $TARGET_COLOR"