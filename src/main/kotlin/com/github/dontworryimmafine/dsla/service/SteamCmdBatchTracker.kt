package com.github.dontworryimmafine.dsla.service

import com.github.dontworryimmafine.dsla.model.AppUpdateResult
import com.github.dontworryimmafine.dsla.model.MessageType
import com.github.dontworryimmafine.dsla.model.ResultMessage
import com.github.dontworryimmafine.dsla.model.SteamApp
import com.github.dontworryimmafine.dsla.model.SteamCmdBatchResult

/**
 * Collects app results and authentication failures for one sequential SteamCMD session.
 * Each session owns its tracker; access is confined to the thread consuming its output.
 *
 * @param apps Apps with distinct IDs in the same order as the SteamCMD update commands.
 * @param nanoTime Monotonic time source returning nanoseconds for measuring app update durations.
 */
internal class SteamCmdBatchTracker(
    apps: List<SteamApp>,
    nanoTime: () -> Long = System::nanoTime,
) {
    private val queue = SteamCmdUpdateQueue(apps, nanoTime)
    private val results: MutableMap<SteamApp, AppUpdateResult?> = apps.associateWith { null }.toMutableMap()
    private var sessionFailure: ResultMessage? = null

    /**
     * Records one classified message and identifies the app that should receive it.
     *
     * App completion messages save a result and advance the queue. Messages without an
     * app ID belong to the current app. Authentication failures are stored for the whole
     * session; the caller remains responsible for stopping the process.
     *
     * @param message Classified SteamCMD output, already redacted if it contains credentials.
     * @return The associated app, including an app just completed by this message, or null
     * if no app is active or the explicit app ID is no longer pending or is unknown.
     */
    fun record(message: ResultMessage): SteamApp? {
        if (message.type.isAuthenticationFailure) {
            sessionFailure = message
            return queue.currentApp
        }

        val appId = message.appId ?: return queue.currentApp
        val app = queue.select(appId) ?: return null

        if (message.type.isSuccessful || message.type == MessageType.ERROR) {
            results[app] =
                AppUpdateResult(
                    steamApp = app,
                    resultMessage = message,
                    duration = queue.completeCurrentUpdate(),
                )
        }
        return app
    }

    /**
     * Creates a snapshot containing a result for every requested app in command order.
     *
     * Recorded completions are preserved. Missing completions use the session failure
     * when present, otherwise an error containing the process exit code. Even a zero exit
     * code does not imply success for an app without a completion message.
     *
     * @param exitCode SteamCMD process exit code, used to explain missing app results.
     * @return An independent batch result snapshot; creating it does not change tracker state.
     */
    fun toResult(exitCode: Int): SteamCmdBatchResult =
        SteamCmdBatchResult(
            results =
                results.mapValues { (app, result) ->
                    result ?: incompleteResult(app, exitCode)
                },
            sessionFailure = sessionFailure,
        )

    /**
     * Creates a failure result for an app that produced no explicit completion message.
     * A stored authentication failure takes precedence over a generic process-exit error.
     *
     * @param app App whose update result is missing.
     * @param exitCode Process exit code included in the generic error when no session failure exists.
     * @return A failed app result with zero duration, since completion timing is unavailable.
     */
    private fun incompleteResult(
        app: SteamApp,
        exitCode: Int,
    ): AppUpdateResult {
        val failure =
            sessionFailure ?: ResultMessage(
                message = "SteamCMD exited with code $exitCode without an update result for app ${app.appId}",
                type = MessageType.ERROR,
            )
        return AppUpdateResult(steamApp = app, resultMessage = failure)
    }
}
