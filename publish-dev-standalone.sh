#!/bin/bash

set -e
set -o pipefail

cd "$(dirname "$0")"

./gradlew clean
./gradlew build
./gradlew buildProductDev

. ./tunnel.sh

cd build/output

#do includes *before* excludes

echo "STOP REMOTE SERVICES"
ssh -p "$PORT" "$USER"@"$HOST" 'cd Dev; bash stop.sh' || true
#recursive rsync
rsync	--exclude /work/ \
		--exclude /temp/ \
		--progress --verbose --human-readable --compress --delete-before -a \
		-e "ssh -p $PORT" \
	Dev/ "$USER"@"$HOST":Dev/
	
echo "START REMOTE SERVICES"
ssh -p "$PORT" "$USER"@"$HOST" 'cd Dev; (bash start.sh) < /dev/null > tomcat.log 2> tomcat.err & sleep 6' || true
ssh -p "$PORT" "$USER"@"$HOST" 'cd Dev; (bash restart.sh || bash start.sh) < /dev/null > tomcat.log 2> tomcat.err & sleep 3'
echo "DONE"
