package com.kmrite

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import android.widget.EditText
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kmrite.databinding.ActivityMainBinding
import com.topjohnwu.superuser.CallbackList
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ipc.RootService

class MainActivity : AppCompatActivity(), Handler.Callback {
    lateinit var bind: ActivityMainBinding

    private var consoleList: AppendCallbackList = AppendCallbackList()
    private val myMessenger = Messenger(Handler(Looper.getMainLooper(), this))
    var remoteMessenger: Messenger? = null
    private var serviceTestQueued = false
    private var conn: MSGConnection? = null

    companion object {
        const val TAG = "KMrite"
    }

    private var EditText.value
        get() = this.text.toString()
        set(value) {
            this.setText(value)
        }

    private fun getRequired(): Boolean {
        return !(bind.pkg.text.isNullOrBlank() && bind.libname.text.isNullOrBlank() && bind.offset.text.isNullOrBlank() && bind.hex.text.isNullOrBlank())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityMainBinding.inflate(layoutInflater)
        Shell.rootAccess()
        setContentView(bind.root)

        bind.startPatcher.setOnClickListener {
            if (getRequired()) {
                if (Shell.rootAccess()) {
                    if (remoteMessenger == null) {
                        bind.console.append("startRootServices\n")
                        bind.console.append("Result : ")
                        serviceTestQueued = true
                        val intent = Intent(this, RootServices::class.java)
                        conn = MSGConnection()
                        RootService.bind(intent, conn!!)
                    }
                    if (remoteMessenger != null) {
                        bind.console.append("RootServiceRunning")
                        bind.console.append("Result : ")
                        testService()
                    }
                } else {
                    bind.console.append("Result : ")
                    bind.console.append(
                        Tools.setCode(
                            bind.pkg.value,
                            bind.libname.value,
                            Integer.decode(bind.offset.value),
                            bind.hex.value
                        ).toString()
                    )
                }
            } else {
                Toast.makeText(this, "fill all needed info", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun handleMessage(msg: Message): Boolean {
        val dump = msg.data.getString("result")
        consoleList.add(dump)
        return false
    }

    private fun testService() {
        val message: Message = Message.obtain(null, RootServices.MSG_GETINFO)
        message.data.putString("pkg", bind.pkg.value)
        message.data.putString("fileSo", bind.libname.value)
        message.data.putInt("offset", Integer.decode(bind.offset.value))
        message.data.putString("hexNumber", bind.hex.value)
        message.replyTo = myMessenger
        try {
            remoteMessenger?.send(message)
        } catch (e: RemoteException) {
            Log.e(TAG, "Remote error", e)
        }
    }

    inner class MSGConnection : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.d(TAG, "service onServiceConnected")
            remoteMessenger = Messenger(service)
            if (serviceTestQueued) {
                serviceTestQueued = false
                testService()
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.d(TAG, "service onServiceDisconnected")
            remoteMessenger = null
        }
    }

    inner class AppendCallbackList : CallbackList<String?>() {
        override fun onAddElement(s: String?) {
            bind.console.append(s)
            bind.console.append("\n")
            bind.sv.postDelayed({ bind.sv.fullScroll(ScrollView.FOCUS_DOWN) }, 10)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (conn != null) {
            RootService.unbind(conn!!)
        }
    }
}