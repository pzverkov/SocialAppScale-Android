package com.pzverkov.socialapp.feature.itemdetail.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pzverkov.socialapp.core.network.NetworkResult
import com.pzverkov.socialapp.core.sharing.ShareLinkBuilder
import com.pzverkov.socialapp.core.store.Store
import com.pzverkov.socialapp.feature.favorite.domain.repository.FavoriteRepository
import com.pzverkov.socialapp.feature.itemlist.domain.model.Item
import com.pzverkov.socialapp.feature.itemlist.domain.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val itemRepository: ItemRepository,
    private val favoriteRepository: FavoriteRepository,
    private val shareLinkBuilder: ShareLinkBuilder,
) : ViewModel() {

    private val itemId: Int = checkNotNull(savedStateHandle["itemId"])

    private val store = Store<ItemDetailState, ItemDetailEvent>(
        initialState = ItemDetailState.Loading,
    )

    val state: StateFlow<ItemDetailState> = store.state
    val events: SharedFlow<ItemDetailEvent> = store.events

    private var loadedItem: Item? = null

    init {
        loadItem()
        observeFavorite()
    }

    fun retry() {
        store.updateState { ItemDetailState.Loading }
        loadItem()
    }

    private fun loadItem() {
        viewModelScope.launch {
            when (val result = itemRepository.getItem(itemId)) {
                is NetworkResult.Success -> {
                    loadedItem = result.data
                    val isFavorite = favoriteRepository.isFavorite(itemId)
                    store.updateState {
                        ItemDetailState.Loaded(result.data.toDetailUiModel(isFavorite))
                    }
                }
                is NetworkResult.Error -> {
                    store.updateState { ItemDetailState.Error(result.type) }
                }
            }
        }
    }

    private fun observeFavorite() {
        viewModelScope.launch {
            favoriteRepository.observeFavoriteIds().collect { favoriteIds ->
                val item = loadedItem ?: return@collect
                val current = state.value
                if (current is ItemDetailState.Loaded) {
                    store.updateState {
                        ItemDetailState.Loaded(item.toDetailUiModel(item.id in favoriteIds))
                    }
                }
            }
        }
    }

    fun onFavoriteClicked() {
        viewModelScope.launch {
            favoriteRepository.toggleFavorite(itemId)
        }
    }

    fun onShareClicked() {
        val item = (state.value as? ItemDetailState.Loaded)?.item ?: return
        val text = shareLinkBuilder.buildShareText(item.title, item.formattedPrice, item.id)
        store.emitEvent(ItemDetailEvent.ShareItem(text))
    }

    fun onBuyClicked() {
        val item = (state.value as? ItemDetailState.Loaded)?.item ?: return
        store.emitEvent(ItemDetailEvent.ShowPurchaseMessage(item.title))
    }
}
