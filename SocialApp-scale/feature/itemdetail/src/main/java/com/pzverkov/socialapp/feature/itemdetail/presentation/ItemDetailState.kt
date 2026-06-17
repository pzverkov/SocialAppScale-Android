package com.pzverkov.socialapp.feature.itemdetail.presentation

sealed interface ItemDetailState {
    data object Loading : ItemDetailState
    data class Loaded(
        val item: ItemDetailUiModel,
        val summary: SummaryUiState = SummaryUiState.Hidden,
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

sealed interface ItemDetailEvent {
    data class ShareItem(val text: String) : ItemDetailEvent
    data class ShowPurchaseMessage(val itemTitle: String) : ItemDetailEvent
}
