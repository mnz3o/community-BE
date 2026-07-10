#!/bin/bash

set -e

TARGET_COLOR=$1
RULE_ARN=$2

echo "BE ALB 전환 시작"
echo "Target Color: $TARGET_COLOR"

if [ "$TARGET_COLOR" = "blue" ]; then
  TARGET_TG_NAME="community-be-tg"
  PREVIOUS_TG_NAME="community-be-green-tg"
elif [ "$TARGET_COLOR" = "green" ]; then
  TARGET_TG_NAME="community-be-green-tg"
  PREVIOUS_TG_NAME="community-be-tg"
else
  echo "잘못된 Target Color: $TARGET_COLOR"
  exit 1
fi

TARGET_TG_ARN=$(aws elbv2 describe-target-groups \
  --names "$TARGET_TG_NAME" \
  --query 'TargetGroups[0].TargetGroupArn' \
  --output text)

PREVIOUS_TG_ARN=$(aws elbv2 describe-target-groups \
  --names "$PREVIOUS_TG_NAME" \
  --query 'TargetGroups[0].TargetGroupArn' \
  --output text)

echo "BE ALB 트래픽 전환: $TARGET_COLOR"

aws elbv2 modify-rule \
  --rule-arn "$RULE_ARN" \
  --actions Type=forward,TargetGroupArn="$TARGET_TG_ARN"

echo "새 Target Group Healthy 대기"

if aws elbv2 wait target-in-service \
  --target-group-arn "$TARGET_TG_ARN"; then

  echo "새 Target Group Healthy"
  echo "BE ALB 전환 완료"
  echo "Active Color: $TARGET_COLOR"

else

  echo "Health Check 실패"
  echo "기존 Target Group으로 롤백"

  aws elbv2 modify-rule \
    --rule-arn "$RULE_ARN" \
    --actions Type=forward,TargetGroupArn="$PREVIOUS_TG_ARN"

  echo "롤백 완료"
  exit 1
fi