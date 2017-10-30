package com.ivianuu.betterandroid

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.PreferenceFragmentCompat
import com.ivianuu.betterandroid.xposed.ACTION_PREFS_CHANGED
import com.ivianuu.betterandroid.xposed.EXTRA_KEY

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction()
                .replace(R.id.prefs_container, PrefsFragment()).commit()
    }

    class PrefsFragment: PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.prefs)
        }

        override fun onResume() {
            super.onResume()
            preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onPause() {
            super.onPause()
            preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        }

        private val handler = Handler()
        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            handler.postDelayed({
                val intent = Intent(ACTION_PREFS_CHANGED).apply {
                    putExtra(EXTRA_KEY, key)
                }
                activity.sendBroadcast(intent)
            }, 500)
        }
    }
}
