package com.github.dontworryimmafine.dsla.aop

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ExecutionLock(
    val key: String = "",
)
