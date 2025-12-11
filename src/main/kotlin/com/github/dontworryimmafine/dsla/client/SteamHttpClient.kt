package com.github.dontworryimmafine.dsla.client

import com.github.dontworryimmafine.dsla.model.SteamApp

interface SteamHttpClient {
    fun getSteamApp(appId: Long): SteamApp?
}
