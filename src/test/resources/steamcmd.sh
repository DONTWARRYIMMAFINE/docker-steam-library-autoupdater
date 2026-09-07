#!/usr/bin/env bash
# Offline process fixture: executes SteamCMD-style arguments without contacting Steam.
set -euo pipefail

echo process >> processes.log
echo "$$" > process.pid
scenario=$(<scenario)
shutdown_on_failure=0
no_prompt=0
platform=linux

login() {
    local password="$1"
    [[ "$shutdown_on_failure" == 1 && "$no_prompt" == 1 && "$platform" == windows ]] || exit 90

    if [[ -n "$password" ]]; then
        echo password-login >> calls.log
    else
        echo cached-login >> calls.log
    fi

    case "$scenario" in
        cache-never) echo 'No cached credentials found.'; exit 5 ;;
        cache-retry)
            if [[ -z "$password" ]]; then echo 'No cached credentials found.'; exit 5; fi ;;
        rate-limit) echo 'FAILED (Rate Limit Exceeded)'; exit 5 ;;
        invalid-password) echo 'FAILED (Invalid Password)'; exit 5 ;;
        guard-timeout) echo 'FAILED (Wait for confirmation timed out)'; exit 5 ;;
        login-failed) echo 'FAILED (No Connection)'; exit 5 ;;
    esac

    echo 'Waiting for user info...OK'
    if [[ "$scenario" == echo-password ]]; then echo "credential: $password"; fi
}

fail_app() {
    local app_id="$1"
    local reason="$2"
    echo "ERROR! Failed to install app '$app_id' ($reason)"
    if [[ "$shutdown_on_failure" == 1 ]]; then exit 8; fi
}

update_app() {
    local app_id="$1"
    local validation="$2"
    local command="app_update $app_id${validation:+ validate}"
    echo "$command" >> calls.log

    # Command echoes are optional; parsing must also work without them.
    if [[ "$scenario" == missing-result ]]; then
        echo "$command"
        if [[ "$app_id" == 10 ]]; then return; fi
    fi
    if [[ "$scenario" == startup-only ]]; then
        echo 'Success! SteamCMD update downloaded.'
        exit 0
    fi

    echo ' Update state (0x61) downloading, progress: 50.00 (50 / 100)'
    if [[ -n "$validation" ]]; then
        echo ' Update state (0x101) verifying install, progress: 50.00 (50 / 100)'
    fi
    if [[ "$scenario" == hold ]]; then sleep 30; fi

    case "$scenario:$app_id" in
        partial-exit:20) exit 42 ;;
        app-password-error:10) fail_app "$app_id" 'Invalid Password' ;;
        mixed:10) fail_app "$app_id" 'No subscription' ;;
        mixed:30) echo "Success! App '$app_id' already up to date." ;;
        *) echo "Success! App '$app_id' fully installed." ;;
    esac
}

while (( $# )); do
    case "$1" in
        +@ShutdownOnFailedCommand) shutdown_on_failure="$2"; shift 2 ;;
        +@NoPromptForPassword) no_prompt="$2"; shift 2 ;;
        +@sSteamCmdForcePlatformType) platform="$2"; shift 2 ;;
        +login)
            shift 2
            password=""
            if (( $# )) && [[ "$1" != +* ]]; then
                password="$1"
                shift
            fi
            login "$password"
            ;;
        +app_update)
            app_id="$2"
            shift 2
            validation=""
            if [[ "${1:-}" == validate ]]; then
                validation=validate
                shift
            fi
            update_app "$app_id" "$validation"
            ;;
        +runscript)
            echo 'File-based commands are not supported by this fixture.'
            exit 91
            ;;
        +quit)
            echo 'Unloading Steam API...OK'
            exit 0
            ;;
        *)
            echo 'Unexpected SteamCMD argument.'
            exit 92
            ;;
    esac
done

echo 'SteamCMD fixture did not receive +quit.'
exit 93
