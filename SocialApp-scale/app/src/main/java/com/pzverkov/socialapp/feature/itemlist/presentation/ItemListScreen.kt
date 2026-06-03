package com.pzverkov.socialapp.feature.itemlist.presentation

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import com.pzverkov.socialapp.R
import com.pzverkov.socialapp.core.ui.components.EmptyState
import com.pzverkov.socialapp.core.ui.components.ErrorState
import com.pzverkov.socialapp.core.ui.components.ItemImage
import com.pzverkov.socialapp.core.ui.components.LoadingIndicator
import com.pzverkov.socialapp.core.ui.theme.Accent
import com.pzverkov.socialapp.core.ui.theme.Dimens
import com.pzverkov.socialapp.core.ui.theme.GradientBottom
import com.pzverkov.socialapp.core.ui.theme.GradientMid
import com.pzverkov.socialapp.core.ui.theme.GradientTop

private val CardGradient = Brush.verticalGradient(
    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
)

private val OverlayDark = Color.Black.copy(alpha = 0.65f)
private val OverlayMedium = Color.Black.copy(alpha = 0.35f)
private val WhiteFaded = Color.White.copy(alpha = 0.8f)
private val CompactTitleStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = Color.White.copy(alpha = 0.9f))
private val AnnounceModifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
private const val CONTENT_TYPE_COMPACT = "compact"
private const val CONTENT_TYPE_EXPANDED = "expanded"

private val ScreenGradient = Brush.verticalGradient(
    colors = listOf(
        GradientTop.copy(alpha = 0.08f),
        GradientMid.copy(alpha = 0.05f),
        GradientBottom.copy(alpha = 0.03f),
        Color.Transparent,
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemListScreen(
    onNavigateToDetail: (Int) -> Unit,
    viewModel: ItemListViewModel = metroViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val gridColumns by viewModel.gridColumns.collectAsStateWithLifecycle()
    val activeFilter by viewModel.activeFilter.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ItemListEvent.NavigateToDetail -> onNavigateToDetail(event.itemId)
                is ItemListEvent.ShareItem -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, event.text)
                    }
                    context.startActivity(Intent.createChooser(intent, null))
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column {
                SearchTopBar(
                    query = searchQuery,
                    onQueryChange = viewModel::onSearchQueryChanged,
                    gridColumns = gridColumns,
                    onToggleGrid = viewModel::toggleGridColumns,
                    scrollBehavior = scrollBehavior,
                )
                FilterBar(
                    activeFilter = activeFilter,
                    onFilterChanged = viewModel::onFilterChanged,
                )
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(ScreenGradient)) {
            when (val current = state) {
                is ItemListState.Loading -> LoadingIndicator(
                    message = stringResource(R.string.loading_items),
                    modifier = AnnounceModifier.padding(padding),
                )
                is ItemListState.Empty -> {
                    val isFavFilter = activeFilter == ItemFilter.FAVORITES
                    EmptyState(
                        title = stringResource(if (isFavFilter) R.string.empty_favorites_title else R.string.empty_title),
                        subtitle = stringResource(if (isFavFilter) R.string.empty_favorites_subtitle else R.string.empty_subtitle),
                        modifier = AnnounceModifier.padding(padding),
                    )
                }
                is ItemListState.SearchEmpty -> EmptyState(
                    title = stringResource(R.string.search_empty_title, current.query),
                    subtitle = stringResource(R.string.search_empty_subtitle),
                    modifier = AnnounceModifier.padding(padding),
                )
                is ItemListState.Error -> ErrorState(
                    errorType = current.errorType,
                    onRetry = viewModel::loadItems,
                    modifier = AnnounceModifier.padding(padding),
                )
                is ItemListState.Loaded -> ItemGrid(
                    items = current.items,
                    columns = gridColumns,
                    onItemClick = viewModel::onItemClicked,
                    onFavoriteClick = viewModel::onFavoriteClicked,
                    onShareClick = viewModel::onShareClicked,
                    contentPadding = padding,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    gridColumns: Int,
    onToggleGrid: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    TopAppBar(
        title = {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(stringResource(R.string.search_placeholder), style = MaterialTheme.typography.bodyLarge)
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.clear_search))
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(Dimens.SearchBarHeight).clip(RoundedCornerShape(Dimens.CardRadius)),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                ),
                textStyle = MaterialTheme.typography.bodyLarge,
            )
        },
        actions = {
            IconButton(onClick = onToggleGrid) {
                Icon(
                    imageVector = if (gridColumns == 1) Icons.Default.GridView else Icons.AutoMirrored.Filled.ViewList,
                    contentDescription = stringResource(if (gridColumns == 1) R.string.grid_view else R.string.list_view),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = GradientTop.copy(alpha = 0.08f),
            scrolledContainerColor = GradientTop.copy(alpha = 0.06f),
        ),
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun FilterBar(activeFilter: ItemFilter, onFilterChanged: (ItemFilter) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Dimens.SpacingLg, vertical = Dimens.SpacingXs),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
    ) {
        val chipColors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f),
        )
        val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        FilterChip(
            selected = activeFilter == ItemFilter.ALL,
            onClick = { onFilterChanged(ItemFilter.ALL) },
            label = { Text(stringResource(R.string.filter_all)) },
            colors = chipColors,
            border = FilterChipDefaults.filterChipBorder(
                borderColor = outlineColor,
                selectedBorderColor = Color.Transparent, enabled = true, selected = activeFilter == ItemFilter.ALL,
            ),
        )
        FilterChip(
            selected = activeFilter == ItemFilter.FAVORITES,
            onClick = { onFilterChanged(ItemFilter.FAVORITES) },
            label = { Text(stringResource(R.string.filter_favorites)) },
            leadingIcon = { Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(14.dp)) },
            colors = chipColors,
            border = FilterChipDefaults.filterChipBorder(
                borderColor = outlineColor,
                selectedBorderColor = Color.Transparent, enabled = true, selected = activeFilter == ItemFilter.FAVORITES,
            ),
        )
    }
}

