#!/bin/bash

set -e
set -o pipefail

export SOURCE="$(pwd)/build/libs/dporn-gmd.war"
export DEST="/var/lib/tomcat8/webapps/dev-dporn-gmd.war"

cd "$(dirname "$0")"

./gradlew clean
./gradlew build

. ./tunnel.sh

cd 

#single file rsync directly to tomcat webapps folder, must be group member of tomcat8
rsync	--progress --verbose --human-readable --compress -a \
		-e "ssh -p $PORT" "$SOURCE" "$USER"@"$HOST":"$DEST"
	
echo "DONE"
