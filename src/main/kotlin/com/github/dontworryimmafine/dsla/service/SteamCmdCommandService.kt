package com.github.dontworryimmafine.dsla.service

import com.github.dontworryimmafine.dsla.config.properties.SteamProperties
import org.springframework.stereotype.Service

@Service
class SteamCmdCommandService(
    private val properties: SteamProperties
) {
    fun buildAppUpdateCommand(appId: Long, isPasswordRequired: Boolean = false, validate: Boolean = false): List<String> {
        return listOfNotNull(
            "./steamcmd.sh",
            "+@NoPromptForPassword", "1",
            "+@sSteamCmdForcePlatformType", "windows",
            "+login", properties.username, if (isPasswordRequired) properties.password else "",
            "+app_update", appId.toString(), if (validate) "validate" else null,
            "+quit"
        )
    }
}
