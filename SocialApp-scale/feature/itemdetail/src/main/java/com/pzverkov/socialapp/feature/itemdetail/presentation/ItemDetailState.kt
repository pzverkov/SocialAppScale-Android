package com.pzverkov.socialapp.feature.itemdetail.presentation

sealed interface ItemDetailState {
    data object Loading : ItemDetailState
    data class Loaded(
        val item: ItemDetailUiModel,
        val summary: SummaryUiState = SummaryUiState.Hidden,
        val translation: TranslationUiState = TranslationUiState.Hidden,
        // AI-generated alt text for the item image, populated only when a screen reader is active
        // and the device supports on-device image description; null falls back to the item title.
        val imageContentDescription: String? = null,
        val canDescribeImage: Boolean = false,
    ) : ItemDetailState
    data class Error(val errorType: com.pzverkov.socialapp.core.model.ErrorType) : ItemDetailState
}

/** State of the on-device AI summary affordance on the detail screen. */
sealed interface SummaryUiState {
    /** Device cannot summarize; show nothing. */
    data object Hidden : SummaryUiState
    /** Summarization is supported but not yet requested; show the call to action. */
    data object Available : SummaryUiState
    data object Loading : SummaryUiState
    data class Ready(val text: String) : SummaryUiState
    data object Failed : SummaryUiState
}

/** State of the on-device translation affordance for the item description. */
sealed interface TranslationUiState {
    /** Description language matches the device, or translation is unsupported; show nothing. */
    data object Hidden : TranslationUiState
    /** Description is in another language; offer to translate. */
    data class Available(val sourceLanguageTag: String) : TranslationUiState
    data object Loading : TranslationUiState
    data class Translated(val text: String) : TranslationUiState
    data object Failed : TranslationUiState
}

sealed interface ItemDetailEvent {
    data class ShareItem(val text: String) : ItemDetailEvent
    data class ShowPurchaseMessage(val itemTitle: String) : ItemDetailEvent
}
