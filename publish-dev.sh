#!/bin/bash

set -e
set -o pipefail

cd "$(dirname "$0")"
. ./tunnel.sh

cd build/output

rsync -e "ssh -p $PORT" --progress --verbose --human-readable --compress --delete-after -a Dev/ "$USER"@"$HOST":Dev/

ssh -p "$PORT" "$USER"@"$HOST" 'cd Dev; bash stop.sh'

ssh -p "$PORT" "$USER"@"$HOST" 'cd Dev; bash start.sh'
