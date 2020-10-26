package com.pavelkrylov.vsafe.base

import android.os.Process
import androidx.lifecycle.MutableLiveData

class RequestThread<T>(val request: () -> T, val ld: MutableLiveData<T>) : Thread() {
    init {
        priority = Process.THREAD_PRIORITY_DEFAULT + Process.THREAD_PRIORITY_LESS_FAVORABLE
    }

    override fun run() {
        super.run()
        var fail = false
        do {
            fail = false
            try {
                val result = request()
                ld.postValue(result)
            } catch (e: Exception) {
                e.printStackTrace()
                fail = true
                sleep(500)
            }

        } while (fail && !isInterrupted);
    }
}