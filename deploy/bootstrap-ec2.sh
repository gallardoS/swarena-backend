#!/usr/bin/env sh
set -eu

GAME_DIRECTORY=${GAME_DIRECTORY:-/home/ubuntu/swarena-game}
DEPLOY_DIRECTORY=${DEPLOY_DIRECTORY:-/opt/swarena-backend}
TURNSTILE_ENV=${TURNSTILE_ENV:-/home/ubuntu/swarena-backend-turnstile.env}

if ! grep -Eq '^TURNSTILE_SECRET=.+' "$TURNSTILE_ENV"; then
    echo "TURNSTILE_SECRET is missing" >&2
    exit 1
fi

sudo install -d -o "$(id -un)" -g "$(id -gn)" -m 700 "$DEPLOY_DIRECTORY"

set -a
# shellcheck disable=SC1091
. "$GAME_DIRECTORY/.env"
set +a

backend_database_password=$(openssl rand -hex 32)

docker compose --project-directory "$GAME_DIRECTORY" exec -T \
    -e MYSQL_PWD="$DOCKER_DB_ROOT_PASSWORD" \
    ac-database mysql -uroot -e "
        CREATE USER IF NOT EXISTS 'swarena_backend'@'%' IDENTIFIED BY '${backend_database_password}';
        ALTER USER 'swarena_backend'@'%' IDENTIFIED BY '${backend_database_password}';
        GRANT INSERT ON acore_auth.account TO 'swarena_backend'@'%';
        GRANT INSERT ON acore_auth.realmcharacters TO 'swarena_backend'@'%';
        GRANT SELECT ON acore_auth.realmlist TO 'swarena_backend'@'%';
    "

temporary_env="$DEPLOY_DIRECTORY/.env.new"
umask 077
grep '^TURNSTILE_SECRET=' "$TURNSTILE_ENV" > "$temporary_env"
cat >> "$temporary_env" <<EOF
TURNSTILE_HOSTNAMES=arena.swami.dev
SPRING_DATASOURCE_URL=jdbc:mysql://ac-database:3306/acore_auth?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
SPRING_DATASOURCE_USERNAME=swarena_backend
SPRING_DATASOURCE_PASSWORD=${backend_database_password}
SWARENA_REGISTRATION_ENABLED=true
SWARENA_REGISTRATION_EXPANSION=2
EOF

mv "$temporary_env" "$DEPLOY_DIRECTORY/.env"
chmod 600 "$DEPLOY_DIRECTORY/.env"
rm -f "$TURNSTILE_ENV"

echo "EC2 backend configuration created in $DEPLOY_DIRECTORY"