@Composable
private fun ItemGrid(
    items: List<ItemUiModel>,
    columns: Int,
    onItemClick: (Int) -> Unit,
    onFavoriteClick: (Int) -> Unit,
    onShareClick: (ItemUiModel) -> Unit,
    contentPadding: PaddingValues,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Dimens.SpacingMd, end = Dimens.SpacingMd,
            top = contentPadding.calculateTopPadding() + Dimens.SpacingSm,
            bottom = Dimens.SpacingMd,
        ),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
    ) {
        items(
            items,
            key = { it.id },
            contentType = { if (columns == 2) CONTENT_TYPE_COMPACT else CONTENT_TYPE_EXPANDED },
        ) { item ->
            ItemCard(
                item = item,
                isCompact = columns == 2,
                onClick = remember(item.id) { { onItemClick(item.id) } },
                onFavoriteClick = remember(item.id) { { onFavoriteClick(item.id) } },
                onShareClick = remember(item) { { onShareClick(item) } },
            )
        }
    }
}

// Single card composable handling both compact and expanded layouts
@Composable
private fun ItemCard(
    item: ItemUiModel,
    isCompact: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onShareClick: () -> Unit,
) {
    val imageHeight = if (isCompact) 200.dp else 220.dp

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(Dimens.CardRadius),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = if (isCompact) 0.dp else 1.dp,
        modifier = Modifier.semantics {
            role = Role.Button
        },
    ) {
        Column {
            ImageWithOverlays(
                item = item,
                imageHeight = imageHeight,
                showGradientText = isCompact,
                onFavoriteClick = onFavoriteClick,
                onShareClick = onShareClick,
            )

            if (!isCompact) {
                Column(modifier = Modifier.padding(Dimens.SpacingMd)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpacingXs))
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ImageWithOverlays(
    item: ItemUiModel,
    imageHeight: Dp,
    showGradientText: Boolean,
    onFavoriteClick: () -> Unit,
    onShareClick: () -> Unit,
) {
    val addFavDesc = stringResource(R.string.add_to_favorites)
    val removeFavDesc = stringResource(R.string.remove_from_favorites)
    val favoriteDesc = if (item.isFavorite) removeFavDesc else addFavDesc

    Box {
        ItemImage(
            imageUrl = item.imageUrl,
            contentDescription = item.title,
            modifier = Modifier.fillMaxWidth().height(imageHeight),
        )

        // Location pill - top left
        LocationPill(location = item.location, modifier = Modifier.align(Alignment.TopStart).padding(Dimens.SpacingSm))

        // Favorite - top right
        IconButton(
            onClick = onFavoriteClick,
            modifier = Modifier.align(Alignment.TopEnd).padding(Dimens.SpacingXs).size(48.dp),
        ) {
            Icon(
                imageVector = if (item.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = favoriteDesc,
                tint = if (item.isFavorite) Accent else Color.White,
                modifier = Modifier.size(22.dp),
            )
        }

        // Bottom overlay: gradient with price/title (compact) or price badge + share (expanded)
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .drawBehind { drawRect(CardGradient) }
                .padding(start = Dimens.SpacingSm, end = Dimens.SpacingXs, top = Dimens.SpacingXl, bottom = Dimens.SpacingSm),
            verticalAlignment = Alignment.Bottom,
        ) {
            if (showGradientText) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = item.formattedPrice, style = MaterialTheme.typography.labelLarge, color = Color.White)
                    Text(
                        text = item.title,
                        style = CompactTitleStyle,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                }
            } else {
                Surface(color = OverlayMedium, shape = RoundedCornerShape(Dimens.SpacingSm)) {
                    Text(
                        text = item.formattedPrice,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = Dimens.SpacingSm, vertical = Dimens.SpacingXs),
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }

            IconButton(onClick = onShareClick, modifier = Modifier.size(48.dp)) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = stringResource(R.string.share),
                    tint = WhiteFaded,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun LocationPill(location: String, modifier: Modifier = Modifier) {
    Surface(
        color = OverlayDark,
        shape = RoundedCornerShape(Dimens.SpacingSm),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Dimens.SpacingSm, vertical = Dimens.SpacingXs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
            Spacer(modifier = Modifier.width(2.dp))
            Text(text = location, style = MaterialTheme.typography.labelSmall, color = Color.White)
        }
    }
}
