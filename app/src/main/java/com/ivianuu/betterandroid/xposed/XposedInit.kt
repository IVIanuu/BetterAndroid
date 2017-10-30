package com.ivianuu.betterandroid.xposed

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Xposed init
 */
class XposedInit: IXposedHookZygoteInit, IXposedHookLoadPackage {

    private lateinit var prefs: XSharedPreferences

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        prefs = XSharedPreferences(
                PACKAGE_THIS_PACKAGE)
        prefs.makeWorldReadable()
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        when(lpparam.packageName) {
            PACKAGE_SYSTEM_UI -> {
                NotificationPanelHooks.hook(lpparam, prefs)
                StatusBarHooks.hook(lpparam, prefs)
            }
        }
    }

    private companion object {
        private const val PACKAGE_ANDROID = "android"
        private const val PACKAGE_SYSTEM_UI = "com.android.systemui"
        private const val PACKAGE_THIS_PACKAGE = "com.ivianuu.betterandroid"
    }
}