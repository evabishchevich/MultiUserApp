package com.example.multiuserapp

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.RemoteCallbackList
import android.util.Log

class CounterServiceImpl : Service() {

    @Volatile
    private var intCounter = 0

    private val callbacks: RemoteCallbackList<CounterCallback> = RemoteCallbackList<CounterCallback>()

    private val binder = object : CounterService.Stub() {
        override fun getCounter(): Int {
            Log.d("XXX", "CounterServiceImpl getCounter")
            return intCounter
        }

        override fun changeValue(newValue: Int) {
            Log.d("XXX", "CounterServiceImpl changeValue $newValue")
            intCounter = newValue

            callbacks.beginBroadcast()
            for (i in 0 until callbacks.registeredCallbackCount) {
                callbacks.getBroadcastItem(i).onValueChanged(intCounter)
            }
            callbacks.finishBroadcast()
        }

        override fun register(callback: CounterCallback?) {
            callbacks.register(callback)
        }

        override fun unregister(callback: CounterCallback?) {
            callbacks.unregister(callback)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("XXX", "CountingService onCreate")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        Log.d("XXX", "CountingService onRebind")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("XXX", "CountingService onUnbind")
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("XXX", "CountingService onDestroy")
    }
}
