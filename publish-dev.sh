#!/bin/bash

set -e
set -o pipefail

cd "$(dirname "$0")"

gradle clean
gradle build
gradle buildProductDev

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
ssh -p "$PORT" "$USER"@"$HOST" 'cd Dev; (bash start.sh < /dev/null > /dev/null 2>&1) & sleep 3' || true
echo "DONE"
