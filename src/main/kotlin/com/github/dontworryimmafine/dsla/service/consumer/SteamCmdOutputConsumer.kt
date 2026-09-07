package com.github.dontworryimmafine.dsla.service.consumer

import com.github.dontworryimmafine.dsla.model.ResultMessage
import com.github.dontworryimmafine.dsla.model.SteamApp
import java.util.function.BiConsumer

/**
 * Consumes SteamCMD messages after classification, password redaction, and app association.
 *
 * The inherited [BiConsumer.accept] receives a [ResultMessage] and its associated [SteamApp].
 * Implementations may ignore message types they do not render or log. They are called
 * synchronously while the session output is being read.
 */
interface SteamCmdOutputConsumer : BiConsumer<ResultMessage, SteamApp>
