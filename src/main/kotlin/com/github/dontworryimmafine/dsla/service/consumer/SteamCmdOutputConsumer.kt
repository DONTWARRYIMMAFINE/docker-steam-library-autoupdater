package com.github.dontworryimmafine.dsla.service.consumer

import java.util.function.BiConsumer

interface SteamCmdOutputConsumer : BiConsumer<String, Long>