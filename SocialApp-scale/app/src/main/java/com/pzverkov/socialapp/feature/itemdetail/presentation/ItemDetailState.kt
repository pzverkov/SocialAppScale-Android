package com.pzverkov.socialapp.feature.itemdetail.presentation

sealed interface ItemDetailState {
    data object Loading : ItemDetailState
    data class Loaded(val item: ItemDetailUiModel) : ItemDetailState
    data class Error(val errorType: com.pzverkov.socialapp.core.model.ErrorType) : ItemDetailState
}

sealed interface ItemDetailEvent {
    data class ShareItem(val text: String) : ItemDetailEvent
    data class ShowPurchaseMessage(val itemTitle: String) : ItemDetailEvent
}
