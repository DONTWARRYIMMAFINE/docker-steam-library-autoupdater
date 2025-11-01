# Build an app
FROM gradle:8.5-jdk17-alpine AS builder

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts ./
COPY src ./src

RUN gradle build -x test --no-daemon

# Final image
FROM cm2network/steamcmd:root

# Set environment variables
ENV DEBIAN_FRONTEND=noninteractive \
    LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    TZ=Europe/Berlin \
    TERM=xterm-256color \
    PUID=1000 \
    PGID=1000 \
    SCHEDULE="0 0 0-11 * * *" \
    STEAM_ROOT=/home/steam/Steam \
    STEAM_CMD_ROOT=/home/steam/steamcmd \
    STEAM_CMD_REDIRECT_OUTPUT=false \
    STEAM_CMD_FILTER_OUTPUT=true \
    STEAM_CMD_VALIDATE_INSTALLED=false \
    STEAM_APP_ID_RESOLVE_STRATEGY=MANUAL,INSTALLED \
    STEAM_MANUAL_APP_IDS="" \
    STEAM_IGNORE_APP_IDS="" \
    STEAM_USERNAME="" \
    STEAM_PASSWORD="" \
    JAVA_OPTS="-Xmx512m -Xms256m -Dfile.encoding=UTF-8"

# Install required packages with locales
RUN set -x \
    && apt-get update \
    && apt-get install -y --no-install-recommends \
        cron \
        gosu \
        procps \
        openjdk-17-jre-headless \
        locales \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# Generate and set locale
RUN sed -i '/en_US.UTF-8/s/^# //g' /etc/locale.gen && \
    locale-gen && \
    update-locale LANG=en_US.UTF-8

# Copy entrypoint script
COPY --chmod=755 entrypoint.sh /
COPY --chmod=755 includes/ /includes

ENTRYPOINT ["/entrypoint.sh"]

# Copy JAR from builder stage
COPY --from=builder --chown=steam:steam /app/build/libs/*.jar /app/app.jar
CMD ["java", "-jar", "/app/app.jar"]
