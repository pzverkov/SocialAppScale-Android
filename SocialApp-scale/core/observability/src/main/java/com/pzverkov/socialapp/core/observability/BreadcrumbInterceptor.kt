package com.pzverkov.socialapp.core.observability

import com.pzverkov.socialapp.core.domain.CrashReporter
import com.pzverkov.socialapp.core.store.StoreInterceptor
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject

/**
 * Feeds every Store state transition and event to the [CrashReporter] as a breadcrumb, so a
 * crash report carries the trail of UI states that led to it. Contributed into the
 * Set<StoreInterceptor> multibinding the ViewModels collect.
 */
@ContributesIntoSet(AppScope::class)
@Inject
class BreadcrumbInterceptor(
    private val crashReporter: CrashReporter,
) : StoreInterceptor {

    override fun onState(old: Any?, new: Any?) {
        crashReporter.log("state ${old.typeName()} -> ${new.typeName()}")
    }

    override fun onEvent(event: Any?) {
        crashReporter.log("event ${event.typeName()}")
    }

    private fun Any?.typeName(): String = this?.let { it::class.simpleName } ?: "null"
}
