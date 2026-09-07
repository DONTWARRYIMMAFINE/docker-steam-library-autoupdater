package com.github.dontworryimmafine.dsla.service.handler.impl

import com.github.dontworryimmafine.dsla.model.MessageType
import com.github.dontworryimmafine.dsla.model.ResultMessage
import com.github.dontworryimmafine.dsla.service.handler.OutputHandler

class LoginFailureOutputHandler : OutputHandler {
    /**
     * Classifies authentication errors with rate-limit messages taking precedence.
     * Returns the latest rate-limit line if present, otherwise the latest generic login failure.
     *
     * @param output SteamCMD lines in their original chronological order; may be empty.
     * @return A LOGIN_RATE_LIMIT or LOGIN_FAILED result preserving the matched text,
     * or null if neither kind of failure is recognized.
     */
    override fun handle(output: List<String>): ResultMessage? {
        val rateLimit = output.lastOrNull(::isRateLimited)
        if (rateLimit != null) return ResultMessage(rateLimit, MessageType.LOGIN_RATE_LIMIT)

        val failure = output.lastOrNull(::isLoginFailure) ?: return null
        return ResultMessage(failure, MessageType.LOGIN_FAILED)
    }

    /**
     * Checks for known rate-limit and login-throttling phrases without regard to case.
     *
     * @param line One SteamCMD output line to inspect.
     * @return True if the line contains a recognized throttling phrase; false otherwise.
     */
    private fun isRateLimited(line: String): Boolean =
        line.contains("rate limit", ignoreCase = true) ||
            line.contains("ratelimit", ignoreCase = true) ||
            line.contains("too many login", ignoreCase = true) ||
            line.contains("account logon denied throttle", ignoreCase = true)

    /**
     * Checks for SteamCMD's generic FAILED status or an explicit failed-login phrase.
     * Matching is case-insensitive; this check does not identify a specific failure cause.
     *
     * @param line One SteamCMD output line to inspect.
     * @return True if the line contains a recognized login-failure phrase; false otherwise.
     */
    private fun isLoginFailure(line: String): Boolean =
        line.contains("FAILED (", ignoreCase = true) ||
            line.contains("failed to log in", ignoreCase = true) ||
            line.contains("failed to login", ignoreCase = true)
}
