package com.github.dontworryimmafine.dsla.client

import com.github.dontworryimmafine.dsla.model.SteamApp

interface SteamStoreHttpClient {
    fun getSteamApp(appId: Long): SteamApp?
}
