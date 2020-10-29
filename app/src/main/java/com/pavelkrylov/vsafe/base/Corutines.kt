package com.pavelkrylov.vsafe.base

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

suspend fun <T> retry(block: suspend () -> T): T {
    while (coroutineContext.isActive) {
        try {
            return block()
        } catch (e: Exception) {
            e.printStackTrace()
            delay(500)
        }
    }
    return block()
}