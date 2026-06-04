package com.pzverkov.socialapp.core.sharing

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import java.util.UUID

interface InstallationIdProvider {
    fun get(): String
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
class InstallationIdProviderImpl(
    private val context: Context,
) : InstallationIdProvider {

    private val prefs by lazy {
        context.getSharedPreferences("socialapp_install", Context.MODE_PRIVATE)
    }

    @Volatile
    private var cachedId: String? = null

    override fun get(): String {
        cachedId?.let { return it }
        synchronized(this) {
            cachedId?.let { return it }
            val stored = prefs.getString(KEY, null)
            if (stored != null) {
                cachedId = stored
                return stored
            }
            val generated = UUID.randomUUID().toString().take(8)
            prefs.edit().putString(KEY, generated).apply()
            cachedId = generated
            return generated
        }
    }

    companion object {
        private const val KEY = "installation_id"
    }
}
