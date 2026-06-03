package com.pzverkov.socialapp.core.sharing

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface InstallationIdProvider {
    fun get(): String
}

@Singleton
class InstallationIdProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context,
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

@Module
@InstallIn(SingletonComponent::class)
abstract class InstallationIdModule {
    @Binds
    abstract fun bind(impl: InstallationIdProviderImpl): InstallationIdProvider
}
