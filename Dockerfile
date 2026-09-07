FROM gradle:8.14.3-jdk17 AS builder

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts ./
COPY src ./src

RUN gradle --no-daemon bootJar

FROM steamcmd/steamcmd:debian-12

USER root

ARG DEBIAN_FRONTEND=noninteractive
ARG PUID=1000
ARG PGID=1000

ENV LANG=en_US.UTF-8 \
    LC_ALL=en_US.UTF-8 \
    TZ=Europe/Berlin \
    TERM=xterm-256color \
    PUID=${PUID} \
    PGID=${PGID} \
    SCHEDULE="0 0 0-11 * * *" \
    STEAM_ROOT=/home/steam/Steam \
    STEAM_CMD_ROOT=/home/steam/steamcmd \
    STEAM_CMD_FILTER_OUTPUT=true \
    STEAM_CMD_VALIDATE_INSTALLED=false \
    STEAM_CMD_LOGIN_COOLDOWN=1h \
    STEAM_APP_ID_RESOLVE_STRATEGY=MANUAL,INSTALLED \
    STEAM_ALLOWED_STATES=OFFLINE,AWAY,SNOOZE,UNKNOWN \
    STEAM_MANUAL_APP_IDS="" \
    STEAM_IGNORE_APP_IDS=""

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        gosu \
        openjdk-17-jre-headless \
        tzdata \
    && rm -rf /var/lib/apt/lists/*

RUN test "$PUID" -gt 0 && test "$PGID" -gt 0 \
    && if ! getent group "$PGID" > /dev/null; then groupadd --gid "$PGID" steam; fi \
    && useradd --uid "$PUID" --gid "$PGID" --create-home --shell /bin/bash steam

# Reuse the initialized SteamCMD installation with the existing non-root user and mount paths.
RUN mv /root/.local/share/Steam/steamcmd "$STEAM_CMD_ROOT" \
    && mkdir -p "$STEAM_ROOT" /home/steam/.steam \
    && ln -s "$STEAM_CMD_ROOT" /home/steam/.steam/steamcmd \
    && ln -s "$STEAM_ROOT" /home/steam/.steam/root \
    && ln -s "$STEAM_ROOT" /home/steam/.steam/steam \
    && ln -s "$STEAM_CMD_ROOT/linux32" /home/steam/.steam/sdk32 \
    && ln -s "$STEAM_CMD_ROOT/linux64" /home/steam/.steam/sdk64 \
    && ln -sf steamclient.so "$STEAM_CMD_ROOT/linux32/steamservice.so" \
    && ln -sf steamclient.so "$STEAM_CMD_ROOT/linux64/steamservice.so" \
    && chown -R "$PUID:$PGID" /home/steam

ENV JAVA_TOOL_OPTIONS="-Xmx512m -Xms256m -Dfile.encoding=UTF-8"

WORKDIR /app

COPY --from=builder --chown=${PUID}:${PGID} /app/build/libs/*.jar /app/app.jar
COPY --chmod=755 entrypoint.sh /entrypoint.sh
COPY --chmod=755 includes/ /includes/

ENTRYPOINT ["/entrypoint.sh"]
CMD ["java", "-jar", "/app/app.jar"]
