#!/bin/bash
set -e
set -u
export SCRIPT_PATH=`( cd \`dirname "$0"\` && pwd )`
export DC_FILES="-f docker-compose.yml"

commands_exists() {
    for cmd in $@
    do
        if ! command -v "$cmd" > /dev/null 2>&1; then
            error "this script needs the ability to run $cmd command"
        fi
    done
}

error() {
    echo -e "\033[33;31m \nError: $@\n\033[33;0m"; exit 1
}

for profile in "$@"
do
    export DC_FILES="$DC_FILES -f docker-compose-$profile.yml"
done

commands_exists docker docker-compose
docker-compose ${DC_FILES} build --compress --force-rm