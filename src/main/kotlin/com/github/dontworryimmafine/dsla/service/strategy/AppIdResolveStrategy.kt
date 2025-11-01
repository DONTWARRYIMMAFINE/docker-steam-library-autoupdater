package com.github.dontworryimmafine.dsla.service.strategy

import com.github.dontworryimmafine.dsla.model.AppIdResolveStrategyType

interface AppIdResolveStrategy {
    fun getType(): AppIdResolveStrategyType

    fun resolve(): Set<Long>
}
