package com.pzverkov.socialapp.core.store

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class StoreTest {

    @Test
    fun `initial state is emitted`() = runTest {
        val store = Store<String, Unit>("initial")

        store.state.test {
            assertEquals("initial", awaitItem())
        }
    }

    @Test
    fun `updateState applies reducer`() = runTest {
        val store = Store<Int, Unit>(0)

        store.updateState { it + 1 }
        store.updateState { it + 10 }

        store.state.test {
            assertEquals(11, awaitItem())
        }
    }

    @Test
    fun `emitEvent delivers to collector`() = runTest {
        val store = Store<Unit, String>(Unit)

        store.events.test {
            store.emitEvent("hello")
            assertEquals("hello", awaitItem())
        }
    }

    @Test
    fun `emitEvent does not crash without collector`() {
        val store = Store<Unit, String>(Unit)
        // Should not throw or block
        store.emitEvent("orphan")
    }

    @Test
    fun `multiple rapid state updates all apply`() = runTest {
        val store = Store<Int, Unit>(0)

        repeat(100) { store.updateState { it + 1 } }

        store.state.test {
            assertEquals(100, awaitItem())
        }
    }

    @Test
    fun `event buffer drops oldest on overflow`() = runTest {
        val store = Store<Unit, Int>(Unit)

        // Emit without collector, buffer is 1 with DROP_OLDEST
        store.emitEvent(1)
        store.emitEvent(2) // should drop 1

        store.events.test {
            store.emitEvent(3)
            assertEquals(3, awaitItem())
        }
    }
}
