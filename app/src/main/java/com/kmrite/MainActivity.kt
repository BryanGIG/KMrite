package com.kmrite

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.*
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)

        val cacheShell = Shell.getCachedShell()
        cacheShell?.let {
            if (it.isRoot) {
                if (remoteMessenger == null) {
                    serviceTestQueued = true
                    val intent = Intent(this@MainActivity, RootServices::class.java)
                    conn = MSGConnection()
                    RootService.bind(intent, conn!!)
                }
            }
        }

        with(bind) {
            startPatcher.setOnClickListener {
                if (getRequired()) {
                    cacheShell?.let {
                        if (it.isRoot) {
                            consoleList.add("Result : ")
                            testService()
                        } else {
                            consoleList.add("Result : ")
                            consoleList.add(
                                Tools.setCode(
                                    pkg.text.toString(),
                                    libname.text.toString(),
                                    Integer.decode(offset.text.toString()),
                                    hex.text.toString()
                                )
                            )
                        }
                    }
                } else {
                    Toast.makeText(this@MainActivity, "fill all needed info", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            github.setOnClickListener {
                val intent =
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/BryanGIG/KMrite"))
                startActivity(intent)
            }
        }
    }

    override fun handleMessage(msg: Message): Boolean {
        val dump = msg.data.getString("result")
        consoleList.add(dump)
        return false
    }

    //    private fun patch()
    private fun testService() {
        val message = Message.obtain(null, RootServices.MSG_GETINFO)
        message.data.putString("pkg", bind.pkg.text.toString())
        message.data.putString("fileSo", bind.libname.text.toString())
        message.data.putInt("offset", Integer.decode(bind.offset.text.toString()))
        message.data.putString("hexNumber", bind.hex.text.toString())
        message.replyTo = myMessenger
        try {
            remoteMessenger?.send(message)
        } catch (e: RemoteException) {
            consoleList.add("Remote error : ${e.message}")
        }
    }

    inner class MSGConnection : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            consoleList.add("service: rootService connected")
            remoteMessenger = Messenger(service)
            if (serviceTestQueued) {
                serviceTestQueued = false
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            consoleList.add("service: rootService disconnected")
            remoteMessenger = null
        }
    }

    inner class AppendCallbackList : CallbackList<String?>() {
        override fun onAddElement(s: String?) {
            bind.console.append("$s\n")
            bind.sv.postDelayed({ bind.sv.fullScroll(ScrollView.FOCUS_DOWN) }, 10)
        }
    }

    private fun getRequired() =
        !(bind.pkg.text.isNullOrBlank() && bind.libname.text.isNullOrBlank() && bind.offset.text.isNullOrBlank() && bind.hex.text.isNullOrBlank())

    override fun onDestroy() {
        super.onDestroy()
        conn?.let {
            RootService.unbind(it)
        }
    }
}