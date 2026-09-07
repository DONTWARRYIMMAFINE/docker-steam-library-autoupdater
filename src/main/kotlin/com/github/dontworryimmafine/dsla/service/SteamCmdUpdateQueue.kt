package com.github.dontworryimmafine.dsla.service

import com.github.dontworryimmafine.dsla.model.SteamApp
import java.util.concurrent.TimeUnit

/**
 * Tracks the active app and its elapsed time within a sequential SteamCMD update queue.
 * The first app's timer starts when the queue is constructed. Use one queue per session.
 *
 * @param apps Apps with distinct IDs in SteamCMD command order; an empty list creates an exhausted queue.
 * @param nanoTime Monotonic time source returning nanoseconds, used whenever the active app changes.
 */
internal class SteamCmdUpdateQueue(
    apps: List<SteamApp>,
    private val nanoTime: () -> Long,
) {
    private val pendingApps = apps.associateByTo(linkedMapOf()) { it.appId }
    private var startedAt = nanoTime()

    /**
     * The first pending app, or null when the queue is empty.
     * Reading this property does not advance the queue or restart the app's timer.
     */
    val currentApp: SteamApp?
        get() = pendingApps.values.firstOrNull()

    /**
     * Selects the app identified by an explicit SteamCMD message.
     *
     * Selecting a later app discards preceding queue entries and starts its timer.
     * Selecting the current app preserves its elapsed time. This operation does not
     * create results for skipped apps; the batch tracker handles missing completions.
     *
     * @param appId ID of the pending app to make current.
     * @return The selected app, or null for an unknown, completed, or skipped app ID.
     * An absent ID leaves both the queue and timer unchanged.
     */
    fun select(appId: Long): SteamApp? {
        val app = pendingApps[appId] ?: return null
        if (app == currentApp) return app

        // Commands can advance even when an earlier update produced no result.
        while (pendingApps.keys.first() != appId) {
            pendingApps.remove(pendingApps.keys.first())
        }
        startedAt = nanoTime()
        return app
    }

    /**
     * Removes the current app and starts timing the next app at the same instant.
     *
     * @return The completed app's elapsed duration in whole milliseconds, measured since
     * it became current. The queue becomes exhausted after its final app completes.
     * @throws IllegalStateException If the queue has no current app.
     */
    fun completeCurrentUpdate(): Long {
        val app = checkNotNull(currentApp) { "No update in progress" }
        val completedAt = nanoTime()
        val duration = TimeUnit.NANOSECONDS.toMillis(completedAt - startedAt)

        pendingApps.remove(app.appId)
        startedAt = completedAt
        return duration
    }
}
