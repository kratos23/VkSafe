package com.pavelkrylov.vsafe.base

import android.os.Process
import androidx.lifecycle.MutableLiveData
import com.vk.api.sdk.VK
import com.vk.api.sdk.internal.ApiCommand

class VkRequestThread<T>(val command: ApiCommand<T>, val ld: MutableLiveData<T>) : Thread() {
    init {
        priority = Process.THREAD_PRIORITY_DEFAULT + Process.THREAD_PRIORITY_LESS_FAVORABLE
    }

    override fun run() {
        kotlin.runCatching {
            var fail = false
            do {
                fail = false
                try {
                    val result = VK.executeSync(command)
                    if (!isInterrupted) {
                        ld.postValue(result)
                    }
                } catch (e: InterruptedException) {
                    return@runCatching
                } catch (e: Exception) {
                    e.printStackTrace()
                    fail = true
                    try {
                        sleep(500)
                    } catch (e: InterruptedException) {
                        return@runCatching
                    }
                }

            } while (fail && !isInterrupted);
        }
    }
}