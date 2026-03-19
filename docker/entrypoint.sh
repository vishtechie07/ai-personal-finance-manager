#!/bin/sh
set -eu

java -jar /app/app.jar &

sleep 3

exec nginx -g 'daemon off;'

