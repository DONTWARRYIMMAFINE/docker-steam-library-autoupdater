package com.github.dontworryimmafine.dsla.service

import com.github.dontworryimmafine.dsla.aop.ExecutionLock
import com.github.dontworryimmafine.dsla.config.properties.SteamProperties
import com.github.dontworryimmafine.dsla.model.AppUpdateResult
import com.github.dontworryimmafine.dsla.model.MessageType
import com.github.dontworryimmafine.dsla.model.SteamApp
import com.github.dontworryimmafine.dsla.model.SteamCmdBatchResult
import com.github.dontworryimmafine.dsla.service.resolver.SteamAppResolver
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant

@Service
class SteamAppsUpdateService(
    private val properties: SteamProperties,
    private val steamApiService: SteamApiService,
    private val appIdResolutionService: AppIdResolutionService,
    private val steamAppResolver: SteamAppResolver,
    private val steamCmdService: SteamCmdService,
    private val clock: Clock,
) {
    private var nextLoginAttemptAt = Instant.MIN

    init {
        require(!properties.cmdLoginCooldown.isNegative) { "SteamCMD login cooldown must not be negative" }
    }

    /**
     * Runs an update when the login cooldown and the user's Steam state allow it.
     *
     * Resolves the configured app IDs and updates the resulting batch. Calls through the
     * Spring proxy are protected from concurrent execution by [ExecutionLock]. Failures
     * during resolution or updating are logged; interruptions also restore the thread's
     * interrupt flag. A skipped run leaves the update state unchanged.
     */
    @ExecutionLock
    fun performUpdate() {
        if (clock.instant().isBefore(nextLoginAttemptAt)) {
            logger.warn("SteamCMD login is paused until $nextLoginAttemptAt after an authentication failure")
            return
        }
        if (!steamApiService.hasAllowedToUpdateState()) {
            logger.warn("Current steam user state is not allowed. Update will not be performed.")
            return
        }

        try {
            val appIds = appIdResolutionService.resolveAppIds()
            updateApps(appIds)
            logger.info("Update completed")
        } catch (ex: InterruptedException) {
            Thread.currentThread().interrupt()
            logger.warn("Update interrupted")
        } catch (ex: Exception) {
            logger.error("Update failed. Reason: ${ex.message}")
        }
    }

    /**
     * Resolves apps and updates them using cached credentials with an optional password retry.
     *
     * A final authentication failure postpones future login attempts by the configured
     * cooldown. Logs per-app failures and batch totals after the session completes.
     *
     * @param appIds App IDs to resolve and update; an empty set is logged and skipped.
     * @throws InterruptedException If waiting for a SteamCMD session is interrupted.
     * @throws java.io.IOException If a SteamCMD process cannot be started or its output cannot be read.
     */
    private fun updateApps(appIds: Set<Long>) {
        if (appIds.isEmpty()) {
            logger.warn("No applications to update")
            return
        }

        val steamApps = steamAppResolver.resolve(appIds)
        val batch = updateWithCachedCredentials(steamApps)

        batch.sessionFailure?.let { failure ->
            nextLoginAttemptAt = clock.instant().plus(properties.cmdLoginCooldown)
            logger.error("SteamCMD authentication failed: ${failure.message}. Login paused until $nextLoginAttemptAt")
        }
        logResults(batch.results.values)
    }

    /**
     * Tries cached credentials first and retries once with a password when eligible.
     *
     * A retry requires a configured password and missing-cache failures for the entire
     * batch. Other authentication failures and partially completed batches are not retried.
     *
     * @param apps Resolved apps in SteamCMD command order.
     * @return The first batch result, or the password attempt's result when a retry occurs.
     * @throws InterruptedException If waiting for either SteamCMD session is interrupted.
     * @throws java.io.IOException If either process cannot be started or its output cannot be read.
     */
    private fun updateWithCachedCredentials(apps: List<SteamApp>): SteamCmdBatchResult {
        val batch = steamCmdService.updateApps(apps)
        if (!canRetryWithPassword(batch)) return batch

        logger.warn("No cached Steam credentials. Trying password authentication once; approve Steam Guard if requested.")
        return steamCmdService.updateApps(apps, usePassword = true)
    }

    /**
     * Checks whether the batch can be retried with the configured password.
     *
     * Both the session failure and every app result must indicate missing cached
     * credentials. A blank password or any other result prevents the retry.
     *
     * @param batch Result of the attempt made with cached credentials.
     * @return True when a non-blank password is available and all missing-cache checks pass.
     */
    private fun canRetryWithPassword(batch: SteamCmdBatchResult): Boolean =
        properties.password.isNotBlank() &&
            batch.sessionFailure?.type == MessageType.NO_CREDENTIAL_CACHE &&
            batch.results.values.all { it.resultMessage.type == MessageType.NO_CREDENTIAL_CACHE }

    /**
     * Logs each app failure with its message and elapsed milliseconds, then a batch summary.
     * The summary distinguishes successful updates, already up-to-date apps, and failures.
     *
     * @param results Final per-app results to summarize; an empty collection produces zero totals.
     */
    private fun logResults(results: Collection<AppUpdateResult>) {
        val success = results.count { it.resultMessage.type == MessageType.SUCCESS }
        val upToDate = results.count { it.resultMessage.type == MessageType.ALREADY_UP_TO_DATE }
        val failed = results.filterNot { it.resultMessage.type.isSuccessful }

        failed.forEach {
            logger.warn("${it.steamApp} failed: ${it.resultMessage.message} (${it.duration}ms)")
        }

        logger.info(
            "Update summary: $success successful, $upToDate already up to date, ${failed.size} failed out of ${results.size} total",
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SteamAppsUpdateService::class.java)
    }
}
