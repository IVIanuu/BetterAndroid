package com.ivianuu.betterandroid.xposed

import android.annotation.SuppressLint
import android.content.Context
import android.os.SystemClock
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.ivianuu.xposedextensions.*
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import android.text.method.Touch.onTouchEvent




@SuppressLint("StaticFieldLeak")
/**
 * Quick pulldown hooks
 */
object NotificationPanelHooks {

    private const val CLASS_NOTIFICATION_PANEL_VIEW =
            "com.android.systemui.statusbar.phone.NotificationPanelView"

    private lateinit var notificationPanelView: View
    private lateinit var statusBar: Any
    private lateinit var prefs: XSharedPreferences
    private lateinit var gestureDetector: GestureDetector

    private var quickPulldownEnabled = false
    private var dt2sOnLockScreenEnabled = false

    /**
     * Quick pulldown hooks
     */
    fun hook(lpparam: XC_LoadPackage.LoadPackageParam, prefs: XSharedPreferences) {
        this.prefs = prefs

        val notificationPanelViewClass = lpparam.classLoader.find(
                CLASS_NOTIFICATION_PANEL_VIEW)

        notificationPanelViewClass.hook {
            after {
                notificationPanelView = it.instance()
                createGestureDetector()

                notificationPanelView.context.prefChanges {
                    when(it) {
                        PREF_KEY_DT2S_LOCK_SCREEN -> {
                            prefs.reload()
                            dt2sOnLockScreenEnabled = prefs.getBoolean(PREF_KEY_DT2S_LOCK_SCREEN, false)
                        }
                        PREF_KEY_QUICK_PULLDOWN -> {
                            prefs.reload()
                            quickPulldownEnabled = prefs.getBoolean(PREF_KEY_QUICK_PULLDOWN, false)
                        }
                    }
                }

                quickPulldownEnabled = prefs.getBoolean(PREF_KEY_QUICK_PULLDOWN, false)
            }
        }

        notificationPanelViewClass.hook("setStatusBar") {
            after { statusBar = notificationPanelView.get("mStatusBar") }
        }

        // hook touch event
        notificationPanelViewClass.hook("onTouchEvent", MotionEvent::class.java) {
            after {
                val event = it.args[0] as MotionEvent
                if (quickPulldownEnabled) {
                    val quickPulldown = (event.actionMasked == MotionEvent.ACTION_DOWN
                            && shouldQuickSettingsIntercept(
                            event.x, event.y)
                            && event.getY(event.actionIndex) < notificationPanelView.get<Int>("mStatusBarMinHeight"))

                    if (quickPulldown) {
                        notificationPanelView.set("mQsExpandImmediate", true)
                        notificationPanelView.invoke("requestPanelHeightUpdate")
                        notificationPanelView.invoke("setListening", true)
                    }
                }
                if (dt2sOnLockScreenEnabled
                        && statusBar.invoke<Int>("getBarState") == 1) {
                    gestureDetector.onTouchEvent(event)
                }
            }
        }

    }

    private fun shouldQuickSettingsIntercept(x: Float, y: Float): Boolean {
        if (!notificationPanelView.get<Boolean>("mQsExpansionEnabled")) {
            return false
        }

        val w = notificationPanelView.invoke<Int>("getMeasuredWidth")
        val region = w * (1f / 4f)
        val showQsOverride = x > w - region

        return if (notificationPanelView.get<Boolean>("mQsExpanded")) {
            val scrollView = notificationPanelView.get("mScrollView")
            (scrollView.invoke<Boolean>("isScrolledToBottom")
                    && notificationPanelView.invoke<Boolean>("isInQsArea", x, y))
        } else {
            showQsOverride
        }
    }

    private fun createGestureDetector() {
        gestureDetector = GestureDetector(
                notificationPanelView.context, object: GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent?): Boolean {
                goToSleep()
                logX { "should go to sleep" }
                return true
            }
        })
    }

    private fun goToSleep() {
        notificationPanelView.context.getSystemService(Context.POWER_SERVICE)
                .invoke("goToSleep", SystemClock.uptimeMillis())
    }
}