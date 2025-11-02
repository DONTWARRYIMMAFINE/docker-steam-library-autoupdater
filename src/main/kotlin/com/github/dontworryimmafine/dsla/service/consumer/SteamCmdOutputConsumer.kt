package com.github.dontworryimmafine.dsla.service.consumer

import com.github.dontworryimmafine.dsla.model.SteamApp
import java.util.function.BiConsumer

interface SteamCmdOutputConsumer : BiConsumer<String, SteamApp>
