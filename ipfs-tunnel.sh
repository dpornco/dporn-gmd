#!/bin/bash
set -e
set -o pipefail

#used to redirect local port to remote IPFS API
#assumes you have setup ssh shared key
#pulls basic connection info from local tunnel.sh file, not part of repo

. ./tunnel.sh

echo "IPFS TUNNEL"

ssh -N -L "$IPFS_GW_LOCAL_PORT:$IPFS_GW_REMOTE_HOST:$IPFS_GW_REMOTE_PORT" -L "$IPFS_LOCAL_PORT:$IPFS_REMOTE_HOST:$IPFS_REMOTE_PORT" -p "$IPFS_SSH_PORT" "$IPFS_SSH_USER"@"$IPFS_SSH_HOST"

exit 0
