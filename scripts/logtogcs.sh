#!/bin/bash

snap/bin/gsutil -m  cp /var/log/pochak-prod/application.*.log gs://pochak-log-bucket/application/
# application.*.log 버킷 업로드
snap/bin/gsutil -m cp /var/log/pochak-prod/error.*.log gs://pochak-log-bucket/error/
# error.*.log 버킷 업로드

echo "GCS 업로드 완료"

# crontab -l 로 0 0 * * * /home/ubuntu/logtogcs.sh 자정 실행 command 확인 가능
# /var/log/cron.log 에서 크론탭 실행 로그 확인 가능