package com.github.dontworryimmafine.dsla.service.resolver

import com.github.dontworryimmafine.dsla.model.SteamApp

interface SteamAppResolver {
    fun resolve(appIds: Set<Long>): List<SteamApp>
}
