package com.pzverkov.socialapp.feature.itemlist.presentation

sealed interface ItemListState {
    data object Loading : ItemListState
    data class Loaded(val items: List<ItemUiModel>, val searchQuery: String = "") : ItemListState
    data class Error(val errorType: com.pzverkov.socialapp.core.network.ErrorType) : ItemListState
    data object Empty : ItemListState
    data class SearchEmpty(val query: String) : ItemListState
}

sealed interface ItemListEvent {
    data class NavigateToDetail(val itemId: Int) : ItemListEvent
    data class ShareItem(val text: String) : ItemListEvent
}

enum class ItemFilter { ALL, FAVORITES }
