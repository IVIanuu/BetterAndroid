package com.ivianuu.betterandroid.xposed

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

// Broadcasts
const val ACTION_PREFS_CHANGED = "com.ivianuu.betterandroid.PREFS_CHANGED"
const val EXTRA_KEY = "key"

fun Context.prefChanges(action: (String) -> Unit): BroadcastReceiver {
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            action.invoke(intent.getStringExtra(EXTRA_KEY))
        }
    }

    val intentFilter = IntentFilter(ACTION_PREFS_CHANGED)
    registerReceiver(receiver, intentFilter)
    return receiver
}

// Packages

// Prefs
const val PREF_KEY_DT2S_STATUS_BAR = "dt2s_status_bar"
const val PREF_KEY_DT2S_LOCK_SCREEN = "dt2s_lock_screen"
const val PREF_KEY_QUICK_PULLDOWN = "quick_pulldown"