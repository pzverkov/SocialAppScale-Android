package com.pzverkov.socialapp.core.observability

import com.pzverkov.socialapp.core.domain.CrashReporter
import org.junit.Assert.assertEquals
import org.junit.Test

class BreadcrumbInterceptorTest {

    private class RecordingReporter : CrashReporter {
        val breadcrumbs = mutableListOf<String>()
        override fun log(breadcrumb: String) { breadcrumbs += breadcrumb }
        override fun report(throwable: Throwable) = Unit
    }

    @Test
    fun `state transition is logged with class names`() {
        val reporter = RecordingReporter()
        BreadcrumbInterceptor(reporter).onState("old", 42)

        assertEquals(listOf("state String -> Int"), reporter.breadcrumbs)
    }

    @Test
    fun `null state is rendered as null`() {
        val reporter = RecordingReporter()
        BreadcrumbInterceptor(reporter).onState(null, null)

        assertEquals(listOf("state null -> null"), reporter.breadcrumbs)
    }

    @Test
    fun `event is logged with class name`() {
        val reporter = RecordingReporter()
        BreadcrumbInterceptor(reporter).onEvent("hello")

        assertEquals(listOf("event String"), reporter.breadcrumbs)
    }
}
