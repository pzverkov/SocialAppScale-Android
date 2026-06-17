package com.pzverkov.socialapp.core.domain

/**
 * Sink for breadcrumbs and non-fatal reports. The contract lives in domain so any layer can
 * record without depending on a concrete reporter; the implementation (Logcat today, a vendor
 * SDK later) is bound in the observability module and swapped through DI.
 */
interface CrashReporter {
    fun log(breadcrumb: String)
    fun report(throwable: Throwable)
}
