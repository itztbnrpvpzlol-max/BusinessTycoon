package com.tidebound.game

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import java.io.File

class MainActivity : AndroidApplication() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep a copy of unexpected startup/runtime errors so they can be shared
        // even when Android closes the game immediately.
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            try {
                val report = File(filesDir, "tidebound-crash.txt")
                report.writeText(
                    "Thread: ${thread.name}\n" +
                    Log.getStackTraceString(error)
                )
                Log.e("TideboundCrash", "Uncaught game error saved to ${report.absolutePath}", error)
            } catch (_: Exception) {
                Log.e("TideboundCrash", "Could not save crash report", error)
            }
            previousHandler?.uncaughtException(thread, error)
        }

        val config = AndroidApplicationConfiguration().apply {
            useAccelerometer = false
            useCompass = false
            useImmersiveMode = true
            // Avoid requesting multisampling on devices whose EGL driver
            // cannot provide an MSAA surface.
            numSamples = 0
        }

        try {
            initialize(TideboundGame(), config)
        } catch (error: Throwable) {
            Log.e("TideboundStartup", "Failed to initialize Tidebound", error)
            AlertDialog.Builder(this)
                .setTitle("Tidebound couldn't start")
                .setMessage(Log.getStackTraceString(error).take(3500))
                .setPositiveButton("Close", null)
                .show()
        }
    }
}
