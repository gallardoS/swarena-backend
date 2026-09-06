#!/usr/bin/env sh
set -eu

GAME_DIRECTORY=${GAME_DIRECTORY:-/home/ubuntu/swarena-game}

set -a
# shellcheck disable=SC1091
. "$GAME_DIRECTORY/.env"
set +a

docker compose --project-directory "$GAME_DIRECTORY" exec -T \
    -e MYSQL_PWD="$DOCKER_DB_ROOT_PASSWORD" \
    ac-database mysql -uroot -e "
        GRANT SELECT ON acore_characters.arena_team TO 'swarena_backend'@'%';
        GRANT SELECT ON acore_characters.arena_team_member TO 'swarena_backend'@'%';
        GRANT SELECT ON acore_characters.characters TO 'swarena_backend'@'%';
    "
