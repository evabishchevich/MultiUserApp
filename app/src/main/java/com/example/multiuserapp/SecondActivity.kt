package com.example.multiuserapp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.RemoteException
import android.os.UserHandle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.multiuserapp.databinding.ActivitySecondBinding

private const val MSG_NEW_COUNTER_VALUE = 112

class SecondActivity : AppCompatActivity() {

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            Log.d("XXX", " SecondActivity onServiceDisconnected ${this@SecondActivity}")
            isBound = false
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.d("XXX", " SecondActivity onServiceConnected ${this@SecondActivity}")
            countingService = CounterService.Stub.asInterface(service)
            try {
                countingService?.register(counterCallback)
            } catch (e: RemoteException) {
                // There is nothing special we need to do if the service has crashed.
            }
            isBound = true
        }
    }

    private val counterCallback = CounterCallbackImpl()
    private var countingService: CounterService? = null
    private var isBound = false
    private lateinit var binding: ActivitySecondBinding

    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_NEW_COUNTER_VALUE -> binding.tvCounter.text = msg.arg1.toString()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    override fun onStart() {
        super.onStart()

        val intent = Intent(this, CounterServiceImpl::class.java)
        val userSystem = 0
        bindServiceAsUser(
            intent,
            serviceConnection,
            Context.BIND_AUTO_CREATE,
            UserHandle.getUserHandleForUid(userSystem)
        )
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            try {
                countingService?.unregister(counterCallback)
            } catch (e: RemoteException) {
                // There is nothing special we need to do if the service has crashed.
            }
            unbindService(serviceConnection)
            isBound = false;
        }
    }

    inner class CounterCallbackImpl : CounterCallback.Stub() {

        override fun onValueChanged(newValue: Int) {
            Log.d("XXX", "onValueChanged ${this@SecondActivity}, thread: ${Thread.currentThread()}")
            handler.sendMessage(handler.obtainMessage(MSG_NEW_COUNTER_VALUE, newValue, 0))
        }
    }
}
