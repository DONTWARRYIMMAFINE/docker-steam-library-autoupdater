package com.github.dontworryimmafine.dsla.service

import com.github.dontworryimmafine.dsla.client.SteamApiHttpClient
import com.github.dontworryimmafine.dsla.config.HandlerConfig
import com.github.dontworryimmafine.dsla.config.properties.SteamProperties
import com.github.dontworryimmafine.dsla.model.AppIdResolveStrategyType
import com.github.dontworryimmafine.dsla.model.MessageType
import com.github.dontworryimmafine.dsla.model.PlayerSummary
import com.github.dontworryimmafine.dsla.model.PlayerSummaryState
import com.github.dontworryimmafine.dsla.model.SteamApp
import com.github.dontworryimmafine.dsla.service.resolver.SteamAppResolver
import com.github.dontworryimmafine.dsla.service.strategy.impl.ManualAppIdResolveStrategy
import java.nio.file.Path
import java.time.Clock
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readLines
import kotlin.io.path.readText
import kotlin.io.path.writeText

internal class SteamCmdTestFixture(
    directory: Path,
    scenario: SteamCmdScenario = SteamCmdScenario.SUCCESS,
    password: String = "test-password",
    appIds: Set<Long> = linkedSetOf(10, 20),
    validate: Boolean = false,
) {
    private val root = directory.resolve("Steam root with spaces").createDirectories()
    private val outputConsumer = RecordingSteamCmdOutputConsumer()
    private val properties =
        SteamProperties(
            schedule = "-",
            webApiKey = "",
            steamId = "",
            username = "test-user",
            password = password,
            allowedStates = setOf(PlayerSummaryState.UNKNOWN),
            rootPath = root.toString(),
            cmdRootPath = root.toString(),
            cmdFilterOutput = "true",
            cmdValidateInstalled = validate,
            appIdResolveStrategies = setOf(AppIdResolveStrategyType.MANUAL),
            manualAppIds = appIds,
            ignoreAppIds = emptySet(),
        )

    val apps = appIds.map { SteamApp(it) }
    val steamCmdService =
        SteamCmdService(
            properties,
            listOf(outputConsumer),
            HandlerConfig().defaultOutputHandler(),
            SteamCmdCommandService(properties),
        )

    val output: List<CapturedSteamCmdOutput>
        get() = outputConsumer.messages

    val processCount: Int
        get() = readLog("processes.log").size

    val loginAttempts: List<LoginMethod>
        get() =
            readLog("calls.log").mapNotNull { command ->
                when (command) {
                    "cached-login" -> LoginMethod.CACHED_CREDENTIALS
                    "password-login" -> LoginMethod.PASSWORD
                    else -> null
                }
            }

    val updateCommands: List<String>
        get() = readLog("calls.log").filter { it.startsWith("app_update ") }

    val isProcessAlive: Boolean
        get() {
            val pidFile = root.resolve("process.pid")
            if (!pidFile.exists()) return false
            val pid = pidFile.readText().trim().toLong()
            return ProcessHandle.of(pid).map { it.isAlive }.orElse(false)
        }

    init {
        val resource = checkNotNull(javaClass.getResource("/steamcmd.sh")) { "SteamCMD test executable is missing" }
        val executable = root.resolve("steamcmd.sh")
        executable.writeText(resource.readText())
        check(executable.toFile().setExecutable(true)) { "Cannot make the SteamCMD fixture executable" }
        simulate(scenario)
    }

    fun simulate(scenario: SteamCmdScenario) {
        root.resolve("scenario").writeText(scenario.fixtureName)
    }

    fun awaitDownloadStarted() {
        outputConsumer.awaitDownloadStarted()
    }

    fun appIdsWithOutput(type: MessageType): List<Long> = output.filter { it.result.type == type }.map { it.appId }

    fun createUpdater(clock: Clock = Clock.systemUTC()): SteamAppsUpdateService {
        val apiClient =
            object : SteamApiHttpClient {
                override fun getPlayerSummary(): PlayerSummary? = null
            }
        val resolver =
            object : SteamAppResolver {
                override fun resolve(appIds: Set<Long>): List<SteamApp> = appIds.map { SteamApp(it) }
            }
        val strategy = ManualAppIdResolveStrategy(properties)
        return SteamAppsUpdateService(
            properties,
            SteamApiService(properties, apiClient),
            AppIdResolutionService(properties, mapOf(AppIdResolveStrategyType.MANUAL to strategy)),
            resolver,
            steamCmdService,
            clock,
        )
    }

    private fun readLog(name: String): List<String> {
        val log = root.resolve(name)
        return if (log.exists()) log.readLines() else emptyList()
    }
}

internal enum class LoginMethod {
    CACHED_CREDENTIALS,
    PASSWORD,
}

internal enum class SteamCmdScenario(
    val fixtureName: String,
) {
    SUCCESS("success"),
    PASSWORD_REQUIRED("cache-retry"),
    CREDENTIALS_UNAVAILABLE("cache-never"),
    RATE_LIMITED("rate-limit"),
    INVALID_PASSWORD("invalid-password"),
    STEAM_GUARD_TIMEOUT("guard-timeout"),
    CONNECTION_FAILED("login-failed"),
    MIXED_RESULTS("mixed"),
    PROCESS_CRASH("partial-exit"),
    MISSING_RESULT("missing-result"),
    SELF_UPDATE_ONLY("startup-only"),
    APP_PASSWORD_ERROR("app-password-error"),
    ECHO_PASSWORD("echo-password"),
    RUNNING_UPDATE("hold"),
}
