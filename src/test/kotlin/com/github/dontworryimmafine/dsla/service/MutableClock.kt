package com.github.dontworryimmafine.dsla.service

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.concurrent.atomic.AtomicReference

internal class MutableClock private constructor(
    private val currentInstant: AtomicReference<Instant>,
    private val zone: ZoneId,
) : Clock() {
    constructor() : this(AtomicReference(Instant.parse("2026-09-07T00:00:00Z")), ZoneOffset.UTC)

    override fun instant(): Instant = currentInstant.get()

    override fun getZone(): ZoneId = zone

    override fun withZone(zone: ZoneId): Clock = MutableClock(currentInstant, zone)

    fun advance(duration: Duration) {
        currentInstant.updateAndGet { it.plus(duration) }
    }
}
