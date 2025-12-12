package com.github.dontworryimmafine.dsla.aop

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.integration.support.locks.LockRegistry
import org.springframework.stereotype.Component
import java.util.concurrent.locks.Lock

@Aspect
@Component
class ExecutionLockAspect(
    private val lockRegistry: LockRegistry,
) {
    @Around("@annotation(executionLock)")
    fun aroundExecutionLock(
        joinPoint: ProceedingJoinPoint,
        executionLock: ExecutionLock,
    ): Any? {
        val signature = joinPoint.signature as MethodSignature
        val method = signature.method

        val lockKey = executionLock.key.ifBlank { "${joinPoint.target.javaClass.simpleName}.${method.name}" }

        val lock: Lock = lockRegistry.obtain(lockKey)
        val acquired = lock.tryLock()

        if (!acquired) {
            logger.error("Execution lock: [$lockKey] already exists")
            return null
        }

        try {
            return joinPoint.proceed()
        } finally {
            lock.unlock()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ExecutionLockAspect::class.java)
    }
}
