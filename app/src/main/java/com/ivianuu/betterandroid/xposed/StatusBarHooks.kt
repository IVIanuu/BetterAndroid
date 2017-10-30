package com.ivianuu.betterandroid.xposed

import android.annotation.SuppressLint
import android.content.Context
import android.os.SystemClock
import android.view.GestureDetector
import android.view.MotionEvent
import com.ivianuu.xposedextensions.*
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Status bar hooks
 */
@SuppressLint("StaticFieldLeak")
object StatusBarHooks {

    private const val CLASS_PHONE_STATUSBAR = "com.android.systemui.statusbar.phone.PhoneStatusBar"
    private const val CLASS_PHONE_STATUSBAR_VIEW = "com.android.systemui.statusbar.phone.PhoneStatusBarView"
    private const val CLASS_PANEL_VIEW = "com.android.systemui.statusbar.phone.PanelView"

    private lateinit var gestureDetector: GestureDetector

    private lateinit var context: Context
    private lateinit var prefs: XSharedPreferences

    private var dt2sEnabled = false

    fun hook(lpparam: XC_LoadPackage.LoadPackageParam, prefs: XSharedPreferences) {
        this.prefs = prefs

        val phoneStatusBarClass = lpparam.classLoader.find(CLASS_PHONE_STATUSBAR)

        phoneStatusBarClass.hook("start") {
            after {
                context = it.instance.get<Context>("mContext")

                createGestureDetector()

                // pref changes
                context.prefChanges {
                    logX { "prefs changed $it" }
                    when(it) {
                        PREF_KEY_DT2S_STATUS_BAR -> {
                            prefs.reload()
                            dt2sEnabled = prefs.getBoolean(PREF_KEY_DT2S_STATUS_BAR, false)
                        }
                    }
                }

                // init
                dt2sEnabled = prefs.getBoolean(PREF_KEY_DT2S_STATUS_BAR, false)
            }
        }

        val phoneStatusBarViewClass = lpparam.classLoader.find(CLASS_PHONE_STATUSBAR_VIEW)

        phoneStatusBarViewClass.hook("onTouchEvent", MotionEvent::class.java) {
            before {
                if (dt2sEnabled) {
                    gestureDetector.onTouchEvent(it.args[0] as MotionEvent)
                }
            }
        }

        val panelViewClass = lpparam.classLoader.find(CLASS_PANEL_VIEW)

        panelViewClass.hook("schedulePeek") {
            before {
                logX { "schedule peek" }
                if (dt2sEnabled) it.result = null
            }
        }

        panelViewClass.hook("expand") {
            before {
                if (dt2sEnabled) {
                    logX { "expand" }
                    it.instance.set("mQsExpandImmediate", false)
                }
            }
        }
    }

    private fun createGestureDetector() {
        gestureDetector = GestureDetector(context, object: GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent?): Boolean {
                goToSleep()
                logX { "should go to sleep" }
                return true
            }
        })
    }

    private fun goToSleep() {
        context.getSystemService(Context.POWER_SERVICE)
                .invoke("goToSleep", SystemClock.uptimeMillis())
    }

}