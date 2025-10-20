package com.posite.my_alarm.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun CoroutineScope.launchPeriodicAsync(
    intervalMs: Long,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    block: suspend (iteration: Int) -> Unit
): Job {
    return launch(dispatcher) {
        val startTime = System.currentTimeMillis()
        var iteration = 0

        while (true) {
            iteration++
            val targetTime = startTime + (iteration * intervalMs)
            val currentTime = System.currentTimeMillis()
            val delayTime = (targetTime - currentTime).coerceAtLeast(0)

            delay(delayTime)
            block(iteration)
        }
    }
}