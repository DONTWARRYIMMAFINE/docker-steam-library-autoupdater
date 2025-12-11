package com.github.dontworryimmafine.dsla.client

import com.github.dontworryimmafine.dsla.model.PlayerSummary

interface SteamApiHttpClient {
    fun getPlayerSummary(): PlayerSummary?
}
