package com.pzverkov.socialapp.core.sharing

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@Inject
class ShareLinkBuilder(
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
