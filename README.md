# Steam Library Manager

A Dockerized application to automate the download and update of Steam games from your library. Built on [`steamcmd/steamcmd:debian-12`](https://github.com/steamcmd/docker) with non-root execution and flexible scheduling.

## Features

- **Automated Management**: Download and update Steam games automatically
- **Shared SteamCMD Session**: Update the entire game list sequentially with one login per scheduled run
- **Multiple Strategies**: Combine manual and installed game detection strategies
- **Flexible Scheduling**: Cron-based scheduling with off-peak hours default
- **Secure Execution**: Non-root user execution with configurable PUID/PGID
- **Colorful Logging**: ANSI-colored output for better readability

## Environment Variables

| Variable                        | Required | Default            | Description                                                                                                                          |
|---------------------------------|----------|--------------------|--------------------------------------------------------------------------------------------------------------------------------------|
| `PUID`                          | No       | `1000`             | User ID for non-root execution                                                                                                       |
| `PGID`                          | No       | `1000`             | Group ID for non-root execution                                                                                                      |
| `TZ`                            | No       | `Europe/Berlin`    | Time zone                                                                                                                            |
| `JAVA_TOOL_OPTIONS`              | No       | `-Xmx512m -Xms256m -Dfile.encoding=UTF-8` | JVM options, applied automatically by Java. Override this variable to change memory limits.                              |
| `SCHEDULE`                      | No       | `0 0 0-11 * * *`   | Cron schedule (runs hourly from 12AM to 11AM)                                                                                        |
| `STEAM_WEB_API_KEY`             | No       | -                  | [Steam Web API Key](https://steamcommunity.com/dev/apikey). Used to obtain user information.                                         |
| `STEAM_ID`                      | No       | -                  | Steam64 ID (ex. 76561197960287930). Used to obtain user status.                                                                      |
| `STEAM_USERNAME`                | No       | `anonymous`        | Steam account username                                                                                                               |
| `STEAM_PASSWORD`                | No       | -                  | Steam account password                                                                                                               |
| `STEAM_ALLOWED_STATES`          | No       | `UNKNOWN,OFFLINE`  | Comma-separated steam user state allowed to start update. (OFFLINE,ONLINE,BUSY,AWAY,SNOOZE,LOOKING_TO_TRADE,LOOKING_TO_PLAY,UNKNOWN) |
| `STEAM_CMD_FILTER_OUTPUT`       | No       | `true`             | Removes SteamCMD output from console                                                                                                 |
| `STEAM_CMD_VALIDATE_INSTALLED`  | No       | `false`            | Use validate option during SteamCMD execution                                                                                        |
| `STEAM_CMD_LOGIN_COOLDOWN`      | No       | `1h`               | Minimum pause after SteamCMD authentication failures (e.g. `30m`, `1h`, `2h`)                                                         |
| `STEAM_APP_ID_RESOLVE_STRATEGY` | No       | `MANUAL,INSTALLED` | Comma-separated strategies: `MANUAL`, `INSTALLED`                                                                                    |
| `STEAM_MANUAL_APP_IDS`          | No       | -                  | Comma-separated app IDs (e.g., `570,440,730`)                                                                                        |

## Quick Start

### Docker Run

```bash
docker run -d \
  --name dsla \
  -v /path/on/host:/home/steam/Steam \
  -e PUID=1000 \
  -e PGID=1000 \
  -e TZ=Europe/Berlin \
  -e SCHEDULE="0 0 0-11 * * *" \
  -e STEAM_WEB_API_KEY="your_steam_web_api_key" \
  -e STEAM_ID="your_steam_id" \
  -e STEAM_USERNAME="your_username" \
  -e STEAM_PASSWORD="your_password" \
  -e STEAM_ALLOWED_STATES="UNKNOWN,OFFLINE,AWAY,SNOOZE" \
  -e STEAM_APP_ID_RESOLVE_STRATEGY="MANUAL,INSTALLED" \
  -e STEAM_MANUAL_APP_IDS="730,440" \
  -e STEAM_IGNORE_APP_IDS="520" \
  dontworryimmafine/steam-library-autoupdater:latest
```

### Docker Compose
```yaml
version: '3.8'

services:
  steam-library-autoupdater:
    image: dontworryimmafine/steam-library-autoupdater:latest
    container_name: dsla
    restart: unless-stopped
    volumes:
      - /path/on/host:/home/steam/Steam
    environment:
      - PUID=1000
      - PGID=1000
      - TZ=Europe/Berlin
      - SCHEDULE=0 0 0-11 * * *
      - STEAM_WEB_API_KEY=your_steam_web_api_key
      - STEAM_ID=your_steam_id
      - STEAM_USERNAME=your_username
      - STEAM_PASSWORD=your_password
      - STEAM_ALLOWED_STATES=UNKNOWN,OFFLINE,AWAY,SNOOZE
      - STEAM_APP_ID_RESOLVE_STRATEGY=MANUAL,INSTALLED
      - STEAM_MANUAL_APP_IDS=730,440
      - STEAM_IGNORE_APP_IDS=520
```

### AppID Discovery Strategies

##### MANUAL Strategy

Specify app IDs directly via STEAM_MANUAL_APP_IDS environment variable:

```text
STEAM_MANUAL_APP_IDS="730,440,570"
```

##### INSTALLED Strategy

Automatically detects installed games by parsing appmanifest_*.acf files in your Steam directory:
```text
/steam/steamapps/appmanifest_570.acf → app_id: 570
```

##### Combined Strategies

Use multiple strategies together:
```text
STEAM_APP_ID_RESOLVE_STRATEGY="MANUAL,INSTALLED"
```

### Scheduling
The application uses cron expressions for scheduling. Default schedule (0 0 0-11 * * *) runs every hour from midnight to 11 AM.

### SteamCMD sessions and authentication

Each scheduled run starts one SteamCMD process, logs in using cached credentials, and updates all resolved games sequentially using repeated `+app_update` command-line arguments. No temporary command files are created. A library with 100 games therefore needs one cached login for the run. The process exits when the batch finishes. Games already up to date are checked by SteamCMD, and `validate` remains optional.

If SteamCMD explicitly reports missing cached credentials and `STEAM_PASSWORD` is set, the application retries the batch once with the password. Approve Steam Guard when requested. Invalid passwords, Steam Guard timeouts, login throttling, and other recognized login failures end the run without immediate retries. Subsequent scheduled runs are skipped until `STEAM_CMD_LOGIN_COOLDOWN` has elapsed; the next attempt still follows `SCHEDULE`. The cooldown is kept in memory and resets when the application restarts. Its default is an application setting, not a guaranteed Steam unblock time.

A failure updating one game allows the remaining games to continue in the same session. The summary reports updated, already up-to-date, and failed games separately. If SteamCMD exits before reporting a result for a game, that game is reported as failed.

Preserve SteamCMD's `config/config.vdf` between container replacements to retain cached authentication. The game library (`/home/steam/Steam`) and SteamCMD installation (`/home/steam/steamcmd`) are separate paths in this image. Verify where SteamCMD writes its configuration in your container, persist that directory too, and make it writable by the configured `PUID`/`PGID`. A game-library mount alone does not establish that SteamCMD's login cache is persisted. Valve describes cached logins in its [SteamPipe automation documentation](https://partner.steamgames.com/doc/sdk/uploading#Using_SteamPipe_In_A_CI_CD_Environment).

The Steam Web API remains responsible for the configured player-state check. SteamCMD determines which game content needs updating. See [SteamCMD and Web API research](docs/steam-update-research.md) for the sources and API limitations.

### Local verification

```bash
./gradlew test ktlintCheck build
```

The regression tests use a local SteamCMD process fixture. They cover session reuse, bounded credential retries, authentication cooldown, per-game results, and incomplete batches without contacting Steam or downloading games.

### Building from Source

The multi-stage Dockerfile uses Gradle 8.14.3 and Java 17 to build the executable Spring Boot JAR. The runtime uses `steamcmd/steamcmd:debian-12` from the [SteamCMD Docker project](https://github.com/steamcmd/docker), with the Java 17 runtime added. The entrypoint configures `PUID`/`PGID` as root, then runs Java and SteamCMD as `steam`. Existing `/home/steam/Steam` and `/home/steam/steamcmd` paths are preserved. Scheduling is handled by the application; no system cron daemon is needed.

Use `JAVA_TOOL_OPTIONS` for JVM settings; the previous `JAVA_OPTS` variable was not passed to Java by the entrypoint.

```bash
git clone https://github.com/DONTWARRYIMMAFINE/docker-steam-library-autoupdater
cd docker-steam-library-autoupdater
docker build -t docker-steam-library-autoupdater .
```

`PUID` and `PGID` also accept build arguments to set the image's default user and group IDs:

```bash
docker build --build-arg PUID=1026 --build-arg PGID=100 -t docker-steam-library-autoupdater .
```

Both default to `1000`. Runtime environment variables, including those in Compose, override these defaults without rebuilding the image. Existing group IDs are reused. Local `src/main/resources/env/` configuration is excluded from the application JAR and Docker build context; supply Steam credentials at runtime.
