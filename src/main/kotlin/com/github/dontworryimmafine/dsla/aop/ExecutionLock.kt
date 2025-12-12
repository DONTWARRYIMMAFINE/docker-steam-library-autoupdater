package com.github.dontworryimmafine.dsla.aop

import java.util.concurrent.TimeUnit

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ExecutionLock(
    val key: String = "",
    val timeout: Long = 1,
    val timeoutUnit: TimeUnit = TimeUnit.HOURS,
)
