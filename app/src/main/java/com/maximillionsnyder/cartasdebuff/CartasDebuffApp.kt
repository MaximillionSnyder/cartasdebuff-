package com.maximillionsnyder.cartasdebuff

import android.app.Application
import com.maximillionsnyder.cartasdebuff.crash.CrashReporter

class CartasDebuffApp : Application() {

    override fun onCreate() {
        super.onCreate()
        CrashReporter.instalar(this)
    }
}
