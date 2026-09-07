package com.github.dontworryimmafine.dsla.service.handler.impl

import com.github.dontworryimmafine.dsla.model.ResultMessage
import com.github.dontworryimmafine.dsla.service.handler.OutputHandler

/**
 * Associates recognized update results with the app IDs embedded in their output lines.
 *
 * @param resultHandler Handler that classifies an individual app result line as a success or error.
 */
class AppUpdateOutputHandler(
    private val resultHandler: OutputHandler,
) : OutputHandler {
    /**
     * Searches output from newest to oldest for a recognized result containing an app ID.
     * Lines with absent or unparseable IDs, or no result recognized by the delegate, are skipped.
     *
     * @param output SteamCMD lines in their original chronological order; may be empty.
     * @return The latest matching result with its parsed app ID, or null when no line matches.
     * The delegate's message text and classification are preserved.
     */
    override fun handle(output: List<String>): ResultMessage? {
        for (line in output.asReversed()) {
            val match = appIdPattern.find(line) ?: continue
            val appId = match.groupValues[1].toLongOrNull() ?: continue
            val result = resultHandler.handle(listOf(line)) ?: continue
            return result.copy(appId = appId)
        }
        return null
    }

    companion object {
        private val appIdPattern = Regex("\\bapp\\s+['\"]?(\\d+)['\"]?(?=\\s|[.,:]|$)", RegexOption.IGNORE_CASE)
    }
}
