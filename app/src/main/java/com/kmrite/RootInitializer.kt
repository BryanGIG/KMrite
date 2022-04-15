package com.kmrite

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.topjohnwu.superuser.Shell

class RootInitializer : Activity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        Shell.getShell {
            if (it.isRoot) {
                Toast.makeText(this, "Using root method!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Using non-root method!", Toast.LENGTH_SHORT).show()
            }
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}