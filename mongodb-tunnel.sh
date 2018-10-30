#!/bin/bash
set -e
set -o pipefail

#used to redirect local port to remote mongodb - should only be used in a READONLY way for testing
#assumes you have setup ssh shared key
#pulls basic connection info from local tunnel.sh file, not part of repo

. ./tunnel.sh

ssh -N -L "$MONGO_LOCAL_PORT:$MONGO_REMOTE_HOST:$MONGO_REMOTE_PORT" -p "$PORT" "$USER"@"$HOST"

exit 0
