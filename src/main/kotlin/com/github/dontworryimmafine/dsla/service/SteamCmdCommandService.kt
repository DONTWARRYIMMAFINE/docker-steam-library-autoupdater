package com.github.dontworryimmafine.dsla.service

import com.github.dontworryimmafine.dsla.config.properties.SteamProperties
import org.springframework.stereotype.Service

@Service
class SteamCmdCommandService(
    private val properties: SteamProperties,
) {
    /**
     * Builds a SteamCMD invocation with one login, all requested updates, and a final quit.
     *
     * Targets Windows and applies the configured validation option. Login failures stop execution;
     * individual app failures allow later updates to continue. The arguments are passed
     * directly to the process without shell quoting or a command script.
     *
     * @param appIds Non-empty list of positive app IDs, in the order they should be updated.
     * @param usePassword Whether to include the configured password when it is non-blank;
     * defaults to false to use cached credentials.
     * @return The executable path followed by its arguments, including a password if requested.
     * @throws IllegalArgumentException If [appIds] is empty or contains a non-positive ID.
     */
    fun buildUpdateCommand(
        appIds: List<Long>,
        usePassword: Boolean = false,
    ): List<String> =
        buildList {
            require(appIds.isNotEmpty()) { "No applications to update" }
            require(appIds.all { it > 0 }) { "App IDs must be positive" }

            add("./steamcmd.sh")
            addAll(listOf("+@NoPromptForPassword", "1"))
            addAll(listOf("+@sSteamCmdForcePlatformType", "windows"))
            addAll(listOf("+@ShutdownOnFailedCommand", "1"))
            addAll(listOf("+login", properties.username.ifBlank { "anonymous" }))
            if (usePassword && properties.password.isNotBlank()) add(properties.password)

            // Stop on login failure, but continue after individual game failures.
            addAll(listOf("+@ShutdownOnFailedCommand", "0"))
            appIds.forEach { appId ->
                addAll(listOf("+app_update", appId.toString()))
                if (properties.cmdValidateInstalled) add("validate")
            }
            add("+quit")
        }
}
