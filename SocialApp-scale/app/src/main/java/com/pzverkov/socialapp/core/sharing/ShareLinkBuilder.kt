package com.pzverkov.socialapp.core.sharing

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareLinkBuilder @Inject constructor(
    private val installationIdProvider: InstallationIdProvider,
) {
    fun buildDeepLink(itemId: Int): String {
        return "socialapp://item/$itemId?ref=${installationIdProvider.get()}"
    }

    fun buildShareText(title: String, formattedPrice: String, itemId: Int): String {
        val link = buildDeepLink(itemId)
        return "$title - $formattedPrice on SocialApp\n$link"
    }
}
