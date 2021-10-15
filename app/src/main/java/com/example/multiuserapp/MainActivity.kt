package com.example.multiuserapp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Process
import android.os.RemoteException
import android.os.UserHandle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.multiuserapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            Log.d("XXX", " MainActivity onServiceDisconnected ")

            isBound = false
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.d("XXX", " MainActivity onServiceConnected ")
            countingService = CounterService.Stub.asInterface(service)
            isBound = true
        }
    }

    private var countingService: CounterService? = null
    private var isBound = false
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = Runnable {
        if (isBound) {
            try {
                countingService?.changeValue((countingService?.counter ?: 0) + 1)
            } catch (e: RemoteException) {
                // There is nothing special we need to do if the service has crashed.
            }
            startCounter()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val ai = applicationInfo
        Log.d("XXX", "Application UID: " + ai.uid + ", SYSTEM_UID: " + Process.SYSTEM_UID)

        binding.btnIncreaseCounter.setOnClickListener {
            if (isBound) {
                Log.d("XXX", "startCounter")
                startCounter()
            } else {
                Log.d("XXX", "No service bound")
            }

            binding.tvCounter.text = countingService?.counter?.toString() ?: "No service bound"
        }

        val intent = Intent(this, CounterServiceImpl::class.java)
//        startServiceAsUser(intent, UserHandle(0))
        val userSystem = 0
        bindServiceAsUser(
            intent,
            mServiceConnection,
            Context.BIND_AUTO_CREATE,
            UserHandle.getUserHandleForUid(userSystem)
        )
    }

    private fun startCounter() {
        handler.postDelayed(runnable, 1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            handler.removeCallbacks(runnable)
            unbindService(mServiceConnection);
            isBound = false;
        }
    }
}
