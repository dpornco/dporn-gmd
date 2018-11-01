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

#recursive rsync
rsync	--exclude /work/ \
		--exclude /temp/ \
		--progress --verbose --human-readable --compress --delete-after -a \
		-e "ssh -p $PORT" \
	Dev/ "$USER"@"$HOST":Dev/
	
ssh -p "$PORT" "$USER"@"$HOST" 'cd Dev; bash stop.sh' || true

ssh -p "$PORT" "$USER"@"$HOST" 'cd Dev; (bash start.sh < /dev/null > /dev/null 2>&1) & sleep 3' || true
