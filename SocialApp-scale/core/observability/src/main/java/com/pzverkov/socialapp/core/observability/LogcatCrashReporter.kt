package com.pzverkov.socialapp.core.observability

import android.util.Log
import com.pzverkov.socialapp.core.domain.CrashReporter
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Default [CrashReporter] that writes to Logcat. The seam exists so a vendor SDK
 * (Crashlytics, Sentry) can replace this binding without touching call sites.
 */
@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
class LogcatCrashReporter : CrashReporter {

    override fun log(breadcrumb: String) {
        Log.d(TAG, breadcrumb)
    }

    override fun report(throwable: Throwable) {
        Log.e(TAG, "Non-fatal reported", throwable)
    }

    private companion object {
        const val TAG = "SocialApp"
    }
}
