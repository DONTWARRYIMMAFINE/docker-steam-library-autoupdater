package com.github.dontworryimmafine.dsla.service.handler.impl

import com.github.dontworryimmafine.dsla.model.MessageType
import com.github.dontworryimmafine.dsla.model.ResultMessage
import com.github.dontworryimmafine.dsla.service.handler.OutputHandler

class AppUpdateStartedOutputHandler : OutputHandler {
    /**
     * Recognizes echoed app_update commands, including an optional Steam prompt and validate flag.
     * Scans from newest to oldest and skips commands whose app IDs cannot be parsed as a Long.
     *
     * @param output SteamCMD lines in their original chronological order; may be empty.
     * @return An UPDATE_STARTED message with the original line and app ID, or null when no command matches.
     */
    override fun handle(output: List<String>): ResultMessage? {
        for (line in output.asReversed()) {
            val match = commandPattern.matchEntire(line.trim()) ?: continue
            val appId = match.groupValues[1].toLongOrNull() ?: continue
            return ResultMessage(line, MessageType.UPDATE_STARTED, appId)
        }
        return null
    }

    companion object {
        private val commandPattern = Regex("(?:Steam>\\s*)?app_update\\s+(\\d+)(?:\\s+validate)?", RegexOption.IGNORE_CASE)
    }
}
