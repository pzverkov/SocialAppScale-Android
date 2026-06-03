package com.pzverkov.socialapp.feature.itemlist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pzverkov.socialapp.core.network.NetworkResult
import com.pzverkov.socialapp.core.sharing.ShareLinkBuilder
import com.pzverkov.socialapp.core.store.Store
import com.pzverkov.socialapp.feature.favorite.domain.repository.FavoriteRepository
import com.pzverkov.socialapp.feature.itemlist.domain.model.Item
import com.pzverkov.socialapp.feature.itemlist.domain.repository.ItemRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
@Inject
@ViewModelKey(ItemListViewModel::class)
@ContributesIntoMap(AppScope::class)
class ItemListViewModel(
    private val itemRepository: ItemRepository,
    private val favoriteRepository: FavoriteRepository,
    private val shareLinkBuilder: ShareLinkBuilder,
) : ViewModel() {

    private val store = Store<ItemListState, ItemListEvent>(
        initialState = ItemListState.Loading,
    )

    val state: StateFlow<ItemListState> = store.state
    val events: SharedFlow<ItemListEvent> = store.events

    private val loadedItems = MutableStateFlow<List<Item>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _gridColumns = MutableStateFlow(2)
    val gridColumns: StateFlow<Int> = _gridColumns.asStateFlow()

    private val _activeFilter = MutableStateFlow(ItemFilter.ALL)
    val activeFilter: StateFlow<ItemFilter> = _activeFilter.asStateFlow()

    init {
        loadItems()
        observeItemsWithFavoritesAndSearch()
    }

    fun loadItems() {
        store.updateState { ItemListState.Loading }
        viewModelScope.launch {
            when (val result = itemRepository.getItems()) {
                is NetworkResult.Success -> {
                    loadedItems.value = result.data
                    if (result.data.isEmpty()) {
                        store.updateState { ItemListState.Empty }
                    }
                }
                is NetworkResult.Error -> {
                    store.updateState { ItemListState.Error(result.type) }
                }
            }
        }
    }

    private fun observeItemsWithFavoritesAndSearch() {
        viewModelScope.launch {
            combine(
                loadedItems,
                favoriteRepository.observeFavoriteIds(),
                _searchQuery.debounce(300),
                _activeFilter,
            ) { items, favoriteIds, query, filter ->
                ItemsCombined(items, favoriteIds, query, filter)
            }.collect { (items, favoriteIds, query, filter) ->
                if (items.isEmpty()) return@collect

                var filtered = if (query.isBlank()) items else {
                    val lowerQuery = query.lowercase()
                    items.filter { item ->
                        item.title.lowercase().contains(lowerQuery) ||
                            item.description.lowercase().contains(lowerQuery) ||
                            item.location.lowercase().contains(lowerQuery)
                    }
                }

                if (filter == ItemFilter.FAVORITES) {
                    filtered = filtered.filter { it.id in favoriteIds }
                }

                val uiModels = filtered.map { it.toUiModel(isFavorite = it.id in favoriteIds) }

                store.updateState {
                    when {
                        filtered.isEmpty() && query.isNotBlank() -> ItemListState.SearchEmpty(query)
                        filtered.isEmpty() && filter == ItemFilter.FAVORITES -> ItemListState.Empty
                        else -> ItemListState.Loaded(uiModels, query)
                    }
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onFilterChanged(filter: ItemFilter) {
        _activeFilter.value = filter
    }

    fun toggleGridColumns() {
        _gridColumns.value = if (_gridColumns.value == 2) 1 else 2
    }

    fun onItemClicked(itemId: Int) {
        store.emitEvent(ItemListEvent.NavigateToDetail(itemId))
    }

    fun onShareClicked(item: ItemUiModel) {
        val text = shareLinkBuilder.buildShareText(item.title, item.formattedPrice, item.id)
        store.emitEvent(ItemListEvent.ShareItem(text))
    }

    fun onFavoriteClicked(itemId: Int) {
        viewModelScope.launch {
            favoriteRepository.toggleFavorite(itemId)
        }
    }
}

private data class ItemsCombined(
    val items: List<Item>,
    val favoriteIds: Set<Int>,
    val query: String,
    val filter: ItemFilter,
)
