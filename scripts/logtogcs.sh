#!/bin/bash

gsutil -m  cp /var/log/pochak-prod/application.*.log gs://pochak-log-bucket/application/
# application.*.log 버킷 업로드
gsutil -m cp /var/log/pochak-prod/error.*.log gs://pochak-log-bucket/error/
# error.*.log 버킷 업로드

echo "GCS 업로드 완료"

rm -f /var/log/pochak-prod/application*.log
rm -f /var/log/pochak-prod/error*.log

echo "로컬 파일 삭제 완료"