#!/bin/bash

set -e
set -o pipefail

cd "$(dirname "$0")"

gradle clean
gradle compileGwt
gradle build
gradle buildProductDev

. ./tunnel.sh

cd build/output

#do includes *before* excludes

rsync	--include '/webapps/*.*' --exclude '/webapps/*/' \
		--exclude /work/ \
		--exclude /temp/ \
		-e "ssh -p $PORT" --progress --verbose --human-readable --compress --delete-after -a \
	Dev/ "$USER"@"$HOST":Dev/
	
#rsync	--include '*.*' --exclude '*/' \
#	-e "ssh -p $PORT" --progress --verbose --human-readable --compress --delete-after -a \
#	Dev/webapps/ "$USER"@"$HOST":Dev/webapps/

ssh -p "$PORT" "$USER"@"$HOST" 'cd Dev; bash stop.sh'

ssh -p "$PORT" "$USER"@"$HOST" 'cd Dev; (bash start.sh < /dev/null > /dev/null 2>&1) & sleep 3'
