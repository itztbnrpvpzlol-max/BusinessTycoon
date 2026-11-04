package com.tidebound.game

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import java.io.File

class MainActivity : AndroidApplication() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val report = File(filesDir, "tidebound-crash.txt")
        if (report.exists()) {
            val savedError = try { report.readText() } catch (_: Exception) { "Crash report could not be read." }
            AlertDialog.Builder(this)
                .setTitle("Previous Tidebound crash")
                .setMessage(savedError.take(12000))
                .setPositiveButton("Copy error") { _, _ ->
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Tidebound crash", savedError))
                }
                .setNegativeButton("Close", null)
                .show()
        }

        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            try {
                report.writeText("Thread: ${thread.name}\n" + Log.getStackTraceString(error))
                Log.e("TideboundCrash", "Crash report saved", error)
            } catch (_: Exception) {
                Log.e("TideboundCrash", "Could not save crash report", error)
            }
            previousHandler?.uncaughtException(thread, error)
        }

        val config = AndroidApplicationConfiguration().apply {
            useAccelerometer = false
            useCompass = false
            useImmersiveMode = true
            numSamples = 0
        }

        try {
            initialize(TideboundGame(), config)
        } catch (error: Throwable) {
            val details = Log.getStackTraceString(error)
            try { report.writeText("Startup initialization error\n" + details) } catch (_: Exception) {}
            AlertDialog.Builder(this)
                .setTitle("Tidebound couldn't start")
                .setMessage(details.take(12000))
                .setPositiveButton("Copy error") { _, _ ->
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Tidebound startup error", details))
                }
                .setNegativeButton("Close", null)
                .show()
        }
    }
}
