package com.ivianuu.betterandroid

import android.app.Application
import com.ivianuu.worldreadableprefs.WorldReadablePrefsFix

/**
 * App
 */
class App: Application() {

    override fun onCreate() {
        super.onCreate()

        WorldReadablePrefsFix.builder(this)
                .fixDefault()
                .start()
    }
}