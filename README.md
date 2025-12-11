# Steam Library Manager

A Dockerized application to automate the download and update of Steam games from your library. Built on the official `steamcmd` image with secure non-root execution and flexible scheduling.

## Features

- **Automated Management**: Download and update Steam games automatically
- **Multiple Strategies**: Combine manual and installed game detection strategies
- **Flexible Scheduling**: Cron-based scheduling with off-peak hours default
- **Secure Execution**: Non-root user execution with configurable PUID/PGID
- **Colorful Logging**: ANSI-colored output for better readability

## Environment Variables

| Variable                        | Required | Default            | Description                                                                                              |
|---------------------------------|----------|--------------------|----------------------------------------------------------------------------------------------------------|
| `PUID`                          | No       | `1000`             | User ID for non-root execution                                                                           |
| `PGID`                          | No       | `1000`             | Group ID for non-root execution                                                                          |
| `TZ`                            | No       | `Europe/Berlin`    | Time zone                                                                                                |
| `SCHEDULE`                      | No       | `0 0 0-11 * * *`   | Cron schedule (runs hourly from 12AM to 11AM)                                                            |
| `STEAM_WEB_API_KEY`             | No       | -                  | [Steam Web API Key](https://steamcommunity.com/dev/apikey). Used to obtain game/user data from steam api |
| `STEAM_USERNAME`                | No       | `anonymous`        | Steam account username                                                                                   |
| `STEAM_PASSWORD`                | No       | -                  | Steam account password                                                                                   |
| `STEAM_CMD_FILTER_OUTPUT`       | No       | `true`             | Removes SteamCMD output from console                                                                     |
| `STEAM_CMD_VALIDATE_INSTALLED`  | No       | `false`            | Use validate option during SteamCMD execution                                                            |
| `STEAM_APP_ID_RESOLVE_STRATEGY` | No       | `MANUAL,INSTALLED` | Comma-separated strategies: `MANUAL`, `INSTALLED`                                                        |
| `STEAM_MANUAL_APP_IDS`          | No       | -                  | Comma-separated app IDs (e.g., `570,440,730`)                                                            |

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
  -e STEAM_WEB_API_KEY="XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" \
  -e STEAM_USERNAME="your_username" \
  -e STEAM_PASSWORD="your_password" \
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
      - STEAM_USERNAME=your_username
      - STEAM_PASSWORD=your_password
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

### Building from Source
```bash
git clone https://github.com/DONTWARRYIMMAFINE/docker-steam-library-autoupdater
cd docker-steam-library-autoupdater
docker build -t docker-steam-library-autoupdater .
```
