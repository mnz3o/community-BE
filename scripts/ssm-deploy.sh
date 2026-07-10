#!/bin/bash

set -e

IMAGE_TAG=$1
TARGET_COLOR=$2

INSTANCE_A="i-01d75fd54fbe9641a"
INSTANCE_C="i-041f03685bee07ddd"

echo "BE SSM 배포 시작"
echo "Image Tag: $IMAGE_TAG"
echo "Target Color: $TARGET_COLOR"

COMMAND_ID=$(aws ssm send-command \
  --instance-ids "$INSTANCE_A" "$INSTANCE_C" \
  --document-name "AWS-RunShellScript" \
  --parameters "commands=[
    \"cd /home/ssm-user/community-app/be\",
    \"curl -fsSLO https://raw.githubusercontent.com/mnz3o/community-BE/main/compose.blue.yml\",
    \"curl -fsSLO https://raw.githubusercontent.com/mnz3o/community-BE/main/compose.green.yml\",
    \"mkdir -p scripts\",
    \"curl -fsSL -o scripts/deploy.sh https://raw.githubusercontent.com/mnz3o/community-BE/main/scripts/deploy.sh\",
    \"bash scripts/deploy.sh $IMAGE_TAG $TARGET_COLOR\"
  ]" \
  --query "Command.CommandId" \
  --output text)

echo "Command ID: $COMMAND_ID"
echo "SSM 명령 실행 대기"

aws ssm wait command-executed \
  --command-id "$COMMAND_ID" \
  --instance-id "$INSTANCE_A"

echo "App EC2 A BE 배포 성공"

aws ssm wait command-executed \
  --command-id "$COMMAND_ID" \
  --instance-id "$INSTANCE_C"

echo "App EC2 C BE 배포 성공"
echo "BE SSM 배포 완료"
